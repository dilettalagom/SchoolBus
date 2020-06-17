package custom_function.apply;

import model.VendorsDelayPojo;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple3;

public class ComputeVendorsPartialScore implements WindowFunction<VendorsDelayPojo, Tuple3<Long, String, Tuple3<Long, Long, Long>>, String, TimeWindow> {


    @Override
    public void apply(String key,
                      TimeWindow timeWindow,
                      Iterable<VendorsDelayPojo> iterable,
                      Collector<Tuple3<Long, String, Tuple3<Long, Long, Long>>> out) throws Exception {

        Tuple3<Long, Long, Long> actualValue = new Tuple3<>(0L, 0L, 0L);

        for(VendorsDelayPojo v : iterable){
            Long valueToAdd = checkDelay(v.getDelay());
            actualValue = updatScoreValues(actualValue, v, valueToAdd);
    }

    out.collect(new Tuple3<>(timeWindow.getStart(), key, actualValue));

    }


    private Tuple3<Long, Long, Long> updatScoreValues(Tuple3<Long, Long, Long> actualValue, VendorsDelayPojo v, Long valueToAdd) {
            switch (v.getReason()){
                case "Heavy Traffic":
                    actualValue =  new Tuple3<>(actualValue._1() + valueToAdd, actualValue._2(), actualValue._3());
                    break;
                case "Mechanical Problem":
                    actualValue =  new Tuple3<>(actualValue._1(), actualValue._2() + valueToAdd, actualValue._3());
                    break;
                case "Other Reason":
                    actualValue =  new Tuple3<>(actualValue._1(), actualValue._2(), actualValue._3() + valueToAdd);
                    break;
                default:
                    actualValue = null;
                    break;
            }
            return actualValue;
    }



    private Long checkDelay(Integer delay){
        if(delay > 30){
            return 2L;
        }else{
            return 1L;
        }
    }
}
