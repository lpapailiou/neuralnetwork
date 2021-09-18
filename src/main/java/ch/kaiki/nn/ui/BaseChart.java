package ch.kaiki.nn.ui;

import ch.kaiki.nn.data.BackPropEntity;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.NNChartColor;
import ch.kaiki.nn.ui.color.NNHeatMap;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static ch.kaiki.nn.ui.color.NNColor.blend;
import static javafx.scene.paint.Color.*;

public abstract class BaseChart {
    private static DecimalFormat df = new DecimalFormat("#.###");
    private final static double GRID_PADDING_OFFSET = 0.05; // 0.05 = 5% offset on each side -> grid size is 110% of data range
    protected double gridPaddingOffset = GRID_PADDING_OFFSET;
    protected final GraphicsContext context;
    protected final double width;
    protected final double height;
    protected double xMin;
    protected double xMax;
    protected double yMin;
    protected double yMax;
    protected double zMin;
    protected double zMax;
    protected double[][] projectionMatrix;
    protected double zoom = 1;
    protected double xAngle = 68;
    protected double zAngle = 46;
    protected boolean isInteractive = false;
    protected double dataScaling = 1;
    protected String[] axisLabels = new String[3];
    protected double resolution;
    protected double initResolution;
    protected NNChartColor chartColors;
    protected Color backgroundColor;
    protected Color axisColor;
    protected Color labelColor;
    protected double legendWidth;
    protected final static double OFFSET_BASE = 5;
    protected String title;

    protected boolean showBorder = false;
    protected boolean showTitle = false;
    protected boolean showGrid = true;
    protected boolean showGridContent = true;
    protected boolean showAxisLabels = false;
    protected boolean showTickMarkLabels = true;
    protected boolean showTickMarks = true;
    protected boolean showLegend = false;

    protected List<Series> series = new ArrayList<>();
    protected VisualizationMode mode = VisualizationMode.CUBE;
    public BaseChart(GraphicsContext context) {
        this.context = context;
        width = context.getCanvas().getWidth();
        height = context.getCanvas().getHeight();
        NNChartColor chartColors = new NNChartColor(TRANSPARENT, TRANSPARENT, DARKGRAY, LIGHTGRAY, LIGHTGRAY, DARKGRAY, DARKGRAY, DARKGRAY);
        //NNChartColor chartColors = new NNChartColor(TRANSPARENT, blend(LIGHTGRAY, TRANSPARENT, 0.25), DARKGRAY, GRAY, GRAY, DARKGRAY, GRAY, GRAY);
        setChartColors(chartColors);
        invalidate();
    }

    // --------------------------------------------- functional setters ---------------------------------------------

    public void setTitle(String text) {
        if (text != null && !text.equals("")) {
            this.title = text;
            showTitle = true;
        } else {
            this.title = null;
            showTitle = false;
        }
    }

    public void setAxisLabels(String... labels) {
        if (labels == null) {
            axisLabels = new String[3];
            showAxisLabels = false;
            return;
        }
        int length = Math.min(axisLabels.length, labels.length);
        boolean foundLabel = false;
        for (int i = 0; i < length; i++) {
            String label = labels[i];
            if (!(label == null || label.trim().equals(""))) {
                foundLabel = true;
            }
            axisLabels[i] = labels[i];
        }
        showAxisLabels = foundLabel;
    }

    public void showTickMarkLabels(boolean showTickMarkLabels) {
        this.showTickMarkLabels = showTickMarkLabels;
    }

    public void showTickMarks(boolean showTickMarks) {
        this.showTickMarks = showTickMarks;
    }

    public void showLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    // --------------------------------------------- setter for styling ---------------------------------------------

    public void showBorder(boolean showBorder) {
        this.showBorder = showBorder;
    }

    public void showGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public void showGridContent(boolean showGridContent) {
        this.showGridContent = showGridContent;
    }

    public void setChartColors(NNChartColor colors) {
        if (colors == null) {
            throw new NullPointerException("Colors must be set!");
        }
        this.chartColors = colors;
        this.backgroundColor = colors.getBackgroundColor();
        this.axisColor = colors.getAxisColor();
        this.labelColor = colors.getLabelColor();
    }


    public void setInnerDataPadding(double innerDataPadding) {  // will expand data if possible
        if (dataScaling < 1) {
            throw new IllegalArgumentException("Inner data padding must be equal to or greater than 1!");
        }
        dataScaling = innerDataPadding;
    }

