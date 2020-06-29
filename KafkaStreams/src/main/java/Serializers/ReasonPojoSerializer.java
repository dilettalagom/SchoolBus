package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ReasonDelayPojo;
import org.apache.kafka.common.serialization.Serializer;
import java.util.Map;

public class ReasonPojoSerializer implements Serializer<ReasonDelayPojo> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String s, ReasonDelayPojo pojo) {

        byte[] pojoByte = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            pojoByte = objectMapper.writeValueAsString(pojo).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pojoByte;
    }

    @Override
    public void close() {

    }
}
