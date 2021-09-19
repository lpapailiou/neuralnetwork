package ch.kaiki.nn.ui.series;

import ch.kaiki.nn.data.BackPropEntity;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.BaseChart;
import ch.kaiki.nn.ui.NN2DChart;
import ch.kaiki.nn.ui.util.ChartMode;
import ch.kaiki.nn.ui.seriesobject.Line;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;

public class LineSeries extends Series {

    private List<double[]> seriesData = new ArrayList<>();
    private final BaseChart chart;
    private final GraphicsContext context;
    private NeuralNetwork neuralNetwork;
    private Function<BackPropEntity, Double> backPropFunction;
    private Function<Double, Double> genericFunction;
    private double smoothing;
    private boolean is2D;
    private double yMinLimit = Double.MIN_VALUE;
    private double yMaxLimit = Double.MAX_VALUE;

    public LineSeries(BaseChart chart, NeuralNetwork neuralNetwork, Function<BackPropEntity, Double> function, String name, Color color, double smoothing) {
        super(Arrays.asList(name), Arrays.asList(color), ChartMode.LINE_OR_SCATTER);
        this.chart = chart;
        this.context = chart.getContext();
        this.neuralNetwork = neuralNetwork;
        this.backPropFunction = function;
        this.smoothing = smoothing;
        this.is2D = chart instanceof NN2DChart;
    }

    public LineSeries(BaseChart chart, Function<Double, Double> function, String name, Color color, double minX, double maxX, double minY, double maxY) {
        super(Arrays.asList(name), Arrays.asList(color), ChartMode.LINE_OR_SCATTER);
        this.chart = chart;
        this.context = chart.getContext();
        this.genericFunction = function;
        this.is2D = chart instanceof NN2DChart;
        xMin = minX;
        xMax = maxX;
        yMinLimit = minY;
        yMaxLimit = maxY;
    }

    @Override
    public void compute() {
        seriesData.clear();
        int index = 0;
        for (Series s: chart.getSeries()) {
            if (this == s) {
                break;
            }
            index++;
        }

        if (genericFunction != null) {
            int iter = 100;
            double xStep = Math.abs(xMax - xMin) / iter;
            double x = xMin;
            for (int i = 0; i < iter; i++) {
                double y = genericFunction.apply(x);
                if (y >= yMinLimit && y <= yMaxLimit) {
                    if (is2D) {
                        if (y < yMin) {
                            yMin = y;
                        }
                        if (y > yMax) {
                            yMax = y;
                        }
                        seriesData.add(new double[]{x, y, index});
                    } else {
                        if (y < zMin) {
                            zMin = y;
                        }
                        if (y > zMax) {
                            zMax = y;
                        }
                        seriesData.add(new double[]{x, index, y});
                    }
                }
                x += xStep;
            }
        } else {
            xMin = Double.MAX_VALUE;
            xMax = Double.MIN_VALUE;
            yMin = Double.MAX_VALUE;
            yMax = Double.MIN_VALUE;
            zMin = Double.MAX_VALUE;
            zMax = Double.MIN_VALUE;
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
                y += backPropFunction.apply(rawData.get(key));

                if (counter == 0 || counter == size - 1 || batch == 0) {
                    if (x < xMin) {
                        xMin = x;
                    }
                    if (x > xMax) {
                        xMax = x;
                    }
                    double yValue = batch == 0 ? y / modulo : y / batch;
                    if (is2D) {

                        if (y < yMin) {
                            yMin = y;
                        }
                        if (y > yMax) {
                            yMax = yValue;
                        }
                        seriesData.add(new double[]{x, yValue, index});
                    } else {
                        if (y < zMin) {
                            zMin = y;
                        }
                        if (y > zMax) {
                            zMax = yValue;
                        }
                        seriesData.add(new double[]{x, index, yValue});
                    }

                    y = 0;
                }
                counter++;
            }
        }
        if (is2D) {
            zMin = index;
            zMax = index;
        } else {
            yMin = index;
            yMax = index + 0.000001;
        }
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
            lines.add(new Line(context, lastPoint[0], lastPoint[1], lastPoint[2], t[0], t[1], t[2], color));
            lastPoint = t;
        }

        for (Line line : lines) {
            line.render();
        }

    }

}
