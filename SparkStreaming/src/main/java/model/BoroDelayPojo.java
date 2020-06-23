package model;

import lombok.Data;
import java.time.Instant;
import java.time.format.DateTimeParseException;


@Data
public class BoroDelayPojo {

    private Integer delay;
    private String boro;
    private Timestamp timestamp;


    public BoroDelayPojo(String timestamp, String boro, String delay) {
        this.boro = boro;
        this.delay = parseInteger(delay);
        this.timestamp = ;
    }


    private int parseInteger( String delay)
    {
        try{
            return Integer.parseInt(delay);
        }catch (NumberFormatException e){
            return -1;
        }
    }


    @Override
    public String toString() {
        return "boro=" + boro +
                ", delay=" + delay +
                ", timestamp=" + timestamp;
    }

    public long convertToEpochMilli(String timestampString){
        try {
            long epochToMilli = Instant.parse(timestampString+'Z').toEpochMilli();
            return  epochToMilli;
        } catch (DateTimeParseException e) {
            return 0L;
        }
    }

}
