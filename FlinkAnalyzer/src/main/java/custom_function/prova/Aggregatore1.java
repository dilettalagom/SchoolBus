package custom_function.prova;

import model.ReasonDelayPojo;
import org.apache.flink.api.common.functions.AggregateFunction;
import scala.Tuple2;

public class Aggregatore1 implements AggregateFunction<ReasonDelayPojo, Tuple2<Long,Long>, Tuple2<Long,Long>> {

    @Override
    public Tuple2<Long,Long> createAccumulator() {
        return new Tuple2<>(0L,0L);
    }

    @Override
    public Tuple2<Long,Long> add(ReasonDelayPojo pojo, Tuple2<Long,Long> acc) {
        long eventTime = Math.max(pojo.getCurrentEventTime(), acc._2());
        return new Tuple2<>(acc._1() + 1L,eventTime);
    }

    @Override
    public Tuple2<Long,Long> getResult(Tuple2<Long,Long> acc) {
        return acc;
    }

    @Override
    public Tuple2<Long,Long> merge(Tuple2<Long,Long> acc1, Tuple2<Long,Long> acc2) {
        long eventTime = Math.max(acc1._2(),acc2._2());
        return new Tuple2<>(acc1._1() + acc2._1(),eventTime);
    }
}
