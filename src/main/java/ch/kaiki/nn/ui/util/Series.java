package ch.kaiki.nn.ui.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.List;

public class Series {
    private String name;
    private Color color;
    private double xMin = Double.MAX_VALUE;
    private double xMax = Double.MIN_VALUE;
    private double yMin = Double.MAX_VALUE;
    private double yMax = Double.MIN_VALUE;
    private ObservableList<Point> data = FXCollections.observableArrayList();

    public Series(String name, List<Point> data, Color color) {
        this.name = name;
        this.color = color;
        for (Point point : data) {
            double x = point.getX();
            double y = point.getY();
            if (x < xMin) {
                xMin = x;
            }
            if (x > xMax) {
                xMax = x;
            }
            if (y < yMin) {
                yMin = y;
            }
            if (y > yMax) {
                yMax = y;
            }
            this.data.add(point);
        }
    }

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

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public ObservableList<Point> getData() {
        return data;
    }

    public void transform(double width, double height, double xRange, double yRange) {
        for (Point point : data) {
            point.transform(width, height, xRange, yRange);
        }
    }
}
