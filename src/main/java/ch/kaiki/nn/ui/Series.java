package ch.kaiki.nn.ui;

import ch.kaiki.nn.ui.util.Point;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    public Series(List<String> name, List<Color> color) {
        if (name != null) {
            this.name.addAll(name);
        }
        this.color.addAll(color);
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

    public List<String> getName() {
        return name;
    }

    public List<Color> getColor() {
        return color;
    }

}
