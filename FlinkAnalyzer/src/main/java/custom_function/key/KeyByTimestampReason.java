package custom_function.key;

import org.apache.flink.api.java.functions.KeySelector;
import scala.Tuple2;

public class KeyByTimestampReason implements KeySelector<Tuple2<Long, Tuple2<String, Long>>,Long> {


    @Override
    public Long getKey(Tuple2<Long, Tuple2<String, Long>> in) throws Exception {
        return in._1();
    }
}
