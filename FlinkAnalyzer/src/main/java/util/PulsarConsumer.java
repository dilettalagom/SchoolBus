package util;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.pulsar.PulsarSourceBuilder;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.Properties;

public class PulsarConsumer {

    private static final String pulsarURL = "pulsar://pulsar-node:6650";
    private String topic;

    public PulsarConsumer(String topic) {
        this.topic = topic;
    }


    public SourceFunction<String> initPulsarConnection() {

        SourceFunction<String> src = null;

        PulsarSourceBuilder<String> builder = PulsarSourceBuilder
                .builder(new SimpleStringSchema())
                .serviceUrl(pulsarURL)
                .topic(this.topic)
                .subscriptionName(generateNewSubScription())
                .acknowledgementBatchSize(1000L);
        try {
            src = builder.build();
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
        return src;
    }


    public String generateNewSubScription() {
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder builder = new StringBuilder();
        int count = 10;
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

}
