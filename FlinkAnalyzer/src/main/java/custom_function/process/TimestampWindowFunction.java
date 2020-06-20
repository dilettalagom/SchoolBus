package custom_function.process;

import scala.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple3;
import time.TimeConverter;
import java.util.ArrayList;


public class TimestampWindowFunction extends ProcessWindowFunction<ArrayList<Tuple3<String, Double, Long>>, Tuple2<String, ArrayList<Tuple3<String, Double, Long>>>, Long, TimeWindow> {

    @Override
    public void process(Long key, Context context, Iterable<ArrayList<Tuple3<String, Double, Long>>> iterable,
                        Collector<Tuple2<String, ArrayList<Tuple3<String, Double, Long>>>> out) throws Exception {

        ArrayList<Tuple3<String, Double, Long>> values = iterable.iterator().next();
        String date = TimeConverter.getInstance().convertFromEpochToDate(key);
        out.collect(new Tuple2<>(date,values) );
    }

}
