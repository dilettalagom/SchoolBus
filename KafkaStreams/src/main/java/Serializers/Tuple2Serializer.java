package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.SnappyTuple2;
import model.SnappyTuple4;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;


public class Tuple2Serializer implements Serializer<SnappyTuple2<String,Long>> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String s, SnappyTuple2 tuple) {
        byte[] result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            result = objectMapper.writeValueAsString(tuple).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void close() {

    }
}
