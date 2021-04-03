package data;

import java.util.ArrayList;
import java.util.List;

public class ForwardPropData {

    private List<ForwardPropEntity> data = new ArrayList<>();

    public void add(double[] in, List<Double> out) {
        data.add(new ForwardPropEntity(in[0], in[1], out));
    }

    public List<ForwardPropEntity> get() {
        return data;
    }

}
