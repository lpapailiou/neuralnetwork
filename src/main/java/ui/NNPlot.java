package ui;

import data.BackPropData;
import data.ForwardPropData;
import data.IterationObject;
import data.Tuple;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.text.DecimalFormat;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javafx.scene.paint.Color.*;

public class NNPlot {


    private double width;
    private double height;
    private double wOffsetLeft = 120;
    private double wOffsetRight = 10;
    private double hOffsetTop = 10;
    private double hOffsetBottom = 50;
    private double padding = 0.1;
    private double graphLineWidth = 1.5;
    private double gridLineWidth = 0.5;
    private double dataLineWidth = 2;
    private double dotRadius = 4;
    private double graphWidth;
    private double graphHeight;
    private double xmin;
    private double xmax;
    private double ymin;
    private double ymax;
    private BackPropData data;
    private SortedMap<Integer, IterationObject> map;
    private Function<IterationObject, Double> function;

    private GraphicsContext context;
    private Color backgroundColor = WHITE;
    private Color plotBackgroundColor = NNColorPalette.blend(LIGHTSKYBLUE, WHITE, 0.2);
    private Color plotLineColor = LIGHTSKYBLUE.darker();
    private Color plotGridColor = LIGHTSKYBLUE;
    private Color plotTextColor = LIGHTSKYBLUE;

    private Color plotDataColor = ROYALBLUE;

    public NNPlot(GraphicsContext context) {
        this.context = context;
        width = context.getCanvas().getWidth();
        height = context.getCanvas().getHeight();
        graphWidth = width - wOffsetLeft - wOffsetRight;
        graphHeight = height - hOffsetTop - hOffsetBottom;
    }

    public void setPadding(double top, double right, double bottom, double left) {
        wOffsetLeft = left;
        wOffsetRight = right;
        hOffsetTop = top;
        hOffsetBottom = bottom;
        graphWidth = width - wOffsetLeft - wOffsetRight;
        graphHeight = height - hOffsetTop - hOffsetBottom;
    }


    public void plot(ForwardPropData data) {
        List<Tuple> tuples = data.get();
        xmin = tuples.stream().map(Tuple::getX).min(Double::compare).get();
        xmax = tuples.stream().map(Tuple::getX).max(Double::compare).get();
        ymin = tuples.stream().map(Tuple::getY).min(Double::compare).get();
        ymax = tuples.stream().map(Tuple::getY).max(Double::compare).get();

        drawBackground();
        setTicks(false);
        plotScatter(tuples);

    }

    public void plot(BackPropData data, Function<IterationObject, Double> function, boolean asDot, int modulo) {
        this.data = data;
        this.map = data.getMap();
        this.function = function;
        xmin = map.keySet().stream().min(Integer::compare).get();
        xmax = map.keySet().stream().max(Integer::compare).get();
        ymin = map.values().stream().map(function::apply).min(Double::compare).get();
        ymax = map.values().stream().map(function::apply).max(Double::compare).get();

        drawBackground();
        setTicks(true);
        plotData(asDot, modulo);
/*

        System.out.println("cost: " + data.getMap().get((int)xmax).getCost());
        System.out.println("cost sum: " + data.getMap().get((int)xmax).getCostSum());
        System.out.println("accuracy: " + data.getMap().get((int)xmax).getAccuracy());
        System.out.println("precision: " + data.getMap().get((int)xmax).getPrecision());
        System.out.println("recall: " + data.getMap().get((int)xmax).getRecall());
        System.out.println("accuracy sum: " + data.getMap().get((int)xmax).getAccuracySum());
        System.out.println("precision sum: " + data.getMap().get((int)xmax).getPrecisionSum());
        System.out.println("recall sum: " + data.getMap().get((int)xmax).getRecallSum());*/
    }

    private void drawBackground() {
        context.setFill(backgroundColor);
        context.fillRect(0,0,width, height);
        context.setFill(plotBackgroundColor);
        context.fillRect(wOffsetLeft,hOffsetTop,width-wOffsetLeft-wOffsetRight,height-hOffsetTop-hOffsetBottom);
    }

