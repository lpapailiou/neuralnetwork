package nnui;

import nndata.BackPropEntity;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import nn.NeuralNetwork;
import nnui.color.NNColorSupport;
import nnutil.Rectifier;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public class NNPlot extends Plot {

    private boolean hasData;

    public NNPlot(GraphicsContext context) {
        super(context);
    }

    public void plotCost(NeuralNetwork neuralNetwork, boolean scatter, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getCost, scatter, smoothing);
    }

    public void plotCostSum(NeuralNetwork neuralNetwork, boolean scatter, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getCostSum, scatter, smoothing);
    }

    public void plotAccuracy(NeuralNetwork neuralNetwork, boolean scatter, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getAccuracy, scatter, smoothing);
    }

    public void plotAccuracySum(NeuralNetwork neuralNetwork, boolean scatter, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getAccuracySum, scatter, smoothing);
    }

    public void plotPrecision(NeuralNetwork neuralNetwork, boolean scatter, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getPrecision, scatter, smoothing);
    }

    public void plotPrecisionSum(NeuralNetwork neuralNetwork, boolean scatter, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getPrecisionSum, scatter, smoothing);
    }

    public void plotRecall(NeuralNetwork neuralNetwork, boolean scatter, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getRecall, scatter, smoothing);
    }

    public void plotRecallSum(NeuralNetwork neuralNetwork, boolean scatter, double smoothing) {
        plot(neuralNetwork, BackPropEntity::getRecallSum, scatter, smoothing);
    }


    /**
     *
     * @param neuralNetwork the neural network to plot for
     * @param function
     * @param scatter
     * @param smoothing the smoothing factor * 100 equals the evaluated modulo (e.g. smoothing of 0.02 = modulo 2).
     */
    private void plot(NeuralNetwork neuralNetwork, Function<BackPropEntity, Double> function, boolean scatter, double smoothing) {
        Map<Double, Double> data = new TreeMap<>();
        SortedMap<Integer, BackPropEntity> rawData = neuralNetwork.getBackPropData().getMap();

        for (Integer key : rawData.keySet()) {
            data.put((double) key, function.apply(rawData.get(key)));
        }

        setLimits(data);

        if (!hasData) {
            drawBackground();
            drawGrid(true);
        } else {
            plotDataColor = NNColorSupport.randomColor(false);
        }
        int smooth = (int) (smoothing * 100);
        smooth = Math.max(smooth, 1);
        smooth = Math.min(smooth, data.size() - 1);
        plotData(data, scatter, smooth);

        if (!hasData) {
            drawAxes(true, true, true);
        }
        setTitle(title);
        hasData = true;
    }

    private void setLimits(Map<Double, Double> data) {
        xMin = data.keySet().stream().min(Double::compare).get();
        xMax = data.keySet().stream().max(Double::compare).get();
        yMin = data.values().stream().min(Double::compare).get();
        yMax = data.values().stream().max(Double::compare).get();

        if (xMin == xMax) {
            xMin = xMin - 0.5;
            xMax = xMax + 0.5;
        }
        if (yMin == yMax) {
            yMin = yMin - 0.5;
            yMax = yMax + 0.5;
        }
    }

    public void plot(Rectifier rectifier, boolean showDerivation) {
        dataLineWidth = 3;
        Map<Double, Double> rectifierData = new TreeMap<>();
        Map<Double, Double> derivationData = new TreeMap<>();
        int iter = 500;
        double step = 8.0 / iter;

        xMin = -4;
        xMax = 4;
        yMin = -1;
        yMax = 1;

        for (int i = -(iter/2); i < iter/2; i++) {
            double valueX = i*step;
            double valueYa = rectifier.activate(valueX);
            double valueYd = rectifier.derive(valueX);
            if (valueYa >= yMin && valueYa <= yMax) {
                rectifierData.put(valueX, valueYa);
            }
            if (valueYd >= yMin && valueYd <= yMax) {
                derivationData.put(valueX, valueYd);
            }
        }

        if (!hasData) {
            drawBackground();
            //drawGrid(true);
            drawCross();
        } else {
            plotDataColor = NNColorSupport.randomColor(false);
        }
        Color cache = plotDataColor;
        if (showDerivation) {
            plotDataColor = NNColorSupport.blend(plotDataColor, plotBackgroundColor, 0.4);
            plotData(derivationData, false, 1);
        }
        plotDataColor = cache;
        plotData(rectifierData, false, 1);

        if (!hasData) {
            drawAxes(true, true, false);
        }

        setTitle(title);
        hasData = true;
    }

    private void plotData(Map<Double, Double> data, boolean scatter, int modulo) {
        context.setStroke(plotDataColor);
        context.setFill(plotDataColor);
        context.setLineWidth(dataLineWidth);

        double y = 0;
        double oldX = 0;
        double oldY = 0;
        boolean init = true;
        int counter = 0;
        for (Double key : data.keySet()) {
            counter++;
            double x = key;
            y += data.get(key);
            if ((counter - 1) % modulo != 0) {
                continue;
            } else {
                y = y / modulo;
            }
            if (scatter) {
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
