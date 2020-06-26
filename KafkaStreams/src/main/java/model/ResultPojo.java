package model;

import lombok.Data;
import java.io.Serializable;

@Data
public class ResultPojo implements Serializable {

    String timeslot;
    String reason;
    Integer count;

    public ResultPojo() {
    }

    public ResultPojo(String timeslot, String reason, Integer count) {
        this.timeslot = timeslot;
        this.reason = reason;
        this.count = count;
    }

    @Override
    public String toString() {
        return timeslot + ", " + reason + ", " + count;
    }

}
