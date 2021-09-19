package ch.kaiki.nn.ui.series;

import ch.kaiki.nn.ui.util.ChartMode;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public abstract class Series {

    private List<String> name = new ArrayList<>();
    private List<Color> color = new ArrayList<>();

    protected double xMin = Double.MAX_VALUE;
    protected double xMax = Double.MIN_VALUE;
    protected double yMin = Double.MAX_VALUE;
    protected double yMax = Double.MIN_VALUE;
    protected double zMin = Double.MAX_VALUE;
    protected double zMax = Double.MIN_VALUE;

    protected final ChartMode mode;

    public Series(List<String> name, List<Color> color, ChartMode mode) {
        if (name != null) {
            this.name.addAll(name);
        }
        this.color.addAll(color);
        this.mode = mode;
    }

    public abstract void compute();

    public abstract void render();

    public double getMinX() {
        return xMin;
    }

    public double getMaxX() {
        return xMax;
    }

    public double getMinY() {
        return yMin;
    }

    public double getMaxY() {
        return yMax;
    }

    public double getMinZ() {
        return zMin;
    }

    public double getMaxZ() {
        return zMax;
    }

    public void addName(String name) {
        if (name == null) {
            return;
        }
        this.name.add(name);
    }

    public void setName(String name) {
        this.name.clear();
        this.addName(name);
    }

    public void addColor(Color color) {
        if (color == null) {
            this.color.add(Color.TRANSPARENT);
        }
        this.color.add(color);
    }

    public List<String> getName() {
        return name;
    }

    public List<Color> getColor() {
        return color;
    }

    public ChartMode getMode() {
        return mode;
    }

}
