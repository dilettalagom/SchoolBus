package model;

import lombok.Data;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;


@Data
public class ReasonDelayPojo implements Serializable {

    private String reason;
    private String timestamp;
    private String timeslot;

    public ReasonDelayPojo(){}

    public ReasonDelayPojo(String reason, String timestamp) {
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public ReasonDelayPojo(String reason, String timestamp, String timeslot) {
        this.reason = reason;
        this.timestamp = timestamp;
        this.timeslot = timeslot;
    }

    public String convertFromEpochToDate(Long epochMilli){
        Date date = new Date(epochMilli);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }

    public long convertToEpochMilli(String timestampString){
        try {
            long epochToMilli = Instant.parse(timestampString+'Z').toEpochMilli();
            return  epochToMilli;
        } catch (DateTimeParseException e) {
            return 0L;
        }
    }

    @Override
    public String toString() {
        return
                "timestamp=" + timestamp +
                ", timeslot=" + timeslot +
                ", reason=" + reason ;}


}
