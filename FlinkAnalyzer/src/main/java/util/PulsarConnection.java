package util;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.pulsar.PulsarSourceBuilder;
import org.apache.pulsar.client.api.PulsarClientException;

public class PulsarConnection {

    private String pulsarURL;
    private String topic;

    public PulsarConnection(String pulsarURL, String topic){
        this.pulsarURL = pulsarURL;
        this.topic = topic;
    }


    public SourceFunction<String> createPulsarConnection(){

        SourceFunction<String> src = null;

        PulsarSourceBuilder<String> builder = PulsarSourceBuilder
                .builder(new SimpleStringSchema())
                .serviceUrl(this.pulsarURL)
                .topic(this.topic)
                .subscriptionName((new Subscription()).generateNewSubScription());
        try {
            src = builder.build();
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
        return src;
    }
}
