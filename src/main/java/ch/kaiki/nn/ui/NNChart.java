package ch.kaiki.nn.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NNChart extends Pane {
    private static DecimalFormat df = new DecimalFormat("#.###");
    private NNChart chart;

    private double titleSpace = 12;
    private double tickMarkSpace = 4;
    private double tickMarkLabelSpace = 20;
    private double axisLabelSpace = 24;

    private double scaling = 0.8;

    private SimpleDoubleProperty wOffsetLeft = new SimpleDoubleProperty(0);
    private SimpleDoubleProperty wOffsetRight = new SimpleDoubleProperty(50);
    private SimpleDoubleProperty hOffsetTop = new SimpleDoubleProperty(50);
    private SimpleDoubleProperty hOffsetBottom = new SimpleDoubleProperty(0);

    private double axisStrokeWidth = 1.2;
    private double gridStrokeWidth = 0.5;
    private double tickStrokeWidth = 1.2;
    private double tickLength = 4;

    private Color backgroundColor = Color.TRANSPARENT;
    private Color chartBackgroundColor = Color.TRANSPARENT;
    private Color axisColor = Color.LIGHTGRAY.darker();
    private Color gridColor = Color.LIGHTGRAY;
    private Color labelColor = Color.LIGHTGRAY.darker();
    // TODO: textcolor

    private Font titleFont;
    private Font axisLabelFont = new Font(null, 12.5);
    private Font tickLabelFont = new Font(null, 11);

    private boolean showTickMarks;
    private boolean showTickLabels;
    private boolean showGrid;

    private ChartArea chartArea;

    Axis xAxis;
    Axis yAxis;

    public NNChart(double width, double height, boolean showTickMarks, boolean showTickLabels, boolean showGrid, double scaling) {
        chart = this;
        this.showTickMarks = showTickMarks;
        if (showTickMarks) {
            wOffsetLeft.set(wOffsetLeft.get() + tickMarkSpace);
            hOffsetBottom.set(hOffsetBottom.get() + tickMarkSpace);
        }
        if (showTickLabels) {
            wOffsetLeft.set(wOffsetLeft.get() + tickMarkLabelSpace);
            hOffsetBottom.set(hOffsetBottom.get() + tickMarkLabelSpace);
        }
        this.showTickLabels = showTickLabels;
        this.showGrid = showGrid;
        this.scaling = scaling;
        this.setMinWidth(width);
        this.setWidth(width);
        this.setMaxWidth(width);
        this.setMinHeight(height);
        this.setHeight(height);
        this.setMaxHeight(height);
        this.setClip(new Rectangle(0, 0, width, height));
        this.setBackground(new Background(new BackgroundFill(backgroundColor, null, null)));

        this.chartArea = new ChartArea();
        xAxis = new Axis(Orientation.HORIZONTAL);
        yAxis = new Axis(Orientation.VERTICAL);
    }

    private boolean hasData() {
        return chartArea.series != null;
    }

    public void plot(List<Point> data) {
        chartArea.addData(data);
        xAxis.buildAxis();
        yAxis.buildAxis();
        chartArea.renderData();
    }

    public void setXAxisLabel(String label) {
        if (label == null || label.equals("")) {
            if (xAxis.axisLabel != null && !xAxis.axisLabel.getText().equals("")) {
                xAxis.axisLabel.setText("");
                hOffsetBottom.set(hOffsetBottom.get() - axisLabelSpace);
            }
        } else {
            hOffsetBottom.set(hOffsetBottom.get() + axisLabelSpace);
            xAxis.axisLabel.setText(label);
        }
        invalidateChart();
    }

    public void setYAxisLabel(String label) {
        if (label == null || label.equals("")) {
            if (yAxis.axisLabel != null && !yAxis.axisLabel.getText().equals("")) {
                yAxis.axisLabel.setText("");
                wOffsetLeft.set(wOffsetLeft.get() - axisLabelSpace);
            }
        } else {
            wOffsetLeft.set(wOffsetLeft.get() + axisLabelSpace);
            yAxis.axisLabel.setText(label);
        }
        invalidateChart();
    }

    private void invalidateChart() {
        chartArea.invalidate();
        xAxis.buildAxis();
        yAxis.buildAxis();
        chartArea.renderData();
    }

    private void setWidth(Pane node, double value) {
        node.setMinWidth(value);
        node.setMaxWidth(value);
        node.setPrefWidth(value);
    }

    private void setHeight(Pane node, double value) {

    }
    private class ChartArea extends Pane {
        Series series;
        Pane dataPane;
        ChartArea() {
            chart.getChildren().add(this);
            this.setBackground(new Background(new BackgroundFill(chartBackgroundColor, null, null)));
            this.setStyle("-fx-border-width: " + axisStrokeWidth + ";-fx-border-color: " + format(axisColor) +";");
            dataPane = new Pane();
            this.getChildren().add(dataPane);
            this.minWidthProperty().bind(Bindings.createDoubleBinding(() -> chart.getWidth() - wOffsetLeft.get() - wOffsetRight.get(), wOffsetLeft, wOffsetRight));
            this.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> chart.getWidth() - wOffsetLeft.get() - wOffsetRight.get(), wOffsetLeft, wOffsetRight));
            this.minHeightProperty().bind(Bindings.createDoubleBinding(() -> chart.getHeight() - hOffsetTop.get() - hOffsetBottom.get(), hOffsetTop, hOffsetBottom));
            this.maxHeightProperty().bind(Bindings.createDoubleBinding(() -> chart.getHeight() - hOffsetTop.get() - hOffsetBottom.get(), hOffsetTop, hOffsetBottom));
            this.translateXProperty().bind(wOffsetLeft);
            this.translateYProperty().bind(hOffsetTop);
            invalidate();
        }

        void invalidate() {
            double width = chart.getWidth() - wOffsetLeft.get() - wOffsetRight.get();
            double height = chart.getHeight() - hOffsetTop.get() - hOffsetBottom.get();
            this.setWidth(width);
            this.setHeight(height);
            dataPane.setClip(new Rectangle(0, 0, width, height));
            dataPane.getChildren().clear();
        }

        void renderData() {
            if (series != null) {
                series.render();
            }
        }

        public void addData(List<Point> data) {
            series = new Series(dataPane, data);
        }

    }

    private class Axis extends Pane {
        Orientation orientation;
        Label axisLabel = new Label();

        double min;
        double max;

        double chartDimension;
        double axisLocation;

        Axis(Orientation orientation) {
            chartArea.getChildren().add(this);
            axisLabel.setAlignment(Pos.CENTER);
            axisLabel.setFont(axisLabelFont);
            axisLabel.setTextFill(labelColor);
            this.orientation = orientation;

        }

        private void invalidate() {
            if (!hasData()) {
                return;
            }

            if (orientation == Orientation.HORIZONTAL) {
                //this.setStyle("-fx-border-color: green; -fx-background-color: transparent;-fx-border-width: 2");
                this.setWidth(chartArea.getWidth());
                this.setMinWidth(chartArea.getWidth());
                this.setMaxWidth(chartArea.getWidth());
                this.setHeight(hOffsetBottom.get());
                this.setMinHeight(hOffsetBottom.get());
                this.setMaxHeight(hOffsetBottom.get());
                this.setTranslateY(chartArea.getHeight());
                //this.setTranslateY(10);
                min = chartArea.series.xMin;
                max = chartArea.series.xMax;
                chartDimension = chartArea.getWidth();
                axisLocation = chartArea.getHeight();

                axisLabel.setRotate(0 + this.getRotate());
                axisLabel.setTranslateY(hOffsetBottom.get() - axisLabelSpace);
            } else {
                //this.setStyle("-fx-border-color: green; -fx-background-color: limegreen;-fx-border-width: 2");
                this.setWidth(wOffsetLeft.get());
                this.setMinWidth(wOffsetLeft.get());
                this.setMaxWidth(wOffsetLeft.get());
                this.setHeight(chartArea.getHeight());
                this.setMinHeight(chartArea.getHeight());
                this.setMaxHeight(chartArea.getHeight());
                this.setTranslateX(-wOffsetLeft.get());
                min = chartArea.series.yMin;
                max = chartArea.series.yMax;
                chartDimension = chartArea.getHeight();
                axisLocation = wOffsetLeft.get();
                axisLabel.setRotate(-90 + this.getRotate());
                axisLabel.setTranslateX(-chartDimension/2+axisLabelSpace/2);
                axisLabel.setTranslateY(chartDimension/2-axisLabelSpace/2);
            }
            axisLabel.setMinWidth(chartDimension);
            axisLabel.setMaxWidth(chartDimension);
            //axisLabel.setStyle("-fx-border-color: red");

        }

        void buildAxis() {
            if (!hasData()) {
                return;
            }
            this.getChildren().clear();
            this.getChildren().add(axisLabel);
            invalidate();
            double scaledRange = Math.abs(max-min) / scaling;
            double tickDistance = chartDimension <= 350 ? calculateIntervalSmall(scaledRange) : calculateIntervalLarge(scaledRange);
            double startingPoint = min - (min % tickDistance) - tickDistance - (scaledRange - Math.abs(max - min))/2;
            double tick = orientation == Orientation.HORIZONTAL ? transformXCoord(startingPoint, min, max, chartDimension) : transformYCoord(startingPoint, min, max, chartDimension);
            int tickCount = (int) (scaledRange / tickDistance) + 2;
            if (orientation == Orientation.HORIZONTAL) {
                for (int i = 0; i < tickCount; i++) {
                    if (i > 0) {
                        tick = transformXCoord(startingPoint + i * tickDistance, min, max, chartDimension);
                    }
                    if (tick <= 0 || tick >= chartDimension) {
                        continue;
                    }
                    if (showTickMarks) {
                        drawLine(this, tick, 0, tick, 0 + tickLength, tickStrokeWidth, axisColor);
                        if (showTickLabels)
                            drawText(this, formatTickLabel(startingPoint + tickDistance * i), tick - 12, 0 + tickLength + 2, Pos.CENTER);
                    }
                    if (showGrid) {
                        drawLine(this, tick, -axisLocation, tick, 0, gridStrokeWidth, gridColor);
                    }
                }
            } else {
                for (int i = 0; i < tickCount; i++) {
                    if (i > 0) {
                        tick = transformYCoord(startingPoint + i * tickDistance, min, max, chartDimension);
                    }
                    if (tick <= 0 || tick >= chartDimension) {
                        continue;
                    }
                    if (showTickMarks) {
                        drawLine(this, axisLocation, tick, axisLocation - tickLength, tick, tickStrokeWidth, axisColor);
                        if (showTickLabels) {
                            drawText(this, formatTickLabel(startingPoint + tickDistance * i), axisLocation - 28 - tickLength, tick - 10, Pos.CENTER_RIGHT);
                        }
                    }
                    if (showGrid) {
                        drawLine(this, axisLocation, tick, chartArea.getWidth()+axisLocation, tick, gridStrokeWidth, gridColor);
                    }
                }
            }
        }
    }

    private void drawLine(Pane parent, double x0, double y0, double x1, double y1, double strokeWidth, Color color) {
        Line line = new Line(x0, y0, x1,y1);
        line.setStrokeWidth(strokeWidth);
        line.setStroke(color);
        parent.getChildren().add(line);
    }

    private void drawText(Pane parent, String value, double x, double y, Pos position) {
        Label label = new Label(value);
        label.setMinWidth(24);
        label.setFont(tickLabelFont);
        label.setTextFill(axisColor);
        //label.setStyle("-fx-border-color: red");
        label.setTranslateX(x);
        label.setAlignment(position);
        label.setTranslateY(y);
        parent.getChildren().add(label);
    }

    private String formatTickLabel(double value) {
        double formatted = Double.parseDouble(df.format(value));
        if (formatted % 1 == 0) {
            return Integer.toString((int) formatted);
        }
        return Double.toString(formatted);
    }

    private class Series {
        Pane pane;
        double xMin;
        double xMax;
        double yMin;
        double yMax;
        List<Point> data = new ArrayList<>();

        private Series(Pane pane, List<Point> data) {
            this.pane = pane;
            for (Point point : data) {
                double x = point.x;
                double y = point.y;
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
                this.data.add(point);
            }
            transform();
        }

        private void render() {
            pane.getChildren().addAll(data);
        }

        private void transform() {
            double xRange = Math.abs(xMax - xMin);
            double yRange = Math.abs(yMax - yMin);
            double chartWidth = chartArea.getWidth() * scaling;
            double chartHeight = chartArea.getHeight() * scaling;
            double xOffset = -(chartWidth - chartArea.getWidth()) / 2;
            double yOffset = -(chartHeight - chartArea.getHeight()) / 2;
            for (Point point : data) {
                point.transform(xRange, yRange, chartWidth, chartHeight, xOffset, yOffset);
            }
        }
    }

    private double transformXCoord(double x, double dataMin, double dataMax, double chartDimension) {
        double range = Math.abs(dataMax - dataMin);
        return x / (range / scaling) * chartDimension + (chartDimension - chartDimension*scaling)/2;
    }

    private double transformYCoord(double x, double dataMin, double dataMax, double chartDimension) {
        double range = Math.abs(dataMax - dataMin);
        return chartDimension- (x / (range / scaling) * chartDimension + (chartDimension - chartDimension*scaling)/2);
    }
    private String format(Color color) {
        return color.toString().replaceAll("0x", "#");
    }
    public static class Point extends Circle {
        double x;
        double y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
            this.setRadius(5);
        }

        void transform(double xRange, double yRange, double chartWidth, double chartHeight, double xOffset, double yOffset) {
            this.setCenterX((x / xRange * chartWidth + xOffset));
            this.setCenterY((-y / yRange * chartHeight) + chartHeight + yOffset);
        }
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

}
