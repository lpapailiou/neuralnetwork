package ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import neuralnet.NeuralNetwork;
import ui.color.NNDataColor;
import ui.color.NNHeatMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javafx.scene.paint.Color.TRANSPARENT;
import static ui.color.NNColorSupport.blend;

public class NNMeshGrid extends Plot {

    private NNDataColor dataColor;
    private double[][] data;

    public NNMeshGrid(GraphicsContext context) {
        super(context);
    }

    public void plot(NeuralNetwork neuralNetwork, double[][] in, double resolution, double opacity, boolean drawAxes, boolean drawTicks, boolean drawAxisLabels, NNDataColor dataColor) {
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

        this.dataColor = dataColor;

        ForwardPropData data = new ForwardPropData();

        int iterX = (int) Math.ceil(plotWidth * resolution);
        int iterY = (int) Math.ceil(plotHeight * resolution);
        double stepX = 1.0 / iterX;
        double stepY = 1.0 / iterY;
        double x = stepX / 2;
        double y = stepY / 2;
        double xOffset = Math.ceil(plotWidth / iterX) + 1;
        double yOffset = Math.ceil(plotHeight / iterY) + 1;

        for (int i = 0; i < iterX; i++) {
            for (int j = 0; j < iterY; j++) {
                double[] input = {scale(x, xMin, xMax), scale(y, yMin, yMax)};
                y += stepY;
                List<Double> output = neuralNetwork.predict(input);
                data.add(input, output);
            }
            y = stepY / 2;
            x += stepX;
        }

        List<ForwardPropEntity> forwardPropEntities = data.get();

        double xMinNext = scale(0, xMin, xMax);
        double xMaxNext = scale(1, xMin, xMax);
        double yMinNext = scale(0, yMin, yMax);
        double yMaxNext = scale(1, yMin, yMax);

        xMin = xMinNext;
        xMax = xMaxNext;
        yMin = yMinNext;
        yMax = yMaxNext;
/*
        if (xMin == xMax) {
            xMin = xMin - 0.5;
            xMax = xMax + 0.5;
        }
        if (yMin == yMax) {
            yMin = yMin - 0.5;
            yMax = yMax + 0.5;
        } */

        double cachedPadding = padding;
        padding = 0;

        drawBackground();
        if (configuration[configuration.length - 1] == 1) {
            if (dataColor.getColors().size() < 2) {
                throw new IllegalArgumentException("At least 2 data color items must be provided!");
            }
            plotBinaryClassifierDecisionBoundaries(forwardPropEntities, xOffset, yOffset);
        } else {
            if (dataColor.getColors().size() != configuration[configuration.length-1]) {
                throw new IllegalArgumentException("Count of data color items " + dataColor.getColors().size() + " must match output class dimensions " + configuration[configuration.length-1] + "!");
            }
            plotMultiClassClassifierDecisionBoundaries(forwardPropEntities, xOffset, yOffset);
        }
        drawOverlay(opacity);
        drawAxes(drawAxes, drawTicks, drawAxisLabels);
        setTitle(title);

        padding = cachedPadding;
    }

    private void plotBinaryClassifierDecisionBoundaries(List<ForwardPropEntity> forwardPropEntities, double xOffset, double yOffset) {
        List<Color> customColors = dataColor.getColors();
        double zMin = forwardPropEntities.stream().map(ForwardPropEntity::getZ).min(Double::compare).get();
        double zMax = forwardPropEntities.stream().map(ForwardPropEntity::getZ).max(Double::compare).get();
        if (dataColor instanceof NNHeatMap && ((NNHeatMap) dataColor).isScaled()) {
            zMin = ((NNHeatMap) dataColor).getMin();
            zMax = ((NNHeatMap) dataColor).getMax();
        }
        double range = Math.abs(zMax - zMin);
        double step = range / (customColors.size()-1);
        for (ForwardPropEntity forwardPropEntity : forwardPropEntities) {
            double x = x(forwardPropEntity.getX());
            double y = y(forwardPropEntity.getY());
            double output = forwardPropEntity.getOutput().get(0);

            Color color;
            int stepIndex = 0;
            double value = zMin;
            for (int i = 0; i < customColors.size()-1; i++) {
                value += step;
                if (output <= value || i == customColors.size()-2) {
                    stepIndex = i;
                    break;
                }
            }
            double ratio = 1 / step * Math.abs(value - output);
            if (output > zMax) {
                ratio = 0;
            }
            color = blend(customColors.get(stepIndex), customColors.get(stepIndex+1), ratio);
            context.setFill(color);
            context.fillRect(x - xOffset/2 , y - yOffset/2, xOffset, yOffset);
        }
    }

    private void plotMultiClassClassifierDecisionBoundaries(List<ForwardPropEntity> forwardPropEntities, double xOffset, double yOffset) {
        List<Color> customColors = dataColor.getColors();
        for (ForwardPropEntity forwardPropEntity : forwardPropEntities) {
            double x = x(forwardPropEntity.getX());
            double y = y(forwardPropEntity.getY());
            List<Double> output = forwardPropEntity.getOutput();
            Color color = new Color(plotBackgroundColor.getRed(), plotBackgroundColor.getGreen(), plotBackgroundColor.getBlue(), 0);
            for (int i = 0; i < output.size(); i++) {
                double value = output.get(i);
                if (value <= 0.5) {
                    color = blend(color, customColors.get(i), 1 - (value * 2));
                } else {
                    color = blend(customColors.get(i), customColors.get(i), (value - 0.5) * 2);
                }
            }
            context.setFill(color);
            context.fillRect(x - xOffset/2 , y - yOffset/2, xOffset, yOffset);
        }
    }

    public void plotData(double[][] definedClasses, double radius) {
        List<Color> customColors = dataColor.getColors();
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
            colors.add(customColors.get(0));
            colors.add(customColors.get(customColors.size()-1));

        } else {
            colors = customColors;
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
        return x * ((1 + padding) * Math.abs(min - max)) + min - (Math.abs(min - max) * (padding/2));
    }

    class ForwardPropData {

        private List<ForwardPropEntity> data = new ArrayList<>();

        public void add(double[] in, List<Double> out) {
            data.add(new ForwardPropEntity(in[0], in[1], out));
        }

        public List<ForwardPropEntity> get() {
            return data;
        }

    }

    class ForwardPropEntity {

        private double x;
        private double y;
        private List<Double> output;

        public ForwardPropEntity(double x, double y, List<Double> output) {
            this.x = x;
            this.y = y;
            this.output = output;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return output.get(0);
        }

        public List<Double> getOutput() {
            return output;
        }
    }

}

