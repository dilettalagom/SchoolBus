package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.RankBox;
import org.apache.kafka.common.serialization.Serializer;
import java.util.Map;

public class RankBoxSerializer implements Serializer<RankBox> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String s, RankBox rankBox) {

        byte[] pojoByte = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            pojoByte = objectMapper.writeValueAsString(rankBox).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pojoByte;
    }

    @Override
    public void close() {

    }
}
