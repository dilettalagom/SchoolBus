package query;
import aggregate.AverageDelay;
import aggregate.DelayProcessWindowFunction;
import key.KeyByBoro;
import key.KeyByWindowStart;
import model.BoroDelayPojo;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.pulsar.PulsarSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.Tuple3;
import time.DateTimeAscendingAssignerQuery1;
import util.Subscription;
import validator.BoroDelayPojoValidator;


public class FirstQuery {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";


    public static void main(String[] args) throws Exception{

        Logger log = LoggerFactory.getLogger(FirstQuery.class);
        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        PulsarSourceBuilder<String> builder = PulsarSourceBuilder
                .builder(new SimpleStringSchema())
                .serviceUrl(pulsarUrl)
                .topic("persistent://public/default/dataQuery1")
                .subscriptionName((new Subscription()).generateNewSubScription());
        SourceFunction<String> src = builder.build();


        KeyedStream<BoroDelayPojo, String> inputStream = see.addSource(src)
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                    return new BoroDelayPojo(tokens[0], tokens[1], tokens[2]);

                })
                .filter(new BoroDelayPojoValidator())
                .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery1())
                .keyBy(new KeyByBoro());

        DataStream<Tuple3<Long, String, Double>> dayStream = inputStream
                .timeWindow(Time.days(1))
                .aggregate(new AverageDelay(), new DelayProcessWindowFunction())
                .keyBy(new KeyByWindowStart());
                //.process();
                //.timeWindow(Time.days(1));

        dayStream.writeAsText("/opt/flink/flink-jar/results/query1/oneday.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        //weekStream.writeAsText("/opt/flink/flink-jar/results/query1/oneweek.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        //monthStream.writeAsText("/opt/flink/flink-jar/results/query1/onemonth.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        try {
            see.execute("FlinkQuery1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
