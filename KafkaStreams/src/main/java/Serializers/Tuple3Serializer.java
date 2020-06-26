package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.SnappyTuple3;
import org.apache.kafka.common.serialization.Serializer;
import java.util.Map;


public class Tuple3Serializer implements Serializer<SnappyTuple3<String, String, Integer>> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String s, SnappyTuple3 tuple) {
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
