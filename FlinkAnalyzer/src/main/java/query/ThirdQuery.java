package query;

import custom_function.apply.ComputeVendorsPartialScore;
import custom_function.apply.ComputeVendorsRank;
import custom_function.validator.VendorsDelayValidator;
import model.VendorsDelayPojo;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple3;
import time.watermark.DateTimeAscendingAssignerQuery3;
import util.PulsarConnection;

import java.util.Map;
import java.util.PriorityQueue;

public class ThirdQuery {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";
    private static final String topic = "persistent://public/default/dataQuery3";

    public static void main(String[] args) {

        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        see.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //see.setParallelism(3);
        PulsarConnection conn = new PulsarConnection(pulsarUrl, topic);
        SourceFunction<String> src = conn.createPulsarConnection();


        KeyedStream<VendorsDelayPojo, String> inputStream = see.addSource(src)
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                    return new VendorsDelayPojo(tokens[0], tokens[1], tokens[2], Integer.parseInt(tokens[3]));
                })
                .filter(new VendorsDelayValidator())
                .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery3())
                .keyBy((KeySelector<VendorsDelayPojo, String>) vendorsDelayPojo -> vendorsDelayPojo.getVendor());

        //day
        SingleOutputStreamOperator<String> resultDay = inputStream
                .timeWindow(Time.days(1))
                .apply(new ComputeVendorsPartialScore())
                .keyBy((KeySelector<Tuple3<Long, String, Tuple3<Long, Long, Long>>, Long>) t -> t._1())
                .timeWindow(Time.days(1))
                .apply(new ComputeVendorsRank());

        //week
        SingleOutputStreamOperator<String> resultWeek = inputStream
                .timeWindow(Time.days(7))
                .apply(new ComputeVendorsPartialScore())
                .keyBy((KeySelector<Tuple3<Long, String, Tuple3<Long, Long, Long>>, Long>) t -> t._1())
                .timeWindow(Time.days(7))
                .apply(new ComputeVendorsRank());


        resultDay.writeAsText("/opt/flink/flink-jar/results/query3/resultDay.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        resultWeek.writeAsText("/opt/flink/flink-jar/results/query3/resultWeek.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        try {
            see.execute("FlinkQuery3");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
