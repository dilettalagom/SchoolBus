package query;

import custom_function.aggregate.AverageDelayAggregator;
import custom_function.apply.ComputeBoroDelayResult;
import custom_function.process.DelayProcessWindowFunction;
import model.BoroDelayPojo;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import scala.Tuple4;
import time.MonthWindow;
import time.watermark.DateTimeAscendingAssignerQuery1;
import custom_function.validator.BoroDelayPojoValidator;
import util.Consumer;


public class FirstQuery{

    private static final String topic = "non-persistent://public/default/dataQuery1";

    public static void main(String[] args) throws Exception{

        ParameterTool parameter = ParameterTool.fromArgs(args);
        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        see.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        String connector = parameter.get("con");


        DataStreamSource<String> input = (new Consumer()).initConsumer(connector, see, topic);
        assert input!=null;


        KeyedStream<BoroDelayPojo, String> inputStream = input
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                   // return new BoroDelayPojo(tokens[7], tokens[9], tokens[11]);
                    return new BoroDelayPojo(tokens[0], tokens[1], tokens[2]);
                })
                .filter(new BoroDelayPojoValidator())
                .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery1())
                .keyBy((KeySelector<BoroDelayPojo, String>) BoroDelayPojo::getBoro);

        /* day */
        SingleOutputStreamOperator<String> dayResult = inputStream
                .timeWindow(Time.days(1))
                .aggregate(new AverageDelayAggregator(), new DelayProcessWindowFunction())
                .keyBy((KeySelector<Tuple4<Long, String, Double, Long>, Long>) t -> t._1())
                .timeWindow(Time.days(1))
                .apply(new ComputeBoroDelayResult())
                .name("Compute day");

        /* week */
        SingleOutputStreamOperator<String> weekResult = inputStream
                .timeWindow(Time.days(7))
                .aggregate(new AverageDelayAggregator(), new DelayProcessWindowFunction())
                .keyBy((KeySelector<Tuple4<Long, String, Double, Long>, Long>) t -> t._1())
                .timeWindow(Time.hours(7))
                .apply(new ComputeBoroDelayResult())
                .name("Compute week");

        /* month */
        SingleOutputStreamOperator<String> monthResult = inputStream
                .window(new MonthWindow())
                .aggregate(new AverageDelayAggregator(), new DelayProcessWindowFunction())
                .keyBy((KeySelector<Tuple4<Long, String, Double, Long>, Long>) t -> t._1())
                .window(new MonthWindow())
                .apply(new ComputeBoroDelayResult())
                .name("Compute month");


        /* save results on textfile */
        String outputPath = "/opt/flink/flink-jar/results-"+connector+"/query1/";
        dayResult.writeAsText(outputPath + "dayResult.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1).name("Write day result ");
        weekResult.writeAsText(outputPath + "weekResult.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1).name("Write week result ");
        monthResult.writeAsText(outputPath + "monthResult.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1).name("Write month result ");

        /*StreamingFileSink<Tuple3<String, ArrayList<Tuple3<String, Double, Long>>,Long>> sink = StreamingFileSink
                 .forRowFormat(new Path("/opt/flink/flink-jar/results/query1/"),
                         (Encoder<Tuple3<String, ArrayList<Tuple3<String, Double, Long>>,Long>>) (element, stream) -> {
                             PrintStream out = new PrintStream(stream);
                             out.println(element);
                 })
                .build();
        dayResultPT2.addSink(sink).name("Save dayResult");*/

        //dayResult.union(weekResult, monthResult).writeAsText(outputPath + "forthroughput.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1).name("For Throughput");

        try {
            see.execute("FlinkQuery1");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

