package model;

import lombok.Data;
import java.io.Serializable;

@Data
public class SnappyTuple2<T1, T2> implements Serializable {

    public T1 k1;
    public T2 k2;



    public SnappyTuple2() {}

    public SnappyTuple2(T1 v1, T2 v2) {
        k1 = v1;
        k2 = v2;

    }

    @Override
    public String toString() {
        return k1 +
                "; " + k2;

    }

}
