package query;

import custom_function.aggregate.AverageDelayAggregator;
import custom_function.process.DelayProcessWindowFunction;
import custom_function.aggregate.TimestampAggregator;
import custom_function.process.TimestampWindowFunction;
import custom_function.key.KeyByBoro;
import custom_function.key.KeyByTimestamp;
import model.BoroDelayPojo;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import scala.Tuple2;
import time.DateTimeAscendingAssignerQuery1;
import time.MonthWindow;
import util.PulsarConnection;
import custom_function.validator.BoroDelayPojoValidator;
import java.util.ArrayList;


public class FirstQuery {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";
    private static final String topic = "persistent://public/default/dataQuery1";


    public static void main(String[] args) throws Exception{

        //Logger log = LoggerFactory.getLogger(FirstQuery.class);
        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        see.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //see.setParallelism(3);
        //see.enableCheckpointing(10);
        PulsarConnection conn = new PulsarConnection(pulsarUrl, topic);
        SourceFunction<String> src = conn.createPulsarConnection();
        assert src!=null;

        KeyedStream<BoroDelayPojo, String> inputStream = see.addSource(src)
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                    return new BoroDelayPojo(tokens[0], tokens[1], tokens[2]);
                })
                .filter(new BoroDelayPojoValidator())
                .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery1())
                .keyBy(new KeyByBoro());

        inputStream.writeAsText("/opt/flink/flink-jar/results/query1/inputFromPulsar.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        //day
        SingleOutputStreamOperator<Tuple2<Long, ArrayList<Tuple2<String, Double>>>> prova = inputStream
                .timeWindow(Time.days(1))
                .aggregate(new AverageDelayAggregator(), new DelayProcessWindowFunction())
                .keyBy(new KeyByTimestamp())
                .timeWindow(Time.days(1))
                .aggregate(new TimestampAggregator(), new TimestampWindowFunction());

        //week
        SingleOutputStreamOperator<Tuple2<Long, ArrayList<Tuple2<String, Double>>>> weekResult = inputStream
                .timeWindow(Time.days(7))
                .aggregate(new AverageDelayAggregator(), new DelayProcessWindowFunction())
                .keyBy(new KeyByTimestamp())
                .timeWindow(Time.hours(7))
                .aggregate(new TimestampAggregator(), new TimestampWindowFunction());

        //month
        SingleOutputStreamOperator<Tuple2<Long, ArrayList<Tuple2<String, Double>>>> monthResult = inputStream
                .window(new MonthWindow())
                .aggregate(new AverageDelayAggregator(), new DelayProcessWindowFunction())
                .keyBy(new KeyByTimestamp())
                .window(new MonthWindow())
                .aggregate(new TimestampAggregator(), new TimestampWindowFunction());

        //write results on textfile
        prova.writeAsText("/opt/flink/flink-jar/results/query1/dayResult.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        weekResult.writeAsText("/opt/flink/flink-jar/results/query1/weekResult.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        monthResult.writeAsText("/opt/flink/flink-jar/results/query1/monthResult.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);


        try {
            see.execute("FlinkQuery1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}

