package custom_function.key;

import org.apache.flink.api.java.functions.KeySelector;
import scala.Tuple2;
import scala.Tuple3;

public class KeyByTimestampReason implements KeySelector<Tuple3<Long,String,Tuple2<String,Long>>,Long> {


    @Override
    public Long getKey(Tuple3<Long,String,Tuple2<String,Long>> in) throws Exception {
        return in._1();
    }
}
