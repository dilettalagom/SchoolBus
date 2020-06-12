package query;
import model.BoroDelayPojo;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
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

        /*Properties props = new Properties();
        props.setProperty("topic", "dataQuery1");
        props.setProperty("service.url", pulsarUrl);
        props.setProperty("admin.url", adminUrl);
        props.setProperty("flushOnCheckpoint", "true");*/


        PulsarSourceBuilder<String> builder = PulsarSourceBuilder
                .builder(new SimpleStringSchema())
                .serviceUrl(pulsarUrl)
                .topic("persistent://public/default/dataQuery1")
                .subscriptionName("Flink2");

        SourceFunction<String> src = builder.build();


        DataStream<String> stream = see.addSource(src);
        DataStream<BoroDelayPojo> pojo = stream
                .filter(new FilterFunction<String>() {
                    public boolean filter(String line) {
                        return !line.contains("How_Long_Delayed");
                    }})
                .map(x -> {

                    String[] tokens = x.split(";", -1);

                    System.out.println(Arrays.asList(tokens));
                    return new BoroDelayPojo(tokens[0], tokens[1], tokens[2]);

                });


        try {
            see.execute("FlinkQuery1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
