package custom_function.cogroup;

import model.ResultSlotRankPojo;
import org.apache.flink.api.common.functions.CoGroupFunction;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple4;
import time.TimeConverter;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;

public class PrintRankCoGroupResults implements CoGroupFunction<Tuple4<Long, String, Map<String, Long>,Long>, Tuple4<Long, String, Map<String, Long>,Long>, ResultSlotRankPojo> {
    @Override
    public void coGroup(Iterable<Tuple4<Long, String, Map<String, Long>,Long>> rankAM, Iterable<Tuple4<Long, String, Map<String, Long>,Long>> rankPM, Collector<ResultSlotRankPojo> out) throws Exception {

        ArrayList<Tuple2<String,Map<String, Long>>> temp = new ArrayList<>();

        Tuple4<Long, String, Map<String, Long>,Long> elemAM;
        Tuple4<Long, String, Map<String, Long>,Long> elemPM;
        long actualEventTimeAM = 0L;
        long actualEventTimePM = 0L;

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
            actualEventTimeAM = elemAM._4();
        }

        if (elemPM != null){
            timestamp = elemPM._1();
            temp.add(new Tuple2<>(elemPM._2(), elemPM._3()));
            actualEventTimePM = elemPM._4();
        }

        long end = TimeConverter.currentClock() - Math.max(actualEventTimeAM,actualEventTimePM);
        ResultSlotRankPojo pojo = new ResultSlotRankPojo(timestamp, temp, end);
        out.collect(pojo);
    }
}
