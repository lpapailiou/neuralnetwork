package ch.kaiki.nn.ui.series;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.BaseChart;
import ch.kaiki.nn.ui.NN2DChart;
import ch.kaiki.nn.ui.NN3DChart;
import ch.kaiki.nn.ui.color.NNColor;
import ch.kaiki.nn.ui.color.NNHeatMap;
import ch.kaiki.nn.ui.seriesobject.Point;
import ch.kaiki.nn.ui.seriesobject.Polygon;
import ch.kaiki.nn.ui.seriesobject.SortableSeriesData;
import ch.kaiki.nn.ui.util.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

import static ch.kaiki.nn.ui.color.NNColor.blend;
import static javafx.scene.paint.Color.*;

public class DecisionBoundarySeries extends Series {

    private final double[][] inputData;
    private double[][] outputData;
    private List<double[][][]> seriesData = new ArrayList<>();
    private final NNHeatMap colorMap;
    private final BaseChart chart;
    private int iterX;
    private int iterY;
    private final NeuralNetwork neuralNetwork;
    private boolean isBinary;
    private boolean showData;
    private GraphicsContext context;

    public DecisionBoundarySeries(BaseChart chart, NeuralNetwork neuralNetwork, double[][] inputData, double[][] outputData, boolean showData, String[] legend, NNHeatMap colorMap) {
        super(null, colorMap.getColors(), ChartMode.MESH_GRID);
        this.chart = chart;
        this.context = chart.getContext();
        this.neuralNetwork = neuralNetwork;
        this.inputData = inputData;
        this.outputData = outputData;
        this.showData = showData;
        this.colorMap = colorMap;
        int[] configuration = neuralNetwork.getConfiguration();
        this.isBinary = configuration[configuration.length-1] == 1;

        if (legend == null) {
            if (isBinary) {
                super.addName("true");
                super.addName("false");
            } else {
                for (int i = 0; i < configuration[configuration.length - 1]; i++) {
                    super.addName("feature " + i);
                }
            }
        } else {
            for (String label : legend) {
                super.addName(label);
            }
        }
    }

    @Override
    public List<Color> getColor() {
        if (isBinary) {
            List<Color> colors = colorMap.getColors();
            List<Color> featureLabelColors = new ArrayList<>();
            featureLabelColors.add(colors.get(colors.size()-1));
            featureLabelColors.add(colors.get(0));
            return featureLabelColors;
        }
        return super.getColor();
    }

    @Override
    public void compute() {
        double resolution = chart.getResolution();
        double padding = chart.getDataScaling();
        double width = chart.getWidth();
        double height = chart.getHeight();
        VisualizationMode mode = chart.getMode();
        prepareRanges(inputData, padding);
        iterX = (int) Math.ceil(width *resolution);
        switch (mode) {
            case REGULAR:
                iterY = (int) Math.ceil((width * resolution) / Math.abs(xMax - xMin) * Math.abs(yMax - yMin));
                break;
            case SNAP_TO_VIEWPORT:
                iterY = (int) Math.ceil(height * resolution);
                break;
            case CUBE:
            default:
                iterY = iterX;
                break;
        }
        int[] configuration = neuralNetwork.getConfiguration();
        int gridCount = configuration[configuration.length-1];
        List<double[][][]> gridList = getBaseGrid(gridCount);

        double stepX = Math.abs(xMax - xMin) / (iterX);
        double stepY = Math.abs(yMax - yMin) / (iterY);

        double x = xMin;
        double y = yMin;
        for (int i = 0; i <= iterX; i++) {
            for (int j = 0; j <= iterY; j++) {
                double[] input = {x, y};
                y += stepY;
                List<Double> output = neuralNetwork.predict(input);
                for (int k = 0; k < output.size(); k++) {
                    double[][][] grid = gridList.get(k);
                    double out = output.get(k);
                    if (out < zMin) {
                        zMin = out;
                    }
                    if (out > zMax) {
                        zMax = out;
                    }
                    grid[i][j] = new double[]{input[0], input[1], out};
                }
            }
            y = yMin;
            x += stepX;
        }

        if (colorMap.isScaled()) {
            zMin = colorMap.getMin();
            zMax = colorMap.getMax();
        }
        seriesData = gridList;
    }

