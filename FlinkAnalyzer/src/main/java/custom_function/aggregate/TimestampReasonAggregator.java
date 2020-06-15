package custom_function.aggregate;


import org.apache.flink.api.common.functions.AggregateFunction;
import scala.Tuple2;
import java.util.HashMap;
import java.util.Map;

public class TimestampReasonAggregator implements AggregateFunction<Tuple2<Long, Tuple2<String, Long>>, Map<String, Long>, Map<String, Long>> {

    @Override
    public Map<String, Long> createAccumulator() {
        return new HashMap<>();
    }

    @Override
    public Map<String, Long> add(Tuple2<Long, Tuple2<String, Long>> pojo, Map<String, Long> acc) {
        if(acc.containsKey(pojo._2()._1()))
            acc.put(pojo._2()._1(), acc.get(pojo._2()._1()) + pojo._2()._2());
        else
            acc.put(pojo._2()._1(), pojo._2()._2());
        return acc;
    }

    @Override
    public Map<String, Long> getResult(Map<String, Long> acc) {
        return acc;
    }

    @Override
    public Map<String, Long> merge(Map<String, Long> acc1, Map<String, Long> acc2) {
        acc2.forEach(
                (key, value) -> acc1.merge(key, value, (v1, v2) -> v1 + v2)
        );
        return acc2;
    }
}
