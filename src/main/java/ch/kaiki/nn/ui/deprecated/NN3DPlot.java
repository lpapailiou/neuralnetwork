package ch.kaiki.nn.ui.deprecated;

import ch.kaiki.nn.ui.color.NNColorSupport;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.NNDataColor;
import ch.kaiki.nn.ui.color.NNHeatMap;
import javafx.util.Duration;

import java.util.*;

import static javafx.scene.paint.Color.*;
import static ch.kaiki.nn.ui.color.NNColorSupport.blend;

public class NN3DPlot extends BasePlot2 {

    private NNDataColor dataColor;

    private double[][] matrix;
    private double zoom = 1;
    private double xAngle = 68;
    private double zAngle = 46;

    private Map<String, Integer> classMap = new HashMap<>();
    private double[][] inData;
    private double[][] outData;

    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;

    private double zMin;
    private double zMax;
    private int iterX;
    private int iterY;
    private NeuralNetwork neuralNetwork;

    private boolean visualizeAsCube = true;
    private boolean snapToViewPort = false;

    private List<Color> customColors;
    private double step;
    private List<double[][][]> gridList = new ArrayList<>();
    private double resolution;
    private double initResolution;
    private double[][] inputData;
    boolean snapBack = false;
    public NN3DPlot(GraphicsContext context) {
        super(context);
        setProjectionMatrix();
        // TODO: turn on antialiasing
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

        context.getCanvas().setOnMouseDragged(e -> {
            mousePosX = e.getSceneX();
            mousePosY = e.getSceneY();
            this.xAngle = (xAngle - (mousePosY - mouseOldY)) % 360;
            this.zAngle = (zAngle + (mousePosX - mouseOldX)) % 360;
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            if (snapBack && initResolution != resolution) {
                resolution = initResolution;
                processData();
            } else {
                render();
            }
            e.consume();
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            //this.xAngle = (xAngle + 1) % 360;
            this.zAngle = (zAngle + 0.25) % 360;
            render();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        final double MAX_SCALE = 10;
        final double MIN_SCALE = 0.1;
        context.getCanvas().addEventFilter(ScrollEvent.ANY, e -> {
            double delta = 1.2;
            double scale = zoom;
            if (-e.getDeltaY() < 0) {
                scale /= delta;
            } else {
                scale *= delta;
            }
            this.zoom = clamp(scale, MIN_SCALE, MAX_SCALE);
            if (snapBack && initResolution != resolution) {
                resolution = initResolution;
                processData();
            } else {
                render();
            }
            e.consume();
        });

        context.getCanvas().setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                double newResolution = resolution + 0.01;
                if (resolution <= 1 && newResolution <= 1) {
                    resolution = newResolution;
                    processData();
                }
            } else if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                resolution = initResolution;
                processData();
            }

        });

    }

    private String[] axisLabels = new String[3];
    public void setAxisLabels(String... labels) {
        if (labels == null) {
            axisLabels = new String[3];
            return;
        }
        int length = Math.min(axisLabels.length, labels.length);
        for (int i = 0; i < length; i++) {
            axisLabels[i] = labels[i];
        }
    }

    public void plot(NeuralNetwork neuralNetwork, double[][] in, double resolution, double opacity, boolean axes, NNDataColor dataColor) {

        int[] configuration = neuralNetwork.getConfiguration();
        if (configuration[0] != 2) {
            throw new IllegalArgumentException("Decision boundaries can only be plotted for 2-dimensional inputs!");
        }
        this.inputData = in;
        this.neuralNetwork = neuralNetwork;
        this.initResolution = resolution;
        this.resolution = resolution;
        prepareRanges(in);

        this.dataColor = dataColor;
        processData();

        //drawOverlay(opacity);
        //drawAxes(axes, false, false);


    }



    private void processData() {
        int[] configuration = neuralNetwork.getConfiguration();
        if (configuration[configuration.length - 1] == 1) {
            if (dataColor.getColors().size() < 2) {
                throw new IllegalArgumentException("At least 2 data color items must be provided!");
            }
        } else {
            if (dataColor.getColors().size() < configuration[configuration.length-1]) {
                throw new IllegalArgumentException("Data color items " + dataColor.getColors().size() + " must match output class dimensions " + configuration[configuration.length-1] + "!");
            }
        }

        customColors = dataColor.getColors();

        iterX = (int) Math.ceil(width * resolution);
        if (!visualizeAsCube) {
            if (snapToViewPort) {
                iterY = (int) Math.ceil(height * resolution);
            } else {
                iterY = (int) Math.ceil((width * resolution) / Math.abs(xMax - xMin) * Math.abs(yMax - yMin));
            }
        } else {
            iterY = iterX;
        }

        step = Math.abs(zMax - zMin) / (customColors.size()-1);
        gridList = getDecisionBoundaryGrids(neuralNetwork, xMin, xMax, yMin, yMax, iterX+1, iterY+1, 1);
        render();
    }


    private List<Grid> getFaces() {
        double offsetFactor = 0.05;     // data is 90% of the cube size
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

        faces.add(new Grid(this, GridFace.BOTTOM, d0, d3, d2, d1, gridColor, gridLineColor, axisColor, axisLabels));
        faces.add(new Grid(this, GridFace.RIGHT, d3, d7, d6, d2, gridColor, gridLineColor, axisColor, axisLabels));
        faces.add(new Grid(this, GridFace.LEFT, d0, d4, d5, d1, gridColor, gridLineColor, axisColor, axisLabels));
        faces.add(new Grid(this, GridFace.BACK, d1, d2, d6, d5, gridColor, gridLineColor, axisColor, axisLabels));
        faces.add(new Grid(this, GridFace.FRONT, d0, d3, d7, d4, gridColor, gridLineColor, axisColor, axisLabels));
        faces.add(new Grid(this, GridFace.TOP, d4, d7, d6, d5, gridColor, gridLineColor, axisColor, axisLabels));
        Collections.sort(faces, Comparator.comparingDouble(Grid::getZ));
        return faces;
    }

    public void showInputData(double[][] in, double[][] out) {
        inData = in;
        outData = out;
        classMap = new HashMap<>();
        for (int i = 0; i < in.length; i++) {
            String key = Arrays.toString(out[i]);
            int index;
            if (classMap.containsKey(key)) {
                index = classMap.get(key);
            } else {
                index = classMap.size();
                classMap.put(key, index);
            }
            double[] t = transform(new double[] {in[i][0], in[i][1], zMax});
            Color color = customColors.get(index);
            if (out[i].length == 1 && index != 0) {
                color = customColors.get(customColors.size()-1);
            }
            double radius = 8;
            context.setFill(color.invert());
            context.fillOval(t[0]-radius/2, t[1]-radius/2, radius, radius);
            radius = 6;
            context.setFill(color);
            context.fillOval(t[0]-radius/2, t[1]-radius/2, radius, radius);
        }


    }

    private void render() {
        setProjectionMatrix();
        clear();
        setTitle(title);
        List<Grid> faces = getFaces();
        for (int i = faces.size()-1; i > 2; i--) {
            faces.get(i).draw();
        }

        List<double[][][]> gridData = getBaseGrid(gridList.size());

        for (int i = 0; i < gridData.size(); i++) {
            for (int ii = 0; ii < iterX + 1; ii++) {
                for (int jj = 0; jj < iterY + 1; jj++) {
                    double[] t = transform(gridList.get(i)[ii][jj]);
                    double output = gridList.get(i)[ii][jj][2];
                    gridData.get(i)[ii][jj] = new double[]{t[0], t[1], t[3], output};
                }
            }
        }
        List<Polygon> polygons = new ArrayList<>();
        int index = 0;
        for (double[][][] pointGrid : gridData) {
            List<Color> colorList;
            if (gridData.size() > 1) {
                colorList = new ArrayList<>();
                colorList.add(NNColorSupport.blend(customColors.get(index), TRANSPARENT, 0.2));
                colorList.add(NNColorSupport.blend(customColors.get(index), TRANSPARENT, 0.8));

            } else {
                colorList = customColors;
            }
            polygons.addAll(getPolygons(iterX+1 , iterY+1, zMin, zMax, pointGrid, step, 0, colorList));
            index++;
        }
        Comparator<Polygon> comparator = (Polygon::compareTo);
        polygons.sort(comparator.reversed());
        for (Polygon p : polygons) {
            //p.draw(STEELBLUE.brighter(), STEELBLUE.darker(), zMin, zMax);
            p.draw();
        }

        if (classMap.size() > 0) {
            showInputData(inData, outData);
        }

        drawBorder();

    }


    private void prepareRanges(double[][] in) {
        xMin = Double.MAX_VALUE;
        xMax = Double.MIN_VALUE;
        yMin = Double.MAX_VALUE;
        yMax = Double.MIN_VALUE;

        for (int i = 0; i < in.length; i++) {
            double xValue = in[i][0];
            if (xValue < xMin) {
                xMin = xValue;
            }
            if (xValue > xMax) {
                xMax = xValue;
            }
            double yValue = in[i][1];
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

        zMin = Double.MAX_VALUE;
        zMax = Double.MIN_VALUE;
    }

    // TODO: check where text alignment gets messed up

    private List<Polygon> getPolygons(int iterI, int iterJ, double zMin, double zMax, double[][][] pointGrid, double step, double index, List<Color> customColors) {
        List<Polygon> squares = new ArrayList<>();

        for (int i = 0; i < iterI-1; i++) {
            for (int j = 0; j < iterJ-1; j++) {
                double[] a = pointGrid[i][j];
                double[] b = pointGrid[i+1][j];
                double[] c = pointGrid[i+1][j+1];
                double[] d = pointGrid[i][j+1];

                //double pos = 0.5;
                //double neg = -0.5;
                double pos = 0.1;
                double neg = -0.1;
                double[] xEs = {a[0] < c[0] ? a[0]+neg : a[0]+pos,
                        b[0] > d[0] ? b[0]+pos : b[0]+neg,
                        c[0] > a[0] ? c[0]+pos : c[0]+neg,
                        d[0] < b[0] ? d[0]+neg : d[0]+pos};
                double[] ys =  {a[1] > c[1] ? a[1]+pos : a[1]+neg,
                        b[1] > d[1] ? b[1]+pos : b[1]+neg,
                        c[1] < a[1] ? c[1]+neg : c[1]+pos,
                        d[1] < b[1] ? d[1]+neg : d[1]+pos};


                double zSum = (a[3] + b[3] + c[3] + d[3]) / 4;
                if (zSum < zMin || zSum > zMax) {
                    // continue;        // TODO: check if really helpful
                }

                double sort = (a[2] + b[2] + c[2] + d[2]) / 4;

                double output = (a[3] + b[3] + c[3] + d[3]) / 4;
                Color color;
                if (customColors.size() > 2) {
                    int stepIndex = 0;
                    double value = zMin;
                    for (int k = 0; k < customColors.size() - 1; k++) {
                        value += step;
                        if (output <= value || k == customColors.size() - 2) {
                            stepIndex = k;
                            break;
                        }
                    }
                    double ratio = 1 / step * Math.abs(value - output);
                    if (output > zMax) {
                        ratio = 0;
                    }
                    color = blend(customColors.get(stepIndex), customColors.get(stepIndex+1), ratio);
                } else {
                    color = blend(customColors.get(1), customColors.get(0), output);
                }

                squares.add(new Polygon(context, xEs, ys, sort, color));
            }
        }
        return squares;
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

    // --------------------------------------------- data op ---------------------------------------------

    class ForwardPropEntity {

        private double x;
        private double y;
        private List<Double> output;

        public ForwardPropEntity(double x, double y, List<Double> output) {
            this.x = x;
            this.y = y;
            this.output = output;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return output.get(0);
        }

        public List<Double> getOutput() {
            return output;
        }
    }

    private List<double[][][]> getBaseGrid(int count) {
        List<double[][][]> gridList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            gridList.add(new double[iterX+1][iterY+1][]);
        }
        return gridList;
    }

    private List<double[][][]> getDecisionBoundaryGrids(NeuralNetwork neuralNetwork, double minX, double maxX, double minY, double maxY, int iterX, int iterY, double zFactor) {
        int[] configuration = neuralNetwork.getConfiguration();
        int gridCount = configuration[configuration.length-1];

        List<double[][][]> gridList = getBaseGrid(gridCount);
        double stepX = Math.abs(maxX - minX) / (iterX-1);
        double stepY = Math.abs(maxY - minY) / (iterY-1);
        double x = minX;
        double y = minY;
        for (int i = 0; i < iterX; i++) {
            for (int j = 0; j < iterY; j++) {
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
                    grid[i][j] = new double[]{input[0], input[1], out * zFactor};
                }
            }
            y = minY;
            x += stepX;
        }

        if (dataColor instanceof NNHeatMap && ((NNHeatMap) dataColor).isScaled()) {
            zMin = ((NNHeatMap) dataColor).getMin();
            zMax = ((NNHeatMap) dataColor).getMax();
        }
        return gridList;
    }

    // --------------------------------------------- matrix op ---------------------------------------------

    private void setProjectionMatrix() {
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
        double viewPortFactor = snapToViewPort ? 1 : (height/width);
        double yFactorBefore = (visualizeAsCube || snapToViewPort) ? (1 / yRange * xRange) : 1;
        double yFactorAfter = snapToViewPort ? height : width;

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
        matrix = m;
    }

    public double[] transform(double[] v) {
        return lower(multiply(matrix, lift(v)));
    }

    private double[][] lift(double[][] m) {
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

    private double[][] xRotation(double angle) {
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

    private double[][] zRotation(double angle) {
        angle = Math.toRadians(angle);
        return new double[][]{{Math.cos(angle), -Math.sin(angle), 0},
                                {Math.sin(angle), Math.cos(angle), 0},
                                {0, 0, 1}};
    }

    private double[][] translate(double x, double y, double z) {
        return new double[][] {{1,0,0,x},
                                {0,1,0,y},
                                {0,0,1,z},
                                {0,0,0,1}};
    }

    private double[][] scale(double x, double y, double z) {
        return new double[][] {{x,0,0,0},
                                {0,y,0,0},
                                {0,0,z,0},
                                {0,0,0,1}};
    }

    private double[][] xReflect() {
        return new double[][] {{1,0,0,0},
                                {0,-1,0,0},
                                {0,0,1,0},
                                {0,0,0,1}};
    }

    private double[][] multiply(double[][] a, double[][] b) {     // outer arr is line
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

    public double[] multiply(double[][] a, double[] b) {     // outer arr is line
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

    public double[][] add(double[][] a, double[][] b) {
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

    double[] crossProduct(double[] a, double[] b) {
        return new double[] {a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0]};
    }

    double norm(double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    double[] divide(double[] v, double s) {
        if (s == 1) {
            return v;
        }
        return new double[] {v[0]/s, v[1]/s, v[2]/s};
    }

    double[] negate(double[] v) {
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

    private double[][] centralProjection() {
        double zMin = -1;
        return new double[][] {{1,0,0,0},{0,1,0,0},{0,0,0,0},{0,0,1/zMin,1}};
    }


}

