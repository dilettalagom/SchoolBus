package custom_function.aggregate;

import model.ReasonDelayPojo;
import org.apache.flink.api.common.functions.AggregateFunction;
import scala.Tuple2;


public class ReasonAggregator implements AggregateFunction<ReasonDelayPojo, Tuple2<String,Long>, Tuple2<String,Long>> {
    @Override
    public Tuple2<String, Long> createAccumulator() {
        return new Tuple2<>("",0L);
    }

    @Override
    public Tuple2<String, Long> add(ReasonDelayPojo pojo, Tuple2<String, Long> acc) {
        return new Tuple2<>(pojo.getTimeSlot(),acc._2() + 1L);
    }

    @Override
    public Tuple2<String, Long> getResult(Tuple2<String, Long> acc) {
        return acc;
    }

    @Override
    public Tuple2<String, Long> merge(Tuple2<String, Long> acc1, Tuple2<String, Long> acc2) {
        return new Tuple2<>(acc1._1(),acc2._2());
    }



    /*@Override
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
    }*/
}
