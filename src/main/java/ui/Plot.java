package ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import ui.color.NNPlotColor;

import java.text.DecimalFormat;

import static javafx.scene.paint.Color.*;
import static ui.color.NNColorSupport.blend;

/**
 * This abstract class will be extended by specific plotting classes.
 * It holds the basic properties and functionality for plotting.
 */
abstract class Plot {

    private static DecimalFormat df = new DecimalFormat("#.###");
    protected GraphicsContext context;
    double padding = 0.1;
    double dataLineWidth = 2;
    double dotRadius = 4;
    double plotWidth;
    double plotHeight;
    double xMin;
    double xMax;
    double yMin;
    double yMax;
    Color plotDataColor = ROYALBLUE;
    String title;
    private double width;
    private double height;
    private double wOffsetLeft = 30;
    private double wOffsetRight = 10;
    private double hOffsetTop = 30;
    private double hOffsetBottom = 20;
    private double plotLineWidth = 1.5;
    private double gridLineWidth = 0.5;
    private Color backgroundColor = TRANSPARENT;
    Color plotBackgroundColor = blend(LIGHTSKYBLUE, backgroundColor, 0.2);
    private Color plotAxisColor = LIGHTSKYBLUE.darker();
    private Color plotGridColor = LIGHTSKYBLUE;
    private Color plotTextColor = LIGHTSKYBLUE;

    Plot(GraphicsContext context) {
        this.context = context;
        width = context.getCanvas().getWidth();
        height = context.getCanvas().getHeight();
        plotWidth = width - wOffsetLeft - wOffsetRight;
        plotHeight = height - hOffsetTop - hOffsetBottom;
    }

    /**
     * This method allows to set the plot padding. On each side, padding can be set invidually in pixels.
     * The data padding is a factor. 0.1 would mean that between every axis and the plot data, a space of
     * 5% of the plot area would be freed up.
     * Any axis labels or title will not be visible as long as no padding is set.
     *
     * @param top top padding in pixel.
     * @param right right padding in pixel.
     * @param bottom bottom padding in pixel.
     * @param left left padding in pixel.
     * @param dataPadding data padding within plot as factor (1.0 = 100%).
     * @return this plot instance (for chaining).
     */
    public Plot setPadding(double top, double right, double bottom, double left, double dataPadding) {
        if (top < 0 || right < 0 || bottom < 0 || left < 0 || dataPadding < 0) {
            throw new IllegalArgumentException("Padding must not be set below 0.!");
        }
        padding = dataPadding;
        wOffsetLeft = left;
        wOffsetRight = right;
        hOffsetTop = top;
        hOffsetBottom = bottom;
        plotWidth = width - wOffsetLeft - wOffsetRight;
        plotHeight = height - hOffsetTop - hOffsetBottom;
        return this;
    }

    /**
     * Setter for the line width of the font.
     *
     * @param bold     can either be true or false.
     * @param italic   can either be true or false.
     * @param fontSize the fontsize to set, default is 12.
     * @return this plot instance (for chaining).
     */
    public Plot setFontProperties(boolean bold, boolean italic, double fontSize) {
        if (fontSize < 0) {
            throw new IllegalArgumentException("Font size must be greater than 0!");
        }
        context.setFont(Font.font(null, bold ? FontWeight.BOLD : FontWeight.NORMAL, italic ? FontPosture.ITALIC : FontPosture.REGULAR, fontSize));
        return this;
    }

    /**
     * With this method the base color palette for this plot can be set.
     *
     * @param colors the NNPlotColor palette which will assign the base colors for this graph.
     * @return this plot instance (for chaining).
     */
    public Plot setColorPalette(NNPlotColor colors) {
        this.backgroundColor = colors.getBackgroundColor();
        this.plotBackgroundColor = colors.getPlotBackgroundColor();
        this.plotAxisColor = colors.getPlotAxisColor();
        this.plotGridColor = colors.getPlotGridColor();
        this.plotTextColor = colors.getPlotTextColor();
        this.plotDataColor = colors.getPlotDataColor();
        return this;
    }

