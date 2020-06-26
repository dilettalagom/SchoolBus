package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ReasonDelayPojo;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class ReasonPojoDeserializer implements Deserializer<ReasonDelayPojo> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public ReasonDelayPojo deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        ReasonDelayPojo pojo = null;
        try {
            pojo = mapper.readValue(bytes, ReasonDelayPojo.class);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return pojo;
    }


    @Override
    public void close() {

    }
}

