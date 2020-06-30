package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;


@Data @JsonIgnoreProperties
public class RankBox implements Serializable {

    private ResultPojo pos1;
    private ResultPojo pos2;
    private ResultPojo pos3;


    public RankBox(String string) {
        this.pos1 = new ResultPojo("","",0);
        this.pos2 = new ResultPojo("","",0);
        this.pos3 = new ResultPojo("","",0);
    }

    public RankBox() { }


//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        if(pos1.getCount()!=0)
//            sb.append(pos1.toString());
//        if(pos2.getCount()!=0)
//            sb.append(", ").append(pos2.toString());
//        if(pos3.getCount()!=0)
//            sb.append(", ").append(pos3.toString());
//        return  sb.toString();
//}


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(pos1.getTimeslot()).append(" (");
        if(pos1.getCount()!=0)
            sb.append(pos1.toString());
        if(pos2.getCount()!=0)
            sb.append(", ").append(pos2.toString());
        if(pos3.getCount()!=0)
            sb.append(", ").append(pos3.toString());
        sb.append(")");
        return  sb.toString();

    }

}
