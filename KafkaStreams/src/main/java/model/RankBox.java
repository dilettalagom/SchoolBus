package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;


@Data @JsonIgnoreProperties
public class RankBox implements Serializable {

    private ResultPojo pos1;
    private ResultPojo pos2;
    private ResultPojo pos3;


    public RankBox(ResultPojo pos1, ResultPojo pos2, ResultPojo pos3) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.pos3 = pos3;
    }

    public RankBox() { }


    @Override
    public String toString() {
        return  "pos1=" + pos1.toString() +
                ", pos2=" + pos2.toString() +
                ", pos3=" + pos3.toString() + " ";
    }
}
