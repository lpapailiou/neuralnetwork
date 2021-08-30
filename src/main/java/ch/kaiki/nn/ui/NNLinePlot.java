package ch.kaiki.nn.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import ch.kaiki.nn.data.BackPropEntity;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.NNPlotColor;

import java.util.*;
import java.util.function.Function;

import static javafx.scene.paint.Color.*;
import static ch.kaiki.nn.ui.color.NNColorSupport.blend;

public class NNLinePlot extends LineChart {

    private boolean isSceneSet = false;
    private Group parent;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private double smoothing;
    private List<Color> seriesColor = new ArrayList<>();

    private Color backgroundColor = TRANSPARENT;
    private Color plotBackgroundColor = blend(LIGHTGRAY.brighter(), TRANSPARENT, 0.05);
    private Color axisColor = LIGHTGRAY.darker();
    private Color tickColor = LIGHTGRAY;
    private Color gridColor = LIGHTGRAY;
    private Color textColor = LIGHTGRAY.darker();
    private double titleSize = 1.4;
    private double labelFontSize = 1.2;
    private double dataStrokeWidth = 2.5;
/*
    private SimpleDoubleProperty minValue = new SimpleDoubleProperty(Double.MAX_VALUE);
    private SimpleDoubleProperty maxValue = new SimpleDoubleProperty(Double.MIN_VALUE);
    private SimpleDoubleProperty tickUnit = new SimpleDoubleProperty(0);*/


    public NNLinePlot(Pane parent, double width, double height, boolean showTicks, boolean showGrid, boolean showLegend, double smoothing) {
        super(new NumberAxis(), new NumberAxis());
        if (smoothing < 0 || smoothing > 1) {
            throw new IllegalArgumentException("Smoothing factor must be between 0.0 and 1.0!");
        }
        this.smoothing = smoothing;
        xAxis = (NumberAxis) this.getXAxis();
        yAxis = (NumberAxis) this.getYAxis();


/*
        xAxis.setAutoRanging(false);
        xAxis.lowerBoundProperty().bind(minValue);
        xAxis.upperBoundProperty().bind(maxValue);
        xAxis.tickUnitProperty().bind(tickUnit);*/

        xAxis.setTickMarkVisible(showTicks);
        yAxis.setTickMarkVisible(showTicks);
        xAxis.setTickLabelsVisible(showTicks);
        yAxis.setTickLabelsVisible(showTicks);

        if (!showGrid) {
            this.gridColor = TRANSPARENT;
        }
        this.setLegendVisible(showLegend);
        this.setCreateSymbols(false);
        this.setMinWidth(width);
        this.setMaxWidth(width);
        this.setMinHeight(height);
        this.setMaxHeight(height);
        this.setLegendSide(Side.BOTTOM);
        this.setAnimated(true);
        handleParent(parent);
    }

    public NNLinePlot(Pane parent, double width, double height, boolean showTicks, boolean showGrid, boolean showLegend, double smoothing, NNPlotColor colorPalette) {
        this(parent, width, height, showTicks, showGrid, showLegend, smoothing);
        this.backgroundColor = colorPalette.getBackgroundColor();
        this.plotBackgroundColor = colorPalette.getPlotBackgroundColor();
        this.axisColor = colorPalette.getAxisColor();
        this.gridColor = colorPalette.getGridColor();
        this.tickColor = colorPalette.getTickColor();
        this.gridColor = colorPalette.getGridColor();
        this.textColor = colorPalette.getTextColor();
    }

    public void plot(NeuralNetwork neuralNetwork, Function<BackPropEntity, Double> function, String name, Color color) {
        seriesColor.add(color);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        addSeriesListener(series);
        series.setName(name);


        SortedMap<Integer, BackPropEntity> rawData = neuralNetwork.getBackPropData().getMap();
        int size = rawData.size();
        int modulo = (int) (rawData.size() / 100 * smoothing * 100);
        modulo = Math.max(1, modulo);
        int counter = 0;
        double x = 0;
        double y = 0;
        for (Integer key : rawData.keySet()) {
            int batch = counter % modulo;
            x = (double) key;
            y += function.apply(rawData.get(key));
            if (counter == 0 || counter == size -1 || batch == 0) {
                XYChart.Data data =  new XYChart.Data<>(x, batch == 0 ? y/modulo : y / batch);

                series.getData().add(data);
                y = 0;
            }
            counter++;
        }
        this.getData().addAll(series);
        updateColors();
        /*

        minValue.set(Math.min(min, minValue.doubleValue()));
        maxValue.set(Math.max(max, maxValue.doubleValue()));
        tickUnit.set(Math.abs(minValue.get() - maxValue.get()) / this.getMinWidth() * 50);*/
        double min = series.getData().stream().mapToDouble(n -> (double) n.getXValue()).min().orElse(0);
        double max =series.getData().stream().mapToDouble(n -> (double) n.getXValue()).max().orElse(0);
        xAxis.invalidateRange(Arrays.asList(min, 100));
    }

