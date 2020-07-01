package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.SnappyTuple2;
import model.SnappyTuple4;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;


public class Tuple2Deserializer implements Deserializer<SnappyTuple2<String,Long>> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public SnappyTuple2<String,Long> deserialize(String s, byte[] bytes) {

        ObjectMapper mapper = new ObjectMapper();
        SnappyTuple2<String,Long> tuple = null;
        try {
            tuple = mapper.readValue(bytes, SnappyTuple2.class);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return tuple;
    }

    @Override
    public void close() {

    }

}