    /**
     * Setter for a plot title. The plot title will be automatically scaled with the factor 1.2
     * compared to the default text size.
     * To be displayed, it will require a top padding.
     *
     * @param text the text to be set as plot title.
     * @return this plot instance (for chaining).
     */
    public Plot setTitle(String text) {
        this.title = text;
        context.setFill(plotTextColor);
        context.setTextAlign(TextAlignment.CENTER);
        Font currentFont = context.getFont();
        context.setFont(new Font("", currentFont.getSize() * 1.2));
        context.fillText(text, width / 2 + (wOffsetLeft / 2), hOffsetTop - 12);
        context.setFont(currentFont);
        return this;
    }

    void drawBackground() {
        context.setFill(backgroundColor);
        context.fillRect(0, 0, width, height);
        context.setFill(plotBackgroundColor);
        context.fillRect(wOffsetLeft, hOffsetTop, width - wOffsetLeft - wOffsetRight, height - hOffsetTop - hOffsetBottom);
    }

    void drawOverlay(double opacity) {
        context.setFill(blend(TRANSPARENT, backgroundColor, opacity));
        context.fillRect(wOffsetLeft, hOffsetTop, width - wOffsetRight, height - hOffsetBottom);
    }

    private void clearAxeSpace() {
        context.setFill(backgroundColor);
        context.clearRect(0, 0, width, hOffsetTop);
        context.clearRect(0, 0, wOffsetLeft, height);
        context.clearRect(width - wOffsetRight, 0, width - wOffsetRight, height);
        context.clearRect(0, height - hOffsetBottom, width, height - hOffsetBottom);
        context.fillRect(0, 0, width, hOffsetTop);
        context.fillRect(0, 0, wOffsetLeft, height);
        context.fillRect(width - wOffsetRight, 0, width - wOffsetRight, height);
        context.fillRect(0, height - hOffsetBottom, width, height - hOffsetBottom);
    }

    void drawGrid(boolean drawGrid) {
        if (!drawGrid) {
            return;
        }
        double xRange = Math.abs(xMin - xMax) * (1 + padding);
        double xStep = plotWidth <= 350 ? calculateIntervalSmall((xRange)) : calculateIntervalLarge(xRange);
        double xRangeLabel = xMin - (xMin % xStep) - xStep;
        double x = x(xRangeLabel);
        int xTickcount = (int) (xRange / xStep) + 2;
        for (int i = 0; i < xTickcount; i++) {
            if (i > 0) {
                x += x(xStep + xMin) - wOffsetLeft - (plotWidth * padding * 0.5);
            }
            if (x < wOffsetLeft + plotWidth - (plotWidth * padding * 0.1) && x > wOffsetLeft + (plotWidth * padding * 0.1)) {
                drawLine(x, height - hOffsetBottom, x, hOffsetTop, plotGridColor, gridLineWidth, drawGrid);
            }
        }

        double yRange = Math.abs(yMin - yMax) * (1 + padding);
        double yStep = plotHeight <= 350 ? calculateIntervalSmall((yRange)) : calculateIntervalLarge(yRange);
        double yRangeLabel = yMin - (yMin % yStep) - yStep;
        double y = y(yRangeLabel);
        int yTickcount = (int) (yRange / yStep) + 2;

        for (int i = 0; i < yTickcount; i++) {
            if (i > 0) {
                y -= Math.abs(Math.abs(y(yStep + yMin)) - (plotHeight + hOffsetTop - (plotHeight * padding * 0.5)));
            }
            if (y > hOffsetTop + (plotHeight * padding * 0.1) && y < plotHeight + hOffsetTop - (plotHeight * padding * 0.1)) {
                drawLine(wOffsetLeft, y, width - wOffsetRight, y, plotGridColor, gridLineWidth, drawGrid);
            }
        }
    }

