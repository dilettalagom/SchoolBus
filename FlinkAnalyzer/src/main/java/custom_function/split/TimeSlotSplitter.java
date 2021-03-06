package custom_function.split;

import model.ReasonDelayPojo;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotSplitter implements OutputSelector<ReasonDelayPojo> {
    @Override
    public Iterable<String> select(ReasonDelayPojo pojo) {
        List<String> output = new ArrayList<>();
        if (pojo.getTimeSlot().contains("AM")) {
            output.add("AM");
        } else {
            output.add("PM");
        }
        return output;
    }
}
