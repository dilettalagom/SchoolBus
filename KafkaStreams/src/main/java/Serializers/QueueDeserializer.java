package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ResultPojo;
import model.SnappyTuple3;
import org.apache.kafka.common.serialization.Deserializer;
import java.util.Map;
import java.util.PriorityQueue;

public class QueueDeserializer implements Deserializer<PriorityQueue<ResultPojo>> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public PriorityQueue<ResultPojo> deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        PriorityQueue<ResultPojo> queue = null;
        try {
            queue = mapper.readValue(bytes, PriorityQueue.class);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return queue;
    }

    @Override
    public void close() {

    }
}
