package custom_function.aggregate;


import org.apache.flink.api.common.functions.AggregateFunction;
import scala.Tuple2;
import scala.Tuple3;

import java.util.HashMap;
import java.util.Map;

public class TimestampReasonAggregator implements AggregateFunction<Tuple3<Long,String, Tuple2<String, Long>>, Tuple2<String,Map<String, Long>>, Tuple2<String,Map<String, Long>>> {

    @Override
    public Tuple2<String,Map<String, Long>> createAccumulator() {
        return new Tuple2<>("",new HashMap<>());
    }

    @Override
    public Tuple2<String,Map<String, Long>> add(Tuple3<Long,String, Tuple2<String, Long>> pojo, Tuple2<String,Map<String, Long>> acc) {
        if(acc._2().containsKey(pojo._3()._1()))
            acc._2().put(pojo._3()._1(), acc._2().get(pojo._3()._1()) + pojo._3()._2());
        else
            acc._2().put(pojo._3()._1(), pojo._3()._2());
        return new Tuple2<>(pojo._2(),acc._2());
    }

    @Override
    public Tuple2<String,Map<String, Long>> getResult(Tuple2<String,Map<String, Long>> acc) {
        return acc;
    }

    @Override
    public Tuple2<String,Map<String, Long>> merge(Tuple2<String,Map<String, Long>> acc1,Tuple2<String,Map<String, Long>> acc2) {
        acc2._2().forEach(
                (key, value) -> acc1._2().merge(key, value, (v1, v2) -> v1 + v2)
        );
        return acc2;
    }
}
