package custom_function.validator;

import model.BoroDelayPojo;
import org.apache.flink.api.common.functions.FilterFunction;

public class BoroDelayPojoValidator implements FilterFunction<BoroDelayPojo> {

    @Override
    public boolean filter(BoroDelayPojo boroDelayPojo) throws Exception {

        return boroDelayPojo != null && !boroDelayPojo.getBoro().equals("") && boroDelayPojo.getDelay() > 0;
    }
}
