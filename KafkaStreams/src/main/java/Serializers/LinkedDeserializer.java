package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ResultPojo;
import model.SnappyTuple3;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.*;

public class LinkedDeserializer implements Deserializer<ArrayList<ResultPojo>> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public ArrayList<ResultPojo> deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<ResultPojo> queue = null;
        try {
            queue = mapper.readValue(bytes, ArrayList.class);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return queue;
    }

    @Override
    public void close() {

    }
}
