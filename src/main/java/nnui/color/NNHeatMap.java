package nnui.color;


import javafx.scene.paint.Color;

public class NNHeatMap extends NNDataColor {

    private double min;
    private double max;

    public NNHeatMap(Color... colors) {
        super(colors);
    }

    public NNHeatMap(double scaleMin, double scaleMax, Color... colors) {
        this(colors);
        if (scaleMax <= scaleMin) {
            throw new IllegalArgumentException("Max must be larger than min!");
        }
        this.min = scaleMin;
        this.max = scaleMax;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean isScaled() {
        return min != max;
    }
}
