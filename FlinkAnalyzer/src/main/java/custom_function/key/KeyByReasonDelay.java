package custom_function.key;

import model.ReasonDelayPojo;
import org.apache.flink.api.java.functions.KeySelector;

public class KeyByReasonDelay implements KeySelector<ReasonDelayPojo, String> {
    @Override
    public String getKey(ReasonDelayPojo reasonDelayPojo) throws Exception {
        return reasonDelayPojo.getReason();
    }
}
