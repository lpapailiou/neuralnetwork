package ch.kaiki.nn.ui.deprecated;

import ch.kaiki.nn.ui.color.NNColorSupport;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.text.DecimalFormat;

import static javafx.scene.paint.Color.*;
import static javafx.scene.paint.Color.GRAY;

public class BasePlot2 {
    private static DecimalFormat df = new DecimalFormat("#.###");
    private final double borderLineWidth = 2;
    private final double titleOffset = 30;
    protected final GraphicsContext context;
    protected final double width;
    protected final double height;
    protected double xMin;
    protected double xMax;
    protected double yMin;
    protected double yMax;
    protected double padding = 1;

    protected Color backgroundColor = BLACK;
    protected Color axisColor = DARKGRAY;
    protected Color gridColor = NNColorSupport.blend(LIGHTGRAY, TRANSPARENT, 0.4);
    protected Color gridLineColor = GRAY;

    protected String title;

    public BasePlot2(GraphicsContext context) {
        this.context = context;
        width = context.getCanvas().getWidth();
        height = context.getCanvas().getHeight();
        clear();
    }

    protected void clear() {

        context.clearRect(0, 0, width, height);
        context.setFill(backgroundColor);
        context.fillRect(0, 0, width, height);
    }

    protected void drawBorder() {
        context.setStroke(axisColor);
        context.setLineWidth(borderLineWidth);
        context.strokeLine(0,0, width,0);
        context.strokeLine(0, height, width, height);
        context.strokeLine(0,0, 0, height);
        context.strokeLine(width, 0, width, height);
    }

    public void setTitle(String text) {
        this.title = text;
        context.setFill(axisColor);
        context.setTextAlign(TextAlignment.CENTER);
        Font font = context.getFont();
        context.setFont(new Font(null, 18));
        context.fillText(text, width / 2, titleOffset);
        context.setFont(font);
    }

    protected double getInterval(double range, double threshold) {
        if (range <= threshold) {
            return calculateIntervalSmall(range);
        }
        return calculateIntervalLarge(range);
    }

    private double calculateIntervalSmall(double range) {
        double x = Math.pow(10.0, Math.floor(Math.log10(range)));
        if (range / x >= 5) {
            return x;
        } else if (range / (x / 2.0) >= 5) {
            return x / 2.0;
        }
        return x / 5.0;
    }

    private double calculateIntervalLarge(double range) {
        double x = Math.pow(10.0, Math.floor(Math.log10(range)));
        if (range / (x / 2.0) >= 10) {
            return x / 2.0;
        } else if (range / (x / 5.0) >= 10) {
            return x / 5.0;
        }
        return x / 10.0;
    }

    protected String formatTickLabel(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)){
            return "" + value;
        }

        double formatted = Double.parseDouble(df.format(value));
        if (formatted % 1 == 0) {
            return Integer.toString((int) formatted);
        }
        return Double.toString(formatted);
    }

    public void setPadding(double dataPadding) {
        if (padding <= 0) {
            throw new IllegalArgumentException("Paddung must be greater than 0!");
        }
        padding = dataPadding;
    }

}
