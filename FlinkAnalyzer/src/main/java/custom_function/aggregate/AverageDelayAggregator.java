package custom_function.aggregate;

import model.BoroDelayPojo;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.Tuple3;
import time.TimeConverter;

//Tuple3<sum, count, eventTime>
public class AverageDelayAggregator implements AggregateFunction<BoroDelayPojo, Tuple3<Long, Long, Long>, Tuple2<Long,Double>> {

    private Logger log = LoggerFactory.getLogger(AverageDelayAggregator.class);

    @Override
    public Tuple3<Long, Long, Long> createAccumulator() {
        return new Tuple3<>(0L, 0L, 0L);
    }

    @Override
    public Tuple3<Long, Long, Long> add(BoroDelayPojo pojo, Tuple3<Long, Long, Long> accumulator) {

        long actual = TimeConverter.currentClock();

        log.info("actual: {} POJO_T: {}",actual, pojo.getCurrentEventTime());
        return new Tuple3<>(accumulator._1() + (long) pojo.getDelay(), accumulator._2() + 1L,
                Math.max(actual, accumulator._3()) );
    }

    @Override
    public Tuple3<Long, Long, Long> merge(Tuple3<Long, Long, Long> a, Tuple3<Long, Long, Long> b) {

        long sum = a._1() + b._1();
        long count = a._2()+ b._2();
        long eventTime = Math.max(a._3(), b._3());

        return new Tuple3<>(sum, count, eventTime);
    }

    @Override
    public Tuple2<Long,Double> getResult(Tuple3<Long, Long, Long> accumulator) {

        Double mean = ((double) accumulator._1()) / accumulator._2();
        long actual = TimeConverter.currentClock() - accumulator._3();
        return new Tuple2<>(actual, mean);
    }


}