package query;

import Serializers.*;
import custom_function.TimeSlotFilter;
import model.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import static java.time.Duration.ofMinutes;


public class SecondQueryJoin {

    private static Properties createStreamProperties() {

        final String KAFKA_BROKER = "kafka:9092";
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,"SchoolBus");
        props.put(StreamsConfig.CLIENT_ID_CONFIG,"kafka-consumer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, EventTimeExtractor.class.getName());

        return props;
    }



    public static void main(String[] args) throws Exception {

        final Long UNTIL_DAY = 86460000L;
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
                        !pojo.getReason().equals("") &&  !pojo.getReason().equals("Poison") &&
                        (timeSlotFilter.checkAM(pojo) || timeSlotFilter.checkPM(pojo)))
                .selectKey((key, value) -> value.getReason());

        KStream<String, ReasonDelayPojo>[] branches = mapped
                .branch((key, value) -> value.getTimeslot().equals("AM : 5:00-11:59"),
                        (key, value) -> value.getTimeslot().equals("PM : 12:00-19:00"));

        /* day */
        //Windowed<Reason, Tuple3<Timestamp, Timeslot, CountxDay>
        KStream<Windowed<String>, SnappyTuple4<String, String, Integer,Long>> scoresAMDay = computeScores(branches[0], 1,UNTIL_DAY, "accumulator-AM-day");
        KStream<Windowed<String>, SnappyTuple4<String, String, Integer,Long>> scoresPMDay = computeScores(branches[1], 1,UNTIL_DAY,"accumulator-PM-day");

        KTable<Windowed<String>, RankBox> rankedAMDay = computeRankStream(scoresAMDay, 1, UNTIL_DAY, "ranker-AM-day");
        KTable<Windowed<String>, RankBox> rankedPMDay = computeRankStream(scoresPMDay, 1, UNTIL_DAY, "ranker-PM-day");

        KStream<Windowed<String>, String> joinDay = joinFinalResults(rankedAMDay, rankedPMDay).toStream();

        //print on file
        joinDay.print(Printed.<Windowed<String>, String>toFile("ranker-join-day.txt").withLabel("joined-day")
                .withKeyValueMapper((win, v) -> String.format("%s; %s", win.key() , v)));


        /* week */
        //Windowed<Reason, Tuple3<Timestamp, Timeslot, CountxWeek>
        KStream<Windowed<String>, SnappyTuple4<String, String, Integer,Long>> scoresAMWeek = computeScores(branches[0], 7, UNTIL_WEEK,"accumulator-AM-week");
        KStream<Windowed<String>, SnappyTuple4<String, String, Integer,Long>> scoresPMWeek = computeScores(branches[1], 7, UNTIL_WEEK,"accumulator-PM-week");

        KTable<Windowed<String>, RankBox> rankedAMWeek = computeRankStream(scoresAMWeek, 7, UNTIL_WEEK,"ranker-AM-week");
        KTable<Windowed<String>, RankBox> rankedPMWeek = computeRankStream(scoresPMWeek, 7, UNTIL_WEEK, "ranker-PM-week");

        KStream<Windowed<String>, String> joinWeek = joinFinalResults(rankedAMWeek, rankedPMWeek).toStream();

        //print on file
        joinWeek.print(Printed.<Windowed<String>, String>toFile("ranker-join-week.txt").withLabel("joined-week")
                .withKeyValueMapper((win, v) -> String.format("%s; %s", win.key() , v)));



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

    private static KTable<Windowed<String>, String> joinFinalResults(KTable<Windowed<String>, RankBox> rankedAMDay, KTable<Windowed<String>, RankBox> rankedPMDay) {
        return rankedAMDay.outerJoin(rankedPMDay, new ValueJoiner<RankBox, RankBox, String>() {
            @Override
            public String apply(RankBox r1, RankBox r2) {

                Long actualAM = 0L;
                Long actualPM = 0L;

                StringBuilder sb = new StringBuilder();
                if (r1 != null) {
                    sb.append("AM : 5:00-11:59 (")
                    .append(r1.toString()).append(")");

                    actualAM = r1.getCurrentEventTime();
                }
                if(r2 != null) {
                    sb.append(", PM : 12:00-19:00 (")
                    .append(r2.toString()).append(")");

                    actualPM = r2.getCurrentEventTime();

                }

                Long actual = Math.max(actualAM, actualPM);
                Long end = System.nanoTime() - actual;
                sb.append("; ").append(end);

                return sb.toString();
            }
        });
    }

    private static KTable<Windowed<String>, RankBox> computeRankStream(KStream<Windowed<String>, SnappyTuple4<String, String, Integer, Long>> scoresAMDay, int window, Long until, String accName) {
        return scoresAMDay
                .map((key, value) -> {

                    String newKey = value.k1;
                    SnappyTuple4<String, String, Integer, Long> newValue = new SnappyTuple4<String, String, Integer, Long>(value.k2, key.key(), value.k3, value.k4);

                    return KeyValue.pair(newKey, newValue);
                })
                .groupByKey(Serialized.with(Serdes.String(), Serdes.serdeFrom(new Tuple4Serializer(), new Tuple4Deserializer())))
                .windowedBy( TimeWindows.of(Duration.ofDays(window)).until(until).grace(ofMinutes(1)))
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
                        Materialized.<String, SnappyTuple4<String, String, Integer, Long>, WindowStore<Bytes, byte[]>>as(accName)
                                .withValueSerde(Serdes.serdeFrom(new Tuple4Serializer(), new Tuple4Deserializer()))

                ).toStream();
    }

}

        /*TODO: send on topic
        branches[1].to("pm.txt",
                        Produced.with(Serdes.String(), Serdes.serdeFrom(new ReasonPojoSerializer(), new ReasonPojoDeserializer())));
         */
