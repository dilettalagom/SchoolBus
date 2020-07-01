package model;

import lombok.Data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;


@Data
public class BoroDelayPojo implements Serializable {

    private String timestamp;
    private String boro;
    private Integer delay;



    public BoroDelayPojo(String timestamp, String boro, String delay)  {
        this.timestamp = timestamp;
        this.boro = boro;
        this.delay = parseInteger(delay);
    }



    private int parseInteger( String delay){
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
            return Instant.parse(timestampString+'Z').toEpochMilli();
        } catch (DateTimeParseException e) {
            return 0L;
        }
    }

    private String convertFromEpochToDate(Long epochMilli){
        Date date = new Date(epochMilli);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }



}
