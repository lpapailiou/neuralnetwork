package ch.kaiki.nn.ui.series;

import ch.kaiki.nn.data.BackPropEntity;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.BasePlot;
import ch.kaiki.nn.ui.util.ChartMode;
import ch.kaiki.nn.ui.seriesobject.Point;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.function.Function;

public class ScatterSeries extends Series {

    private List<double[]> seriesData = new ArrayList<>();
    private final BasePlot chart;
    private final NeuralNetwork neuralNetwork;
    private final Function<BackPropEntity, Double> function;
    private double smoothing;
    private GraphicsContext context;

    public ScatterSeries(BasePlot chart, NeuralNetwork neuralNetwork, Function<BackPropEntity, Double> function, String name, Color color, double smoothing) {
        super(Arrays.asList(name), Arrays.asList(color), ChartMode.LINE_OR_SCATTER);
        this.chart = chart;
        this.context = chart.getContext();
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
        for (Series s: chart.getSeries()) {
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
                yMax = y;
            }
            if (counter == 0 || counter == size -1 || batch == 0) {
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
        List<Point> points = new ArrayList<>();
        Color color = super.getColor().get(0);
        for (double[] point : seriesData) {
            double[] t = chart.transform(new double[]{point[0], point[1], point[2]});
            points.add(new Point(context, t[0], t[1], t[2], color, false));
        }

        for (Point point : points) {
            point.render();
        }

    }

}
