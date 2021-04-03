package data;

import java.util.List;

public class ForwardPropEntity {

    private double x;
    private double y;
    private List<Double> output;

    public ForwardPropEntity(double x, double y, List<Double> output) {
        this.x = x;
        this.y = y;
        this.output = output;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public List<Double> getOutput() {
        return output;
    }
}
