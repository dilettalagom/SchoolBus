package custom_function.aggregate;

import org.apache.flink.api.common.functions.AggregateFunction;
import scala.Tuple3;
import scala.Tuple4;
import time.TimeConverter;


import java.util.ArrayList;

public class TimestampAggregator implements AggregateFunction<
        Tuple4<Long, String, Double, Long>, //IN
        ArrayList<Tuple3<String, Double, Long>>, //ACC
        ArrayList<Tuple3<String, Double, Long>> //OUT
        > {


    @Override
    public ArrayList<Tuple3<String, Double, Long>> createAccumulator() {

        return new ArrayList<Tuple3<String, Double, Long>>();
    }

    @Override
    public ArrayList<Tuple3<String, Double, Long>> add(Tuple4<Long, String, Double, Long> input, ArrayList<Tuple3<String, Double, Long>> acc) {

        long eventTime = TimeConverter.currentClock();
        acc.add(new Tuple3<String, Double, Long>(input._2(), input._3(), eventTime-input._4()));
        return acc;
    }

    @Override
    public ArrayList<Tuple3<String, Double, Long>> merge(ArrayList<Tuple3<String, Double, Long>> acc1, ArrayList<Tuple3<String, Double, Long>> acc2) {
        acc1.addAll(acc2);
        return acc1;
    }

    @Override
    public ArrayList<Tuple3<String, Double, Long>> getResult(ArrayList<Tuple3<String, Double, Long>> acc) {
        return acc;
    }

}
