package ch.kaiki.nn.ui.series;

import ch.kaiki.nn.data.BackPropEntity;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.BasePlot;
import ch.kaiki.nn.ui.NN2DPlot;
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
    private final BasePlot chart;
    private final GraphicsContext context;
    private NeuralNetwork neuralNetwork;
    private Function<BackPropEntity, Double> backPropFunction;
    private Function<Double, Double> genericFunction;
    private double smoothing;
    private boolean is2D;
    private double yMinLimit = Double.MIN_VALUE;
    private double yMaxLimit = Double.MAX_VALUE;
    boolean clear = true;

    public LineSeries(BasePlot chart, NeuralNetwork neuralNetwork, Function<BackPropEntity, Double> function, String name, Color color, double smoothing) {
        super(Arrays.asList(name), Arrays.asList(color), ChartMode.LINE_OR_SCATTER);
        this.chart = chart;
        this.context = chart.getContext();
        this.neuralNetwork = neuralNetwork;
        this.backPropFunction = function;
        this.smoothing = smoothing;
        this.is2D = chart instanceof NN2DPlot;
    }

    public LineSeries(BasePlot chart, Function<Double, Double> function, String name, Color color, double minX, double maxX, double minY, double maxY) {
        super(Arrays.asList(name), Arrays.asList(color), ChartMode.LINE_OR_SCATTER);
        this.chart = chart;
        this.context = chart.getContext();
        this.genericFunction = function;
        this.is2D = chart instanceof NN2DPlot;
        xMin = minX;
        xMax = maxX;
        yMinLimit = minY;
        yMaxLimit = maxY;
    }

    public LineSeries(BasePlot chart, double x, double y, String name, Color color) {
        super(Arrays.asList(name), Arrays.asList(color), ChartMode.LINE_OR_SCATTER);
        this.chart = chart;
        this.context = chart.getContext();
        this.is2D = chart instanceof NN2DPlot;
        this.clear = false;
        int index = 0;
        for (Series s: chart.getSeries()) {
            if (this == s) {
                break;
            }
            index++;
        }
        if (is2D) {
            seriesData.add(new double[]{x, y, index});
        } else {
            seriesData.add(new double[]{x, index, y});
        }
    }

    @Override
    public void compute() {
        if (clear) {
            seriesData.clear();
        }
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
        } else if (!clear) {
            xMin = Double.MAX_VALUE;
            xMax = Double.MIN_VALUE;
            yMin = Double.MAX_VALUE;
            yMax = Double.MIN_VALUE;
            zMin = Double.MAX_VALUE;
            zMax = Double.MIN_VALUE;
            for (double[] data : seriesData) {
                double dataX = data[0];
                double dataY = data[0];
                double dataZ = data[0];
                if (dataX < xMin) {
                    xMin = dataX;
                } else if (dataX > xMax) {
                    xMax = dataX;
                }
                if (dataY < yMin) {
                    yMin = dataY;
                } else if (dataY > yMax) {
                    yMax = dataY;
                }
                if (dataZ < zMin) {
                    zMin = dataZ;
                } else if (dataZ > zMax) {
                    zMax = dataZ;
                }
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
        if (seriesData.size() < 2) {
            return;
        }
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
