package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.SnappyTuple4;
import org.apache.kafka.common.serialization.Deserializer;
import java.util.Map;


public class Tuple4Deserializer implements Deserializer<SnappyTuple4<String, String, Integer, Long>> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public SnappyTuple4<String, String, Integer, Long> deserialize(String s, byte[] bytes) {

        ObjectMapper mapper = new ObjectMapper();
        SnappyTuple4<String, String, Integer, Long> tuple = null;
        try {
            tuple = mapper.readValue(bytes, SnappyTuple4.class);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return tuple;
    }

    @Override
    public void close() {

    }

}
