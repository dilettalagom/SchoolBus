package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ResultPojo;
import model.SnappyTuple3;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;
import java.util.PriorityQueue;

public class QueueSerializer implements Serializer<PriorityQueue<ResultPojo>> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String s, PriorityQueue<ResultPojo> queue) {

        byte[] pojoByte = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            pojoByte = objectMapper.writeValueAsString(queue).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pojoByte;
    }

    @Override
    public void close() {

    }
}
