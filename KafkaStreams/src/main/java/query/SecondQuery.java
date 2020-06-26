package query;

import Serializers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import custom_function.CustomHeapSort;
import custom_function.TimeSlotFilter;
import model.*;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorSupplier;
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
        //props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
        //props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, EventTimeExtractor.class.getName());
        props.put(StreamsConfig.EXACTLY_ONCE, "exactly_once");

        return props;
    }


    public static void main(String[] args) throws Exception {

        String topic = "dataQuery2"; //TODO:args[0]

        final Properties props = createStreamProperties();
        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<byte[], String> inputStream = builder.stream(topic, Consumed.with(Serdes.ByteArray(), Serdes.String()));

        TimeSlotFilter timeSlotFilter = TimeSlotFilter.getInstance();

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

        branches[0].foreach((key, tuple) -> System.out.println(key + "," + tuple.toString()));

        //Windowed<Reason, Tuple3<Timestamp, Timeslot, Int>
        /* day */
        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresAMDay = computeScores(branches[0], 1L, "accumulator-AM-day");
        System.out.flush();

        scoresAMDay.foreach( (key, tuple) -> System.out.println(key.key() + "," + tuple.toString()));

        //        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresPMDay = computeScores(branches[1], 1L,"accumulator-PM-day");
//
//        /* week */
//        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresAMWeek = computeScores(branches[0], 7L,"accumulator-AM-week");
//        KStream<Windowed<String>, SnappyTuple3<String, String, Integer>> scoresPMWeek = computeScores(branches[1], 7L,"accumulator-PM-week");


        KStream<Windowed<String>, ArrayList<ResultPojo>> remappered = scoresAMDay
                .map((key, value) -> {

                    String newKey = value.k1;
                    SnappyTuple3<String, String, Integer> newValue = new SnappyTuple3<String, String, Integer>(value.k2, key.key(), value.k3);

                    return KeyValue.pair(newKey, newValue);
                })
                .groupByKey(Serialized.with(Serdes.String(), Serdes.serdeFrom(new Tuple3Serializer(), new Tuple3Deserializer())))
                .windowedBy(TimeWindows.of(Duration.ofDays(1)).until(86460000L * 1).grace(ofMinutes(1)))
                .aggregate(
                        new Initializer<ArrayList<ResultPojo>>() {
                            @Override
                            public ArrayList<ResultPojo> apply() {
                                //Comparator<SnappyTuple3<String, String, Integer>> comp = Comparator.comparing(t -> t.k3);

                                return new ArrayList<ResultPojo>();
                            }
                        },
                        new Aggregator<String, SnappyTuple3<String, String, Integer>, ArrayList<ResultPojo>>() {
                            @Override
                            public ArrayList<ResultPojo> apply(String key, SnappyTuple3<String, String, Integer> tuple,
                                                               ArrayList<ResultPojo> list) {
//                              if (list.size() < 3 || list.get(2).k3 < tuple.k3) {
//                                    if (list.size() == 3)
//                                        list.remove(3);
//                                    list.add(tuple);
//                                }
                                list.add(new ResultPojo(tuple.k1,tuple.k2,tuple.k3));

                                return list;
                            }
                        },
                        Materialized.<String, ArrayList<ResultPojo>, WindowStore<Bytes, byte[]>>as("prova")
                                .withValueSerde(Serdes.serdeFrom(new LinkedSerializer(), new LinkedDeserializer()))

                ).toStream();


        KStream<Windowed<String>, PriorityQueue<ResultPojo>> queueStream = remappered.map((key, value) -> {

            Comparator<ResultPojo> comp = Comparator.comparing(ResultPojo::getCount);
            PriorityQueue<ResultPojo> queue = new PriorityQueue<>(3, comp);

            for (ResultPojo p : value) {
                queue.add(p);
            }
            //queue.addAll(value);

            return KeyValue.pair(key, queue);
        });


        System.out.flush();
        queueStream.foreach((key, list) -> {

           /* CustomHeapSort sorted = new CustomHeapSort(list);
            sorted.sort();*/
            System.out.println(key.key() + "," + list.toString());

        });


        // printOnFile("queue.txt", remappered);






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

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        //Runtime.getRuntime().addShutdownHook(new Thread(streams::close));


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

            System.out.println(PriorityQueueToString(rankQueue).toString());
        });


    }

    private static StringBuilder PriorityQueueToString(PriorityQueue<SnappyTuple3<String, String, Integer>> rankQueue){

        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < Math.min(3,rankQueue.size()) - 1 ; i++){
            SnappyTuple3<String, String, Integer> t = rankQueue.poll();
            sb.append(t.k1).append(", ").append(t.k2).append(" ,").append(t.k3);
        }
        SnappyTuple3<String, String, Integer> t = rankQueue.poll();
        sb.append(t.k1).append(", ").append(t.k2).append(" ,").append(t.k3);

        return sb;
    }

}

        /*TODO: send on topic
        branches[1].to("pm.txt",
                        Produced.with(Serdes.String(), Serdes.serdeFrom(new ReasonPojoSerializer(), new ReasonPojoDeserializer())));
         */