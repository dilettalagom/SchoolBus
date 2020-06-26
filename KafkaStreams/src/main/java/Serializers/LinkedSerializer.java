package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ResultPojo;
import model.SnappyTuple3;
import org.apache.kafka.common.serialization.Serializer;

import java.util.ArrayList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class LinkedSerializer implements Serializer<ArrayList<ResultPojo>> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String s, ArrayList<ResultPojo> queue) {

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
