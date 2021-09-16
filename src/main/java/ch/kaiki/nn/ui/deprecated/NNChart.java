package ch.kaiki.nn.ui.deprecated;

import ch.kaiki.nn.ui.util.*;
import ch.kaiki.nn.ui.util.Series;
import javafx.beans.property.ReadOnlyProperty;
import javafx.geometry.*;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.List;
import java.util.Set;

public class NNChart extends BorderPane {
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    private final Rotate rotateX;
    private final Rotate rotateZ;
    private boolean isZoomable = true;
    private double borderWidth = 1.2;
    private Group threeDeeContainer = new Group();

    public Group get3DContainer() {
        return threeDeeContainer;
    }

    // TODO: zeroLine

    private Color backgroundColor = Color.TRANSPARENT;
    private Color chartBackgroundColor = Color.TRANSPARENT;
    private Color axisColor = Color.RED.darker();
    private Color gridColor = Color.BLUE;
    private Color labelColor = Color.GREEN.darker();

    private Font titleFont = new Font(null, 15);;
    private Font axisLabelFont = new Font(null, 12.5);

    private Label title = new Label();
    private ChartArea chartArea;
    LabelBox labelBox;
    NumberAxis xAxis;
    NumberAxis yAxis;

    public void setLabelColor(Color color) {
        title.setTextFill(color);

        //xAxis.setStyle("-fx-tick-unit: red;-fx-tick-label-fill: red;");
        setStyleProperties(".axis-label", "-fx-text-fill: " + format(color) + ";");

    }

    public void setTickColor(Color color) {
        xAxis.setTickLabelFill(color);
        yAxis.setTickLabelFill(color);
        setStyleProperties(".axis-tick-mark", "-fx-stroke: " + format(color) + ";");

    }

    public Rotate getRotateX() {
        return rotateX;
    }

    public Rotate getRotateZ() {
        return rotateZ;
    }

    public NNChart(double width, double height, boolean showTickMarks, boolean showTickLabels, boolean showGrid, double scaling) {
        this.setDepthTest(DepthTest.ENABLE);
        rotateX = new Rotate(0,width/2, height/2, 0, Rotate.X_AXIS);
        rotateZ = new Rotate(0, width/2, height/2, 0, Rotate.Z_AXIS);
        onShow();
        this.getTransforms().addAll(rotateX, rotateZ);
        Sphere sphere = new Sphere(350);
        sphere.setTranslateZ(-100);
        //this.getChildren().add(sphere);
        if (true) {
            this.setMinWidth(width);
            this.setMaxWidth(width);
            this.setWidth(width);
            this.setMinHeight(height);
            this.setMaxHeight(height);
            this.setHeight(height);
        }
        this.setBackground(new Background(new BackgroundFill(backgroundColor, null, null)));

        // chartArea
        chartArea = new ChartArea(scaling);
        this.setCenter(chartArea);

        chartArea.setBackground(new Background(new BackgroundFill(chartBackgroundColor, null, null)));
        chartArea.setStyle("-fx-border-width: " + borderWidth + ";-fx-border-color: " + format(axisColor) +";");
        //chartArea.setStyle("-fx-border-width: 5; -fx-border-color: red;-fx-background-color: pink");



        //chartArea.setPrefWidth(this.getWidth());
        //chartArea.setPrefHeight(this.getHeight());
        //System.out.println("CHART AREA WIDTH: " + chartArea.getWidth());

        // title
        //title.setStyle("-fx-border-color: green; -fx-background-color: limegreen;-fx-border-width: 2");
        this.setTop(title);
        title.setFont(titleFont);
        title.setTextFill(labelColor);
        title.setAlignment(Pos.CENTER);
        title.setMinWidth(width);
        title.managedProperty().bind(title.visibleProperty());

        // labelbox
        labelBox = new LabelBox(axisLabelFont, labelColor);
        chartArea.setLabelBox(labelBox);
        labelBox.managedProperty().bind(labelBox.visibleProperty());
        labelBox.prefHeightProperty().bind(chartArea.heightProperty());
        //labelBox.setVisible(false);


        // axes
        xAxis = new NumberAxis(0,1,1);
        yAxis = new NumberAxis(0,1,1);
        xAxis.setSide(Side.BOTTOM);
        yAxis.setSide(Side.LEFT);



        xAxis.prefWidthProperty().bind(chartArea.widthProperty());
        xAxis.managedProperty().bind(xAxis.visibleProperty());
        yAxis.managedProperty().bind(yAxis.visibleProperty());
        //yAxis.setPrefWidth(0);
        //yAxis.setMaxWidth(100);
        setMinorTickMarks(false);


        this.setLeft(yAxis);
        //yAxis.setLabel("TEST");

        //yAxis.setStyle("-fx-border-color: green; -fx-background-color: limegreen;-fx-border-width: 2");



        HBox bottomBox = new HBox();
        Region leftBottomPadding = new Region();
        Region rightBottomPadding = new Region();
        leftBottomPadding.prefWidthProperty().bind(yAxis.widthProperty());
        rightBottomPadding.prefWidthProperty().bind(labelBox.widthProperty());
        bottomBox.getChildren().addAll(leftBottomPadding, xAxis, rightBottomPadding);
        this.setBottom(bottomBox);


        chartArea.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue && getWidth() != 0 && chartArea.getHeight() != 0) {
                chartArea.render();
            }

        });



        chartArea.setXAxis(xAxis);
        chartArea.setYAxis(yAxis);
