package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.SnappyTuple3;
import org.apache.kafka.common.serialization.Deserializer;
import java.util.Map;


public class Tuple3Deserializer implements Deserializer<SnappyTuple3<String, String, Integer>> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public SnappyTuple3<String, String, Integer> deserialize(String s, byte[] bytes) {

        ObjectMapper mapper = new ObjectMapper();
        SnappyTuple3<String, String, Integer> tuple = null;
        try {
            tuple = mapper.readValue(bytes, SnappyTuple3.class);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return tuple;
    }

    @Override
    public void close() {

    }

}
