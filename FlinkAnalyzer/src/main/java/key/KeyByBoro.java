package key;

import model.BoroDelayPojo;
import org.apache.flink.api.java.functions.KeySelector;

public class KeyByBoro implements KeySelector<BoroDelayPojo, String> {


    @Override
    public String getKey(BoroDelayPojo boroDelayPojo) throws Exception {
        return boroDelayPojo.getBoro();
    }
}
