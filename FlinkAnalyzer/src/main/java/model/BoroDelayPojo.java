package model;

import lombok.Data;
import time.TimeConverter;

import java.time.Instant;
import java.util.Date;

@Data
public class BoroDelayPojo {

    private Integer delay;
    private String boro;
    private String timestamp;
    private Long currentEventTime;


    public BoroDelayPojo(String timestamp, String boro, String delay, Long eventTime) {
        this.boro = boro;
        this.delay = Integer.parseInt(delay);
        this.timestamp = timestamp;
        this.currentEventTime = eventTime;
    }

    @Override
    public String toString() {
        return "boro=" + boro +
                ", delay=" + delay +
                ", timestamp=" + timestamp;
    }

}
