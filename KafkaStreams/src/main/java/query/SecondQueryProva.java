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
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import javax.swing.plaf.synth.SynthScrollBarUI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import static java.time.Duration.ofMinutes;


public class SecondQueryProva {

    private static Properties createStreamProperties() {

        final String KAFKA_BROKER = "kafka:9092";
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "SchoolBus");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "kafka-consumer");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKER);
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, EventTimeExtractor.class.getName());
        props.put(StreamsConfig.EXACTLY_ONCE, "exactly_once");

        return props;
    }


    public static void main(String[] args) throws Exception {

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


//        /* day */
//        //Windowed<Reason, Tuple3<Timestamp, Timeslot, CountxDay>
//        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresAMDay = computeScores(branches[0], 1L, "accumulator-AM-day");
//        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresPMDay = computeScores(branches[1], 1L, "accumulator-PM-day");
//
//        KTable<Windowed<String>, RankBox> rankedAMDay = computeRankStream(scoresAMDay, 1L, "ranker-AM-day");
//        KTable<Windowed<String>, RankBox> rankedPMDay = computeRankStream(scoresPMDay, 1L, "ranker-PM-day");
//
//        KStream<Windowed<String>, String> joinDay = mergeFinalResults(rankedAMDay, rankedPMDay, "merged-day-acc", 1L);
//
//        joinDay.foreach((win, v) -> System.out.println("merged" + win.key() + " " + v));
//        //print on file
//        joinDay.print(Printed.<Windowed<String>, String>toFile("ranker-merged-day.txt").withLabel("merged-day")
//                .withKeyValueMapper((win, v) -> String.format("%s; %s", win.key(), v)));


        /* week */
        //Windowed<Reason, Tuple3<Timestamp, Timeslot, CountxWeek>
        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresAMWeek = computeScores(branches[0], 7L, "accumulator-AM-week");
        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresPMWeek = computeScores(branches[1], 7L, "accumulator-PM-week");

        KTable<Windowed<String>, RankBox> rankedAMWeek = computeRankStream(scoresAMWeek, 7L, "ranker-AM-week");
        KTable<Windowed<String>, RankBox> rankedPMWeek = computeRankStream(scoresPMWeek, 7L, "ranker-PM-week");

        KStream<String, String> joinWeek = mergeFinalResults(rankedAMWeek, rankedPMWeek, "merged-week-acc", 7L);

        //print on file
        joinWeek.print(Printed.<String, String>toFile("ranker-merged-week.txt").withLabel("merged-week")
                .withKeyValueMapper((win, v) -> String.format("%s; %s", win, v)));


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


    private static KStream<String, String> mergeFinalResults(KTable<Windowed<String>, RankBox> streamA, KTable<Windowed<String>, RankBox> streamB, String accName, Long window) {

        KStream<String, RankBox> streamAM = streamA.toStream().selectKey((win, value) -> win.key());
        KStream<String, RankBox> streamPM = streamB.toStream().selectKey((win, value) -> win.key());

        KStream<String, RankBox> merged = streamAM.merge(streamPM);

        KTable<String, String> result = merged
                .groupByKey(Serialized.with(Serdes.String(), Serdes.serdeFrom(new RankBoxSerializer(), new RankBoxDeserializer())))
                .aggregate(
                        new Initializer<String>() {
                            @Override
                            public String apply() {
                                return "";
                            }
                        },
                        new Aggregator<String, RankBox, String>() {
                            @Override
                            public String apply(String windowed, RankBox rankBox, String acc) {

                                StringBuilder sb = new StringBuilder();
                                sb.append(acc).append(rankBox.toString());
                                return sb.toString();
                            }
                        },
                        Materialized.with(Serdes.String(), Serdes.String())
                );
        return result.toStream();

    }



    private static KTable<Windowed<String>, RankBox> computeRankStream(KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresAMDay, Long window, String accName) {
        return scoresAMDay
                .map((key, value) -> {

                    String newKey = value.k1;
                    SnappyTuple3<String, String, Integer> newValue = new SnappyTuple3<String, String, Integer>(value.k2, key.key(), value.k3);

                    return KeyValue.pair(newKey, newValue);
                })
                .groupByKey(Serialized.with(Serdes.String(), Serdes.serdeFrom(new Tuple3Serializer(), new Tuple3Deserializer())))
                .windowedBy(TimeWindows.of(Duration.ofDays(window)))
                .aggregate(
                        new Initializer<RankBox>() {
                            @Override
                            public RankBox apply() {
                                //Comparator<SnappyTuple3<String, String, Integer>> comp = Comparator.comparing(t -> t.k3);
                                return new RankBox( "new");
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
                .windowedBy(TimeWindows.of(Duration.ofDays(window)))
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

}

/*TODO: send on topic
branches[1].to("pm.txt",
     Produced.with(Serdes.String(), Serdes.serdeFrom(new ReasonPojoSerializer(), new ReasonPojoDeserializer())))
*/