    public void setOuterDataPadding(double outerDataPadding) {  // distance data to grid min/max
        if (outerDataPadding < 0 || outerDataPadding >= 1) {
            throw new IllegalArgumentException("Outer data padding must be equal to or greater than 1!");
        }
        this.gridPaddingOffset = outerDataPadding;
    }

    public void setVisualizationMode(VisualizationMode mode) {
        if (mode == null) {
            throw new NullPointerException("Visualization mode must not be null!");
        }
        this.mode = mode;
    }
    // --------------------------------------------- getter ---------------------------------------------

    public double getResolution() {
        return resolution;
    }

    public double getDataScaling() {
        return dataScaling;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getGlobalMinZ() {
        return zMin;
    }

    public double getGlobalMaxZ() {
        return zMax;
    }

    public GraphicsContext getContext() {
        return context;
    }
    public VisualizationMode getMode() {
        return mode;
    }
// --------------------------------------------- plots ---------------------------------------------
public void plotLine(NeuralNetwork neuralNetwork, Function<BackPropEntity, Double> function, String name, Color color, double smoothing) {
        if (smoothing < 0) {    // TODO: improve exception handling
            throw new IllegalArgumentException("Smoothing must be equal to or greater than 0.0!");
        }
    xAngle = 0;
    zAngle = 0;
    zoom = 1;
    boolean clear = false;
    Series id = null;
    for (Series s : series) {
        if (s instanceof DecisionBoundarySeries) {
            clear = true;
        }
    }
    if (clear) {
        series.clear();
    }
    LineSeries scatterSeries = new LineSeries(this, neuralNetwork, function, name, color, smoothing);
    int index = -1;
    for (int i = 0; i < series.size(); i++) {
        if (series.get(i) instanceof LineSeries) {
            List<String> oName = series.get(i).getName();
            if (oName.contains(name)) {
                index = i;
                break;
            }
        }
    }
    if (index == -1) {
        series.add(scatterSeries);
    } else {
        series.set(index, scatterSeries);
    }
    invalidate();
}
    public void scatter(NeuralNetwork neuralNetwork, Function<BackPropEntity, Double> function, String name, Color color) {
        xAngle = 0;
        zAngle = 0;
        zoom = 1;
        boolean clear = false;
        Series id = null;
        for (Series s : series) {
            if (s instanceof DecisionBoundarySeries) {
                clear = true;
            }
        }
        if (clear) {
            series.clear();
        }
        ScatterSeries scatterSeries = new ScatterSeries(this, neuralNetwork, function, name, color, 0);
        int index = -1;
        for (int i = 0; i < series.size(); i++) {
            if (series.get(i) instanceof ScatterSeries) {
                List<String> oName = series.get(i).getName();
                if (oName.contains(name)) {
                    index = i;
                    break;
                }
            }
        }
        if (index == -1) {
            series.add(scatterSeries);
        } else {
            series.set(index, scatterSeries);
        }
        invalidate();
    }

    public void plotDecisionBoundaries(NeuralNetwork neuralNetwork, double[][] inputData, NNHeatMap heatMap, double resolution) {
        plotDecisionBoundaries(neuralNetwork, inputData, null, false, null, heatMap, resolution);
    }
    public void plotDecisionBoundaries(NeuralNetwork neuralNetwork, double[][] inputData, double[][] outputData, boolean showData, NNHeatMap heatMap, double resolution) {
        plotDecisionBoundaries(neuralNetwork, inputData, outputData, showData, null, heatMap, resolution);
    }

    public void plotConfusionMatrix(NeuralNetwork neuralNetwork, NNHeatMap heatMap, boolean normalized) {
        int[] configuration = neuralNetwork.getConfiguration();
        if (configuration[0] != 2) {
            throw new IllegalArgumentException("Decision boundaries can only be plotted for 2-dimensional inputs!");
        }
        if (configuration[configuration.length - 1] > 1) {
            if (heatMap.getColors().size() < configuration[configuration.length-1]) {
                //throw new IllegalArgumentException("Data color items " + heatMap.getColors().size() + " must match output class dimensions " + configuration[configuration.length-1] + "!");
            }
        }
        this.series.clear();
        this.series.add(new ConfusionMatrixSeries(this,  neuralNetwork, heatMap, normalized));
        invalidate();
    }

    public void plotDecisionBoundaries(NeuralNetwork neuralNetwork, double[][] inputData, double[][] outputData, boolean showData, String[] legend, NNHeatMap heatMap, double resolution) {
        int[] configuration = neuralNetwork.getConfiguration();
        if (configuration[0] != 2) {
            throw new IllegalArgumentException("Decision boundaries can only be plotted for 2-dimensional inputs!");
        }
        if (configuration[configuration.length - 1] > 1) {
            if (heatMap.getColors().size() < configuration[configuration.length-1]) {
                throw new IllegalArgumentException("Data color items " + heatMap.getColors().size() + " must match output class dimensions " + configuration[configuration.length-1] + "!");
            }
        }
        if (legend != null && (legend.length != configuration[configuration.length-1] || (configuration[configuration.length-1] == 1 && (legend.length != 2)))) {
            throw new IllegalArgumentException("Feature label length does not match neural network configuration!");
        }
        if (legend != null) {
            this.showLegend = true;
        }
        this.initResolution = resolution;
        this.resolution = resolution;
        this.series.clear();
        this.series.add(new DecisionBoundarySeries(this,  neuralNetwork, inputData, outputData, showData, legend, heatMap));
        invalidate();
    }

    // --------------------------------------------- chart handling ---------------------------------------------

    protected void postInvalidate() {
        invalidateLegendDimensions();
    }

    protected final void invalidate() {
        //System.out.println(xAngle + " " + zAngle + " " + zoom);
        xMin = Double.MAX_VALUE;
        xMax = Double.MIN_VALUE;
        yMin = Double.MAX_VALUE;
        yMax = Double.MIN_VALUE;
        zMin = Double.MAX_VALUE;
        zMax = Double.MIN_VALUE;
        for (Series s : series) {
            s.compute();
            double xMin = s.getMinX();
            double xMax = s.getMaxX();
            double yMin = s.getMinY();
            double yMax = s.getMaxY();
            double zMin = s.getMinZ();
            double zMax = s.getMaxZ();

            if (xMin < this.xMin) {
                this.xMin = xMin;
            }
            if (xMax > this.xMax) {
                this.xMax = xMax;
            }
            if (yMin < this.yMin) {
                this.yMin = yMin;
            }
            if (yMax > this.yMax) {
                this.yMax = yMax;
            }
            if (zMin < this.zMin) {
                this.zMin = zMin;
            }
            if (zMax > this.zMax) {
                this.zMax = zMax;
            }
        }

        int initialization = 0;
        int xInitialization = (xMin != xMax && xMin != Double.MAX_VALUE && xMax != Double.MIN_VALUE) ? 1 : 0;
        int yInitialization = (yMin != yMax && yMin != Double.MAX_VALUE && yMax != Double.MIN_VALUE) ? 1 : 0;
        int zInitialization = (zMin != zMax && zMin != Double.MAX_VALUE && zMax != Double.MIN_VALUE) ? 1 : 0;

        if ((xInitialization + yInitialization) < 2) {
            xMin = 0;
            yMin = 0;
            zMin = 0;
            xMax = 1;
        }

        if (zInitialization == 0) {
            zMin = -0.5;
            zMax = 0.5;
        }

        postInvalidate();
        //System.out.println("xmin: " + xMin + ", xMax: " + xMax + ", ymin: " + yMin + ", ymax: " + yMax + ", zmin " + zMin + ", zmax: " + zMax);
        render();
    }

    protected final void render() {
        //Platform.runLater(() -> {
            setProjectionMatrix();  // customizable
            clear();

            preProcess();           // customizable

            if (showGrid) {
                renderGrid();
            }

            for (Series s: series) {
                s.render();
            }

            postProcess();          // customizable

            drawBorder();
        //});
    }

    private void invalidateLegendDimensions() {
        if (showLegend) {
            double maxCharLength = 0;
            for (Series s : series) {
                for (String n : s.getName()) {
                    if (n.length() > maxCharLength) {
                        maxCharLength = n.length();
                    }
                }
            }
            legendWidth = (maxCharLength * 5.5) + 40;

        } else {
            legendWidth = 0;
        }
    }

    protected void renderLegend(boolean renderAtBottomRight) {
        if (!showLegend) {
            return;
        }
        int count = 0;
        double maxCharLength = 0;
        final double stepSize = 24;
        for (Series s : series) {
            count += s.getName().size();
            for (String n : s.getName()) {
                if (n.length() > maxCharLength) {
                    maxCharLength = n.length();
                }
            }
        }
        final double colorOffset = 12;
        final double maxTextWidth = maxCharLength*5.5;
        final double labelBoxWidth = maxTextWidth + colorOffset + 16;

        double xColor = width-labelBoxWidth;
        double yColor = renderAtBottomRight ? height-(count*stepSize) : height-height/2-(count*stepSize)/2;
        double xText = xColor + colorOffset;
        double yText = yColor;
        context.setFill(blend(chartColors.getGridBackgroundColor(), TRANSPARENT, 0.2));
        context.fillRect(xColor-10, yColor-stepSize, labelBoxWidth, (count*stepSize)+stepSize/2);

        for (Series s : series) {
            List<String> names = s.getName();
            List<Color> colors = s.getColor();

            for (int i = 0; i < names.size(); i++) {

                context.setLineWidth(2);
                double radius = 8;
                context.setStroke(colors.get(i));
                context.strokeOval(xColor-radius/2, yColor-radius-0.5, radius, radius);

                Font font = context.getFont();
                context.setFont(new Font(null, 12.5));
                context.setTextAlign(TextAlignment.LEFT);
                context.setFill(labelColor);
                context.fillText(names.get(i), xText, yText, maxTextWidth);
                context.setFont(font);
                yColor += stepSize;
                yText += stepSize;
            }

        }
    }

    protected void preProcess() {
    }

    protected void postProcess() {
    }

    protected abstract void renderGrid();

    protected abstract List<Grid> getGrid();




    private void clear() {
        context.clearRect(0, 0, width, height);
        context.setFill(backgroundColor);
        context.fillRect(0, 0, width, height);
    }

    private void drawBorder() {
        if (showBorder) {
            context.setStroke(axisColor);
            double borderLineWidth = 2;
            context.setLineWidth(borderLineWidth);
            context.strokeLine(0, 0, width, 0);
            context.strokeLine(0, height, width, height);
            context.strokeLine(0, 0, 0, height);
            context.strokeLine(width, 0, width, height);
        }
    }

    protected void renderTitle() {
        if (showTitle) {
            context.setFill(labelColor);
            context.setTextAlign(TextAlignment.CENTER);
            Font font = context.getFont();
            context.setFont(new Font(null, 18));
            double titleOffset = 30;
            context.fillText(title, width / 2, titleOffset);
            context.setFont(font);
        }
    }

    public void enableMouseInteraction() {
        if (isInteractive) {
            return;
        }
        context.getCanvas().setOnMouseClicked(e -> {            // change resolution
            if (e.getButton() == MouseButton.SECONDARY) {
                double newResolution = resolution + 0.01;
                if (resolution < 1 && newResolution <= 1) {
                    resolution = newResolution;
                    invalidate();
                }
            } else if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                resolution = initResolution;
                invalidate();
            }
            e.consume();
        });
        isInteractive = true;
    }

