package custom_function.aggregate;

import model.ReasonDelayPojo;
import org.apache.flink.api.common.functions.AggregateFunction;
import java.util.HashMap;
import java.util.Map;

public class ReasonAggregator implements AggregateFunction<ReasonDelayPojo, Long, Long> {
    @Override
    public Long createAccumulator() {
        return 0L;
    }

    @Override
    public Long add(ReasonDelayPojo pojo, Long acc) {
       return acc + 1L;
    }

    @Override
    public Long getResult(Long acc) {
        return acc;
    }

    @Override
    public Long merge(Long acc1, Long acc2) {
        return acc1 + acc2;
    }
}
