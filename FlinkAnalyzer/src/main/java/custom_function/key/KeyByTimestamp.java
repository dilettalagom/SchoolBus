package custom_function.key;

import org.apache.flink.api.java.functions.KeySelector;
import scala.Tuple3;

public class KeyByTimestamp implements KeySelector<Tuple3<Long,String,Double>,Long> {

    @Override
    public Long getKey(Tuple3<Long, String, Double> t3) throws Exception {
        return t3._1();
    }
}
