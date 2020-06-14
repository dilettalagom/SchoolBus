package aggregate;

import model.BoroDelayPojo;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AverageDelay implements AggregateFunction <BoroDelayPojo, Tuple2<Long, Long>, Double> {

    private Logger log = LoggerFactory.getLogger(AverageDelay.class);

    @Override
    public Tuple2<Long, Long> createAccumulator() {
        return new Tuple2<>(0L, 0L);
    }

    @Override
    public Tuple2<Long, Long> add(BoroDelayPojo pojo, Tuple2<Long, Long> accumulator) {

        return new Tuple2<>(accumulator.f0 + (long) pojo.getDelay(), accumulator.f1 + 1L);
    }

    @Override
    public Tuple2<Long, Long> merge(Tuple2<Long, Long> a, Tuple2<Long, Long> b) {
        long sum = a.f0 + b.f0;
        long count = a.f1 + b.f1;

        log.debug("sum : {} counter: {}", sum, count);
        return new Tuple2<>(a.f0 + b.f0, a.f1 + b.f1);
    }

    @Override
    public Double getResult(Tuple2<Long, Long> accumulator) {

        Double mean = ((double) accumulator.f0) / accumulator.f1;
        return mean;
    }


}