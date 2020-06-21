package custom_function.process;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;

//OUT: Tuple4<Timestamp, Fascia oraria, Tuple2< Causa disservizio, rank parziale>,latenza>
public class ReasonProcessWindowFunction extends ProcessWindowFunction<Tuple3<String,Long,Long>, Tuple4<Long,String,Tuple2<String,Long>, Long>, String, TimeWindow> {

    @Override
    public void process(String key, Context context, Iterable<Tuple3<String,Long,Long>> count, Collector<Tuple4<Long,String,Tuple2<String,Long>,Long>> out) throws Exception {

        long timestamp = context.window().getStart();
        Tuple3<String,Long,Long> res = count.iterator().next();

        out.collect(new Tuple4(timestamp,res._1(),new Tuple2<>(key,res._2()),res._3()));
    }


}
