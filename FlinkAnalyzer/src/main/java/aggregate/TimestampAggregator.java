package aggregate;

import org.apache.flink.api.common.functions.AggregateFunction;
import scala.Tuple2;
import scala.Tuple3;


import java.util.ArrayList;

public class TimestampAggregator implements AggregateFunction<
        Tuple3<Long, String, Double>, //IN
        ArrayList<Tuple2<String, Double>>, //ACC
        ArrayList<Tuple2<String, Double>> //OUT
        > {


    @Override
    public ArrayList<Tuple2<String, Double>> createAccumulator() {

        return new ArrayList<Tuple2<String, Double>>();
    }

    @Override
    public ArrayList<Tuple2<String, Double>> add(Tuple3<Long, String, Double> input, ArrayList<Tuple2<String, Double>> acc) {
        acc.add(new Tuple2<String, Double>(input._2(), input._3()));
        return acc;
    }

    @Override
    public ArrayList<Tuple2<String, Double>> merge(ArrayList<Tuple2<String, Double>> acc1, ArrayList<Tuple2<String, Double>> acc2) {
        acc1.addAll(acc2);
        return acc1;
    }

    @Override
    public ArrayList<Tuple2<String, Double>> getResult(ArrayList<Tuple2<String, Double>> acc) {
        return acc;
    }

}
