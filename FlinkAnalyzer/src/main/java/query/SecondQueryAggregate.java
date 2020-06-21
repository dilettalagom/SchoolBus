package query;

import custom_function.aggregate.ReasonAggregator;
import custom_function.aggregate.TimestampReasonAggregator;
import custom_function.cogroup.PrintRankCoGroupResults;
import custom_function.key.KeyByReasonAndDelay;
import custom_function.key.KeyByTimestampAndReason;
import custom_function.process.RankingReasonProcessWindowFunction;
import custom_function.process.ReasonProcessWindowFunction;
import custom_function.prova.*;
import custom_function.split.TimeSlotSplitter;
import custom_function.validator.TimeSlotValidator;
import model.ReasonDelayPojo;
import model.ResultSlotRankPojo;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;
import time.watermark.DateTimeAscendingAssignerQuery2;
import util.PulsarConnection;

import java.util.ArrayList;
import java.util.Map;


public class SecondQueryAggregate {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";
    //private static final String topic = "persistent://public/default/dataQuery2";
    private static final String topic = "dataQuery2";


    public static void main(String[] args) {

        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        see.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        PulsarConnection conn = new PulsarConnection(pulsarUrl, topic);
        SourceFunction<String> src = conn.createPulsarConnection();
        assert src!=null;

        SingleOutputStreamOperator<ReasonDelayPojo> inputStream = see.addSource(src)
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                    return new ReasonDelayPojo(tokens[0], tokens[1]);
                })
                .filter(new TimeSlotValidator())
                .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery2());

        /* day */
        SingleOutputStreamOperator<Tuple3<Long, ArrayList<Tuple2<String, Tuple2<String, Long>>>, Long>> dayStream = computeStreamByWindow(inputStream, 1);
        dayStream.writeAsText("/opt/flink/flink-jar/results/query2/noCoGroup.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        /* week */
        SingleOutputStreamOperator<Tuple3<Long, ArrayList<Tuple2<String, Tuple2<String, Long>>>, Long>> weekStream = computeStreamByWindow(inputStream, 7);
        weekStream.writeAsText("/opt/flink/flink-jar/results/query2/noCoGroup.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        try {
            see.execute("FlinkQuery2Aggr");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static SingleOutputStreamOperator<Tuple3<Long, ArrayList<Tuple2<String, Tuple2<String, Long>>>, Long>> computeStreamByWindow ( SingleOutputStreamOperator<ReasonDelayPojo> inputStream, int window){
        return inputStream
                .keyBy(new KeyBySlotAndReason())
                .timeWindow(Time.days(window))
                .aggregate(new Aggregatore1(), new Process1())
                .keyBy(new keyByTimeAndSlot())
                .timeWindow(Time.days(window))
                .aggregate(new TimestampReasonAggregator(), new Process2())
                .keyBy(new KeyByTimestamp())
                .timeWindow(Time.days(window))
                .aggregate(new Aggregatore3(), new Process3());
    }



}
