package Serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.RankBox;
import org.apache.kafka.common.serialization.Deserializer;
import java.util.Map;

public class RankBoxDeserializer implements Deserializer<RankBox> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public RankBox deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        RankBox rankBox = null;
        try {
            rankBox = mapper.readValue(bytes, RankBox.class);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return rankBox;
    }


    @Override
    public void close() {

    }
}