package query;

import model.BoroDelayPojo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.util.StatCounter;
import org.apache.spark.api.java.Optional;
import scala.Tuple2;

import java.util.*;
import java.util.regex.Pattern;


public class FirstQuery {

    private static final String  LOCAL_DIR = "./results/query1";
    private static final String  LOCAL_CHECKPOINT_DIR = "./results/checkpoint/query1/";
    private static final int WINDOW_TIME_UNIT_SECS = 3600;
    private static final String KARKAURI = "kafka:9092";
    private static final Pattern SPACE = Pattern.compile(";");



    public static void main(String[] args) throws Exception {

        // Create the context with a 1 second batch size
        SparkConf sparkConf = new SparkConf()
                .setMaster("local[2]")
                .setAppName("SparkStreaming-Query1");
        JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, Durations.seconds(WINDOW_TIME_UNIT_SECS));
        ssc.sparkContext().setLogLevel("ERROR");
        ssc.checkpoint(LOCAL_CHECKPOINT_DIR);

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", KARKAURI);
        //kafkaParams.put("zookeeper.connect", "zookeeper:2181");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "SchoolBus");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);
        Collection<String> topics = Arrays.asList("dataQuery1");


        JavaInputDStream<ConsumerRecord<String, String>> inputStream = KafkaUtils.createDirectStream(
                ssc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams));


        JavaPairDStream<String,BoroDelayPojo> mapperedStream = inputStream
                .mapToPair(row -> {

                    List<String> splitted = Arrays.asList(row.value().split(";",-1));
                    BoroDelayPojo pojo = new BoroDelayPojo(splitted.get(0), splitted.get(1), splitted.get(2));
                    return new Tuple2<String,BoroDelayPojo>( pojo.getBoro(), pojo);
                });

        mapperedStream.foreachRDD(rdd ->{
            if(!rdd.isEmpty()){
                rdd.saveAsTextFile(LOCAL_DIR);
            }
        });
        mapperedStream.print();

     /*   long day = 24*60;
        JavaPairDStream<String, Iterable<BoroDelayPojo>> aggregate = mapperedStream
                .groupByKeyAndWindow(Durations.minutes(day));

        aggregate.foreachRDD(rdd ->{
            if(!rdd.isEmpty()){
                rdd.saveAsTextFile(LOCAL_DIR);
            }
        });

        JavaPairDStream<String, StatCounter> statCounter = aggregate.updateStateByKey(
                new Function2<List<Iterable<BoroDelayPojo>>, org.apache.spark.api.java.Optional<StatCounter>, org.apache.spark.api.java.Optional<StatCounter>>() {
                    @Override
                    public org.apache.spark.api.java.Optional<StatCounter> call(List<Iterable<BoroDelayPojo>> iterables, org.apache.spark.api.java.Optional<StatCounter> acc) throws Exception {
                        StatCounter stats = new StatCounter();

                        iterables.forEach( x-> stats.merge(x.iterator().next().getDelay()));

                        return Optional.of(stats);
                    }
                }
        );

        statCounter.foreachRDD(rdd ->{
            if(!rdd.isEmpty()){
                rdd.saveAsTextFile(LOCAL_DIR);
            }
        });

        JavaPairDStream<String, Double> avgTemperatureDStream = statCounter.mapToPair(new PairFunction<Tuple2<String,StatCounter>, String, Double>() {
            public Tuple2<String, Double> call(Tuple2<String, StatCounter> statCounterTuple) throws Exception {
                String key = statCounterTuple._1();
                double avgValue = statCounterTuple._2().mean();

                return new Tuple2<String, Double>(key, avgValue);
            }
        });


        avgTemperatureDStream.foreachRDD(rdd ->{
            if(!rdd.isEmpty()){
                rdd.saveAsTextFile(LOCAL_DIR);
            }
        });*/

        //start execution
        ssc.start();
        ssc.awaitTermination();
    }
}
