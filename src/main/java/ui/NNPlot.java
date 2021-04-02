package ui;

import data.BackPropData;
import data.ForwardPropData;
import data.IterationObject;
import data.Tuple;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import neuralnet.NeuralNetwork;

import java.text.DecimalFormat;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;

import static javafx.scene.paint.Color.*;

public class NNPlot {

    private static DecimalFormat df = new DecimalFormat("#.###");
    private double width;
    private double height;
    private double wOffsetLeft = 120;
    private double wOffsetRight = 10;
    private double hOffsetTop = 10;
    private double hOffsetBottom = 50;
    private double padding = 0.1;
    private double plotLineWidth = 1.5;
    private double gridLineWidth = 0.5;
    private double dataLineWidth = 2;
    private double dotRadius = 4;
    private double plotWidth;
    private double plotHeight;
    private double xmin;
    private double xmax;
    private double ymin;
    private double ymax;
    private BackPropData data;
    private SortedMap<Integer, IterationObject> map;
    private Function<IterationObject, Double> function;

    private GraphicsContext context;
    private Color backgroundColor = WHITE;
    private Color plotBackgroundColor = blend(LIGHTSKYBLUE, WHITE, 0.2);
    private Color plotLineColor = LIGHTSKYBLUE.darker();
    private Color plotGridColor = LIGHTSKYBLUE;
    private Color plotTextColor = LIGHTSKYBLUE;

    private Color plotDataColor = ROYALBLUE;

    public NNPlot(GraphicsContext context) {
        this.context = context;
        width = context.getCanvas().getWidth();
        height = context.getCanvas().getHeight();
        plotWidth = width - wOffsetLeft - wOffsetRight;
        plotHeight = height - hOffsetTop - hOffsetBottom;
    }

    public void setPadding(double top, double right, double bottom, double left, double dataPadding) {
        padding = dataPadding;
        wOffsetLeft = left;
        wOffsetRight = right;
        hOffsetTop = top;
        hOffsetBottom = bottom;
        plotWidth = width - wOffsetLeft - wOffsetRight;
        plotHeight = height - hOffsetTop - hOffsetBottom;
    }

    private double getRandomInput() {
        return ((((Math.random() * 2 - 1) * (1 + padding)) + 1 )/ 2);
    }

    public void plot(NeuralNetwork neuralNetwork, double density, boolean drawAxes, boolean drawTicks, boolean drawAxisLabels, double opacity) {
        plotBackgroundColor = TRANSPARENT;
        ForwardPropData data = new ForwardPropData();
        for (int i = 0; i < 75000 * density; i++) {
            double[] input = {getRandomInput(), getRandomInput()};
            List<Double> output = neuralNetwork.predict(input);
            data.add(input, output);
        }
        padding = 0;
        List<Tuple> tuples = data.get();
        xmin = tuples.stream().map(Tuple::getX).min(Double::compare).get();
        xmax = tuples.stream().map(Tuple::getX).max(Double::compare).get();
        ymin = tuples.stream().map(Tuple::getY).min(Double::compare).get();
        ymax = tuples.stream().map(Tuple::getY).max(Double::compare).get();

        if (xmin == xmax) {
            xmin = xmin - 0.5;
            xmax = xmax + 0.5;
        }
        if (ymin == ymax) {
            ymin = ymin - 0.5;
            ymax = ymax + 0.5;
        }

        drawBackground();
        plotBinaryClassifierDecisionBoundaries(tuples);
        drawOverlay(opacity);
        drawAxes(drawAxes, drawTicks, drawAxisLabels);

    }

    public void plot(BackPropData data, Function<IterationObject, Double> function, boolean asDot, int modulo) {
        this.data = data;
        this.map = data.getMap();
        this.function = function;
        xmin = map.keySet().stream().min(Integer::compare).get();
        xmax = map.keySet().stream().max(Integer::compare).get();
        ymin = map.values().stream().map(function::apply).min(Double::compare).get();
        ymax = map.values().stream().map(function::apply).max(Double::compare).get();
        if (xmin == xmax) {
            xmin = xmin - 0.5;
            xmax = xmax + 0.5;
        }
        if (ymin == ymax) {
            ymin = ymin - 0.5;
            ymax = ymax + 0.5;
        }

        drawBackground();
        drawGrid(true);
        plotData(asDot, modulo);

        drawAxes(true, true, true);

    }

    private void drawBackground() {
        context.setFill(backgroundColor);
        context.fillRect(0,0,width, height);
        context.setFill(plotBackgroundColor);
        context.fillRect(wOffsetLeft,hOffsetTop,width-wOffsetLeft-wOffsetRight,height-hOffsetTop-hOffsetBottom);
    }

