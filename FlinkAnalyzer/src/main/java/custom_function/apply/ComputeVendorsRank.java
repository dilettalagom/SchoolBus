package custom_function.apply;

import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;
import time.TimeConverter;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.PriorityQueue;



public class ComputeVendorsRank implements WindowFunction<Tuple4<Long, String, Tuple3<Long, Long, Long>,Long>, String, Long, TimeWindow> {

    private final double WT = 0.3, WM = 0.5, WO = 0.2;
    private final int QUEUE_SIZE = 5;

    @Override
    public void apply(Long key, TimeWindow timeWindow, Iterable<Tuple4<Long, String, Tuple3<Long, Long, Long>,Long>> iterable, Collector<String> out) throws Exception {

        Comparator<Tuple2<String, Double>> comp = Comparator.comparing(Tuple2::_2);
        PriorityQueue<Tuple2<String, Double>> rankQueue = new PriorityQueue<>(QUEUE_SIZE, comp.reversed());
        long maxEventTime = 0L;

        for(Tuple4<Long, String, Tuple3<Long, Long, Long>,Long> t : iterable){
            double val = computeRankScore(t._3());
            rankQueue.add(new Tuple2<>(t._2(),val));
            maxEventTime = Math.max(t._4(),maxEventTime);
        }

        StringBuilder sb = new StringBuilder();
        String date = TimeConverter.getInstance().convertFromEpochToDate(key);
        long end = TimeConverter.currentClock() - maxEventTime;

        sb.append(date + ", ");
        for( int i = 0; i < Math.min(QUEUE_SIZE,rankQueue.size()) - 1 ; i++){
            Tuple2<String, Double> t = rankQueue.poll();
            sb.append(t._1() + ", " + t._2() + " ,");
        }
        Tuple2<String, Double> t = rankQueue.poll();
        sb.append(t._1() + ", " + t._2() + ", " + end);
        out.collect(sb.toString());

    }


    private double computeRankScore(Tuple3<Long, Long, Long> values){
        double score = ( (WT * values._1()) + (WM * values._2()) + (WO * values._3()));
        return Double.parseDouble(new DecimalFormat("####.##").format(score));
    }


}







