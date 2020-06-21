package custom_function.prova;

import org.apache.flink.api.common.functions.AggregateFunction;
import scala.Tuple2;
import scala.Tuple4;
import java.util.ArrayList;

public class Aggregatore3 implements AggregateFunction<Tuple4<Long,String,Tuple2<String, Long>,Long>, Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long>, Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long>> {


    @Override
    public Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long> createAccumulator() {
        return new Tuple2<>(new ArrayList<>(), 0L);
    }

    @Override
    public Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long> add(Tuple4<Long, String, Tuple2<String, Long>,Long> input, Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long> acc) {
        long eventTime = Math.max(input._4(),acc._2());
        acc._1().add(new Tuple2<>(input._2(),input._3()));
        return new Tuple2<>(acc._1(),eventTime);
    }

    @Override
    public Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long> getResult(Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long> acc) {
        return acc;
    }

    @Override
    public Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long> merge(Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long> acc1, Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long> acc2) {
        long eventTime = Math.max(acc1._2(),acc2._2());
        acc1._1().addAll(acc2._1());
        return new Tuple2<>(acc1._1(),eventTime);
    }
}