    @Override
    public void render() {
        List<double[][][]> transformedDataGrid = getBaseGrid(seriesData.size());
        GraphicsContext context = chart.getContext();
        double zMin = chart.getGlobalMinZ();
        double zMax = chart.getGlobalMaxZ();

        for (int i = 0; i < transformedDataGrid.size(); i++) {
            for (int j = 0; j <= iterX; j++) {
                for (int k = 0; k <= iterY; k++) {
                    double[] t = chart.transform(seriesData.get(i)[j][k]);
                    double output = seriesData.get(i)[j][k][2];
                    transformedDataGrid.get(i)[j][k] = new double[]{t[0], t[1], t[3], output};
                }
            }
        }
        List<SortableSeriesData> polygons = new ArrayList<>();
        int index = 0;
        List<Color> colors = colorMap.getColors();
        double step = Math.abs(zMax -zMin)/(colors.size()-1);
        for (double[][][] transformedGrid : transformedDataGrid) {
            List<Color> colorList = new ArrayList<>();
            double minOpacity = colorMap.getMinOpacity();
            double maxOpacity = colorMap.getMaxOpacity();
            if (transformedDataGrid.size() > 1) {
                colorList.add(NNColor.blend(colors.get(index), TRANSPARENT, minOpacity));
                colorList.add(NNColor.blend(colors.get(index), TRANSPARENT, maxOpacity));
            } else {

                if (minOpacity == 1 && maxOpacity == 1) {
                    colorList.addAll(colors);
                } else {
                    if (colors.size() == 1) {
                        colorList.add(NNColor.blend(colors.get(0), TRANSPARENT, minOpacity));
                        colorList.add(NNColor.blend(colors.get(0), TRANSPARENT, maxOpacity));
                    } else {
                        double opacityStep = Math.abs(maxOpacity - minOpacity) / (colors.size() - 1);
                        double opacity = minOpacity;
                        for (Color color : colors) {
                            colorList.add(NNColor.blend(color, TRANSPARENT, opacity));
                            opacity += opacityStep;
                        }
                    }
                }
            }
            polygons.addAll(getPolygons(context, zMin, zMax, transformedGrid, step, colorList));
            index++;
        }

        if (outputData != null && showData) {
            for (int i = 0; i < inputData.length; i++) {
                double[] t = chart.transform(new double[] {inputData[i][0], inputData[i][1], zMax});
                Color color = colorMap.getColors().get(getFeatureIndex(outputData[i]));
                polygons.add(new Point(context, t[0], t[1], t[3], color, true));
            }
        }

        Comparator<SortableSeriesData> comparator = (SortableSeriesData::compareTo);
        polygons.sort(chart instanceof NN3DChart ? comparator.reversed() : comparator);
        for (SortableSeriesData p : polygons) {
            p.render();
        }
    }

    private List<Polygon> getPolygons(GraphicsContext context, double zMin, double zMax, double[][][] grid, double step, List<Color> colors) {
        List<Polygon> polygons = new ArrayList<>();
        double range = Math.abs(zMax-zMin);
        double pos = isBinary ? 0.3 : chart instanceof NN2DChart ? 0.09 : 0.2;
        double neg = -pos;
        for (int i = 0; i < iterX; i++) {
            for (int j = 0; j < iterY; j++) {
                double[] a = grid[i][j];
                double[] b = grid[i+1][j];
                double[] c = grid[i+1][j+1];
                double[] d = grid[i][j+1];

                /*
                    d   c
                    a   b
                 */
                double[] xEs = {a[0] <= c[0] ? a[0] + neg : a[0] + pos,
                        b[0] >= d[0] ? b[0] + pos : b[0] + neg,
                        c[0] >= a[0] ? c[0] + pos : c[0] + neg,
                        d[0] <= b[0] ? d[0] + neg : d[0] + pos};
                double[] ys =  {a[1] >= c[1] ? a[1] + pos : a[1] + neg,
                        b[1] >= d[1] ? b[1] + pos : b[1] + neg,
                        c[1] <= a[1] ? c[1] + neg : c[1] + pos,
                        d[1] <= b[1] ? d[1] + neg : d[1] + pos};


                double zSum = (a[3] + b[3] + c[3] + d[3]) / 4;


                if (zSum < zMin || zSum > zMax) {
                     continue;        // TODO: check if really helpful
                }

                double sort = (a[2] + b[2] + c[2] + d[2]) / 4;

                Color color;
                if (colors.size() > 2) {
                    int stepIndex = 0;
                    double value = zMin;
                    for (int k = 0; k < colors.size() - 1; k++) {
                        value += step;
                        if (zSum <= value || k == colors.size() - 2) {
                            stepIndex = k;
                            break;
                        }
                    }

                    double ratio = 1 / step * Math.abs(value - zSum);
                    if (zSum > zMax) {
                        ratio = 0;
                    }
                    color = blend(colors.get(stepIndex), colors.get(stepIndex+1), ratio);
                } else {
                    color = blend(colors.get(1), colors.get(0), (zSum-zMin)/range);
                }


                double zVal = chart instanceof NN3DChart ? sort : zSum;
                polygons.add(new Polygon(context, xEs, ys, zVal, color));
            }
        }
        return polygons;
    }


    private List<double[][][]> getBaseGrid(int featureSize) {
        List<double[][][]> gridList = new ArrayList<>();
        for (int i = 0; i < featureSize; i++) {
            gridList.add(new double[iterX+1][iterY+1][]);
        }
        return gridList;
    }

    private void prepareRanges(double[][] in, double padding) {
        xMin = Double.MAX_VALUE;
        xMax = Double.MIN_VALUE;
        yMin = Double.MAX_VALUE;
        yMax = Double.MIN_VALUE;

        for (double[] doubles : in) {
            double xValue = doubles[0];
            if (xValue < xMin) {
                xMin = xValue;
            }
            if (xValue > xMax) {
                xMax = xValue;
            }
            double yValue = doubles[1];
            if (yValue < yMin) {
                yMin = yValue;
            }
            if (yValue > yMax) {
                yMax = yValue;
            }
        }

        if (padding > 1) {
            double xPadding = Math.abs(xMax - xMin) * padding;
            double yPadding = Math.abs(yMax - yMin) * padding;
            double xOffset = ((xPadding - Math.abs(xMax - xMin))) / 2;
            double yOffset = ((yPadding - Math.abs(yMax - yMin))) / 2;
            xMin = xMin - xOffset;
            xMax = xMax + xOffset;
            yMin = yMin - yOffset;
            yMax = yMax + yOffset;
        }
        //xMax += 1;
        zMin = Double.MAX_VALUE;
        zMax = Double.MIN_VALUE;
    }

    private int getFeatureIndex(double[] out) {
        int index = 0;
        if (out.length == 1) {
            return out[0] == 0 ? 0 : colorMap.getColors().size()-1;
        }
        for (double d : out) {
            if (d == 1) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
