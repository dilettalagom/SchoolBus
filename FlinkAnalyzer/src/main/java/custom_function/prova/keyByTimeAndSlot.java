package custom_function.prova;

import org.apache.flink.api.java.functions.KeySelector;
import scala.Tuple2;
import scala.Tuple4;

public class keyByTimeAndSlot implements KeySelector<Tuple4<Long,String, Tuple2<String,Long>,Long>,Tuple2<Long,String>> {

    @Override
    public Tuple2<Long, String> getKey(Tuple4<Long, String, Tuple2<String, Long>,Long> input) throws Exception {
        return new Tuple2<>(input._1(),input._2());
    }
}