    public void plot(Function<Double, Double> function, String name, Color color) {
        seriesColor.add(color);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        addSeriesListener(series);
        int iter = 20;
        for (int i = 0; i < iter; i++) {
            double x = (i - (iter / 2.)) / 10;
            double y = function.apply(x);
            //System.out.println("x " + x + " y " + y);
            series.getData().add(new XYChart.Data<>(x, y));
        }
        this.getData().add(series);
    }

    public void setXAxisLabel(String label) {
        xAxis.setLabel(label);
    }

    public void setYAxisLabel(String label) {
        yAxis.setLabel(label);
    }

    public void setDataStrokeWidth(double value) {
        this.dataStrokeWidth = value;
    }

    public void setTitleFontSize(double size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Title font size must be greater than 0!");
        }
        this.titleSize = size;
        if (isSceneSet) {
            updateTitleFontSize();
        }
    }

    public void setLabelFontSize(double size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Label font size must be greater than 0!");
        }
        this.labelFontSize = size;
        if (isSceneSet) {
            updateLabelFontSize();
        }
    }

    private void handleParent(Pane pane) {
        parent = new Group(this);
        pane.getChildren().add(parent);
        parent.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.windowProperty().addListener((observable1, oldValue1, newValue1) -> {
                    isSceneSet = true;
                    updateColors();
                    updateTitleFontSize();
                    updateLabelFontSize();
                    initializeBaseStyle();
                });
            }
        });
    }

    private void addSeriesListener(XYChart.Series series) {
        series.nodeProperty().addListener((observableValue, oldValue, newValue) -> {
            String style = "-fx-padding: 0px;-fx-background-color: " + format(backgroundColor) + ";";
            for (int i = 0; i < seriesColor.size(); i++) {
                style += "CHART_COLOR_" + (i + 1) + ": " + format(seriesColor.get(i)) + ";";
            }
            this.setStyle(style);
        });

    }

    private void updateTitleFontSize() {
        setStyleProperties(".chart-title", "-fx-font-size: " + titleSize + "em;-fx-text-fill: " + format(textColor) + ";");
    }

    private void updateLabelFontSize() {
        setStyleProperties(".chart-legend-item", "-fx-font-size: " + labelFontSize + "em;-fx-text-fill: " + format(textColor) + ";");
        setStyleProperties(".axis", "-fx-font-size: " + labelFontSize + "em;-fx-tick-label-fill: " + format(textColor) + ";");
    }
    private void updateColors() {
        setStyleProperties(".chart-plot-background", "-fx-border-width: 0.3px; -fx-background-color: " + format(plotBackgroundColor) + ";-fx-border-color: " + format(axisColor) + ";");
        setStyleProperties(".chart-vertical-zero-line", "-fx-stroke-width: 0.75px; -fx-stroke: " + format(gridColor) + ";");
        setStyleProperties(".chart-horizontal-zero-line", "-fx-stroke-width: 0.75px; -fx-stroke: " + format(gridColor) + ";");
        setStyleProperties(".chart-vertical-grid-lines", "-fx-stroke-width: 0.3; -fx-stroke: " + format(gridColor) + "; -fx-stroke-dash-array: 1 0 1 0;");
        setStyleProperties(".chart-horizontal-grid-lines", "-fx-stroke-width: 0.3; -fx-stroke: " + format(gridColor) + "; -fx-stroke-dash-array: 1 0 1 0;");
        setStyleProperties(".axis-label", "-fx-text-fill: " + format(textColor) + ";");
        setStyleProperties(".axis-tick-mark", "-fx-stroke: " + format(tickColor) + ";");
    }

    private void initializeBaseStyle() {
        setStyleProperties(".chart", "-fx-padding: 0px;");
        setStyleProperties(".chart-content", "-fx-padding: 0px;");
        //setStyleProperties(".chart-plot-background", "-fx-padding: 20px; -fx-background-insets: 0,0,0,0; -fx-background-color:pink;");
        setStyleProperties(".chart-alternative-row-fill", "-fx-fill: transparent;-fx-stroke: transparent;-fx-stroke-width: 0;");
        setStyleProperties(".chart-legend", "-fx-background-color: transparent;-fx-padding: 10px;");
        setStyleProperties(".axis-minor-tick-mark", "-fx-stroke: transparent;");
        setStyleProperties(".chart-series-line", "-fx-stroke-width: " + dataStrokeWidth + "px;-fx-effect: null;");
    }

    private void setStyleProperties(String selector, String value) {
        Set<Node> nodes = parent.lookupAll(selector);
        for (Node node : nodes) {
            node.setStyle(value);
        }
    }

    private String format(Color color) {
        return color.toString().replaceAll("0x", "#");
    }

}
