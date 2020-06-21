package custom_function.prova;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple4;

public class Process1 extends ProcessWindowFunction<Tuple2<Long,Long>, Tuple4<Long,String,Tuple2<String,Long>,Long>, Tuple2<String,String>, TimeWindow> {
    @Override
    public void process(Tuple2<String, String> key, Context context, Iterable<Tuple2<Long,Long>> count, Collector<Tuple4<Long, String, Tuple2<String, Long>,Long>> out) throws Exception {

        long timestamp = context.window().getStart();
        Tuple2<Long,Long> res = count.iterator().next();
        out.collect(new Tuple4<>(timestamp,key._2(),new Tuple2(key._1(),res._1()),res._2()));
    }
}
