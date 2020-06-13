package aggregate;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple3;

public class DelayProcessWindowFunction extends ProcessWindowFunction<Double, Tuple3<Long,String, Double>, String, TimeWindow> {


    @Override
    public void process(String key, Context context, Iterable<Double> averages, Collector<Tuple3<Long,String, Double>> out) throws Exception {

        long timestamp = context.window().getStart();
        Double average = averages.iterator().next();
        out.collect(new Tuple3(timestamp,key, average));
    }
}