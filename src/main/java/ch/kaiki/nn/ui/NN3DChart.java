package ch.kaiki.nn.ui;

import ch.kaiki.nn.ui.util.GridFace;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;

import java.util.*;

public class NN3DChart extends BaseChart {

    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;

    private Timeline timeline;


    /**
     * Note: turn on antialiasing!
     * @param context
     */
    public NN3DChart(GraphicsContext context) {
        super(context);
        showBorder = true;
        mode = VisualizationMode.CUBE;
    }


    @Override
    protected void preProcess() {
        super.renderTitle();
        super.renderLegend(true);
    }




    @Override
    protected void renderGrid() {
        List<Grid> faces = getGrid();
        for (int i = faces.size() - 1; i > 2; i--) {
            faces.get(i).render();
        }
    }

    @Override
    protected List<Grid> getGrid() {
        double offsetFactor = gridPaddingOffset;     // data is 90% of the cube size
        double xCubeOffset = Math.abs(xMax-xMin) * offsetFactor;
        double yCubeOffset = Math.abs(yMax-yMin) * offsetFactor;
        double zCubeOffset = Math.abs(zMax-zMin) * offsetFactor;

        double xMinCube = xMin - xCubeOffset;
        double xMaxCube = xMax + xCubeOffset;
        double yMinCube = yMin - yCubeOffset;
        double yMaxCube = yMax + yCubeOffset;
        double zMinCube = zMin - zCubeOffset;
        double zMaxCube = zMax + zCubeOffset;

        double[] d0 = new double[] {xMinCube, yMinCube, zMinCube};
        double[] d1 = new double[] {xMinCube, yMaxCube, zMinCube};
        double[] d2 = new double[] {xMaxCube, yMaxCube, zMinCube};
        double[] d3 = new double[] {xMaxCube, yMinCube, zMinCube};
        double[] d4 = new double[] {xMinCube, yMinCube, zMaxCube};
        double[] d5 = new double[] {xMinCube, yMaxCube, zMaxCube};
        double[] d6 = new double[] {xMaxCube, yMaxCube, zMaxCube};
        double[] d7 = new double[] {xMaxCube, yMinCube, zMaxCube};

        List<Grid> faces = new ArrayList<>();

        faces.add(new Grid(this, GridFace.BOTTOM, d0, d3, d2, d1, chartColors, axisLabels));
        faces.add(new Grid(this, GridFace.RIGHT, d3, d7, d6, d2, chartColors, axisLabels));
        faces.add(new Grid(this, GridFace.LEFT, d0, d4, d5, d1, chartColors, axisLabels));
        faces.add(new Grid(this, GridFace.BACK, d1, d2, d6, d5, chartColors, axisLabels));
        faces.add(new Grid(this, GridFace.FRONT, d0, d3, d7, d4, chartColors, axisLabels));
        faces.add(new Grid(this, GridFace.TOP, d4, d7, d6, d5, chartColors, axisLabels));
        Collections.sort(faces, Comparator.comparingDouble(Grid::getZ));
        return faces;
    }


    // TODO: check where text alignment gets messed up




    // --------------------------------------------- ui interaction ---------------------------------------------

    @Override
    public void enableMouseInteraction() {
        if (isInteractive) {
            return;
        }
        super.enableMouseInteraction();
        context.getCanvas().setOnMouseEntered(e -> {
            context.getCanvas().setStyle("-fx-cursor: move;");
        });

        context.getCanvas().setOnMouseExited(e -> {
            context.getCanvas().setStyle("");
        });
        context.getCanvas().setOnMousePressed(e -> {
            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
        });

        context.getCanvas().setOnMouseDragged(e -> {            // rotate
            mousePosX = e.getSceneX();
            mousePosY = e.getSceneY();
            this.xAngle = (xAngle - (mousePosY - mouseOldY)) % 360;
            this.zAngle = (zAngle + (mousePosX - mouseOldX)) % 360;
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            render();
            e.consume();
        });

        final double MAX_SCALE = 10;
        final double MIN_SCALE = 0.1;
        context.getCanvas().addEventFilter(ScrollEvent.ANY, e -> {      // zoom
            double delta = 1.2;
            double scale = zoom;
            if (-e.getDeltaY() < 0) {
                scale /= delta;
            } else {
                scale *= delta;
            }
            this.zoom = clamp(scale, MIN_SCALE, MAX_SCALE);
            render();
            e.consume();
        });

    }

    public void setAnimated(boolean animate) {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = null;

        if (animate) {
            timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                //this.xAngle = (xAngle + 1) % 360;
                this.zAngle = (zAngle + 0.25) % 360;
                render();
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
    }


    private double clamp(double value, double min, double max) {
        if (Double.compare(value, min) < 0) {
            return min;
        }
        if (Double.compare(value, max) > 0) {
            return max;
        }
        return value;
    }

    // --------------------------------------------- matrix op ---------------------------------------------

    @Override
    protected void setProjectionMatrix() {
        double[] camera = {0,0,-1};
        double[][] project = multiply(centralProjection(), baseProjection(camera));
        double[][] rotate = lift(multiply(xRotation(xAngle), zRotation(zAngle)));

        double xRange = Math.abs(xMax - xMin);
        double yRange = Math.abs(yMax - yMin);
        double zRange = Math.abs(zMax - zMin);
        double xTranslate = xRange / 2 - xMin;
        double yTranslate = yRange / 2 - yMin;
        double zTranslate = zRange / 2 - zMin;
        //   System.out.println("xRange: " + xRange + ", yRange: " + yRange + ", zRange: " + zRange);
        double viewPortFactor = mode == VisualizationMode.SNAP_TO_VIEWPORT ? 1 : (height/width);
        double yFactorBefore = mode == VisualizationMode.REGULAR ? 1 : (1 / yRange * xRange);
        double yFactorAfter = mode == VisualizationMode.SNAP_TO_VIEWPORT ? height : width;

        double[][] m = scale(1,1, 1);                                                   // 0. start with identity matrix for better readability
        m = multiply(translate(-(xRange-xTranslate),-(yRange-yTranslate), -(zRange-zTranslate)), m);   // 1. center
        m = multiply(scale(1/xRange, (1/xRange) * yFactorBefore, 1/zRange*0.5), m);       // 2. normalize z (and clamp y to x)
        m = multiply(rotate, m);                                                                // 3. rotate
        m = multiply(translate(0,0, -zoom), m);                                             // 4. zoom
        m = multiply(xReflect(), m);                                                            // 5. reflect on x-axis
        m = multiply(project, m);                                                               // 6. project
        m = multiply(translate(-(0.5), 0.5 * viewPortFactor, 0), m);                        // 7. transform back
        m = multiply(scale(width, yFactorAfter, 1), m);                                     // 8. adjust to plot dimensions
        m = multiply(scale(-1,1,1), m);                                                 // 9. flip x
        projectionMatrix = m;
    }


}

