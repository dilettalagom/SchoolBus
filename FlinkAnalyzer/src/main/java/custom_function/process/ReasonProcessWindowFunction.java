package custom_function.process;


import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;


public class ReasonProcessWindowFunction extends ProcessWindowFunction<Long, Tuple2<Long, Tuple2<String,Long>>, String, TimeWindow> {

    @Override
    public void process(String key, Context context, Iterable<Long> count, Collector<Tuple2<Long, Tuple2<String,Long>>> out) throws Exception {
        long timestamp = context.window().getStart();
        out.collect(new Tuple2(timestamp,new Tuple2<>(key,count.iterator().next())));
    }


}
