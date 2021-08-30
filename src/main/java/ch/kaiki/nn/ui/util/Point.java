package ch.kaiki.nn.ui.util;

import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;

public class Point {
    double x;
    double y;
    double z;
    double transformedX;
    double transformedY;
    double transformedZ;
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(double x, double y, double z) {
        this(x, y);
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double transformedX() {
        return transformedX;
    }

    public double transformedY() {
        return transformedY;
    }

    public double transformedZ() {
        return transformedZ;
    }

    void transform(double width, double height, double xRange, double yRange) {
        transformedX = (x / xRange * width);
        transformedY = height - (y / yRange * height);
    }

    void transform(double width, double height, double depth, double xRange, double yRange, double zRange) {
        transform(width, height,xRange, yRange);
        transformedZ = x / zRange * depth;
    }
}
