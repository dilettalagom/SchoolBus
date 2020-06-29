package time;

import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;


public class MonthWindow extends TumblingEventTimeWindows {


    public MonthWindow() {
        super(Time.days(31).toMilliseconds(), 0L);
    }

    @Override
    public Collection<TimeWindow> assignWindows(Object element, long timestamp, WindowAssignerContext context) {
        LocalDateTime lastTS = LocalDateTime.ofEpochSecond(timestamp/1000L,0, ZoneOffset.UTC);
        long start = getLocalDateStartOfMonth(lastTS);
        long end = getLocalDateEndOfMonth(lastTS);

        return Collections.singletonList(new TimeWindow(start, end));
    }

    private long getLocalDateStartOfMonth(LocalDateTime ldt) {

        LocalDateTime startDT = LocalDateTime.of(ldt.getYear(),ldt.getMonthValue(),1,0,0);

        return  startDT.toEpochSecond(ZoneOffset.UTC)*1000L;

    }

    private long getLocalDateEndOfMonth(LocalDateTime ldt) {

        boolean leapYear = ldt.toLocalDate().isLeapYear();
        int lastDayOfMonth = ldt.getMonth().length(leapYear);


        LocalDateTime endtDT = LocalDateTime.of(ldt.getYear(),ldt.getMonth(),lastDayOfMonth,23,59,59);

        return  endtDT.toEpochSecond(ZoneOffset.UTC)*1000L;
    }

}
