package util;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import java.util.Properties;

public class KafkaConsumer extends FlinkKafkaConsumer<String> {

    public KafkaConsumer(String topic) {

        super(topic, new SimpleStringSchema(), initProperties());
    }

    private static Properties initProperties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "kafka:9092");
        properties.setProperty("zookeeper.connect", "zookeeper:2181");
        properties.setProperty("group.id", "SchoolBus");
        return properties;
    }
            }
