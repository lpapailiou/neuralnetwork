package ui;

import data.BackPropEntity;
import javafx.scene.canvas.GraphicsContext;
import neuralnet.NeuralNetwork;
import ui.color.NNColorSupport;

import java.util.SortedMap;
import java.util.function.Function;

public class NNPlot extends Plot {

    private SortedMap<Integer, BackPropEntity> map;
    private Function<BackPropEntity, Double> function;
    private boolean linePlot = true;
    private double smoothing;

    public NNPlot(GraphicsContext context) {
        super(context);
    }

    public void plotCost(NeuralNetwork neuralNetwork, boolean linePlot, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getCost, linePlot, smoothing);
    }

    public void plotCostSum(NeuralNetwork neuralNetwork, boolean linePlot, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getCostSum, linePlot, smoothing);
    }

    public void plotAccuracy(NeuralNetwork neuralNetwork, boolean linePlot, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getAccuracy, linePlot, smoothing);
    }

    public void plotAccuracySum(NeuralNetwork neuralNetwork, boolean linePlot, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getAccuracySum, linePlot, smoothing);
    }

    public void plotPrecision(NeuralNetwork neuralNetwork, boolean linePlot, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getPrecision, linePlot, smoothing);
    }

    public void plotPrecisionSum(NeuralNetwork neuralNetwork, boolean linePlot, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getPrecisionSum, linePlot, smoothing);
    }

    public void plotRecall(NeuralNetwork neuralNetwork, boolean linePlot, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getRecall, linePlot, smoothing);
    }

    public void plotRecallSum(NeuralNetwork neuralNetwork, boolean linePlot, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getRecallSum, linePlot, smoothing);
    }


    /**
     *
     * @param neuralNetwork the neural network to plot for
     * @param function
     * @param linePlot
     * @param smoothing the smoothing factor * 100 equals the evaluated modulo (e.g. smoothing of 0.02 = modulo 2).
     */
    private void plot(NeuralNetwork neuralNetwork, Function<BackPropEntity, Double> function, boolean linePlot, double smoothing) {
        boolean isOverlay = map != null;
        this.map = neuralNetwork.getBackPropData().getMap();
        this.function = function;
        this.linePlot = linePlot;
        this.smoothing = smoothing;
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

        if (!isOverlay) {
            drawBackground();
            drawGrid(true);
        } else {
            plotDataColor = NNColorSupport.randomColor(false);
        }
        int smooth = (int) (smoothing * 100);
        smooth = Math.max(smooth, 1);
        smooth = Math.min(smooth, map.size() - 1);
        plotData(smooth);

        if (!isOverlay) {
            drawAxes(true, true, true);
        }
        setTitle(title);
    }

    private void plotData(int modulo) {
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
            if ((counter - 1) % modulo != 0) {
                continue;
            } else {
                y = y / modulo;
            }
            if (!linePlot) {
                context.fillOval(x(x) - dotRadius / 2.0, y(y) - dotRadius / 2.0, dotRadius, dotRadius);
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
}
