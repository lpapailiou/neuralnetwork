package ui;

import data.ForwardPropData;
import data.ForwardPropEntity;
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

        int iterX = (int) Math.ceil(plotWidth * resolution);
        int iterY = (int) Math.ceil(plotHeight * resolution);
        double stepX = 1.0 / iterX;
        double stepY = 1.0 / iterY;
        double x = 0;
        double y = 0;
        double xOffset = Math.ceil(plotWidth / iterX);
        double yOffset = Math.ceil(plotHeight / iterY);

        for (int i = 0; i <= iterX; i++) {
            for (int j = 0; j <= iterY; j++) {
                double[] input = {scale(x, xMin, xMax), scale(y, yMin, yMax)};
                y += stepY;
                List<Double> output = neuralNetwork.predict(input);
                data.add(input, output);
            }
            y = 0;
            x += stepX;
        }

        List<ForwardPropEntity> forwardPropEntities = data.get();
        xMin = forwardPropEntities.stream().map(ForwardPropEntity::getX).min(Double::compare).get();
        xMax = forwardPropEntities.stream().map(ForwardPropEntity::getX).max(Double::compare).get();
        yMin = forwardPropEntities.stream().map(ForwardPropEntity::getY).min(Double::compare).get();
        yMax = forwardPropEntities.stream().map(ForwardPropEntity::getY).max(Double::compare).get();

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
            plotBinaryClassifierDecisionBoundaries(forwardPropEntities, xOffset, yOffset);
        } else {
            plotMultiClassClassifierDecisionBoundaries(forwardPropEntities, xOffset, yOffset);
        }
        drawOverlay(opacity);
        drawAxes(drawAxes, drawTicks, drawAxisLabels);
        setTitle(title);

        padding = cachedPadding;
    }

    private void plotBinaryClassifierDecisionBoundaries(List<ForwardPropEntity> forwardPropEntities, double xOffset, double yOffset) {
        for (ForwardPropEntity forwardPropEntity : forwardPropEntities) {
            double x = x(forwardPropEntity.getX());
            double y = y(forwardPropEntity.getY());
            double output = forwardPropEntity.getOutput().get(0);

            Color color;
            if (output <= 0.5) {
                color = blend(customBinaryColors.getNegative(), customBinaryColors.getMargin(), 1 - (output * 2));
            } else {
                color = blend(customBinaryColors.getPositive(), customBinaryColors.getMargin(), (output - 0.5) * 2);
            }

            context.setFill(color);
            context.fillRect(x , y, xOffset, yOffset);
        }
    }

    private void plotMultiClassClassifierDecisionBoundaries(List<ForwardPropEntity> forwardPropEntities, double xOffset, double yOffset) {
        List<Color> colors = customMultiColors.getColors();
        for (ForwardPropEntity forwardPropEntity : forwardPropEntities) {
            double x = x(forwardPropEntity.getX());
            double y = y(forwardPropEntity.getY());
            List<Double> output = forwardPropEntity.getOutput();
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
            context.fillRect(x , y, xOffset, yOffset);
        }
    }

    public void plotData(double[][] definedClasses, double radius) {
        //double innerRadius = radius * 0.8;
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
            Color innerColor = colors.get(classes.indexOf(Arrays.toString(outClass)));
            //Color outerColor = NNColorSupport.blend(innerColor, plotBackgroundColor, 0.5);
            //context.setFill(outerColor);
            //context.fillOval(x(data[i][0]) - radius / 2, y(data[i][1]) - radius / 2, radius, radius);
            context.setFill(innerColor);
            context.fillOval(x(data[i][0]) - radius / 2, y(data[i][1]) - radius / 2, radius, radius);
        }

        padding = cachedPadding;
    }

    private double scale(double x, double min, double max) {
        return x * ((1 + padding *2) * Math.abs(min - max)) + min - (Math.abs(min - max) * padding);
    }

}
