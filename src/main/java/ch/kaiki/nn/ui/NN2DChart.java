package ch.kaiki.nn.ui;

import ch.kaiki.nn.ui.color.NNChartColor;
import ch.kaiki.nn.ui.util.GridFace;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

import static ch.kaiki.nn.ui.color.NNColor.blend;
import static javafx.scene.paint.Color.*;
import static javafx.scene.paint.Color.DARKGRAY;

public class NN2DChart extends BaseChart {


    private boolean isInteractive = false;


    private final static double OFFSET_TITLE = 40;

    private final static double OFFSET_TICK_MARK_LABEL = 28;
    private final static double OFFSET_AXIS_LABEL = 20;

    private double offsetTop;
    private double offsetLeft;
    private double offsetRight;
    private double offsetBottom;

    /**
     * Note: turn on antialiasing!
     * @param context
     */
    public NN2DChart(GraphicsContext context) {
        super(context);
        NNChartColor chartColors = new NNChartColor(TRANSPARENT, blend(LIGHTGRAY, TRANSPARENT, 0.1), DARKGRAY, GRAY, GRAY, DARKGRAY, DARKGRAY, DARKGRAY);
        mode = VisualizationMode.SNAP_TO_VIEWPORT;
    }

    @Override
    protected void postProcess() {
        // clip chart area, in case we overlapped any borders
        context.clearRect(0, 0, width, offsetTop);
        context.clearRect(0, 0, offsetLeft, height);
        context.clearRect(0, height-offsetBottom, width, height-offsetBottom);
        context.clearRect(width-offsetRight, 0, width-offsetRight, height);

        // set background
        context.setFill(backgroundColor);
        context.fillRect(0, 0, width, offsetTop);
        context.fillRect(0, 0, offsetLeft, height);
        context.fillRect(0, height-offsetBottom, width, height-offsetBottom);
        context.fillRect(width-offsetRight, 0, width-offsetRight, height);

        // add chart meta objects
        renderTitle();
        renderLegend(false);

        boolean showGridContentCache = showGridContent;
        showGridContent = false;
        renderGrid();
        showGridContent = showGridContentCache;
    }

    @Override
    protected void postInvalidate() {
        super.postInvalidate();
        for (Series s: series) {
            if (s instanceof DecisionBoundarySeries) {
                gridPaddingOffset = 0;
            }
        }
        invalidateDimensions();
    }

    private void invalidateDimensions() {
        offsetTop = OFFSET_BASE;
        offsetLeft = OFFSET_BASE;
        offsetRight = OFFSET_BASE;
        offsetBottom = OFFSET_BASE;
        if (showTitle) {
            offsetTop += OFFSET_TITLE;
        }

        if (showTickMarkLabels) {
            offsetLeft += OFFSET_TICK_MARK_LABEL;
            offsetBottom += OFFSET_TICK_MARK_LABEL;
        }

        if (showAxisLabels) {
            offsetLeft += OFFSET_AXIS_LABEL;
            offsetBottom += OFFSET_AXIS_LABEL;
        }

        if (showLegend) {
            offsetRight += legendWidth;
        }
    }



    @Override
    protected void renderGrid() {
        List<Grid> faces = getGrid();
        for (Grid grid : faces) {
            grid.render();
        }
    }

    @Override
    public double getWidth() {
        return width-offsetLeft-offsetRight;
    }

    @Override
    public double getHeight() {
        return height-offsetTop-offsetBottom;
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
        double[] d0 = new double[] {xMinCube, yMinCube, zMinCube};
        double[] d1 = new double[] {xMinCube, yMaxCube, zMinCube};
        double[] d2 = new double[] {xMaxCube, yMaxCube, zMinCube};
        double[] d3 = new double[] {xMaxCube, yMinCube, zMinCube};

        List<Grid> faces = new ArrayList<>();
        faces.add(new Grid(this, GridFace.BOTTOM, d0, d3, d2, d1, chartColors, axisLabels));
        return faces;
    }

    @Override
    protected void setProjectionMatrix() {
        double[] camera = {0,0,-1};
        double[][] project = baseProjection(camera);

        double xRangeOld = Math.abs(xMax - xMin);
        double yRangeOld = Math.abs(yMax - yMin);

        double xRange = xRangeOld*(1+ gridPaddingOffset *2);
        double yRange = yRangeOld*(1+ gridPaddingOffset *2);

        double xTranslate = xRange / 2 - (xMin - xRangeOld * gridPaddingOffset);
        double yTranslate = yRange / 2 - (yMin - yRangeOld * gridPaddingOffset);

        double heightOffset =offsetTop+offsetBottom;
        double width = this.width-(offsetLeft+offsetRight);
        double height = this.height-heightOffset;

        double yFactorBefore = (1 / yRange * xRange);
        double yFactorAfter = height;
        double[][] m = scale(1,1, 1);                                                   // 0. start with identity matrix for better readability
        m = multiply(translate(-(xRange-xTranslate),-(yRange-yTranslate), 0), m);   // 1. center
        m = multiply(scale(1/xRange, (1/xRange) * yFactorBefore, 1), m);       // 2. normalize z (and clamp y to x)
        m = multiply(xReflect(), m);                                                            // 5. reflect on x-axis
        m = multiply(project, m);                                                               // 6. project
        m = multiply(translate(-(0.5), 0.5, 0), m);                        // 7. transform back
        m = multiply(scale(width, (yFactorAfter), 1), m);                                     // 8. adjust to plot dimensions
        m = multiply(scale(-1,1,1), m);                                                 // 9. flip x
        m = multiply(translate(offsetLeft, offsetTop, 0), m);
        projectionMatrix = m;

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
            context.getCanvas().setStyle("-fx-cursor: hand;");
        });

        context.getCanvas().setOnMouseExited(e -> {
            context.getCanvas().setStyle("");
        });
    }



    // --------------------------------------------- matrix op ---------------------------------------------




}

