package custom_function.aggregate;

import org.apache.flink.api.common.functions.AggregateFunction;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;
import java.util.HashMap;
import java.util.Map;


//OUT: Tuple3<Fascia oraria, Map<Disserivizio, rankScore>,latenza>
public class TimestampReasonAggregator implements AggregateFunction<Tuple4<Long,String, Tuple2<String, Long>,Long>, Tuple3<String,Map<String, Long>, Long>, Tuple3<String,Map<String, Long>, Long>> {

    @Override
    public Tuple3<String,Map<String, Long>, Long> createAccumulator() {
        return new Tuple3<>("",new HashMap<>(),0L);
    }


    @Override
    public Tuple3<String,Map<String, Long>, Long> add(Tuple4<Long,String, Tuple2<String, Long>,Long> pojo, Tuple3<String,Map<String, Long>, Long> acc) {

        long eventTime =  Math.max(pojo._4(), acc._3());

        if(acc._2().containsKey(pojo._3()._1()))
            acc._2().put(pojo._3()._1(), acc._2().get(pojo._3()._1()) + pojo._3()._2());
        else
            acc._2().put(pojo._3()._1(), pojo._3()._2());

        return new Tuple3<>(pojo._2(),acc._2(),eventTime);
    }


    @Override
    public Tuple3<String,Map<String, Long>, Long> getResult(Tuple3<String,Map<String, Long>, Long> acc) {
        return acc;
    }


    @Override
    public Tuple3<String,Map<String, Long>, Long> merge(Tuple3<String,Map<String, Long>, Long> acc1, Tuple3<String,Map<String, Long>, Long> acc2) {

        long eventTime = Math.max(acc1._3(), acc2._3());
        acc2._2().forEach(
                (key, value) -> acc1._2().merge(key, value, (v1, v2) -> v1 + v2)
        );
        return new Tuple3(acc2._1(),acc2._2(),eventTime);
    }
}
