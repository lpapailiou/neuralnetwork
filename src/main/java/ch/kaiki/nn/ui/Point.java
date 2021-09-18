package ch.kaiki.nn.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static ch.kaiki.nn.ui.color.NNColor.blend;

public class Point implements SortableSeriesData {
    private GraphicsContext context;
    private final double radius = 9;
    private double x;
    private double y;
    private double z;
    private Color color;
    private boolean invertBorder;
    public Point(GraphicsContext context, double x, double y, double z, Color color, boolean invertBorder) {
        this.context = context;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
        this.invertBorder = invertBorder;
    }

    @Override
    public void render() {
        if (invertBorder) {
            double r = radius;
            context.setFill(color.invert());
            context.fillOval(x - r / 2, y - r / 2, r, r);
            r = r - 1;
            context.setFill(color);
            context.fillOval(x - r / 2, y - r / 2, r, r);
        } else {
            context.setFill(color);
            context.fillOval(x - radius / 2, y - radius / 2, radius, radius);
        }
    }

    @Override
    public double getZ() {
        return z;
    }
}
