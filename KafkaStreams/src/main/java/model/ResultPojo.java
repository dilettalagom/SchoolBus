package model;

import lombok.Data;
import java.io.Serializable;

@Data
public class ResultPojo implements Serializable {

    String timeslot;
    String reason;
    Integer count;
    Long currentEventTime;

    public ResultPojo() {
    }

    public ResultPojo(String timeslot, String reason, Integer count) {
        this.timeslot = timeslot;
        this.reason = reason;
        this.count = count;
    }


    public ResultPojo(String timeslot, String reason, Integer count, Long current) {
        this.timeslot = timeslot;
        this.reason = reason;
        this.count = count;
        this.currentEventTime = current;
    }

    @Override
    public String toString() {
        return reason + ":" + count + " ";
    }

}
