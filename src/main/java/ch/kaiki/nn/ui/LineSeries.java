package ch.kaiki.nn.ui;

import ch.kaiki.nn.data.BackPropEntity;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;

public class LineSeries extends Series {

    private List<double[]> seriesData = new ArrayList<>();
    private final BaseChart chart;
    private final NeuralNetwork neuralNetwork;
    private final Function<BackPropEntity, Double> function;
    private double smoothing;

    public LineSeries(BaseChart chart, NeuralNetwork neuralNetwork, Function<BackPropEntity, Double> function, String name, Color color, double smoothing) {
        super(Arrays.asList(name), Arrays.asList(color));
        this.chart = chart;
        this.neuralNetwork = neuralNetwork;
        this.function = function;
        this.smoothing = smoothing;
    }

    @Override
    public void compute() {
        xMin = Double.MAX_VALUE;
        xMax = Double.MIN_VALUE;
        yMin = Double.MAX_VALUE;
        yMax = Double.MIN_VALUE;
        seriesData.clear();

        int index = 0;
        for (Series s: chart.series) {
            if (this == s) {
                break;
            }
            index++;
        }

        SortedMap<Integer, BackPropEntity> rawData = neuralNetwork.getBackPropData().getMap();
        int size = rawData.size();
        int modulo = (int) (rawData.size() / 100 * smoothing * 100);
        modulo = Math.max(1, modulo);
        int counter = 0;
        double x;
        double y = 0;
        for (Integer key : rawData.keySet()) {
            int batch = counter % modulo;
            x = (double) key;
            y += function.apply(rawData.get(key));

            if (counter == 0 || counter == size -1 || batch == 0) {
                if (x < xMin) {
                    xMin = x;
                }
                if (x > xMax) {
                    xMax = x;
                }
                if (y < yMin) {
                    yMin = y;
                }
                if (y > yMax) {
                    yMax = batch == 0 ? y/modulo : y / batch;
                }
                seriesData.add(new double[] {x, batch == 0 ? y/modulo : y / batch, index});
                y = 0;
            }
            counter++;
        }

        /*
        if (chart.dataScaling > 1) {
            double xPadding = Math.abs(xMax - xMin) * chart.dataScaling;
            double yPadding = Math.abs(yMax - yMin) * chart.dataScaling;
            double xOffset = ((xPadding - Math.abs(xMax - xMin))) / 2;
            double yOffset = ((yPadding - Math.abs(yMax - yMin))) / 2;
            xMin = xMin - xOffset;
            xMax = xMax + xOffset;
            yMin = yMin - yOffset;
            yMax = yMax + yOffset;
        }*/

        zMin = index;
        zMax = index + 0.00001;
    }

    @Override
    public void render() {
        List<Line> lines = new ArrayList<>();
        Color color = super.getColor().get(0);
        int index = 0;
        double[] lastPoint = null;
        for (double[] line : seriesData) {
            double[] t = chart.transform(new double[]{line[0], line[1], line[2]});
            if (index == 0) {
                lastPoint = t;
                index++;
                continue;
            }
            lines.add(new Line(chart.context, lastPoint[0], lastPoint[1], lastPoint[2], t[0], t[1], t[2], color));
            lastPoint = t;
        }

        for (Line line : lines) {
            line.render();
        }

    }

}
