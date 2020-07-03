package model;

import lombok.Data;
import java.io.Serializable;


@Data
public class ReasonDelayPojo implements Serializable {

    private String reason;
    private String timestamp;
    private String timeslot;
    private long currentEventTime;


    public ReasonDelayPojo(){}

    public ReasonDelayPojo(String reason, String timestamp) {
        this.reason = reason;
        this.timestamp = timestamp;
        this.currentEventTime = setStartTime();;
    }

    private Long setStartTime() {
        return System.nanoTime();
    }

    public ReasonDelayPojo(String reason, String timestamp, String timeslot) {
        this.reason = reason;
        this.timestamp = timestamp;
        this.timeslot = timeslot;
    }


    @Override
    public String toString() {
        return
                timestamp +
                        ", " + timeslot +
                        ", " + reason + " ";
    }


}
