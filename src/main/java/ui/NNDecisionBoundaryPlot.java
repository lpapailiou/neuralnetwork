package ui;

import data.ForwardPropData;
import data.Tuple;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import neuralnet.NeuralNetwork;
import ui.color.NNBinaryClassifierColor;
import ui.color.NNColorSupport;
import ui.color.NNMultiColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javafx.scene.paint.Color.TRANSPARENT;
import static ui.color.NNColorSupport.blend;

public class NNDecisionBoundaryPlot extends Plot {

    private NNBinaryClassifierColor customBinaryColors;
    private NNMultiColor customMultiColors;
    private double[][] data;

    public NNDecisionBoundaryPlot(GraphicsContext context) {
        super(context);
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

        if (customColors instanceof NNBinaryClassifierColor) {
            this.customBinaryColors = (NNBinaryClassifierColor) customColors;
        } else if (customColors instanceof NNMultiColor) {
            this.customMultiColors = (NNMultiColor) customColors;
        }

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

        double cachedPadding = padding;
        padding = 0;

        drawBackground();
        if (configuration[configuration.length - 1] == 1) {
            plotBinaryClassifierDecisionBoundaries(tuples, resolution);
        } else {
            plotMultiClassClassifierDecisionBoundaries(tuples, resolution);
        }
        drawOverlay(opacity);
        drawAxes(drawAxes, drawTicks, drawAxisLabels);
        setTitle(title);

        padding = cachedPadding;
    }

    private void plotBinaryClassifierDecisionBoundaries(List<Tuple> tuples, double resolution) {
        double dotRadius = (((plotWidth + plotHeight) / 2) / 64) / resolution;

        for (Tuple tuple : tuples) {
            double x = x(tuple.getX());
            double y = y(tuple.getY());
            double output = tuple.getOutput().get(0);

            Color color;
            if (output <= 0.5) {
                color = blend(customBinaryColors.getNegative(), customBinaryColors.getMargin(), 1 - (output * 2));
            } else {
                color = blend(customBinaryColors.getPositive(), customBinaryColors.getMargin(), (output - 0.5) * 2);
            }

            context.setFill(color);
            context.fillOval(x - dotRadius / 2.0, y - dotRadius / 2.0, dotRadius, dotRadius);

        }
    }

    private void plotMultiClassClassifierDecisionBoundaries(List<Tuple> tuples, double resolution) {
        double dotRadius = (((plotWidth + plotHeight) / 2) / 64) / resolution;
        List<Color> colors = customMultiColors.getColors();

        for (Tuple tuple : tuples) {
            double x = x(tuple.getX());
            double y = y(tuple.getY());
            List<Double> output = tuple.getOutput();
            Color color = new Color(plotBackgroundColor.getRed(), plotBackgroundColor.getGreen(), plotBackgroundColor.getBlue(), 0);
            for (int i = 0; i < output.size(); i++) {
                double value = output.get(i);
                if (value <= 0.5) {
                    color = blend(color, colors.get(i), 1 - (value * 2));
                } else {
                    color = blend(colors.get(i), colors.get(i), (value - 0.5) * 2);
                }
            }
            context.setFill(color);
            context.fillOval(x - dotRadius / 2.0, y - dotRadius / 2.0, dotRadius, dotRadius);
        }
    }

    public void plotData(double[][] definedClasses, double dotRadius) {
        if (data == null || data[0].length != 2) {
            throw new IllegalArgumentException("Data must be 2-dimensional!");
        }

        double cachedPadding = padding;
        padding = 0;
        List<String> classes = new ArrayList<>();
        for (double[] o : definedClasses) {
            String outStr = Arrays.toString(o);
            if (!classes.contains(outStr)) {
                classes.add(outStr);
            }
        }
        List<Color> colors = new ArrayList<>();
        if (definedClasses[0].length == 1) {
            colors.add(customBinaryColors.getNegative());
            colors.add(customBinaryColors.getPositive());
        } else {
            colors.addAll(customMultiColors.getColors());
        }
        for (int i = 0; i < data.length; i++) {
            double[] outClass = definedClasses[i];
            int colorIndex = classes.indexOf(Arrays.toString(outClass));
            context.setFill(colors.get(colorIndex));
            context.fillOval(x(data[i][0]) - dotRadius / 2.0, y(data[i][1]) - dotRadius / 2.0, dotRadius, dotRadius);
        }

        padding = cachedPadding;
    }

    private double getRandomInput(double min, double max) {
        return (((((Math.random() * 2 - 1) * (1 + padding * 2)) + 1) / 2) * Math.abs(min - max)) + min;
    }
}
