package custom_function.prova;

import org.apache.flink.api.java.functions.KeySelector;
import scala.Tuple2;
import scala.Tuple4;

public class KeyByTimestamp implements KeySelector<Tuple4<Long,String, Tuple2<String, Long>,Long>,Long> {

    @Override
    public Long getKey(Tuple4<Long, String, Tuple2<String, Long>,Long> o) throws Exception {
        return o._1();
    }
}
