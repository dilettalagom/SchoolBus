package aggregate;

import model.BoroDelayPojo;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AverageDelay implements AggregateFunction <BoroDelayPojo, Tuple2<Long, Long>, Double> {

    Logger log;
    @Override
    public Tuple2<Long, Long> createAccumulator() {
        log = LoggerFactory.getLogger(AverageDelay.class);
        return new Tuple2<>(0L, 0L);
    }

    @Override
    public Tuple2<Long, Long> add(BoroDelayPojo value, Tuple2<Long, Long> accumulator) {
        //log.info("actSum : " + accumulator.f0.toString() + " boro: " + value.getBoro());
        return new Tuple2<>(accumulator.f0 + (long) value.getDelay(), accumulator.f1 + 1L);
    }

    @Override
    public Double getResult(Tuple2<Long, Long> accumulator) {
        //log.info("I'M HERE");
        return ((double) accumulator.f0) / accumulator.f1;
    }

    @Override
    public Tuple2<Long, Long> merge(Tuple2<Long, Long> a, Tuple2<Long, Long> b) {
        return new Tuple2<>(a.f0 + b.f0, a.f1 + b.f1);
    }
}

