package custom_function.apply;

import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple3;
import scala.Tuple4;
import time.TimeConverter;
import java.util.ArrayList;

//out Tuple3<data, latency, ArrayList<Tuple3<boro, avg, oldlatency>>>
public class ComputeBoroDelayResult implements WindowFunction<Tuple4<Long, String, Double, Long>, Tuple3<String, ArrayList<Tuple3<String, Double, Long>>,Long>, Long, TimeWindow> {
    @Override
    public void apply(Long key, TimeWindow timeWindow, Iterable<Tuple4<Long, String, Double, Long>> iterable, Collector<Tuple3<String, ArrayList<Tuple3<String, Double, Long>>,Long>> out) throws Exception {

        long maxWindow = 0L;
        ArrayList<Tuple3<String, Double, Long>> list = new ArrayList<>();

        for(Tuple4<Long, String, Double, Long> values : iterable){
            maxWindow = Math.max(values._4(),maxWindow);
            list.add(new Tuple3<String, Double, Long>(values._2(),values._3(),0L));
        }
        long end = TimeConverter.currentClock() - maxWindow;
        out.collect(new Tuple3<String, ArrayList<Tuple3<String, Double, Long>>,Long>(TimeConverter.getInstance().convertFromEpochToDate(key), list, end));
    }
}
