package model;

import lombok.Data;
import scala.Tuple2;
import time.TimeConverter;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ResultSlotRankPojo {

    private String timestamp;
    private ArrayList<Tuple2<String, Map<String, Long>>> result;
    private Long latency;

    public ResultSlotRankPojo(Long timestamp, ArrayList<Tuple2<String, Map<String, Long>>> result,Long latency) {
        this.timestamp = TimeConverter.getInstance().convertFromEpochToDate(timestamp);
        this.result = result;
        this.latency = latency;
    }

    @Override
    public String toString() {
        return timestamp + ", " +
                result.stream().map(Object::toString)
                .collect(Collectors.joining(", "))
                + ", " + latency;

    }

}
