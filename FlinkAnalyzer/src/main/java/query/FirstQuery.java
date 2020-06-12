package query;
import model.BoroDelayPojo;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.pulsar.PulsarSourceBuilder;

import java.util.Arrays;
import java.util.Properties;

public class FirstQuery {

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";
    private static final String adminUrl = "pulsar://localhost:8080";

    public static void main(String[] args) throws Exception{
        System.out.println("SALVE");



        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();

        PulsarSourceBuilder<String> builder = PulsarSourceBuilder
                .builder(new SimpleStringSchema())
                .serviceUrl(pulsarUrl)
                .topic("persistent://public/default/dataQuery1")
                .subscriptionName(generateNewSubScription());

        SourceFunction<String> src = builder.build();


        DataStream<String> stream = see.addSource(src);

        DataStream<BoroDelayPojo> pojo = stream
                .map(x -> {
                    String[] tokens = x.split(";", -1);
                    return new BoroDelayPojo(tokens[0], tokens[1], tokens[2]);

                });


        pojo.writeAsText("/opt/flink/flink-jar/results/query1.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1);

        try {
            see.execute("FlinkQuery1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String generateNewSubScription(){
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        int count = 10;
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

}
