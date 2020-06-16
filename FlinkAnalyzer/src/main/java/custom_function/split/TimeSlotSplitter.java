package custom_function.split;

import model.ReasonDelayPojo;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import time.TimeSlotFilter;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotSplitter implements OutputSelector<ReasonDelayPojo> {
    @Override
    public Iterable<String> select(ReasonDelayPojo pojo) {
        List<String> output = new ArrayList<>();
        if (TimeSlotFilter.getInstance().ckeckAM(pojo)) {
            pojo.setTimeSlot("AM : 5:00-11:59");
            output.add("AM");
        } else {
            pojo.setTimeSlot("PM : 12:00-19:00");
            output.add("PM");
        }
        return output;
    }
}
