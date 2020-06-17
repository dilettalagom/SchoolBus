package query;

import custom_function.aggregate.ReasonAggregator;
import custom_function.aggregate.TimestampReasonAggregator;
import custom_function.key.KeyByReasonDelay;
import custom_function.key.KeyByTimestampReason;
import custom_function.process.RankingReasonProcessWindowFunction;
import custom_function.process.ReasonProcessWindowFunction;
import custom_function.prova.*;
import custom_function.split.TimeSlotSplitter;
import model.ReasonDelayPojo;
import model.ResultSlotRankPojo;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.common.functions.JoinFunction;
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
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple5;
import time.watermark.DateTimeAscendingAssignerQuery2;
import util.PulsarConnection;
import custom_function.validator.TimeSlotValidator;

import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;

public class SecondQuery {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";
    private static final String topic = "persistent://public/default/dataQuery2";

    public static void main(String[] args) {


        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        see.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        PulsarConnection conn = new PulsarConnection(pulsarUrl, topic);
        SourceFunction<String> src = conn.createPulsarConnection();
        //assert src!=null;

        SplitStream<ReasonDelayPojo> inputStream = see.addSource(src)
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                    return new ReasonDelayPojo(tokens[0], tokens[1]);
                })
                .filter(new TimeSlotValidator())
                .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery2())
                .split(new TimeSlotSplitter());

        //AM - 24h
        SingleOutputStreamOperator<Tuple3<Long, String, Map<String, Long>>> rankAM = inputStream
                .select("AM")
                .keyBy(new KeyByReasonDelay())
                .timeWindow(Time.days(1))
                .aggregate(new ReasonAggregator(), new ReasonProcessWindowFunction())
                .keyBy(new KeyByTimestampReason())
                .timeWindow(Time.days(1))
                .aggregate(new TimestampReasonAggregator(), new RankingReasonProcessWindowFunction());

        rankAM.writeAsText("/opt/flink/flink-jar/results/query2/rankAM.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        SingleOutputStreamOperator<Tuple3<Long, String, Map<String, Long>>> rankPM = inputStream
                .select("PM")
                .keyBy(new KeyByReasonDelay())
                .timeWindow(Time.days(1))
                .aggregate(new ReasonAggregator(), new ReasonProcessWindowFunction())
                .keyBy(new KeyByTimestampReason())
                .timeWindow(Time.days(1))
                .aggregate(new TimestampReasonAggregator(), new RankingReasonProcessWindowFunction());

        rankPM.writeAsText("/opt/flink/flink-jar/results/query2/rankPM.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        //ts (AM (reason, count),(reason, count)) (PM (reason, count),(reason, count)))
        //        DataStream<Tuple2<Long, ArrayList<Tuple2<String, Map<String, Long>>>>> join = rankAM
        DataStream<ResultSlotRankPojo> join = rankAM

                .coGroup(rankPM)
                .where(new KeySelector<Tuple3<Long, String, Map<String, Long>>, Long>() {
                    @Override
                    public Long getKey(Tuple3<Long, String, Map<String, Long>> row) throws Exception {
                        return row._1();
                    }
                })
                .equalTo(new KeySelector<Tuple3<Long, String, Map<String, Long>>, Long>() {
                    @Override
                    public Long getKey(Tuple3<Long, String, Map<String, Long>> row) throws Exception {
                        return row._1();
                    }
                })
                .window(TumblingEventTimeWindows.of(Time.days(1)))
                //.apply(new CoGroupFunction<Tuple3<Long, String, Map<String, Long>>, Tuple3<Long, String, Map<String, Long>>, Tuple2<Long, ArrayList<Tuple2<String,Map<String, Long>>>> > () {
                .apply(new CoGroupFunction<Tuple3<Long, String, Map<String, Long>>, Tuple3<Long, String, Map<String, Long>>, ResultSlotRankPojo > () {

                    @Override
                    public void coGroup(Iterable<Tuple3<Long, String, Map<String, Long>>> rankAM,
                                        Iterable<Tuple3<Long, String, Map<String, Long>>> rankPM,
                                        //Collector<Tuple2<Long, ArrayList<Tuple2<String,Map<String, Long>>>>> out) throws Exception {
                                        Collector<ResultSlotRankPojo> out) throws Exception {


                        ArrayList<Tuple2<String,Map<String, Long>>> temp = new ArrayList<>();

                        Tuple3<Long, String, Map<String, Long>> elemAM;
                        Tuple3<Long, String, Map<String, Long>> elemPM;
                        try{
                            elemAM = rankAM.iterator().next();
                        }catch (NoSuchElementException e){
                            elemAM = null;
                        }
                        try{
                            elemPM = rankPM.iterator().next();
                        }catch (NoSuchElementException e){
                            elemPM = null;
                        }

                        Long timestamp = 0L;
                        if( elemAM != null) {
                            timestamp = elemAM._1();
                            temp.add(new Tuple2<String, Map<String, Long>>(elemAM._2(), elemAM._3()));
                        }

                        if (elemPM != null){
                            timestamp = elemPM._1();
                            temp.add(new Tuple2<String, Map<String, Long>>(elemPM._2(), elemPM._3()));
                        }

                        ResultSlotRankPojo pojo = new ResultSlotRankPojo(timestamp, temp);

                        out.collect(pojo);

                    }
                })
                ;
        /*JOIN
        DataStream<Tuple5<Long, String, Tuple2<String, Long>, String, Tuple2<String, Long>>> join = rankAM.join(rankPM)
                .where(new KeySelector<Tuple3<Long, String, Tuple2<String, Long>>, Long>() {
                    @Override
                    public Long getKey(Tuple3<Long, String, Tuple2<String, Long>> input) throws Exception {
                        return input._1();
                    }
                })
                .equalTo(new KeySelector<Tuple3<Long, String, Tuple2<String, Long>>, Long>() {
                    @Override
                    public Long getKey(Tuple3<Long, String, Tuple2<String, Long>> input) throws Exception {
                        return input._1();
                    }
                })
                .window(TumblingEventTimeWindows.of(Time.days(1)))
                .apply(new JoinFunction<Tuple3<Long, String, Tuple2<String, Long>>, Tuple3<Long, String, Tuple2<String, Long>>, Tuple5<Long, String, Tuple2<String, Long>, String, Tuple2<String, Long>>>() {
                    @Override
                    public Tuple5<Long, String, Tuple2<String, Long>, String, Tuple2<String, Long>> join(Tuple3<Long, String, Tuple2<String, Long>> t1, Tuple3<Long, String, Tuple2<String, Long>> t2) throws Exception {
                        return new Tuple5<>(t1._1(), t1._2(), t1._3(), t2._2(), t2._3());
                    }
                });

         */

        join.writeAsText("/opt/flink/flink-jar/results/query2/res24.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

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
}
