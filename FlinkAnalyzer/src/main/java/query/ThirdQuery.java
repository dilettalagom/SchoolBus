package query;

import custom_function.apply.ComputeVendorsPartialScore;
import custom_function.apply.ComputeVendorsRank;
import custom_function.validator.VendorsDelayValidator;
import model.VendorsDelayPojo;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import scala.Tuple3;
import scala.Tuple4;
import time.watermark.DateTimeAscendingAssignerQuery3;
import util.Consumer;


public class ThirdQuery {

    private static final String topic = "dataQuery3";

    public static void main(String[] args) {

        ParameterTool parameter = ParameterTool.fromArgs(args);
        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        see.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        String connector = parameter.get("con");

        DataStreamSource<String> input = (new Consumer()).initConsumer(connector, see, topic);
        assert input!=null;


        KeyedStream<VendorsDelayPojo, String> inputStream = input
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                    if ( !tokens[3].equals(""))
                        return new VendorsDelayPojo(tokens[0], tokens[1], tokens[2], Integer.parseInt(tokens[3]));
                    return null;
                })
                .filter(new VendorsDelayValidator())
                .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery3())
                .keyBy((KeySelector<VendorsDelayPojo, String>) vendorsDelayPojo -> vendorsDelayPojo.getVendor());


        /* day */
        SingleOutputStreamOperator<String> resultDay = inputStream
                .timeWindow(Time.days(1))
                .apply(new ComputeVendorsPartialScore())
                .keyBy((KeySelector<Tuple4<Long, String, Tuple3<Long, Long, Long>,Long>, Long>) t -> t._1())
                .timeWindow(Time.days(1))
                .apply(new ComputeVendorsRank());

        /* week */
        SingleOutputStreamOperator<String> resultWeek = inputStream
                .timeWindow(Time.days(7))
                .apply(new ComputeVendorsPartialScore())
                .keyBy((KeySelector<Tuple4<Long, String, Tuple3<Long, Long, Long>,Long>, Long>) t -> t._1())
                .timeWindow(Time.days(7))
                .apply(new ComputeVendorsRank());

        String outputPath = "/opt/flink/flink-jar/results-"+connector+"/query3/";
        resultDay.writeAsText(outputPath + "resultDay.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
        resultWeek.writeAsText(outputPath + "resultWeek.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);


        try {
            see.execute("FlinkQuery3");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
