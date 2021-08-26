package ch.kaiki.nn.ui.deprecated;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

import static ch.kaiki.nn.ui.color.NNColorSupport.blend;

public class Polygon implements Comparable<Polygon> {
    private GraphicsContext context;
    private double[] x;
    private double[] y;
    private double z;
    private Color color;
    public Polygon(GraphicsContext context, double[] x, double[] y, double z, Color color) {
        this.context = context;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }
    public void draw() {
        context.setFill(color);
        context.fillPolygon(x, y, 4);
    }

    void draw(Color a, Color b, double min, double max) {

        double factor = 1-(1/Math.abs(max-min) * (z - min));
        Color blend;
        if (factor > 0.7) {
            blend = blend(a, color, (0.3-(1-factor))*2);
            context.setFill(blend);
        } else if (factor < 0.3) {
            blend = blend(b, color, (0.3-factor)*0.5);
            context.setFill(blend);
        } else {
            context.setFill(color);
        }
        //Color blend = factor > 0.5 ? blend(a, TRANSPARENT,  factor) : blend(TRANSPARENT, b, 0.5+factor);
        //context.setFill(blend(color, blend, 0.1));
        context.fillPolygon(x, y, 4);
    }

    @Override
    public int compareTo(@NotNull Polygon o) {
        return Double.compare(this.z, o.z);
    }
}
