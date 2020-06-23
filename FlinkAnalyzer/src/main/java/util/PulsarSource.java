package util;

import lombok.Data;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.Consumer;
import java.io.Serializable;


@Data
public class PulsarSource implements SourceFunction<String>,Serializable{

    private static final String pulsarUrl = "pulsar://pulsar-node:6650";
    private static final String topic = "dataQuery1";
    private volatile boolean isRunning = true;


    private PulsarClient client;
    private Consumer consumer;


    public PulsarSource(){
        //this.client = client;
        initPulsarClient();
        initPulsarConsumer();
    }

    @Override
    public void run(SourceContext<String> sourceContext) throws Exception {
        while(isRunning){
            Message<Serializable> msg = consumer.receive();
            //synchronized (sourceContext.getCheckpointLock()){
                sourceContext.collect(new String(msg.getData()));
           // }
            consumer.acknowledge(msg);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
        /*try {
            consumer.close();
            client.close();
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }*/
    }

    public static PulsarClient initPulsarClient() {
        try {
            return PulsarClient.builder()
                    .serviceUrl(pulsarUrl)
                    .build();
        } catch (PulsarClientException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    private void initPulsarConsumer(){
        try {
            consumer = client.newConsumer()
                    .topic(topic)
                    .subscriptionName("consumer")
                    .subscribe();
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }

}