/*
        xAxis.setTickLabelFont(axisLabelFont);
        yAxis.setTickLabelFont(axisLabelFont);
        xAxis.setTickLabelFill(labelColor);
        yAxis.setTickLabelFill(labelColor);
        setLabelColor(Color.LIGHTGRAY.darker());
        setTickColor(Color.LIGHTGRAY.darker());*/
    }

    public double gethOffset() {
        Node node = this.getTop();
        if (node == null) {
            return 0;
        }
        return title.getHeight();
    }

    private void onShow() {
        this.setOnMouseEntered(e -> {
            //this.setStyle("-fx-background-color: red");
            isZoomable = true;
            //System.out.println("zoomable!!!!");
        });
        this.setOnMouseExited(e -> {
            isZoomable = false;
            //this.setStyle("-fx-background-color: transparent");
            //System.out.println("not zooable");
        });
        this.sceneProperty().addListener((observable, oldValue, scene) -> {
            if (scene != null) {
                scene.windowProperty().addListener((observable1, oldValue1, newValue1) -> {

                    //System.out.println("on show working");
                    final double MAX_SCALE = 20.0;
                    final double MIN_SCALE = 0.1;
                    scene.addEventFilter(ScrollEvent.ANY, event -> {
                        if (!isZoomable) {
                            //System.out.println("not zoomable xxx");
                            //return;
                        }
                        double delta = 1.2;
                        double scale = this.getScaleX();
                        if (event.getDeltaY() < 0) {
                            scale /= delta;
                        } else {
                            scale *= delta;
                        }
                        scale = clamp(scale, MIN_SCALE, MAX_SCALE);
                        this.setScaleX(scale);
                        this.setScaleY(scale);
                        //chartArea.setTranslateZ(-100);
                        event.consume();
                    });
                });
            }
        });
    }
    public static double clamp(double value, double min, double max) {
        if (Double.compare(value, min) < 0)
            return min;
        if (Double.compare(value, max) > 0)
            return max;
        return value;
    }
    private void updateChartAreaSizing() {

    }
    public void setMinorTickMarks(boolean minorTickMarks) {
        xAxis.setMinorTickVisible(minorTickMarks);
        yAxis.setMinorTickVisible(minorTickMarks);
    }

    public void plot(String name, List<Point> data, Color color) {
        ch.kaiki.nn.ui.util.Series series = new Series(name, data, color);
        if (this.getRight() == null) {
            this.setRight(labelBox);
        }
        chartArea.addData(series);
        System.out.println("------------ data added ---------------");
        if (getWidth() != 0 && chartArea.getHeight() != 0) {
            chartArea.render();
        }
        //System.out.println("xmin: " + xAxis.getMin());
    }

    public void setTitle(String text) {
        title.setText(text);
        title.setVisible(!(text == null || text.trim().equals("")));
    }

    public ChartArea getChartArea() {
        return chartArea;
    }

    public void setXAxisLabel(String label) {
        xAxis.setLabel(label);
    }

    public void setYAxisLabel(String label) {
        yAxis.setLabel(label);
    }

    private String format(Color color) {
        return color.toString().replaceAll("0x", "#");
    }
    private void setStyleProperties(String selector, String value) {
        Set<Node> nodes = this.lookupAll(selector);
        for (Node node : nodes) {
            node.setStyle(value);
        }
    }

}
