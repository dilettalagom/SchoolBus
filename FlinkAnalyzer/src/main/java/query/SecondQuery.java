package query;

import model.ReasonDelayPojo;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import util.PulsarConnection;
import validator.TimeSlotValidator;

public class SecondQuery {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";
    private static final String topic = "persistent://public/default/dataQuery2";

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
                ;

        inputStream.writeAsText("/opt/flink/flink-jar/results/query2/mapped.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);


        try {
            see.execute("FlinkQuery1");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
