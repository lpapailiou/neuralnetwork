package ch.kaiki.nn.ui.series;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.BaseChart;
import ch.kaiki.nn.ui.NN3DChart;
import ch.kaiki.nn.ui.color.NNColor;
import ch.kaiki.nn.ui.color.NNHeatMap;
import ch.kaiki.nn.ui.util.ChartMode;
import ch.kaiki.nn.ui.seriesobject.Polygon;
import ch.kaiki.nn.ui.seriesobject.SortableSeriesData;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ch.kaiki.nn.ui.color.NNColor.blend;
import static javafx.scene.paint.Color.TRANSPARENT;

public class ConfusionMatrixSeries extends Series {

    private double[][] seriesData;
    private final NNHeatMap colorMap;
    private final BaseChart chart;
    private final NeuralNetwork neuralNetwork;
    private int dimension;
    private boolean normalized;

    public ConfusionMatrixSeries(BaseChart chart, NeuralNetwork neuralNetwork, NNHeatMap colorMap, boolean normalized) {
        super(null, colorMap.getColors(), ChartMode.MESH_GRID);
        this.chart = chart;
        this.neuralNetwork = neuralNetwork;
        int[] configuration = neuralNetwork.getConfiguration();
        this.dimension = configuration[configuration.length-1];
        if (this.dimension == 1) {
            this.dimension++;
        }
        this.colorMap = colorMap;
        this.normalized = normalized;

        super.addName("high");
        super.addName("low");
    }

    @Override
    public List<Color> getColor() {
        List<Color> colors = colorMap.getColors();
        List<Color> featureLabelColors = new ArrayList<>();
        featureLabelColors.add(colors.get(colors.size()-1));
        featureLabelColors.add(colors.get(0));
        return featureLabelColors;
    }

    @Override
    public void compute() {
        seriesData = neuralNetwork.getBackPropData().getNormalizedConfusionMatrix();

        xMin = -0.5;
        xMax = dimension-0.5;
        yMin = -0.5;
        yMax = dimension-0.5;

        zMin = Double.MAX_VALUE;
        zMax = Double.MIN_VALUE;

        if (seriesData != null) {
            for (double[] i : seriesData) {
                for (double val : i) {
                    if (val < zMin) {
                        zMin = val;
                    }
                    if (val > zMax) {
                        zMax = val;
                    }
                }
            }
        } else {
            zMin = 0;
            zMax = 0;
        }

        if (colorMap.isScaled()) {
            zMin = colorMap.getMin();
            zMax = colorMap.getMax();
        }
    }

    @Override
    public void render() {
        double[][][][] transformedDataGrid = new double[dimension][dimension][4][];
        GraphicsContext context = chart.getContext();
        double zMin = chart.getGlobalMinZ();
        double zMax = chart.getGlobalMaxZ();
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                double value = 0;
                double actualValue = 0;
                if (seriesData != null) {
                    value = seriesData[i][j];
                }
                double[] t0 = chart.transform(new double[] {i-0.5, j-0.5, value});
                double[] t1 = chart.transform(new double[] {i-0.5, j+0.5, value});
                double[] t2 = chart.transform(new double[] {i+0.5, j-0.5, value});
                double[] t3 = chart.transform(new double[] {i+0.5, j+0.5, value});
                transformedDataGrid[i][j][0] = new double[]{t0[0], t0[1], t0[3], value};
                transformedDataGrid[i][j][1] = new double[]{t1[0], t1[1], t0[3], value};
                transformedDataGrid[i][j][2] = new double[]{t2[0], t2[1], t2[3], value};
                transformedDataGrid[i][j][3] = new double[]{t3[0], t3[1], t0[3], value};
            }
        }
        List<SortableSeriesData> polygons = new ArrayList<>();

        List<Color> colors = colorMap.getColors();
        double step = Math.abs(zMax -zMin)/(colors.size()-1);
        List<Color> colorList = new ArrayList<>();
        double minOpacity = colorMap.getMinOpacity();
        double maxOpacity = colorMap.getMaxOpacity();
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
        polygons.addAll(getPolygons(context, zMin, zMax, transformedDataGrid, step, colorList));

        Comparator<SortableSeriesData> comparator = (SortableSeriesData::compareTo);
        polygons.sort(chart instanceof NN3DChart ? comparator.reversed() : comparator);
        for (SortableSeriesData p : polygons) {
            p.render();
        }
    }

    private List<Polygon> getPolygons(GraphicsContext context, double zMin, double zMax, double[][][][] grid, double step, List<Color> colors) {
        double[][] actualData = neuralNetwork.getBackPropData().getConfusionMatrix();
        List<Polygon> polygons = new ArrayList<>();
        double pos = 0.3;
        double neg = -pos;
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                double[] a = grid[i][j][0];
                double[] b = grid[i][j][1];
                double[] c = grid[i][j][3];
                double[] d = grid[i][j][2];

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
                    color = blend(colors.get(1), colors.get(0), zSum);
                }
                double zVal = chart instanceof NN3DChart ? sort : zSum;
                double polygonLabel = (actualData == null) ? 0 : normalized ? seriesData[i][j] : actualData[i][j];
                polygons.add(new Polygon(context, xEs, ys, zVal, color, polygonLabel, true));
            }
        }
        return polygons;
    }

}
