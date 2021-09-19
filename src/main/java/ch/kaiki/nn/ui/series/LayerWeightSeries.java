package ch.kaiki.nn.ui.series;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.BasePlot;
import ch.kaiki.nn.ui.color.NNHeatMap;
import ch.kaiki.nn.ui.util.ChartMode;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class LayerWeightSeries extends Series {

    private final NNHeatMap colorMap;
    private final BasePlot chart;
    private List<Series> subSeries = new ArrayList<>();
    private int imgCount;
    private GraphicsContext context;

    public LayerWeightSeries(BasePlot chart, NeuralNetwork neuralNetwork, NNHeatMap colorMap) {
        super(null, colorMap.getColors(), ChartMode.MESH_GRID);
        imgCount = neuralNetwork.getConfiguration().length-1;
        for (int i = 0; i < imgCount; i++) {
            subSeries.add(new SingleLayerWeightSeries(chart, neuralNetwork, colorMap, i));
        }

        this.chart = chart;
        this.context = chart.getContext();
        this.colorMap = colorMap;
        super.addName("high");
        super.addName("low");
    }

    public LayerWeightSeries(BasePlot chart, NeuralNetwork neuralNetwork, NNHeatMap colorMap, int layerIndex, int width) {
        super(null, colorMap.getColors(), ChartMode.MESH_GRID);
        int nodeCount = neuralNetwork.getConfiguration()[layerIndex+1];
        for (int i = 0; i < nodeCount; i++) {
            subSeries.add(new SingleLayerWeightSeries(chart, neuralNetwork, colorMap, layerIndex, i, width));
        }
        this.imgCount = nodeCount;
        this.chart = chart;
        this.context = chart.getContext();
        this.colorMap = colorMap;
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
        xMin = 0;
        xMax = 1;
        yMin = 0;
        yMax = 1;
        zMin = Double.MAX_VALUE;
        zMax = Double.MIN_VALUE;
        chart.setPreRenderZoom(0.8);
        for (Series s : subSeries) {
            s.compute();
            double zMin = s.getMinZ();
            double zMax = s.getMaxZ();

            if (zMin < this.zMin) {
                this.zMin = zMin;
            }
            if (zMax > this.zMax) {
                this.zMax = zMax;
            }
        }
    }

    @Override
    public void render() {
        int x0 = (int)chart.getOffsetLeft();
        int x1 = (int)chart.getWidth();
        int y0 = (int)chart.getOffsetTop();
        int y1 = (int)chart.getHeight();
        List<WritableImage> weightImgs = new ArrayList<>();
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        for (Series s : subSeries) {
            chart.clear();
            chart.setxMin(s.getMinX());
            chart.setxMax(s.getMaxX());
            chart.setyMin(s.getMinY());
            chart.setyMax(s.getMaxY());
            chart.setProjectionMatrix();
            s.render();
            WritableImage img = new WritableImage(context.getCanvas().snapshot(params, null).getPixelReader(), x0, y0, x1, y1);
            weightImgs.add(img);

        }
        int iterX = 1;
        int iterY = 1;
        int index = 0;
        while (iterX * iterY < imgCount) {
            if (index % 2 == 0) {
                iterX++;
            } else {
                iterY++;
            }
            index++;
        }

        chart.clear();
        chart.showGrid(false);
        chart.showTickMarks(false);
        chart.showTickMarkLabels(false);
        chart.setAxisLabels(null);
        chart.postInvalidate();
        chart.setProjectionMatrix();
        x0 = (int)chart.getOffsetLeft();
        x1 = (int)chart.getWidth();
        y0 = (int)chart.getOffsetTop();
        y1 = (int)chart.getHeight();
        double offset = 5;
        double stepX = (x1) / (double) iterX;
        double stepY = (y1) / (double) iterY;

        int imgIndex = 0;

        double y = y0;
        for (int j = 0; j < iterY; j++) {
            double x = x0;

            for (int i = 0; i < iterX; i++) {
                if (imgIndex < weightImgs.size()) {
                    context.drawImage(weightImgs.get(imgIndex), x+offset, y+offset, stepX- 2* offset, stepY- 2* offset);

                    x += stepX;
                }
                imgIndex++;
            }
            y += stepY;
        }
        chart.preProcess();
        chart.postProcess();

    }


}
