package custom_function.process;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple4;

public class DelayProcessWindowFunction extends ProcessWindowFunction<Tuple2<Long, Double>, Tuple4<Long,String, Double, Long>, String, TimeWindow> {

    @Override
    public void process(String key, Context context, Iterable< Tuple2<Long, Double>> averages,
                        Collector<Tuple4<Long, String, Double, Long>> out) throws Exception {

        long timestamp = context.window().getStart();
        Tuple2<Long, Double> temp = averages.iterator().next();
        out.collect(new Tuple4(timestamp, key, temp._2(),temp._1()));
    }
}