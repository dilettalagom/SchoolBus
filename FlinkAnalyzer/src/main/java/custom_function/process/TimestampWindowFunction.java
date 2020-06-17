package custom_function.process;

import scala.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import time.TimeConverter;

import java.util.ArrayList;


public class TimestampWindowFunction extends ProcessWindowFunction<ArrayList<Tuple2<String, Double>>, Tuple2<String, ArrayList<Tuple2<String, Double>>>, Long, TimeWindow> {

    @Override
    public void process(Long key, Context context, Iterable<ArrayList<Tuple2<String, Double>>> iterable,
                        Collector<Tuple2<String, ArrayList<Tuple2<String, Double>>>> out) throws Exception {

        ArrayList<Tuple2<String,Double>> values = iterable.iterator().next();
        String date = TimeConverter.getInstance().convertFromEpochToDate(key);
        out.collect(new Tuple2<>(date,values) );
    }

}
