package query;

import Serializers.*;
import custom_function.TimeSlotFilter;
import model.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;



public class SecondQueryWeek {

    private static Properties createStreamProperties() {

        final String KAFKA_BROKER = "kafka:9092";
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "SchoolBus");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "kafka-consumer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, EventTimeExtractor.class);
        //props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "50");
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "10");
        //props.put("acks","all");
        //props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        return props;
    }


    public static void main(String[] args) throws Exception {

        //final Long UNTIL_WEEK = 604800000L*2;
        final Long UNTIL_WEEK = 604860000L;

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

        /* week */
        KStream<Windowed<String>, SnappyTuple4<String, String, Integer,Long>> scoresAMWeek = computeScores(branches[0], 7, UNTIL_WEEK,"accumulator-AM-week");
        KStream<Windowed<String>, SnappyTuple4<String, String, Integer,Long>> scoresPMWeek = computeScores(branches[1], 7, UNTIL_WEEK,"accumulator-PM-week");


        KTable<Windowed<String>, RankBox> rankedAMWeek = computeRankStream(scoresAMWeek, 7, UNTIL_WEEK,"ranker-AM-week");
        KTable<Windowed<String>, RankBox> rankedPMWeek = computeRankStream(scoresPMWeek, 7, UNTIL_WEEK,"ranker-PM-week");

        KStream<String, SnappyTuple2<String,Long>> joinWeek = mergeFinalResults(rankedAMWeek, rankedPMWeek);

        //print on file
        joinWeek.mapValues((key, value) -> {
            Long end = System.nanoTime() - value.k2;
            return new SnappyTuple2<String, Long>(value.k1, end);
        }).print(Printed.<String, SnappyTuple2<String,Long>>toFile("weekResult.txt").withLabel("merged-week")
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
                               //TODO: Long end = System.nanoTime() - actual;

                                StringBuilder sb = new StringBuilder();
                                sb.append(acc.k1).append(rankBox.toString());

                                return new SnappyTuple2<String,Long>(sb.toString(), actual);
                            }
                        },
                        Materialized.with(Serdes.String(), Serdes.serdeFrom(new Tuple2Serializer(), new Tuple2Deserializer()))
                );
        return result.toStream();

    }



    private static KTable<Windowed<String>, RankBox> computeRankStream(KStream<Windowed<String>, SnappyTuple4<String, String, Integer, Long>> scoresAMDay, int window, Long until, String accName) {
        return scoresAMDay

                .map(
                        // IN: <Reason,tupl4<data, timeslot, count, eventTime>>
                        // OUT: <weekDate,tupl4<timeslot, reason, count, eventTime>>
                        (key, value) -> {

                            Date date = new Date(key.window().start());
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String startWeek = simpleDateFormat.format(date);
                            SnappyTuple4<String, String, Integer, Long> newValue = new SnappyTuple4<String, String, Integer, Long>(value.k2, key.key(), value.k3, value.k4);

                            return new KeyValue<String, SnappyTuple4<String, String, Integer, Long>>(startWeek, newValue);
                        }

                )
                .groupByKey(Serialized.with(Serdes.String(), Serdes.serdeFrom(new Tuple4Serializer(), new Tuple4Deserializer())))
                .windowedBy(TimeWindows.of(Duration.ofDays(7L)).until(until))
                .aggregate(
                        new Initializer<RankBox>() {
                            @Override
                            public RankBox apply() {
                                //Comparator<SnappyTuple4<String, String, Integer>> comp = Comparator.comparing(t -> t.k3);
                                return new RankBox( "new");
                            }
                        },
                        new Aggregator<String, SnappyTuple4<String, String, Integer, Long>, RankBox>() {
                            @Override
                            public RankBox apply(String key, SnappyTuple4<String, String, Integer, Long> tuple,
                                                 RankBox rankBox) {

                                ResultPojo pojo = new ResultPojo(tuple.k1,tuple.k2,tuple.k3, tuple.k4);

                                return checkIfMustAdd(pojo, rankBox);
                            }
                        },
                        Materialized.<String, RankBox, WindowStore<Bytes, byte[]>>as(accName)
                                .withValueSerde(Serdes.serdeFrom(new RankBoxSerializer(), new RankBoxDeserializer()))

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

    //<Reason,tupl4<timestamp, timeslot, count, eventTime>>
    private static KStream<Windowed<String>, SnappyTuple4<String, String, Integer, Long>> computeScores(KStream<String, ReasonDelayPojo> branch, int window, Long until, String accName) {
        return branch
                .groupByKey(Serialized.with(Serdes.String(), Serdes.serdeFrom(new ReasonPojoSerializer(), new ReasonPojoDeserializer())))
                //until -> window lower bound
                //grace -> admitted out-of-order events
                .windowedBy(TimeWindows.of(Duration.ofDays(7L)).until(until))
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
                        Materialized.<String, SnappyTuple4<String, String, Integer, Long>, WindowStore<Bytes, byte[]>>as(accName)
                                .withValueSerde(Serdes.serdeFrom(new Tuple4Serializer(), new Tuple4Deserializer()))

                ).toStream();
    }

}

/*TODO: send on topic
branches[1].to("pm.txt",
     Produced.with(Serdes.String(), Serdes.serdeFrom(new ReasonPojoSerializer(), new ReasonPojoDeserializer())))
*/

