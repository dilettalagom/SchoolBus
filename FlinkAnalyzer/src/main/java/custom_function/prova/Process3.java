package custom_function.prova;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import time.TimeConverter;
import java.util.ArrayList;


public class Process3 extends ProcessWindowFunction<Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long>, String, Long, TimeWindow> {


    @Override
    public void process(Long key, Context context, Iterable<Tuple2<ArrayList<Tuple2<String,Tuple2<String, Long>>>,Long>> iterable, Collector<String> out) throws Exception {

        Tuple2<ArrayList<Tuple2<String, Tuple2<String, Long>>>, Long> next = iterable.iterator().next();
        StringBuilder sb = new StringBuilder();
        long end = TimeConverter.currentClock() - next._2();
        sb.append(TimeConverter.getInstance().convertFromEpochToDate(key)).append("; ").append(next._1()).append("; ").append(end);
        out.collect(sb.toString());
    }
}

