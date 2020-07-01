package model;

import lombok.Data;
import java.io.Serializable;

@Data
public class SnappyTuple4<T1, T2, T3, T4> implements Serializable {

    public T1 k1;
    public T2 k2;
    public T3 k3;
    public T4 k4;


    public SnappyTuple4() {}

    public SnappyTuple4(T1 v1, T2 v2, T3 v3, T4 v4) {
        k1 = v1;
        k2 = v2;
        k3 = v3;
        k4 = v4;
    }

    @Override
    public String toString() {
        return k1 +
                ", " + k2 +
                ", " + k3 +
                ", " + k4;
    }

}
