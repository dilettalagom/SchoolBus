package query;
import aggregate.AverageDelay;
import aggregate.DelayProcessWindowFunction;
import aggregate.TimestampAggregator;
import aggregate.TimestampWindowFunction;
import key.KeyByBoro;
import key.KeyByWindowStart;
import model.BoroDelayPojo;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.pulsar.PulsarSourceBuilder;
import scala.Tuple2;
import time.DateTimeAscendingAssignerQuery1;
import util.Subscription;
import validator.BoroDelayPojoValidator;
import java.util.ArrayList;


public class FirstQuery {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";


    public static void main(String[] args) throws Exception{

        //Logger log = LoggerFactory.getLogger(FirstQuery.class);
        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        see.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        PulsarSourceBuilder<String> builder = PulsarSourceBuilder
                .builder(new SimpleStringSchema())
                .serviceUrl(pulsarUrl)
                .topic("persistent://public/default/dataQuery1")
                .subscriptionName((new Subscription()).generateNewSubScription());
        SourceFunction<String> src = builder.build();


        SingleOutputStreamOperator<BoroDelayPojo> inputStream =
                see.addSource(src)
                        .map(x -> {
                            String[] tokens = x.split(";", -1);
                            return new BoroDelayPojo(tokens[0], tokens[1], tokens[2]);

                        })
                        .filter(new BoroDelayPojoValidator())
                        .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery1());

        inputStream.writeAsText("/opt/flink/flink-jar/results/query1/inputFromPulsar.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        // tuple2 <Timestamp, ArrayList>
        SingleOutputStreamOperator<Tuple2<Long, ArrayList<Tuple2<String, Double>>>> result = inputStream
                .keyBy(new KeyByBoro())
                .timeWindow(Time.hours(24))
                .aggregate(new AverageDelay(), new DelayProcessWindowFunction())
                .keyBy(new KeyByWindowStart())
                .timeWindow(Time.hours(24))
                .aggregate(new TimestampAggregator(), new TimestampWindowFunction())
                ;



        result.writeAsText("/opt/flink/flink-jar/results/query1/oneday.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        //weekStream.writeAsText("/opt/flink/flink-jar/results/query1/oneweek.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        //monthStream.writeAsText("/opt/flink/flink-jar/results/query1/onemonth.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        try {
            see.execute("FlinkQuery1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}

