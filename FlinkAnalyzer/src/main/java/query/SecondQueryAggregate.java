package query;

import custom_function.aggregate.TimestampReasonAggregator;
import custom_function.prova.*;
import custom_function.validator.TimeSlotValidator;
import model.ReasonDelayPojo;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import time.watermark.DateTimeAscendingAssignerQuery2;
import util.Consumer;



public class SecondQueryAggregate {

    //private static final String topic = "persistent://public/default/dataQuery2";
    private static final String topic = "dataQuery2";


    public static void main(String[] args) {

        ParameterTool parameter = ParameterTool.fromArgs(args);
        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
        see.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        String connector = parameter.get("con");

        DataStreamSource<String> input = (new Consumer()).initConsumer(connector, see, topic);
        assert input!=null;


        SingleOutputStreamOperator<ReasonDelayPojo> inputStream = input
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                    return new ReasonDelayPojo(tokens[0], tokens[1]);
                })
                .filter(new TimeSlotValidator())
                .assignTimestampsAndWatermarks(new DateTimeAscendingAssignerQuery2());

        /* day */
        String outputPath = "/opt/flink/flink-jar/results-"+connector+"/query2-aggregate/";

        SingleOutputStreamOperator<String> dayStream = computeStreamByWindow(inputStream, 1);
        dayStream.writeAsText(outputPath + "dayStreamAggregate.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        /* week */
        SingleOutputStreamOperator<String> weekStream = computeStreamByWindow(inputStream, 7);
        weekStream.writeAsText(outputPath + "weekStreamAggregate.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        try {
            see.execute("FlinkQuery2Aggr");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static SingleOutputStreamOperator<String> computeStreamByWindow (SingleOutputStreamOperator<ReasonDelayPojo> inputStream, int window){
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
