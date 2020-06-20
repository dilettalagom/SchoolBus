package time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.FirstQuery;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;


public class TimeConverter {

    private static TimeConverter instance = null;
    Logger log;

    public TimeConverter(){
        log = LoggerFactory.getLogger(FirstQuery.class);
    }

    public static TimeConverter getInstance(){
        if(instance == null)
            instance = new TimeConverter();
        return instance;
    }

    public long convertToEpochMilli(String timestampString){
        try {
            long epochToMilli = Instant.parse(timestampString+'Z').toEpochMilli();
            return  epochToMilli;
        } catch (DateTimeParseException e) {
            return 0L;
        }
    }

    public String convertFromEpochToDate(Long epochMilli){
        Date date = new Date(epochMilli);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }


    public static Long currentClock(){
        return System.nanoTime();
    }


}
