package ch.kaiki.nn.ui.seriesobject;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Scale;

import java.text.DecimalFormat;

import static ch.kaiki.nn.ui.color.NNColor.blend;

public class Polygon implements SortableSeriesData {
    private static DecimalFormat df = new DecimalFormat("#.###");
    private GraphicsContext context;
    private double[] x;
    private double[] y;
    private double z;
    private Color color;
    private double value;
    private boolean showText;

    public Polygon(GraphicsContext context, double[] x, double[] y, double z, Color color) {
        this.context = context;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }

    public Polygon(GraphicsContext context, double[] x, double[] y, double z, Color color, double value, boolean showText) {
        this(context, x, y, z, color);
        this.value = value;
        this.showText = showText;
    }

    @Override
    public void render() {
        context.setFill(color);
        context.fillPolygon(x, y, 4);

        if (showText) {
            Font font = context.getFont();
            context.setFill(color.invert());
            context.setFont(new Font(null, 18));
            context.setTextAlign(TextAlignment.CENTER);
            context.setTextBaseline(VPos.CENTER);
            double xCenter = (x[0] + x[1] + x[2] + x[3]) / 4;
            double yCenter = (y[0] + y[1] + y[2] + y[3]) / 4;
            context.fillText(formatValue(value), xCenter, yCenter);
            context.setTextBaseline(VPos.BASELINE);
            context.setFont(font);
        }
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
        //context.scale(0.5,1);
    }

    @Override
    public double getZ() {
        return z;
    }


    protected String formatValue(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)){
            return "" + value;
        }

        double formatted = Double.parseDouble(df.format(value));
        if (formatted % 1 == 0) {
            return Integer.toString((int) formatted);
        }
        return Double.toString(formatted);
    }
}
