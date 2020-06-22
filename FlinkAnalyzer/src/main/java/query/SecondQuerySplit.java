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
import custom_function.validator.TimeSlotValidator;

import java.util.ArrayList;
import java.util.Map;


public class SecondQuerySplit {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";
    //private static final String topic = "persistent://public/default/dataQuery2";
    private static final String topic = "dataQuery2";


    public static void main(String[] args) {

        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        see.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        PulsarConnection conn = new PulsarConnection(pulsarUrl, topic);
        SourceFunction<String> src = conn.createPulsarConnection();
        assert src!=null;


        SplitStream<ReasonDelayPojo> inputStream = see.addSource(src)
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                    return new ReasonDelayPojo(tokens[0], tokens[1]);
                })
                .filter(new TimeSlotValidator())
                .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery2())
                .split(new TimeSlotSplitter());


        /* AM - 24h */
        SingleOutputStreamOperator<Tuple4<Long, String, Map<String, Long>,Long>> rankAMday = computeRankBySlot(inputStream, "AM",1);
        /* PM - 24h */
        SingleOutputStreamOperator<Tuple4<Long, String, Map<String, Long>,Long>> rankPMday = computeRankBySlot(inputStream, "PM",1);
        /* save 24h results */
        DataStream<ResultSlotRankPojo> resultDay = joinSlotResults(rankAMday, rankPMday);
        resultDay.writeAsText("/opt/flink/flink-jar/results/query2/dayResult.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        /* AM - 1week */
        SingleOutputStreamOperator<Tuple4<Long, String, Map<String, Long>, Long>> rankAMweek = computeRankBySlot(inputStream, "AM", 7);
        /* PM - 1week */
        SingleOutputStreamOperator<Tuple4<Long, String, Map<String, Long>, Long>> rankPMweek = computeRankBySlot(inputStream, "PM",7);
        /* save 1week results */
        DataStream<ResultSlotRankPojo> resultWeek = joinSlotResults(rankAMweek, rankPMweek);
        resultWeek.writeAsText("/opt/flink/flink-jar/results/query2/weekResult.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        try {
            see.execute("FlinkQuery2Split");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static SingleOutputStreamOperator<Tuple4<Long, String, Map<String, Long>,Long>> computeRankBySlot(SplitStream<ReasonDelayPojo> inputStream, String type, Integer window) {
        return inputStream
                .select(type)
                .keyBy(new KeyByReasonAndDelay())
                .timeWindow(Time.days(window))
                .aggregate(new ReasonAggregator(), new ReasonProcessWindowFunction())
                .keyBy(new KeyByTimestampAndReason())
                .timeWindow(Time.days(window))
                .aggregate(new TimestampReasonAggregator(), new RankingReasonProcessWindowFunction());
    }


    private static DataStream<ResultSlotRankPojo> joinSlotResults(SingleOutputStreamOperator<Tuple4<Long, String, Map<String, Long>,Long>> rankAM,
                                                                  SingleOutputStreamOperator<Tuple4<Long, String, Map<String, Long>,Long>> rankPM) {
        return rankAM
                .coGroup(rankPM)
                .where((KeySelector<Tuple4<Long, String, Map<String, Long>,Long>, Long>) row -> row._1())
                .equalTo((KeySelector<Tuple4<Long, String, Map<String, Long>,Long>, Long>) row -> row._1())
                .window(TumblingEventTimeWindows.of(Time.days(1)))
                .apply(new PrintRankCoGroupResults());
    }

}