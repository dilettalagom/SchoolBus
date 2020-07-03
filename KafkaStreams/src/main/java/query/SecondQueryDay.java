package query;

import Serializers.*;
import custom_function.TimeSlotFilter;
import model.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import static java.time.Duration.ofMinutes;


public class SecondQueryDay {

    private static Properties createStreamProperties() {

        final String KAFKA_BROKER = "kafka:9092";
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "SchoolBus");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "kafka-consumer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, EventTimeExtractor.class.getName());
        //props.put(StreamsConfig.METRICS_RECORDING_LEVEL_CONFIG,"DEBUG");
        //props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "10");

        return props;
    }


    public static void main(String[] args) throws Exception {

        //BasicConfigurator.configure();

        final Long UNTIL_DAY = 86460000L;

        final String topic = "dataQuery2";
        final Properties props = createStreamProperties();
        final StreamsBuilder builder = new StreamsBuilder();
        TimeSlotFilter timeSlotFilter = TimeSlotFilter.getInstance();


        final KStream<byte[], String> inputStream = builder.stream(topic, Consumed.with(Serdes.ByteArray(), Serdes.String()));

        KStream<String, ReasonDelayPojo> mapped = inputStream
                .mapValues(value -> {
                    String[] splitted = value.split(";", -1);
                    return new ReasonDelayPojo(splitted[0], splitted[1]);
                })
                .filter((bytes, pojo) -> pojo != null &&
                        !pojo.getReason().equals("") && !pojo.getReason().equals("Poison") &&
                        (timeSlotFilter.checkAM(pojo) || timeSlotFilter.checkPM(pojo)))
                .selectKey((key, value) -> value.getReason());

        KStream<String, ReasonDelayPojo>[] branches = mapped
                .branch((key, value) -> value.getTimeslot().equals("AM : 5:00-11:59"),
                        (key, value) -> value.getTimeslot().equals("PM : 12:00-19:00"));


        /* day */
        //Windowed<Reason, Tuple3<Timestamp, Timeslot, CountxDay>
        KStream<Windowed<String>, SnappyTuple4<String, String, Integer,Long>> scoresAMDay = computeScores(branches[0], 1, UNTIL_DAY,"accumulator-AM-day");
        KStream<Windowed<String>, SnappyTuple4<String, String, Integer,Long>> scoresPMDay = computeScores(branches[1], 1, UNTIL_DAY,"accumulator-PM-day");

        KTable<Windowed<String>, RankBox> rankedAMDay = computeRankStream(scoresAMDay, 1, UNTIL_DAY, "ranker-AM-day");
        KTable<Windowed<String>, RankBox> rankedPMDay = computeRankStream(scoresPMDay, 1, UNTIL_DAY, "ranker-PM-day");

        KStream<String, SnappyTuple2<String,Long>> joinDay = mergeFinalResults(rankedAMDay, rankedPMDay);

        KStream<String, SnappyTuple2<String, Long>> finale = joinDay.mapValues((key, value) -> {
            Long end = System.nanoTime() - value.k2;
            return new SnappyTuple2<String, Long>(value.k1, end);
        });


        //print on file
        finale.print(Printed.<String, SnappyTuple2<String,Long>>toFile("dayResult.txt").withLabel("merged-day")
                .withKeyValueMapper((win, v) -> String.format("%s; %s", win, v.toString())));


        // attach shutdown handler to catch control-c
        final CountDownLatch latch = new CountDownLatch(1);
        final KafkaStreams streams = new KafkaStreams(builder.build(), props);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);


    }


    private static KStream<String, SnappyTuple2<String,Long>> mergeFinalResults(KTable<Windowed<String>, RankBox> streamA, KTable<Windowed<String>, RankBox> streamB) {

        KStream<String, RankBox> streamAM = streamA.toStream().selectKey((win, value) -> win.key());
        KStream<String, RankBox> streamPM = streamB.toStream().selectKey((win, value) -> win.key());

        KStream<String, RankBox> merged = streamAM.merge(streamPM);

        KTable<String, SnappyTuple2<String,Long>> result = merged
                .groupByKey(Serialized.with(Serdes.String(), Serdes.serdeFrom(new RankBoxSerializer(), new RankBoxDeserializer())))
                .aggregate(
                        new Initializer<SnappyTuple2<String,Long>>() {
                            @Override
                            public SnappyTuple2<String,Long> apply() {
                                return new SnappyTuple2<String,Long>("", 0L);
                            }
                        },
                        new Aggregator<String, RankBox, SnappyTuple2<String,Long>>() {
                            @Override
                            public SnappyTuple2<String,Long> apply(String windowed, RankBox rankBox, SnappyTuple2<String,Long> acc) {

                                Long actual = Math.max(acc.k2, rankBox.getCurrentEventTime());

                                StringBuilder sb = new StringBuilder();
                                sb.append(acc.k1).append(rankBox.toString());

                                return new SnappyTuple2<String,Long>(sb.toString(), actual);
                            }
                        },
                        //Materialized.as("final-merger").withLoggingDisabled().with(Serdes.String(), Serdes.serdeFrom(new Tuple2Serializer(), new Tuple2Deserializer()))
                        Materialized.as("final-merger").with(Serdes.String(), Serdes.serdeFrom(new Tuple2Serializer(), new Tuple2Deserializer()))


                );
        return result.toStream();

    }



    private static KTable<Windowed<String>, RankBox> computeRankStream(KStream<Windowed<String>, SnappyTuple4<String, String, Integer, Long>> scoresAMDay, int window, Long until, String accName) {
        return scoresAMDay
                .map((key, value) -> {

                    String newKey = value.k1;
                    SnappyTuple4<String, String, Integer, Long> newValue = new SnappyTuple4<String, String, Integer, Long>(value.k2, key.key(), value.k3, value.k4);

                    return KeyValue.pair(newKey, newValue);
                })
                .groupByKey(Serialized.with(Serdes.String(), Serdes.serdeFrom(new Tuple4Serializer(), new Tuple4Deserializer())))
                .windowedBy(TimeWindows.of(Duration.ofDays(window)).until(until).grace(ofMinutes(1)))
                .aggregate(
                        new Initializer<RankBox>() {
                            @Override
                            public RankBox apply() {
                                return new RankBox( "new");
                            }
                        },
                        new Aggregator<String, SnappyTuple4<String, String, Integer, Long>, RankBox>() {
                            @Override
                            public RankBox apply(String key, SnappyTuple4<String, String, Integer, Long> tuple,
                                                 RankBox rankBox) {

                                ResultPojo pojo = new ResultPojo(tuple.k1,tuple.k2,tuple.k3, Long.valueOf(tuple.k4));

                                return checkIfMustAdd(pojo, rankBox);
                            }
                        },
                        //Materialized.<String, RankBox, WindowStore<Bytes, byte[]>>as(accName).withLoggingDisabled().withValueSerde(Serdes.serdeFrom(new RankBoxSerializer(), new RankBoxDeserializer()))
                        Materialized.<String, RankBox, WindowStore<Bytes, byte[]>>as(accName).withValueSerde(Serdes.serdeFrom(new RankBoxSerializer(), new RankBoxDeserializer()))

                );
    }


    private static RankBox checkIfMustAdd(ResultPojo p, RankBox rankB){
        int actualValue = p.getCount();

        rankB.setCurrentEventTime(Math.max(p.getCurrentEventTime(), rankB.getCurrentEventTime()));

        if ( actualValue > rankB.getPos3().getCount()) {

            if (actualValue >= rankB.getPos1().getCount()) {
                rankB.setPos3(rankB.getPos2());
                rankB.setPos2(rankB.getPos1());
                rankB.setPos1(p);

            } else if (actualValue >= rankB.getPos2().getCount()) {
                rankB.setPos3(rankB.getPos2());
                rankB.setPos2(p);

            } else {
                rankB.setPos3(p);
            }
        }
        return rankB;
    }


    private static KStream<Windowed<String>, SnappyTuple4<String, String, Integer, Long>> computeScores(KStream<String, ReasonDelayPojo> branch, int window, Long until, String accName) {
        return branch
                .groupByKey(Serialized.with(Serdes.String(), Serdes.serdeFrom(new ReasonPojoSerializer(), new ReasonPojoDeserializer())))
                //until -> window lower bound
                //grace -> admitted out-of-order events
                .windowedBy(TimeWindows.of(Duration.ofDays(window)).until(until).grace(ofMinutes(1)))
                .aggregate(
                        new Initializer<SnappyTuple4<String, String, Integer, Long>>() {
                            @Override
                            public SnappyTuple4<String, String, Integer, Long> apply() {
                                return new SnappyTuple4<String, String, Integer, Long>("", "", 0, 0L);
                            }
                        },
                        new Aggregator<String, ReasonDelayPojo, SnappyTuple4<String, String, Integer, Long>>() {
                            @Override
                            public SnappyTuple4<String, String, Integer, Long> apply(String key, ReasonDelayPojo pojo, SnappyTuple4<String, String, Integer, Long> acc) {

                                Long actual =  Math.max(pojo.getCurrentEventTime(), acc.k4 );

                                return new SnappyTuple4<String, String, Integer, Long>(pojo.getTimestamp(), pojo.getTimeslot(), acc.k3 + 1, actual);
                            }
                        },
                        //Materialized.<String, SnappyTuple4<String, String, Integer, Long>, WindowStore<Bytes, byte[]>>as(accName).withLoggingDisabled().withValueSerde(Serdes.serdeFrom(new Tuple4Serializer(), new Tuple4Deserializer()))
                        Materialized.<String, SnappyTuple4<String, String, Integer, Long>, WindowStore<Bytes, byte[]>>as(accName).withValueSerde(Serdes.serdeFrom(new Tuple4Serializer(), new Tuple4Deserializer()))

                ).toStream();
    }

}

