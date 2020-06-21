package custom_function.validator;

import model.BoroDelayPojo;
import org.apache.flink.api.common.functions.FilterFunction;
import time.TimeConverter;

public class BoroDelayPojoValidator implements FilterFunction<BoroDelayPojo> {

    @Override
    public boolean filter(BoroDelayPojo boroDelayPojo) throws Exception {

        //long actual = TimeConverter.getInstance().currentClock();
        //boroDelayPojo.setCurrentEventTime(actual - boroDelayPojo.getCurrentEventTime());
        return boroDelayPojo != null && !boroDelayPojo.getBoro().equals("") && boroDelayPojo.getDelay() > 0;
    }
}
