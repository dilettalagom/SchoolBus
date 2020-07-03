package util;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;


public class Consumer {

    public  DataStreamSource<String> initConsumer(String type, StreamExecutionEnvironment see, String topic ){
        DataStreamSource<String> input = null;
        switch (type){
            case "pulsar":
                PulsarConsumer conn = new PulsarConsumer("non-persistent://public/default/"+topic);
                SourceFunction<String> src = conn.initPulsarConnection();
                input = see.addSource(src);
                break;
            case "kafka":
                input = see.addSource(new KafkaConsumer(topic));
                break;
        }
        return input;
    }


}
