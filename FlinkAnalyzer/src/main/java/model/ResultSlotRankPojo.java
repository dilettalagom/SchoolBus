package model;

import lombok.Data;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ResultSlotRankPojo {

    private Long timestamp;
    private ArrayList<Tuple2<String, Map<String, Long>>> result;

    public ResultSlotRankPojo(Long timestamp, ArrayList<Tuple2<String, Map<String, Long>>> result) {
        this.timestamp = timestamp;
        this.result = result;
    }

    @Override
    public String toString() {
        return timestamp + " " +
                result.stream().map(Object::toString)
                .collect(Collectors.joining(", "));

    }

}
