package model;

import lombok.Data;
import java.io.Serializable;

@Data
public class SnappyTuple3<T1, T2, T3> implements Serializable {

    public T1 k1;
    public T2 k2;
    public T3 k3;

    public SnappyTuple3() {}

    public SnappyTuple3(T1 v1, T2 v2, T3 v3) {
        k1 = v1;
        k2 = v2;
        k3 = v3;
    }

    @Override
    public String toString() {
        return k1 +
                ", " + k2 +
                ", " + k3;
    }

}
