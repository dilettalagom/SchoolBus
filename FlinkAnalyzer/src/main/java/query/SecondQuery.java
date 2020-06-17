package query;

import custom_function.aggregate.ReasonAggregator;
import custom_function.aggregate.TimestampReasonAggregator;
import custom_function.cogroup.PrintRankResults;
import custom_function.key.KeyByReasonAndDelay;
import custom_function.key.KeyByTimestampAndReason;
import custom_function.process.RankingReasonProcessWindowFunction;
import custom_function.process.ReasonProcessWindowFunction;
import custom_function.split.TimeSlotSplitter;
import model.ReasonDelayPojo;
import model.ResultSlotRankPojo;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import scala.Tuple3;
import time.watermark.DateTimeAscendingAssignerQuery2;
import util.PulsarConnection;
import custom_function.validator.TimeSlotValidator;
import java.util.Map;


public class SecondQuery {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";
    private static final String topic = "persistent://public/default/dataQuery2";


    public static void main(String[] args) {

        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        see.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //see.setParallelism(3);

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
        SingleOutputStreamOperator<Tuple3<Long, String, Map<String, Long>>> rankAMday = computeRankBySlot(inputStream, "AM",1);
        /* PM - 24h */
        SingleOutputStreamOperator<Tuple3<Long, String, Map<String, Long>>> rankPMday = computeRankBySlot(inputStream, "PM",1);
        /* save 24h results */
        DataStream<ResultSlotRankPojo> resultDay = joinSlotResults(rankAMday, rankPMday);
        resultDay.writeAsText("/opt/flink/flink-jar/results/query2/dayResult.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        /* AM - 1week */
        SingleOutputStreamOperator<Tuple3<Long, String, Map<String, Long>>> rankAMweek = computeRankBySlot(inputStream, "AM",7);
        /* PM - 1week */
        SingleOutputStreamOperator<Tuple3<Long, String, Map<String, Long>>> rankPMweek = computeRankBySlot(inputStream, "PM",7);
        /* save 1week results */
        DataStream<ResultSlotRankPojo> resultWeek = joinSlotResults(rankAMweek, rankPMweek);
        resultWeek.writeAsText("/opt/flink/flink-jar/results/query2/weekResult.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);


        /*TODO:CODICE PROVA
        SingleOutputStreamOperator<Tuple2<Long, ArrayList<Tuple2<String, Tuple2<String, Long>>>>> prova = see.addSource(src)
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                    return new ReasonDelayPojo(tokens[0], tokens[1]);
                })
                .filter(new TimeSlotValidator())
                .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery2())
                .split(new TimeSlotSplitter())
                .keyBy(new KeyBySlotAndReason())
                .timeWindow(Time.days(1))
                .aggregate(new Aggregatore1(), new Process1())
                .keyBy(new keyByTimeAndSlot())
                .timeWindow(Time.days(1))
                .aggregate(new TimestampReasonAggregator(), new Process2())
                .keyBy(new KeyByTimestamp())
                .timeWindow(Time.days(1))
                .aggregate(new Aggregatore3(), new Process3());

        prova.writeAsText("/opt/flink/flink-jar/results/query2/prova.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        */

        try {
            see.execute("FlinkQuery2");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static SingleOutputStreamOperator<Tuple3<Long, String, Map<String, Long>>> computeRankBySlot(SplitStream<ReasonDelayPojo> inputStream, String am, Integer window) {
        return inputStream
                .select(am)
                .keyBy(new KeyByReasonAndDelay())
                .timeWindow(Time.days(window))
                .aggregate(new ReasonAggregator(), new ReasonProcessWindowFunction())
                .keyBy(new KeyByTimestampAndReason())
                .timeWindow(Time.days(window))
                .aggregate(new TimestampReasonAggregator(), new RankingReasonProcessWindowFunction());
    }

    private static DataStream<ResultSlotRankPojo> joinSlotResults(SingleOutputStreamOperator<Tuple3<Long, String, Map<String, Long>>> rankAM,
                                                                  SingleOutputStreamOperator<Tuple3<Long, String, Map<String, Long>>> rankPM) {
        return rankAM
                    .coGroup(rankPM)
                    .where((KeySelector<Tuple3<Long, String, Map<String, Long>>, Long>) row -> row._1())
                    .equalTo((KeySelector<Tuple3<Long, String, Map<String, Long>>, Long>) row -> row._1())
                    .window(TumblingEventTimeWindows.of(Time.days(1)))
                    .apply(new PrintRankResults());
    }

}
