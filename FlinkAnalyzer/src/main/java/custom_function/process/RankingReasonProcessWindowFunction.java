package custom_function.process;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RankingReasonProcessWindowFunction extends ProcessWindowFunction<Map<String, Long>, Tuple2<Long, Map<String, Long>>, Long, TimeWindow> {

    @Override
    public void process(Long aLong, Context context, Iterable<Map<String, Long>> map, Collector<Tuple2<Long, Map<String, Long>>> out) throws Exception {

        Stream<Map.Entry<String, Long>> myNewMap = map.iterator().next().entrySet().stream()
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

                //.collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll).;

        out.collect(new Tuple2(aLong, remappered));
    }
}
