package custom_function.aggregate;

import model.ReasonDelayPojo;
import org.apache.flink.api.common.functions.AggregateFunction;
import scala.Tuple3;

//OUT: reason, count, latency
public class ReasonAggregator implements AggregateFunction<ReasonDelayPojo, Tuple3<String,Long,Long>, Tuple3<String,Long,Long>> {

    @Override
    public Tuple3<String,Long,Long> createAccumulator() {
        return new Tuple3<>("",0L,0L);
    }

    @Override
    public Tuple3<String,Long,Long> add(ReasonDelayPojo pojo, Tuple3<String,Long,Long> acc) {

        long actualTime = Math.max(pojo.getCurrentEventTime(),acc._3());
        return new Tuple3<>(pojo.getTimeSlot(),acc._2() + 1L,actualTime);
        //return new Tuple3<>(pojo.getReason(),acc._2() + 1L,actualTime);
    }

    @Override
    public Tuple3<String,Long,Long> getResult(Tuple3<String,Long,Long> acc) {
        return acc;
    }

    @Override
    public Tuple3<String,Long,Long> merge(Tuple3<String,Long,Long> acc1, Tuple3<String,Long,Long> acc2) {
        long actualTime = Math.max(acc1._3(),acc2._3());
        return new Tuple3<>(acc1._1(),acc1._2() + acc2._2(),actualTime);
    }

}
