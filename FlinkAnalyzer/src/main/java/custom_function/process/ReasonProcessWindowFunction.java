package custom_function.process;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple3;


public class ReasonProcessWindowFunction extends ProcessWindowFunction<Tuple2<String,Long>, Tuple3<Long,String,Tuple2<String,Long>>, String, TimeWindow> {

    @Override
    public void process(String key, Context context, Iterable<Tuple2<String,Long>> count, Collector<Tuple3<Long,String,Tuple2<String,Long>>> out) throws Exception {
        long timestamp = context.window().getStart();
        Tuple2<String,Long> res = count.iterator().next();
        out.collect(new Tuple3(timestamp,res._1(),new Tuple2<>(key,res._2())));
    }


}