    private void setTicks(boolean drawGrid) {
        DecimalFormat df = new DecimalFormat("#.###");

        drawAxis(wOffsetLeft, height-hOffsetBottom, width-wOffsetRight, height-hOffsetBottom);
        drawAxis(wOffsetLeft, height-hOffsetBottom, wOffsetLeft, hOffsetTop);

        double xRange = Math.abs(xmin - xmax)* (1 + padding);
        double xStep = graphWidth <= 350 ? calculateIntervalSmall((xRange)) : calculateInterval(xRange);
        int xTickcount = (int) (xRange / xStep) + 1;

        context.setLineWidth(2);
        double x = x(0);
        context.setTextAlign(TextAlignment.LEFT);
        double xRangeLabel = xmin;
        for (int i = 0; i < xTickcount; i++) {
            if (i > 0) {
                x += x(xStep) - wOffsetLeft - (graphWidth*padding*0.5);
            }
            context.setStroke(plotLineColor);
            context.strokeLine(x, height-hOffsetBottom, x, height-hOffsetBottom+6);
            context.setFill(plotTextColor);
            context.fillText("" + Double.valueOf(df.format(xRangeLabel + i*xStep)), x, height-hOffsetBottom+18);
            drawGrid(x, height - hOffsetBottom, x, hOffsetTop, drawGrid);
        }

        double yRange = Math.abs(ymin - ymax) * (1 + padding);
        double yStep = graphHeight <= 350 ? calculateIntervalSmall((yRange)) : calculateInterval(yRange);
        int yTickcount = (int) (yRange / yStep) + 1;
        double y = y(0);
        context.setTextAlign(TextAlignment.RIGHT);
        double yRangeLabel = ymin;
        for (int i = 0; i < yTickcount; i++) {
            if (i > 0) {
                y -= Math.abs(Math.abs(y(yStep)) - (graphHeight+hOffsetTop - (graphHeight*padding*0.5))) ;
            }
            context.setStroke(plotLineColor);
            if (y > hOffsetTop) {
                context.strokeLine(wOffsetLeft, y, wOffsetLeft - 6, y);
                context.setFill(plotTextColor);
                context.fillText("" + Double.valueOf(df.format(yRangeLabel + i * yStep)), wOffsetLeft - 18, y);
                drawGrid(wOffsetLeft, y, width - wOffsetRight, y, drawGrid);
            }
        }
    }

    private void drawGrid(double x1, double y1, double x2, double y2, boolean draw) {
        if (!draw) {
            return;
        }
        context.setStroke(plotGridColor);
        context.setLineWidth(gridLineWidth);
        context.strokeLine(x1, y1, x2, y2);
    }

    private double x(double x) {
        return (x / Math.abs(xmin-xmax) * (graphWidth * ((1-padding)))) + (wOffsetLeft + (graphWidth*padding*0.5));
    }

    private double y(double y) {
        return (graphHeight+hOffsetTop - (graphHeight*padding*0.5)) - (y / Math.abs(ymin - ymax) * (graphHeight * (1-padding)));
    }

    private void plotScatter(List<Tuple> tuples) {
        double dotRadius = 8;// width/50;

        context.setLineWidth(dataLineWidth);

        for (Tuple tuple : tuples) {
            double x = x(tuple.getX()-xmin);
            double y =  y(tuple.getY()-ymin);
            double output = tuple.getOutput();
            Color color;

            if (output <= 0.5) {
                color = NNColorPalette.blend(RED, YELLOW, 1-(output*2));
            } else {
                color = NNColorPalette.blend(GREEN, YELLOW, (output-0.5)*2);
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
            double x = x(key-xmin);
            double y =  y(function.apply(map.get(key))-ymin);
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
        context.setLineWidth(graphLineWidth);
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

    private double calculateInterval(double range) {
        double x = Math.pow(10.0, Math.floor(Math.log10(range)));
        if (range / (x / 2.0) >= 10) {
            return x / 2.0;
        } else if (range / (x / 5.0) >= 10) {
            return x / 5.0;
        }
        return x / 10.0;
    }
}
