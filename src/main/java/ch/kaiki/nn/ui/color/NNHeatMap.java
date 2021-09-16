package ch.kaiki.nn.ui.color;

import javafx.scene.paint.Color;

public class NNHeatMap extends NNDataColor {

    private double min;
    private double max;
    private double minOpacity = 1;
    private double maxOpacity = 1;

    public NNHeatMap(Color... colors) {
        super(colors);
    }

    public NNHeatMap(double minZ, double maxZ, Color... colors) {
        this(colors);
        if (maxZ <= minZ) {
            throw new IllegalArgumentException("Max must be larger than min!");
        }
        if (minZ > maxZ) {
            throw new IllegalArgumentException("Min must be lower or equal than max!");
        }
        this.min = minZ;
        this.max = maxZ;
    }

    public void setOpacity(double minOpacity, double maxOpacity) {
        if (minOpacity < 0 || minOpacity > 1) {
            throw new IllegalArgumentException("MinOpacity must be between 0.0 and 1.0!");
        }
        if (maxOpacity < 0 || maxOpacity > 1) {
            throw new IllegalArgumentException("MaxOpacity must be between 0.0 and 1.0!");
        }
        this.minOpacity = minOpacity;
        this.maxOpacity = maxOpacity;
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

    public double getMinOpacity() {
        return minOpacity;
    }

    public double getMaxOpacity() {
        return maxOpacity;
    }
}
