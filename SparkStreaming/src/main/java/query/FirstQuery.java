package query;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class FirstQuery {

    private static final String  LOCAL_DIR = "./results/query1";
    private static final String  LOCAL_CHECKPOINT_DIR = "tmp/checkpoint/query1/";
    private static final int WINDOW_TIME_UNIT_SECS = 1;
    private static final String KARKAURI = "kafka:9092";
    private static final Pattern SPACE = Pattern.compile(";");



    public static void main(String[] args) throws Exception {

        // Create the context with a 1 second batch size
        SparkConf sparkConf = new SparkConf()
                .setMaster("local[2]")
                .setAppName("SparkStreaming-Query1");
        JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, Durations.seconds(WINDOW_TIME_UNIT_SECS));
        ssc.sparkContext().setLogLevel("ERROR");

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


        JavaDStream<String> values = inputStream.flatMap(
                x -> Arrays.asList(SPACE.split(x.toString())).iterator()
        );

        values.print();

        values.foreachRDD(rdd ->{
            if(!rdd.isEmpty()){
                rdd.saveAsTextFile(LOCAL_DIR);
            }
        });
        ssc.start();
        ssc.awaitTermination();
    }
}
