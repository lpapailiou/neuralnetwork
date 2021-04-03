package ui;

import data.BackPropData;
import data.ForwardPropData;
import data.IterationObject;
import data.Tuple;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import neuralnet.NeuralNetwork;
import ui.color.NNBinaryClassifierColor;
import ui.color.NNColorSupport;
import ui.color.NNMultiColor;
import ui.color.NNPlotColor;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;

import static javafx.scene.paint.Color.*;
import static ui.color.NNColorSupport.blend;

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
    private double xMin;
    private double xMax;
    private double yMin;
    private double yMax;
    private double[][] data;
    private SortedMap<Integer, IterationObject> map;
    private Function<IterationObject, Double> function;

    private GraphicsContext context;
    private Color backgroundColor = TRANSPARENT;
    private Color plotBackgroundColor = blend(LIGHTSKYBLUE, backgroundColor, 0.2);
    private Color plotAxisColor = LIGHTSKYBLUE.darker();
    private Color plotGridColor = LIGHTSKYBLUE;
    private Color plotTextColor = LIGHTSKYBLUE;
    private Color plotDataColor = ROYALBLUE;
    private NNColorSupport customColors;

    private String title;

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

    private double getRandomInput(double min, double max) {
        return (((((Math.random() * 2 - 1) * (1 + padding*2)) + 1) / 2) * Math.abs(min - max)) + min;
    }

    public void plot(NeuralNetwork neuralNetwork, double[][] in, double resolution, double opacity, boolean drawAxes, boolean drawTicks, boolean drawAxisLabels, NNColorSupport customColors) {
        int[] configuration = neuralNetwork.getConfiguration();
        if (configuration[0] != 2) {
            throw new IllegalArgumentException("Decision boundaries can only be plotted for 2-dimensional inputs!");
        }
        data = in;

        xMin = Double.MAX_VALUE;
        xMax = Double.MIN_VALUE;
        yMin = Double.MAX_VALUE;
        yMax = Double.MIN_VALUE;

        for (int i = 0; i < in.length; i++) {
            double xValue = in[i][0];
            if (xValue < xMin) {
                xMin = xValue;
            }
            if (xValue > xMax) {
                xMax = xValue;
            }
            double yValue = in[i][1];
            if (yValue < yMin) {
                yMin = yValue;
            }
            if (yValue > yMax) {
                yMax = yValue;
            }
        }

        plotBackgroundColor = TRANSPARENT;
        this.customColors = customColors;

        ForwardPropData data = new ForwardPropData();
        for (int i = 0; i < 60000 * resolution; i++) {
            double[] input = {getRandomInput(xMin, xMax), getRandomInput(yMin, yMax)};
            List<Double> output = neuralNetwork.predict(input);
            data.add(input, output);
        }


        List<Tuple> tuples = data.get();
        xMin = tuples.stream().map(Tuple::getX).min(Double::compare).get();
        xMax = tuples.stream().map(Tuple::getX).max(Double::compare).get();
        yMin = tuples.stream().map(Tuple::getY).min(Double::compare).get();
        yMax = tuples.stream().map(Tuple::getY).max(Double::compare).get();

        if (xMin == xMax) {
            xMin = xMin - 0.5;
            xMax = xMax + 0.5;
        }
        if (yMin == yMax) {
            yMin = yMin - 0.5;
            yMax = yMax + 0.5;
        }
        padding = 0;

        drawBackground();
        if (configuration[configuration.length-1] == 1) {
            plotBinaryClassifierDecisionBoundaries(tuples, resolution);
        } else {
            plotMultiClassClassifierDecisionBoundaries(tuples, resolution);
        }
        drawOverlay(opacity);
        drawAxes(drawAxes, drawTicks, drawAxisLabels);
        setTitle(title);
    }

    public void plot(BackPropData data, Function<IterationObject, Double> function, boolean asDot, double smoothing) {
        this.map = data.getMap();
        this.function = function;
        xMin = map.keySet().stream().min(Integer::compare).get();
        xMax = map.keySet().stream().max(Integer::compare).get();
        yMin = map.values().stream().map(function).min(Double::compare).get();
        yMax = map.values().stream().map(function).max(Double::compare).get();
        if (xMin == xMax) {
            xMin = xMin - 0.5;
            xMax = xMax + 0.5;
        }
        if (yMin == yMax) {
            yMin = yMin - 0.5;
            yMax = yMax + 0.5;
        }

        drawBackground();
        drawGrid(true);

        int smooth = (int) (smoothing * 100);
        smooth = Math.max(smooth, 1);
        smooth = Math.min(smooth, map.size()-1);
        plotData(asDot, smooth);

        drawAxes(true, true, true);
        setTitle(title);
    }

    public void setTitle(String text) {
        this.title = text;
        context.setFill(plotTextColor);
        context.setTextAlign(TextAlignment.CENTER);
        Font currentFont = context.getFont();
        context.setFont(new Font("", currentFont.getSize() * 1.2));
        context.fillText(text, width/2+(wOffsetLeft/2), hOffsetTop-12);
        context.setFont(currentFont);
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

    private void drawAxes(boolean drawAxes, boolean drawTicks, boolean drawAxisLabels) {
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
        return ((x- xMin) / Math.abs(xMin - xMax) * (plotWidth * ((1-padding)))) + (wOffsetLeft + (plotWidth *padding*0.5));
    }

    private double y(double y) {
        return (plotHeight +hOffsetTop - (plotHeight *padding*0.5)) - ((y- yMin) / Math.abs(yMin - yMax) * (plotHeight * (1-padding)));
    }

    private void plotBinaryClassifierDecisionBoundaries(List<Tuple> tuples, double resolution) {
        double dotRadius = (((plotWidth+plotHeight)/2) / 64) / resolution;


        for (Tuple tuple : tuples) {
            double x = x(tuple.getX());
            double y =  y(tuple.getY());
            double output = tuple.getOutput().get(0);

            NNBinaryClassifierColor colors = (NNBinaryClassifierColor) customColors;
            Color color;
            if (output <= 0.5) {
                color = blend(colors.getNegative(), colors.getMargin(), 1-(output*2));
            } else {
                color = blend(colors.getPositive(), colors.getMargin(), (output-0.5)*2);
            }

            context.setFill(color);
            context.fillOval(x- dotRadius /2.0, y- dotRadius /2.0, dotRadius, dotRadius);

        }
    }

    private void plotMultiClassClassifierDecisionBoundaries(List<Tuple> tuples, double resolution) {
        double dotRadius = (((plotWidth+plotHeight)/2) / 64) / resolution;
        List<Color> colors = ((NNMultiColor) customColors).getColors();

        for (Tuple tuple : tuples) {
            double x = x(tuple.getX());
            double y =  y(tuple.getY());
            List<Double> output = tuple.getOutput();
            Color color = new Color(plotBackgroundColor.getRed(), plotBackgroundColor.getGreen(), plotBackgroundColor.getBlue(), 0);
            for (int i = 0; i <  output.size(); i++) {
                double value = output.get(i);
                if (value <= 0.5) {
                    color = blend(color, colors.get(i), 1-(value*2));
                } else {
                    color = blend(colors.get(i), colors.get(i), (value-0.5)*2);
                }
            }
            context.setFill(color);
            context.fillOval(x- dotRadius /2.0, y- dotRadius /2.0, dotRadius, dotRadius);
        }

    }


    private void plotData(boolean asDot, int modulo) {
        System.out.println("modulo : " + modulo);
        context.setStroke(plotDataColor);
        context.setFill(plotDataColor);
        context.setLineWidth(dataLineWidth);

        double y = 0;
        double oldX = 0;
        double oldY = 0;
        boolean init = true;
        int counter = 0;
        for (Integer key : map.keySet()) {
            counter++;
            double x = key;
            y += function.apply(map.get(key));
            if ((counter-1)%modulo != 0) {
                continue;
            } else {
                y = y / modulo;
            }
            if (asDot) {
                context.fillOval(x(x)- dotRadius /2.0, y(y) - dotRadius /2.0, dotRadius, dotRadius);
            } else {
                if (!init) {
                    context.strokeLine(x(oldX), y(oldY), x(x), y(y));
                } else {
                    init = false;
                }
                oldX = x;
                oldY = y;
            }
            y = 0;
        }
    }

    public void plot2DData(double[][] definedClasses, double dotRadius) {
        if (data == null || data[0].length != 2) {
            throw new IllegalArgumentException("Data must be 2-dimensional!");
        }
        List<String> classes = new ArrayList<>();
        for (double[] o : definedClasses) {
            String outStr = Arrays.toString(o);
            if (!classes.contains(outStr)) {
                classes.add(outStr);
            }
        }
        List<Color> colors = new ArrayList<>();
        if (customColors instanceof NNBinaryClassifierColor) {
            colors.add(((NNBinaryClassifierColor) customColors).getNegative());
            colors.add(((NNBinaryClassifierColor) customColors).getPositive());
        } else {
            colors.addAll(((NNMultiColor) customColors).getColors());
        }
        for (int i = 0; i < data.length; i++) {
            double[] outClass = definedClasses[i];
            int colorIndex = classes.indexOf(Arrays.toString(outClass));
            context.setFill(colors.get(colorIndex));
            context.fillOval(x(data[i][0]) - dotRadius / 2.0, y(data[i][1]) - dotRadius / 2.0, dotRadius, dotRadius);
        }
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

    public NNPlot setFontProperties(boolean bold, boolean italic, double fontSize) {
        if (fontSize < 0) {
            throw new IllegalArgumentException("Font size must be greater than 0!");
        }
        context.setFont(Font.font(null, bold ? FontWeight.BOLD : FontWeight.NORMAL, italic ? FontPosture.ITALIC : FontPosture.REGULAR, fontSize));
        return this;
    }

    public NNPlot setColorPalette(NNPlotColor colors) {
        this.backgroundColor = colors.getBackgroundColor();
        this.plotBackgroundColor = colors.getPlotBackgroundColor();
        this.plotAxisColor = colors.getPlotAxisColor();
        this.plotGridColor = colors.getPlotGridColor();
        this.plotTextColor = colors.getPlotTextColor();
        this.plotDataColor = colors.getPlotDataColor();
        return this;
    }

}
