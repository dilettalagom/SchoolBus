package query;

import Serializers.*;
import custom_function.TimeSlotFilter;
import model.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static java.time.Duration.ofMinutes;


public class SecondQuery {

    private static Properties createStreamProperties() {

        final String KAFKA_BROKER = "kafka:9092";
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,"SchoolBus");
        props.put(StreamsConfig.CLIENT_ID_CONFIG,"kafka-consumer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,KAFKA_BROKER);
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, EventTimeExtractor.class.getName());
        props.put(StreamsConfig.EXACTLY_ONCE, "exactly_once");
        return props;
    }


    public static void main(String[] args) throws Exception {

        String topic = "dataQuery2"; //TODO:args[0]
        TimeSlotFilter timeSlotFilter = TimeSlotFilter.getInstance();

        final Properties props = createStreamProperties();
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<byte[], String> inputStream = builder.stream(topic, Consumed.with(Serdes.ByteArray(), Serdes.String()));

        KStream<String, ReasonDelayPojo> mapped = inputStream
                .mapValues(value -> {
                    String[] splitted = value.split(";", -1);
                    return new ReasonDelayPojo(splitted[0], splitted[1]);
                })
                .filter((bytes, pojo) -> pojo != null &&
                        !pojo.getReason().equals("") &&
                        (timeSlotFilter.ckeckAM(pojo) || timeSlotFilter.ckeckPM(pojo)))
                .selectKey((key, value) -> value.getReason());


        KStream<String, ReasonDelayPojo>[] branches = mapped
                .branch((key, value) -> value.getTimeslot().equals("AM : 5:00-11:59"),
                        (key, value) -> value.getTimeslot().equals("PM : 12:00-19:00"));

        /* day */
        //Windowed<Reason, Tuple3<Timestamp, Timeslot, CountxDay>
        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresAMDay = computeScores(branches[0], 1L, "accumulator-AM-day");
        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresPMDay = computeScores(branches[1], 1L,"accumulator-PM-day");
        KTable<Windowed<String>, RankBox> rankedAMDay = computeRankStream(scoresAMDay, 1L, "ranker-AM-day");
        KTable<Windowed<String>, RankBox> rankedPMDay = computeRankStream(scoresPMDay, 1L, "ranker-PM-day");

        KTable<Windowed<String>, String> joinDay = joinFinalResults(rankedAMDay, rankedPMDay);

        /* week */
        //Windowed<Reason, Tuple3<Timestamp, Timeslot, CountxWeek>
        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresAMWeek = computeScores(branches[0], 7L,"accumulator-AM-week");
        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresPMWeek = computeScores(branches[1], 7L,"accumulator-PM-week");
        KTable<Windowed<String>, RankBox> rankedAMWeek = computeRankStream(scoresAMWeek, 7L, "ranker-AM-week");
        KTable<Windowed<String>, RankBox> rankedPMWeek = computeRankStream(scoresPMWeek, 7L, "ranker-PM-week");

        KTable<Windowed<String>, String> joinWeek = joinFinalResults(rankedAMWeek, rankedPMWeek);


        //TODO: test join */
        joinDay.toStream().foreach((key,val) -> System.out.println(key.key() + " , " + val));

        /*TODO: send on topic
        branches[1].to("pm.txt",
                        Produced.with(Serdes.String(), Serdes.serdeFrom(new ReasonPojoSerializer(), new ReasonPojoDeserializer())));
         */


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

        //to perform a clean up of the local StateStore
        streams.cleanUp();
        streams.start();
    }


    private static KTable<Windowed<String>, String> joinFinalResults(KTable<Windowed<String>, RankBox> rankedAMDay, KTable<Windowed<String>, RankBox> rankedPMDay) {
        return rankedAMDay.outerJoin(rankedPMDay, new ValueJoiner<RankBox, RankBox, String>() {
            @Override
            public String apply(RankBox r1, RankBox r2) {
                String resString = "";
                if (r1 != null)
                    resString += " " + r1.toString();
                if(r2 != null)
                    resString += " " + r2.toString();
                return resString;
            }
        });
    }


    private static KTable<Windowed<String>, RankBox> computeRankStream(KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresAMDay, Long window, String accName) {
        return scoresAMDay
                .map((key, value) -> {

                    String newKey = value.k1;
                    SnappyTuple3<String, String, Integer> newValue = new SnappyTuple3<String, String, Integer>(value.k2, key.key(), value.k3);

                    return KeyValue.pair(newKey, newValue);
                })
                .groupByKey(Serialized.with(Serdes.String(), Serdes.serdeFrom(new Tuple3Serializer(), new Tuple3Deserializer())))
                .windowedBy(TimeWindows.of(Duration.ofDays(1)).until(86460000L * window).grace(ofMinutes(1)))
                .aggregate(
                        new Initializer<RankBox>() {
                            @Override
                            public RankBox apply() {
                                //Comparator<SnappyTuple3<String, String, Integer>> comp = Comparator.comparing(t -> t.k3);
                                return new RankBox(new ResultPojo("","",0),
                                        new ResultPojo("","",0),
                                        new ResultPojo("","",0)
                                );
                            }
                        },
                        new Aggregator<String, SnappyTuple3<String, String, Integer>, RankBox>() {
                            @Override
                            public RankBox apply(String key, SnappyTuple3<String, String, Integer> tuple,
                                                 RankBox rankBox) {

                                ResultPojo pojo = new ResultPojo(tuple.k1,tuple.k2,tuple.k3);

                                return checkIfMustAdd(pojo, rankBox);
                            }
                        },
                        Materialized.<String, RankBox, WindowStore<Bytes, byte[]>>as(accName)
                                .withValueSerde(Serdes.serdeFrom(new RankBoxSerializer(), new RankBoxDeserializer()))

                );
    }


    private static RankBox checkIfMustAdd(ResultPojo p, RankBox rankB){
        int actualValue = p.getCount();

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

    private static KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> computeScores(KStream<String, ReasonDelayPojo> branch, Long window, String accName) {
        return branch
                .groupByKey(Serialized.with(Serdes.String(), Serdes.serdeFrom(new ReasonPojoSerializer(), new ReasonPojoDeserializer())))
                //until -> window lower bound
                //grace -> admitted out-of-order events
                .windowedBy(TimeWindows.of(Duration.ofDays(window)).until(86460000L*window).grace(ofMinutes(1)))
                .aggregate(
                        new Initializer<SnappyTuple3<String, String, Integer>>() {
                            @Override
                            public SnappyTuple3<String, String, Integer> apply() {
                                return new SnappyTuple3<String, String, Integer>("", "", 0);
                            }
                        },
                        new Aggregator<String, ReasonDelayPojo, SnappyTuple3<String, String, Integer>>() {
                            @Override
                            public SnappyTuple3<String, String, Integer> apply(String key, ReasonDelayPojo pojo, SnappyTuple3<String, String, Integer> acc) {
                                return new SnappyTuple3<String, String, Integer>(pojo.getTimestamp(), pojo.getTimeslot(), acc.k3 + 1);
                            }
                        },
                        Materialized.<String,SnappyTuple3<String, String, Integer>, WindowStore<Bytes, byte[]>>as(accName)
                                .withValueSerde(Serdes.serdeFrom(new Tuple3Serializer(), new Tuple3Deserializer()))

                ).toStream();
    }


    private static void printOnFile(String filename,
                                    KStream<Windowed<String>, PriorityQueue<SnappyTuple3<String, String, Integer>>> stream) throws FileNotFoundException {

        PrintStream o = new PrintStream(new File(filename));

        // Store current System.out before assigning a new value
        PrintStream console = System.out;

        // Assign o to output stream
        System.setOut(o);
        stream.foreach((windowed, rankQueue) -> {

            System.out.println();
        });


    }


}