    private void drawOverlay(double opacity) {
        context.setFill(blend(TRANSPARENT, backgroundColor, opacity));
        context.fillRect(wOffsetLeft,hOffsetTop,width-wOffsetRight, height-hOffsetBottom);
    }

    private void clearAxeSpace() {
        context.setFill(backgroundColor);
        context.clearRect(0,0, width, hOffsetTop);
        context.clearRect(0,0,wOffsetLeft, height);
        context.clearRect(width-wOffsetRight, 0, width-wOffsetRight, height);
        context.clearRect(0,height-hOffsetBottom, width, height-hOffsetBottom);
        context.fillRect(0,0, width, hOffsetTop);
        context.fillRect(0,0,wOffsetLeft, height);
        context.fillRect(width-wOffsetRight, 0, width-wOffsetRight, height);
        context.fillRect(0,height-hOffsetBottom, width, height-hOffsetBottom);
    }

    private void drawGrid(boolean drawGrid) {
        if (!drawGrid) {
            return;
        }
        double xRange = Math.abs(xmin - xmax) * (1 + padding);
        double xStep = plotWidth <= 350 ? calculateIntervalSmall((xRange)) : calculateIntervalLarge(xRange);
        double xRangeLabel = xmin - (xmin % xStep) - xStep;
        double x = x(xRangeLabel);
        int xTickcount = (int) (xRange / xStep) + 2;
        for (int i = 0; i < xTickcount; i++) {
            if (i > 0) {
                x += x(xStep + xmin) - wOffsetLeft - (plotWidth * padding * 0.5);
            }
            if (x < wOffsetLeft + plotWidth - (plotWidth * padding * 0.1) && x > wOffsetLeft + (plotWidth * padding * 0.1)) {
                drawLine(x, height - hOffsetBottom, x, hOffsetTop, plotGridColor, gridLineWidth, drawGrid);
            }
        }

        double yRange = Math.abs(ymin - ymax) * (1 + padding);
        double yStep = plotHeight <= 350 ? calculateIntervalSmall((yRange)) : calculateIntervalLarge(yRange);
        double yRangeLabel = ymin - (ymin % yStep) - yStep;
        double y = y(yRangeLabel);
        int yTickcount = (int) (yRange / yStep) + 2;

        for (int i = 0; i < yTickcount; i++) {
            if (i > 0) {
                y -= Math.abs(Math.abs(y(yStep + ymin)) - (plotHeight + hOffsetTop - (plotHeight * padding * 0.5)));
            }
            if (y > hOffsetTop + (plotHeight * padding * 0.1) && y < plotHeight + hOffsetTop - (plotHeight * padding * 0.1)) {
                drawLine(wOffsetLeft, y, width - wOffsetRight, y, plotGridColor, gridLineWidth, drawGrid);
            }
        }
    }

    private void drawAxes(boolean drawAxes, boolean drawTicks, boolean drawAxisLabels) {
        clearAxeSpace();
        if (drawTicks || drawAxisLabels) {
            double xRange = Math.abs(xmin - xmax) * (1 + padding);
            double xStep = plotWidth <= 350 ? calculateIntervalSmall((xRange)) : calculateIntervalLarge(xRange);
            double xRangeLabel = xmin - (xmin % xStep) - xStep;
            double x = x(xRangeLabel);
            int xTickcount = (int) (xRange / xStep) + 2;
            for (int i = 0; i < xTickcount; i++) {
                if (i > 0) {
                    x += x(xStep + xmin) - wOffsetLeft - (plotWidth * padding * 0.5);
                }
                if (x < wOffsetLeft + plotWidth - (plotWidth * padding * 0.1) && x > wOffsetLeft + (plotWidth * padding * 0.1)) {
                    drawText(xRangeLabel + i * xStep, x, height - hOffsetBottom + 18, TextAlignment.LEFT, drawAxisLabels);
                    drawLine(x, height - hOffsetBottom, x, height - hOffsetBottom + 6, plotLineColor, gridLineWidth, drawTicks);
                }
            }

            double yRange = Math.abs(ymin - ymax) * (1 + padding);
            double yStep = plotHeight <= 350 ? calculateIntervalSmall((yRange)) : calculateIntervalLarge(yRange);
            double yRangeLabel = ymin - (ymin % yStep) - yStep;
            double y = y(yRangeLabel);
            int yTickcount = (int) (yRange / yStep) + 2;

            for (int i = 0; i < yTickcount; i++) {
                if (i > 0) {
                    y -= Math.abs(Math.abs(y(yStep + ymin)) - (plotHeight + hOffsetTop - (plotHeight * padding * 0.5)));
                }
                if (y > hOffsetTop + (plotHeight * padding * 0.1) && y < plotHeight + hOffsetTop - (plotHeight * padding * 0.1)) {
                    drawText(yRangeLabel + i * yStep, wOffsetLeft - 12, y, TextAlignment.RIGHT, drawAxisLabels);
                    drawLine(wOffsetLeft, y, wOffsetLeft - 6, y, plotLineColor, gridLineWidth, drawTicks);
                }
            }
        }

        if (drawAxes) {
            drawAxis(wOffsetLeft, height - hOffsetBottom, width - wOffsetRight, height - hOffsetBottom);
            drawAxis(wOffsetLeft, height - hOffsetBottom, wOffsetLeft, hOffsetTop);
            drawAxis(wOffsetLeft, hOffsetTop, width-wOffsetRight, hOffsetTop);
            drawAxis(width-wOffsetRight, hOffsetTop, width-wOffsetRight, height-hOffsetBottom);
        }
    }

