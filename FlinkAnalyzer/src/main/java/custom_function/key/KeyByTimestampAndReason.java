package custom_function.key;

import org.apache.flink.api.java.functions.KeySelector;
import scala.Tuple2;
import scala.Tuple4;

public class KeyByTimestampAndReason implements KeySelector<Tuple4<Long,String,Tuple2<String,Long>,Long>,Long> {


    @Override
    public Long getKey(Tuple4<Long,String,Tuple2<String,Long>,Long> in) throws Exception {
        return in._1();
    }
}
