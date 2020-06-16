package custom_function.prova;

import model.ReasonDelayPojo;
import org.apache.flink.api.java.functions.KeySelector;
import scala.Tuple2;

public class KeyBySlotAndReason implements KeySelector<ReasonDelayPojo, Tuple2<String,String>> {
    @Override
    public Tuple2<String, String> getKey(ReasonDelayPojo pojo) throws Exception {
        return new Tuple2<>(pojo.getReason(),pojo.getTimeSlot());
    }
}
