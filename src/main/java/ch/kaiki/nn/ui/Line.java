package ch.kaiki.nn.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Line implements SortableSeriesData {
    private GraphicsContext context;
    private final double lineWidth = 2;
    private double x0;
    private double y0;
    private double z0;
    private double x1;
    private double y1;
    private double z1;
    private Color color;
    public Line(GraphicsContext context, double x0, double y0, double z0, double x1, double y1, double z1, Color color) {
        this.context = context;
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.color = color;
    }

    @Override
    public void render() {
        context.setLineWidth(lineWidth);
        context.setStroke(color);
        context.strokeLine(x0, y0, x1, y1);
    }

    @Override
    public double getZ() {
        return (z0 + z1)/2;
    }
}
