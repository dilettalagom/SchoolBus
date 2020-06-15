package query;

import custom_function.aggregate.TimestampReasonAggregator;
import custom_function.aggregate.ReasonAggregator;
import custom_function.key.KeyByReasonDelay;
import custom_function.key.KeyByTimestampReason;
import custom_function.process.RankingReasonProcessWindowFunction;
import custom_function.process.ReasonProcessWindowFunction;
import custom_function.split.TimeSlotSplitter;
import model.ReasonDelayPojo;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import scala.Tuple2;
import time.DateTimeAscendingAssignerQuery2;
import util.PulsarConnection;
import custom_function.validator.TimeSlotValidator;

import java.util.TreeMap;

public class SecondQuery {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";
    private static final String topic = "persistent://public/default/dataQuery2";

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

        //AM - 24h
        SingleOutputStreamOperator<Tuple2<Long, Tuple2<String, Long>>> count = inputStream
                .select("AM")
                .keyBy(new KeyByReasonDelay())
                .timeWindow(Time.days(1))
                .aggregate(new ReasonAggregator(), new ReasonProcessWindowFunction());

        SingleOutputStreamOperator<Tuple2<Long, TreeMap<String, Long>>> rank = count.keyBy(new KeyByTimestampReason())
                .timeWindow(Time.days(1))
                .aggregate(new TimestampReasonAggregator(), new RankingReasonProcessWindowFunction());

        count.writeAsText("/opt/flink/flink-jar/results/query2/countAM24.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        rank.writeAsText("/opt/flink/flink-jar/results/query2/rankAM24.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        //PM  24h
        /*SingleOutputStreamOperator<Tuple2<Long, Map<String, Long>>> pm24 = inputStream
                .select("PM")
                .keyBy(new KeyByReasonDelay())
                .timeWindow(Time.days(1))
                .aggregate(new ReasonDelayAggregator(), new RankingReasonProcessWindowFunction());*/

        //AM - 7d


        //PM - 7d


        try {
            see.execute("FlinkQuery2");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
