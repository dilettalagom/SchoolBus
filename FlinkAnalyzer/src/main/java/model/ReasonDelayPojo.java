package model;

import lombok.Data;
import time.TimeConverter;


@Data
public class ReasonDelayPojo {

    private String reason;
    private String timestamp;
    private String timeSlot;
    private Long currentEventTime;


    public ReasonDelayPojo(String reason, String timestamp) {
        this.reason = reason;
        this.timestamp = timestamp;
        this.currentEventTime = setStartTime();
    }

    private Long setStartTime(){
        return TimeConverter.currentClock();
    }

}