    private String formatTickLabel(double value) {
        double formatted = Double.parseDouble(df.format(value));
        if (formatted % 1 == 0) {
            return Integer.toString((int) formatted);
        }
        return Double.toString(formatted);
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

    private double x(double x) {
        return ((x-xmin) / Math.abs(xmin-xmax) * (plotWidth * ((1-padding)))) + (wOffsetLeft + (plotWidth *padding*0.5));
    }

    private double y(double y) {
        return (plotHeight +hOffsetTop - (plotHeight *padding*0.5)) - ((y-ymin) / Math.abs(ymin - ymax) * (plotHeight * (1-padding)));
    }

    private void plotBinaryClassifierDecisionBoundaries(List<Tuple> tuples) {
        double dotRadius = ((plotWidth+plotHeight)/2) /64;

        for (Tuple tuple : tuples) {
            double x = x(tuple.getX());
            double y =  y(tuple.getY());
            double output = tuple.getOutput();
            Color color;

            if (output <= 0.5) {
                color = blend(RED, YELLOW, 1-(output*2));
            } else {
                color = blend(GREEN, YELLOW, (output-0.5)*2);
            }

            context.setFill(color);
            context.fillOval(x- dotRadius /2.0, y- dotRadius /2.0, dotRadius, dotRadius);

        }
    }

    private void plotData(boolean asDot, int modulo) {
        context.setStroke(plotDataColor);
        context.setFill(plotDataColor);
        context.setLineWidth(dataLineWidth);

        double oldX = 0;
        double oldY = 0;
        boolean init = true;
        int counter = 0;
        for (Integer key : map.keySet()) {
            counter++;
            if (counter%modulo != 0) {
                continue;
            }
            double x = x(key);
            double y =  y(function.apply(map.get(key)));
            if (asDot) {
                context.fillOval(x- dotRadius /2.0, y- dotRadius /2.0, dotRadius, dotRadius);
            } else {
                if (init) {
                    init = false;
                    oldX = x;
                    oldY = y;
                    continue;
                }
                context.strokeLine(oldX, oldY, x, y);
                oldX = x;
                oldY = y;
            }
        }
    }


    private void drawAxis(double x1, double y1, double x2, double y2) {
        context.setStroke(plotLineColor);
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

    static Color blend(Color c1, Color c2, double ratio) {
        if (ratio > 1.0)  {
            ratio = 1;
        } else if (ratio < 0.0) {
            ratio = 0;
        }
        double iRatio = 1.0 - ratio;

        Color rgb1 = c1 == TRANSPARENT ? c2 : c1;
        Color rgb2 = c2 == TRANSPARENT ? c1 : c2;

        int r1 = isolateComponent(rgb1.getRed());
        int g1 = isolateComponent(rgb1.getGreen());
        int b1 = isolateComponent(rgb1.getBlue());
        int a1 = isolateComponent(c1.getOpacity());

        int r2 = isolateComponent(rgb2.getRed());
        int g2 = isolateComponent(rgb2.getGreen());
        int b2 = isolateComponent(rgb2.getBlue());
        int a2 = isolateComponent(c2.getOpacity());

        double r = convertComponent((r1 * ratio) + (r2 * iRatio));
        double g = convertComponent((g1 * ratio) + (g2 * iRatio));
        double b = convertComponent((b1 * ratio) + (b2 * iRatio));
        double a = convertComponent((a1 * ratio) + (a2 * iRatio));

        return new Color(r, g, b, a);
    }

    private static double convertComponent(double value) {
        return Double.parseDouble(df.format(value / 255));
    }

    private static int isolateComponent(double component) {
        return Integer.parseInt(Integer.toHexString(((int) (component * 255))), 16);
    }
}
