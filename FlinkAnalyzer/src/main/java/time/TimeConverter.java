package time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import query.FirstQuery;

import java.time.Instant;
import java.time.format.DateTimeParseException;


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
            //log.info("epoch :" + String.valueOf(epochToMilli));

            return  epochToMilli;
        } catch (DateTimeParseException e) {
            return 0L;
        }
    }



}
