package model;

import lombok.Data;
import time.TimeConverter;


@Data
public class BoroDelayPojo {

    private Integer delay;
    private String boro;
    private String timestamp;
    private Long currentEventTime;


    public BoroDelayPojo(String timestamp, String boro, String delay) {
        this.boro = boro;
        this.delay = parseInteger(delay);
        this.timestamp = timestamp;
        this.currentEventTime = setStartTime();
    }

    private Long setStartTime(){
        return TimeConverter.currentClock();
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

}
