package ch.kaiki.nn.ui.util;

import ch.kaiki.nn.ui.deprecated.NNChart;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.*;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;

import java.util.*;

public class ChartArea extends Pane {

    Group dataWrapper = new Group();
    List<Series> series = new ArrayList<>();
    LabelBox labelBox;
    double scaling;

    NNChart parent;

    NumberAxis xAxis;
    NumberAxis yAxis;
    SimpleDoubleProperty minX2 = new SimpleDoubleProperty(Double.MAX_VALUE);

    SimpleDoubleProperty scaledMinX = new SimpleDoubleProperty(0);
    SimpleDoubleProperty scaledMaxX = new SimpleDoubleProperty(0);
    SimpleDoubleProperty scaledMinY = new SimpleDoubleProperty(0);
    SimpleDoubleProperty scaledMaxY = new SimpleDoubleProperty(0);

    SimpleDoubleProperty tickUnitsX = new SimpleDoubleProperty(0);
    SimpleDoubleProperty tickUnitsY = new SimpleDoubleProperty(0);

    public ChartArea(double scaling) {
        SubScene subScene = new SubScene(dataWrapper, 0, 0, true, SceneAntialiasing.BALANCED);
        this.widthProperty().addListener(e -> {
            subScene.setWidth(this.getWidth());
        });
        this.heightProperty().addListener(e -> {
            subScene.setHeight(this.getHeight());
            //System.out.println(dataWrapper.layoutBoundsProperty());
        });

        this.parentProperty().addListener((o, oldv, newv) -> {
            if (newv instanceof NNChart) {
                parent = (NNChart) newv;
            }
        });
        PerspectiveCamera camera = new PerspectiveCamera(false);
        subScene.setCamera(camera);

        subScene.setFill(Color.TRANSPARENT);

        this.getChildren().add(subScene);
        this.scaling = scaling;

        this.prefWidthProperty().addListener(e -> {
            this.setWidth(getPrefWidth());
        });
        this.prefHeightProperty().addListener(e -> {
            setHeight(getPrefHeight());
        });

    }

    public void setXAxis(NumberAxis axis) {
        xAxis = axis;
        xAxis.lowerBoundProperty().bind(scaledMinX);
        xAxis.upperBoundProperty().bind(scaledMaxX);
        xAxis.tickUnitProperty().bind(tickUnitsX);
        //xAxis.setAutoRanging(true);
        xAxis.tickUnitProperty().addListener(e -> {
        });
    }
    public void setYAxis(NumberAxis axis) {
        yAxis = axis;
        yAxis.lowerBoundProperty().bind(scaledMinY);
        yAxis.upperBoundProperty().bind(scaledMaxY);
        yAxis.tickUnitProperty().bind(tickUnitsY);

    }

    public void setLabelBox(LabelBox labelBox) {
        this.labelBox = labelBox;
    }

    public void addData(Series seriesInstance) {
        series.add(seriesInstance);
        labelBox.add(seriesInstance);
    }

    private void calculateBoundaries() {
        double minX = getMinX();
        double maxX = getMaxX();
        double minY = getMinY();
        double maxY = getMaxY();

        double rangeX = Math.abs(maxX - minX);
        double rangeY = Math.abs(maxY - minY);

        double scaledRangeX = rangeX / scaling;
        double scaledRangeY = rangeY / scaling;

        double offsetX = (scaledRangeX - rangeX) / 2;
        double offsetY = (scaledRangeY - rangeY) / 2;

        double tickDistanceX = this.getWidth() <= 350 ? calculateIntervalSmall(scaledRangeX) : calculateIntervalLarge(scaledRangeX);
        double tickDistanceY = this.getHeight() <= 350 ? calculateIntervalSmall(scaledRangeY) : calculateIntervalLarge(scaledRangeY);

        //min.get() - (min.get() % tickDistance)
        scaledMinX.setValue(minX - offsetX);
        scaledMinX.setValue(minX - minX % tickDistanceX - offsetX);
        scaledMaxX.setValue(maxX + offsetX);
        scaledMinY.setValue(minY - minY % tickDistanceY - offsetY);
        scaledMaxY.setValue(maxY + offsetY);

        tickUnitsX.setValue(tickDistanceX);
        tickUnitsY.setValue(tickDistanceY);
    }

