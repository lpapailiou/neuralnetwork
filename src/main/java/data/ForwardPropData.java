package data;

import java.util.ArrayList;
import java.util.List;

public class ForwardPropData {

    private List<Tuple> data = new ArrayList<>();

    public void add(double[] in, List<Double> out) {
        data.add(new Tuple(in[0], in[1], out));
    }

    public List<Tuple> get() {
        return data;
    }


}
