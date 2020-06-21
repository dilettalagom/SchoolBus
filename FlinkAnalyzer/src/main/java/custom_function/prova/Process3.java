package custom_function.prova;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple3;
import time.TimeConverter;

import java.util.ArrayList;

public class Process3 extends ProcessWindowFunction<Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long>, Tuple3<Long,ArrayList<Tuple2<String, Tuple2<String, Long>>>,Long>, Long, TimeWindow> {


    @Override
    public void process(Long key, Context context, Iterable<Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long>> iterable, Collector<Tuple3<Long,ArrayList<Tuple2<String, Tuple2<String, Long>>>,Long>> out) throws Exception {

        Tuple2<ArrayList<Tuple2<String, Tuple2<String, Long>>>, Long> next = iterable.iterator().next();
        long end = TimeConverter.currentClock() - next._2();
        out.collect(new Tuple3<Long,ArrayList<Tuple2<String, Tuple2<String, Long>>>,Long>(key,next._1(),end));
    }
}