    private List<Line> getGrid() {
        List<Line> grid = new ArrayList<>();

        double xRange = Math.abs(scaledMinX.get() - scaledMaxX.get());
        double yRange = Math.abs(scaledMinY.get() - scaledMaxY.get());
        double chartWidth = this.getWidth();
        double chartHeight = this.getHeight();

        double xTickUnit = tickUnitsX.get();
        double yTickUnit = tickUnitsY.get();
        int xTickCount = (int) Math.ceil(xRange / xTickUnit);
        int yTickCount = (int) Math.ceil(yRange / yTickUnit);

        double stepX = transformX(xTickUnit, xRange, chartWidth);
        double stepY = Math.abs(transformY(yTickUnit, yRange, chartHeight));
        double axisValueX = scaledMinX.get();
        double axisValueY = scaledMinY.get();
        for (int i = 0; i <= xTickCount; i++) {
            double x = i * stepX;
            Line line = new Line(x, 0, x, chartHeight);
            line.setStroke(Color.LIGHTGRAY.darker());
            line.setStrokeWidth(0.5);
            if (axisValueX == 0) {
                line.setStrokeWidth(1);
            }
            grid.add(line);
            axisValueX += xTickUnit;
        }

        for (int i = 0; i <= yTickCount; i++) {
            double y = chartHeight - i * stepY;
            Line line = new Line(0, y, chartWidth, y);
            line.setStroke(Color.LIGHTGRAY.darker());
            line.setStrokeWidth(0.5);
            if (axisValueY == 0) {
                line.setStrokeWidth(1);
            }
            grid.add(line);
            axisValueY += yTickUnit;
        }
        return grid;
    }

    private double transformX(double x, double range, double chartWidth) {
        return (x / range * chartWidth);
    }

    private double transformY(double y, double range, double chartHeight) {
        return (y / range * chartHeight);
    }

    public void render() {

        calculateBoundaries();

        dataWrapper.getChildren().clear();
        dataWrapper.getChildren().addAll(getGrid());

        if (parent == null) {
            return;
        }
        List<Node> nodesToRemove = new ArrayList<>();
        for (Node node : parent.getChildren()) {
            if (node.getId() != null && node.getId().equals("data")) {
                nodesToRemove.add(node);
            }
        }
        for (Node node : nodesToRemove) {
            parent.getChildren().remove(node);
        }
        ObservableList<Axis.TickMark<Number>> list = xAxis.getTickMarks();
        list.addListener((ListChangeListener)(c -> {
            System.out.println(xAxis.getTickMarks());
            /* ... */
        }));

        double width = this.getWidth();
        double height = this.getHeight();
        //dataWrapper.setClip(new Rectangle(0,0,width,height));
        double xRange = Math.abs(scaledMaxX.get() - scaledMinX.get());
        double yRange = Math.abs(scaledMaxY.get() - scaledMinY.get());

        double xOffset = yAxis.getWidth();
        double yOffset = +parent.gethOffset();
        PlotType type = PlotType.POLYGON;
        for (Series s : series) {
            double oldx = 0;
            double oldy = 0;
            s.transform(width, height, xRange, yRange);
            if (type == PlotType.LINE) {
                Collections.sort(s.getData(), Comparator.comparingDouble(Point::getX));
            }
            for (int i = 0; i < s.getData().size(); i++) {
                Point point = s.getData().get(i);
                double x = point.transformedX+xOffset;
                double y = point.transformedY+yOffset;

                switch (type) {
                    case LINE:
                        if (s.getData().size() <= 1) {
                            break;
                        }
                        if (i == 0) {
                            oldx = x;
                            oldy = y;
                            continue;
                        }
                        Line line = new Line(oldx, oldy, x, y);
                        line.setStrokeWidth(5);
                        line.setStroke(s.getColor());
                        line.setId("data");
                        parent.getChildren().add(line);
                        oldx = x;
                        oldy = y;
                        break;
                    default:
                        Sphere sphere = new Sphere();
                        //circle.setFill(s.getColor());
                        sphere.setId("data");
                        PhongMaterial material = new PhongMaterial();
                        material.setDiffuseColor(s.getColor());
                        material.setSpecularColor(Color.YELLOW);
                        sphere.setMaterial(material);
                        sphere.setRadius(10);
                        sphere.setTranslateX(point.transformedX() + xOffset);
                        sphere.setTranslateY(point.transformedY() + yOffset);
                        if (s.getColor() == Color.RED) {
                            sphere.setTranslateZ(-0);
                        }
                        parent.getChildren().add(sphere);
                }

            }
        }

        if (series.size() > 0) {
            //yAxis.buildAxis();
        }
    }

    private double getMinX() {
        return series.stream().map(Series::getMinX).min(Double::compare).orElse(0.);
    }

    private double getMaxX() {
        return series.stream().map(Series::getMaxX).max(Double::compare).orElse(0.);
    }

    private double getMinY() {
        return series.stream().map(Series::getMinY).min(Double::compare).orElse(0.);
    }

    private double getMaxY() {
        return series.stream().map(Series::getMaxY).max(Double::compare).orElse(0.);
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
