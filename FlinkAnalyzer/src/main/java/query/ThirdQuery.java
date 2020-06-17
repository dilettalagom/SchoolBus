package query;

import custom_function.key.KeyByVendor;
import custom_function.validator.VendorsDelayValidator;
import model.VendorsDelayPojo;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.CoGroupedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple3;
import scala.Tuple5;
import time.watermark.DateTimeAscendingAssignerQuery3;
import util.PulsarConnection;

public class ThirdQuery {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";
    private static final String topic = "persistent://public/default/dataQuery3";

    public static void main(String[] args) {

        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        see.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //see.setParallelism(3);
        PulsarConnection conn = new PulsarConnection(pulsarUrl, topic);
        SourceFunction<String> src = conn.createPulsarConnection();

        SingleOutputStreamOperator<VendorsDelayPojo> inputStream = see.addSource(src)
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                    return new VendorsDelayPojo(tokens[0], tokens[1], tokens[2], Integer.parseInt(tokens[3]));
                })
                .filter(new VendorsDelayValidator())
                .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery3());
                //.keyBy(new KeyByVendor())
                //.timeWindow(Time.days(1));


        SingleOutputStreamOperator<VendorsDelayPojo> mechanical_problem = inputStream.filter((FilterFunction<VendorsDelayPojo>) vendorsDelayPojo -> vendorsDelayPojo.getReason().equals("Mechanical Problem"));
        SingleOutputStreamOperator<VendorsDelayPojo> heavy_traffic = inputStream.filter((FilterFunction<VendorsDelayPojo>) vendorsDelayPojo -> vendorsDelayPojo.getReason().equals("Heavy Traffic"));
        SingleOutputStreamOperator<VendorsDelayPojo> other_reason = inputStream.filter((FilterFunction<VendorsDelayPojo>) vendorsDelayPojo -> vendorsDelayPojo.getReason().equals("Other Reason"));

        SingleOutputStreamOperator<Tuple3<Long, String, Long>> count_mech = mechanical_problem
                .keyBy(VendorsDelayPojo::getVendor)
                .timeWindow(Time.days(1))
                .aggregate(new AggregateFunction<VendorsDelayPojo, Long, Long>() {
                    @Override
                    public Long createAccumulator() {
                        return 0L;
                    }

                    @Override
                    public Long add(VendorsDelayPojo vendorsDelayPojo, Long acc) {
                        return acc + 1L;
                    }

                    @Override
                    public Long getResult(Long acc) {
                        return acc;
                    }

                    @Override
                    public Long merge(Long acc2, Long acc1) {
                        return acc1 + acc2;
                    }
                }, new ProcessWindowFunction<Long, Tuple3<Long, String, Long>, String, TimeWindow>() {
                    @Override
                    public void process(String s, Context context, Iterable<Long> iterable, Collector<Tuple3<Long, String, Long>> out) throws Exception {
                        long timestamp = context.window().getStart();
                        out.collect(new Tuple3<>(timestamp, s, iterable.iterator().next()));
                    }
                });

        SingleOutputStreamOperator<Tuple3<Long, String, Long>> count_heavy = heavy_traffic
                .keyBy(VendorsDelayPojo::getVendor)
                .timeWindow(Time.days(1))
                .aggregate(new AggregateFunction<VendorsDelayPojo, Long, Long>() {
                    @Override
                    public Long createAccumulator() {
                        return 0L;
                    }

                    @Override
                    public Long add(VendorsDelayPojo vendorsDelayPojo, Long acc) {
                        return acc + 1L;
                    }

                    @Override
                    public Long getResult(Long acc) {
                        return acc;
                    }

                    @Override
                    public Long merge(Long acc2, Long acc1) {
                        return acc1 + acc2;
                    }
                }, new ProcessWindowFunction<Long, Tuple3<Long, String, Long>, String, TimeWindow>() {
                    @Override
                    public void process(String s, Context context, Iterable<Long> iterable, Collector<Tuple3<Long, String, Long>> out) throws Exception {
                        long timestamp = context.window().getStart();
                        out.collect(new Tuple3<>(timestamp, s, iterable.iterator().next()));
                    }
                });



        CoGroupedStreams<Tuple3<Long, String, Long>, Tuple3<Long, String, Long>>.Where<String>.EqualTo cogroup = count_heavy.coGroup(count_mech)
                .where((KeySelector<Tuple3<Long, String, Long>, String>) longStringLongTuple3 -> longStringLongTuple3._2())
                .equalTo((KeySelector<Tuple3<Long, String, Long>, String>) longStringLongTuple3 -> longStringLongTuple3._2());
        DataStream<Tuple5<Long, String, Long, String, Long>> apply = cogroup.window(TumblingEventTimeWindows.of(Time.days(1))).apply(new CoGroupFunction<Tuple3<Long, String, Long>, Tuple3<Long, String, Long>, Tuple5<Long, String, Long, String, Long>>() {
            @Override
            public void coGroup(Iterable<Tuple3<Long, String, Long>> iterable1, Iterable<Tuple3<Long, String, Long>> iterable2, Collector<Tuple5<Long, String, Long, String, Long>> out) throws Exception {
                Tuple3<Long, String, Long> f1 = iterable1.iterator().next();
                Tuple3<Long, String, Long> f2 = iterable2.iterator().next();
                out.collect(new Tuple5<>(f1._1(), f1._2(), f1._3(), f2._2(), f2._3()));
            }
        });

        apply.writeAsText("/opt/flink/flink-jar/results/query2/prova.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        try {
            see.execute("FlinkQuery3");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
