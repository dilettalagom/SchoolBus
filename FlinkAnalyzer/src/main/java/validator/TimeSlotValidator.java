package validator;

import model.ReasonDelayPojo;
import org.apache.flink.api.common.functions.FilterFunction;
import time.TimeSlotFilter;

public class TimeSlotValidator implements FilterFunction<ReasonDelayPojo> {
    @Override
    public boolean filter(ReasonDelayPojo reasonDelayPojo) throws Exception {
        TimeSlotFilter timeSlotFilter = TimeSlotFilter.getInstance();


        return reasonDelayPojo != null && !reasonDelayPojo.getReason().equals("") &&
                (timeSlotFilter.ckeckAM(reasonDelayPojo) || timeSlotFilter.ckeckPM(reasonDelayPojo));
    }
}
