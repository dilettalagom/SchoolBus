package model;

import lombok.Data;



@Data
public class ReasonDelayPojo {

    private String reason;
    private String timestamp;
    private String timeSlot;


    public ReasonDelayPojo(String reason, String timestamp, String timeSlot) {
        this.reason = reason;
        this.timestamp = timestamp;
        this.timeSlot = timeSlot;
    }

    public ReasonDelayPojo(String reason, String timestamp) {
        this.reason = reason;
        this.timestamp = timestamp;
    }


}