    // --------------------------------------------- helper methods ---------------------------------------------

    protected double getInterval(double range, double threshold) {
        if (range <= threshold) {
            return calculateIntervalSmall(range);
        }
        return calculateIntervalLarge(range);
    }

    private double calculateIntervalSmall(double range) {
        double x = Math.pow(10.0, Math.floor(Math.log10(range)));
        if (range / x >= 5) {
            return x;
        } else if (range / (x / 2.0) >= 5) {
            return x / 2.0;
        }
        return x / 5.0;
    }

    private double calculateIntervalLarge(double range) {
        double x = Math.pow(10.0, Math.floor(Math.log10(range)));
        if (range / (x / 2.0) >= 10) {
            return x / 2.0;
        } else if (range / (x / 5.0) >= 10) {
            return x / 5.0;
        }
        return x / 10.0;
    }

    protected String formatTickLabel(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)){
            return "" + value;
        }

        double formatted = Double.parseDouble(df.format(value));
        if (formatted % 1 == 0) {
            return Integer.toString((int) formatted);
        }
        return Double.toString(formatted);
    }

    // --------------------------------------------- matrix op ---------------------------------------------

    protected abstract void setProjectionMatrix();

    public double[] transform(double[] v) {
        return lower(multiply(projectionMatrix, lift(v)));
    }

    double[][] lift(double[][] m) {
        return new double[][] {{m[0][0], m[0][1], m[0][2], 0}, {m[1][0], m[1][1], m[1][2], 0}, {m[2][0], m[2][1], m[2][2], 0}, {0, 0, 0, 1}};
    }

    private double[] lift(double[] v) {
        double zMin = 4;
        return new double[] {v[0]*zMin, v[1]*zMin, v[2]*zMin, zMin};
    }

    private double[] lower(double[] v) {
        double val = v[3];
        if (val == 0) {
            val = 0.001;
        }
        return new double[] {v[0]/val, v[1]/val, v[2]/val, val};
    }

    double[][] xRotation(double angle) {
        angle = Math.toRadians(angle);
        return new double[][]{{1, 0, 0},
                {0, Math.cos(angle), Math.sin(angle)},
                {0, -Math.sin(angle), Math.cos(angle)}};
    }

    private double[][] yRotation(double angle) {
        angle = Math.toRadians(angle);
        return new double[][]{{Math.cos(angle), 0, -Math.sin(angle)},
                {0, 1, 0},
                {Math.sin(angle), 0, Math.cos(angle)}};
    }

    double[][] zRotation(double angle) {
        angle = Math.toRadians(angle);
        return new double[][]{{Math.cos(angle), -Math.sin(angle), 0},
                {Math.sin(angle), Math.cos(angle), 0},
                {0, 0, 1}};
    }

    double[][] translate(double x, double y, double z) {
        return new double[][] {{1,0,0,x},
                {0,1,0,y},
                {0,0,1,z},
                {0,0,0,1}};
    }

    double[][] scale(double x, double y, double z) {
        return new double[][] {{x,0,0,0},
                {0,y,0,0},
                {0,0,z,0},
                {0,0,0,1}};
    }

    double[][] xReflect() {
        return new double[][] {{1,0,0,0},
                {0,-1,0,0},
                {0,0,1,0},
                {0,0,0,1}};
    }

    double[][] multiply(double[][] a, double[][] b) {     // outer arr is line
        double[][] tmp = new double[a.length][b[0].length];
        for (int i = 0; i < tmp.length; i++) {
            for (int j = 0; j < tmp[0].length; j++) {
                double sum = 0;
                for (int k = 0; k < a[0].length; k++) {
                    double sideA = a[i][k];
                    double sideB = b[k][j];
                    sum += sideA * sideB;
                }
                tmp[i][j] = sum;
            }
        }
        return tmp;
    }

    private double[] multiply(double[][] a, double[] b) {     // outer arr is line
        double[] tmp = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            double sum = 0;
            for (int j = 0; j < a[0].length; j++) {
                double sideA = a[i][j];
                double sideB = b[j];
                sum += sideA * sideB;
            }
            tmp[i] = sum;
        }
        return tmp;
    }

    private double[][] add(double[][] a, double[][] b) {
        if (a.length != b.length || a[0].length != b[0].length) {
            throw new IllegalArgumentException("wrong input dimensions for addition!");
        }
        double[][] tmp = new double[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                double sideA = a[i][j];
                double sideB = b[i][j];
                double value = sideA + sideB;
                tmp[i][j] = value;
            }
        }
        return tmp;
    }

    private double[] crossProduct(double[] a, double[] b) {
        return new double[] {a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0]};
    }

    private double norm(double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    private double[] divide(double[] v, double s) {
        if (s == 1) {
            return v;
        }
        return new double[] {v[0]/s, v[1]/s, v[2]/s};
    }

    private double[] negate(double[] v) {
        return new double[] {-v[0], -v[1], -v[2]};
    }

    double[][] baseProjection(double[] vector) {
        double[] p = divide(vector , norm(vector));
        double[] n = negate(p);
        double[] w = {0,1,0};
        double norm = norm(crossProduct(w,p));
        double[] u = norm == 1 ? crossProduct(w,p) : divide(crossProduct(w,p), norm);
        double[] v = negate(crossProduct(n,u));
        return new double[][] {{u[0], u[1], u[2], 0}, {v[0], v[1], v[2], 0}, {n[0], n[1], n[2], 0}, {0,0,0,1}};
    }

    double[][] centralProjection() {
        double zMin = -1;
        return new double[][] {{1,0,0,0},{0,1,0,0},{0,0,0,0},{0,0,1/zMin,1}};
    }

}