    void drawAxes(boolean drawAxes, boolean drawTicks, boolean drawAxisLabels) {
        clearAxeSpace();
        if (drawTicks || drawAxisLabels) {
            double xRange = Math.abs(xMin - xMax) * (1 + padding);
            double xStep = plotWidth <= 350 ? calculateIntervalSmall((xRange)) : calculateIntervalLarge(xRange);
            double xRangeLabel = xMin - (xMin % xStep) - xStep;
            double x = x(xRangeLabel);
            int xTickcount = (int) (xRange / xStep) + 2;
            for (int i = 0; i < xTickcount; i++) {
                if (i > 0) {
                    x += x(xStep + xMin) - wOffsetLeft - (plotWidth * padding * 0.5);
                }
                if (x < wOffsetLeft + plotWidth - (plotWidth * padding * 0.1) && x > wOffsetLeft + (plotWidth * padding * 0.1)) {
                    drawText(xRangeLabel + i * xStep, x, height - hOffsetBottom + 20, TextAlignment.LEFT, drawAxisLabels);
                    drawLine(x, height - hOffsetBottom, x, height - hOffsetBottom + 6, plotAxisColor, plotLineWidth, drawTicks);
                }
            }

            double yRange = Math.abs(yMin - yMax) * (1 + padding);
            double yStep = plotHeight <= 350 ? calculateIntervalSmall((yRange)) : calculateIntervalLarge(yRange);
            double yRangeLabel = yMin - (yMin % yStep) - yStep;
            double y = y(yRangeLabel);
            int yTickcount = (int) (yRange / yStep) + 2;

            for (int i = 0; i < yTickcount; i++) {
                if (i > 0) {
                    y -= Math.abs(Math.abs(y(yStep + yMin)) - (plotHeight + hOffsetTop - (plotHeight * padding * 0.5)));
                }
                if (y > hOffsetTop + (plotHeight * padding * 0.1) && y < plotHeight + hOffsetTop - (plotHeight * padding * 0.1)) {
                    drawText(yRangeLabel + i * yStep, wOffsetLeft - 12, y, TextAlignment.RIGHT, drawAxisLabels);
                    drawLine(wOffsetLeft, y, wOffsetLeft - 6, y, plotAxisColor, plotLineWidth, drawTicks);
                }
            }
        }

        if (drawAxes) {
            drawAxis(wOffsetLeft, height - hOffsetBottom, width - wOffsetRight, height - hOffsetBottom);
            drawAxis(wOffsetLeft, height - hOffsetBottom, wOffsetLeft, hOffsetTop);
            drawAxis(wOffsetLeft, hOffsetTop, width - wOffsetRight, hOffsetTop);
            drawAxis(width - wOffsetRight, hOffsetTop, width - wOffsetRight, height - hOffsetBottom);
        }
    }

    void drawCross() {
        drawAxis(x(0), height - hOffsetBottom, x(0), hOffsetTop);
        drawAxis(wOffsetLeft, y(0), width - wOffsetRight, y(0));
    }

    private void drawLine(double x1, double y1, double x2, double y2, Color color, double lineWidth, boolean draw) {
        if (!draw) {
            return;
        }
        context.setStroke(color);
        context.setLineWidth(lineWidth);
        context.strokeLine(x1, y1, x2, y2);
    }

    private void drawText(double number, double x, double y, TextAlignment textAlignment, boolean draw) {
        if (!draw) {
            return;
        }
        context.setFill(plotTextColor);
        context.setTextAlign(textAlignment);
        context.fillText(formatTickLabel(number), x, y);
    }

    private String formatTickLabel(double value) {
        double formatted = Double.parseDouble(df.format(value));
        if (formatted % 1 == 0) {
            return Integer.toString((int) formatted);
        }
        return Double.toString(formatted);
    }

    private void drawAxis(double x1, double y1, double x2, double y2) {
        context.setStroke(plotAxisColor);
        context.setLineWidth(plotLineWidth);
        context.strokeLine(x1, y1, x2, y2);
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

    double x(double x) {
        return ((x - xMin) / Math.abs(xMin - xMax) * (plotWidth * ((1 - padding)))) + (wOffsetLeft + (plotWidth * padding * 0.5));
    }

    double y(double y) {
        return (plotHeight + hOffsetTop - (plotHeight * padding * 0.5)) - ((y - yMin) / Math.abs(yMin - yMax) * (plotHeight * (1 - padding)));
    }

}
