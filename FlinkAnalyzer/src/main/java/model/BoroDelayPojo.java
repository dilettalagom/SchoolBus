package model;

import lombok.Data;
import java.util.Date;

@Data
public class BoroDelayPojo {

    private String boro;
    private Integer delay;
    private String timestamp;

    public BoroDelayPojo(String timestamp, String boro, String delay) {
        this.boro = boro;
        this.delay = Integer.parseInt(delay);
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "boro=" + boro +
                ", delay=" + delay +
                ", timestamp=" + timestamp;
    }
}
