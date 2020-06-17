package custom_function.cogroup;

import model.ResultSlotRankPojo;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple3;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;

public class PrintRankResults implements CoGroupFunction<Tuple3<Long, String, Map<String, Long>>, Tuple3<Long, String, Map<String, Long>>, ResultSlotRankPojo> {
    @Override
    public void coGroup(Iterable<Tuple3<Long, String, Map<String, Long>>> rankAM, Iterable<Tuple3<Long, String, Map<String, Long>>> rankPM, Collector<ResultSlotRankPojo> out) throws Exception {

        ArrayList<Tuple2<String,Map<String, Long>>> temp = new ArrayList<>();

        Tuple3<Long, String, Map<String, Long>> elemAM;
        Tuple3<Long, String, Map<String, Long>> elemPM;

        try{
            elemAM = rankAM.iterator().next();
        }catch (NoSuchElementException e){
            elemAM = null;
        }

        try{
            elemPM = rankPM.iterator().next();
        }catch (NoSuchElementException e){
            elemPM = null;
        }

        Long timestamp = 0L;
        if( elemAM != null) {
            timestamp = elemAM._1();
            temp.add(new Tuple2<>(elemAM._2(), elemAM._3()));
        }

        if (elemPM != null){
            timestamp = elemPM._1();
            temp.add(new Tuple2<>(elemPM._2(), elemPM._3()));
        }

        ResultSlotRankPojo pojo = new ResultSlotRankPojo(timestamp, temp);
        out.collect(pojo);
    }
}
