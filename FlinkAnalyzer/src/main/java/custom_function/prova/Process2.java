package custom_function.prova;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Process2 extends ProcessWindowFunction<Tuple3<String, Map<String, Long>,Long>, Tuple4<Long,String,Tuple2<String, Long>,Long>, Tuple2<Long,String>, TimeWindow> {

    @Override
    public void process(Tuple2<Long,String> aLong, Context context, Iterable<Tuple3<String,Map<String, Long>,Long>> map, Collector<Tuple4<Long,String,Tuple2<String, Long>,Long>> out) throws Exception {

        Tuple3<String,Map<String, Long>,Long> res = map.iterator().next();
        long maxTime = 0L;

        Stream<Map.Entry<String, Long>> myNewMap = res._2().entrySet().stream()
                .sorted(new Comparator<Map.Entry<String, Long>>() {
                    @Override
                    public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                }
                .reversed())
                .limit(3);

        Map<String, Long> remappered = myNewMap
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        maxTime = Math.max(maxTime,res._3());
        //.collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll).;

        out.collect(new Tuple4(aLong._1(),res._1(),remappered,maxTime));
    }
}
