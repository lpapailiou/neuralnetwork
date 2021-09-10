package ch.kaiki.nn.ui.deprecated;

import ch.kaiki.nn.ui.color.NNColorSupport;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.NNDataColor;
import ch.kaiki.nn.ui.color.NNHeatMap;

import java.util.*;

import static javafx.scene.paint.Color.*;
import static ch.kaiki.nn.ui.color.NNColorSupport.blend;

public class NN3DPlot extends BasePlot {

    private NNDataColor dataColor;
    private double[][] data;
    private double[] dataMin = {0,0};
    private double[] dataRange = {0,0};
    private double[][] matrix;
    private double cachedPadding;
    private double zoom;
    private double xAngle;
    private double yAngle;
    private double zAngle;
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    double zMin = Double.MAX_VALUE;
    double zMax = Double.MIN_VALUE;
    int iterX;
    int iterY;
    NeuralNetwork neuralNetwork;


    boolean snapBack = false;
    public NN3DPlot(GraphicsContext context) {
        super(context);
        matrix = getProjectionMatrix();

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
            setXAngle(xAngle - (mousePosY - mouseOldY));
            setZAngle(zAngle + (mousePosX - mouseOldX));
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

        final double MAX_SCALE = 2;
        final double MIN_SCALE = 0.1;
        context.getCanvas().addEventFilter(ScrollEvent.ANY, e -> {
            double delta = 1.2;
            double scale = zoom;
            if (-e.getDeltaY() < 0) {
                scale /= delta;
            } else {
                scale *= delta;
            }
            scale = clamp(scale, MIN_SCALE, MAX_SCALE);
            setZoom(scale);
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
                double newResolution = resolution + 0.1;
                if (resolution <= 1 && newResolution <= 1) {
                    resolution = newResolution;
                    processData();
                }

            }
        });

    }

    public void plot(double[][] transformationMatrix, NeuralNetwork neuralNetwork, double[][] in, double resolution, double opacity, boolean axes, NNDataColor dataColor) {
        matrix = transformationMatrix;
        plot(neuralNetwork, in, resolution, opacity, axes, dataColor);
    }
    double resolution;
    double initResolution;
    public void plot(NeuralNetwork neuralNetwork, double[][] in, double resolution, double opacity, boolean axes, NNDataColor dataColor) {
        clear();
        int[] configuration = neuralNetwork.getConfiguration();
        if (configuration[0] != 2) {
            throw new IllegalArgumentException("Decision boundaries can only be plotted for 2-dimensional inputs!");
        }
        this.neuralNetwork = neuralNetwork;
        this.initResolution = resolution;
        this.resolution = resolution;
        prepareRanges(in);
        plotBackgroundColor = BLACK;
        cachedPadding = padding;
        this.dataColor = dataColor;

        processData();

        drawOverlay(opacity);
        drawAxes(axes, false, false);
        setTitle(title);

        padding = cachedPadding;
    }

    private void processData() {
        int[] configuration = neuralNetwork.getConfiguration();
        iterX = (int) Math.ceil(plotWidth * resolution);
        iterY = iterX; //(int) Math.ceil(plotHeight * resolution);
        ForwardPropEntity[][] data = new ForwardPropEntity[iterX+1][iterY+1];

        double stepX = 1.0 / iterX;
        double stepY = 1.0 / iterY;
        double x = 0;
        double y = 0;
        int outLength = configuration[configuration.length-1];



        padding = 0;

        drawBackground();
        if (configuration[configuration.length - 1] == 1) {
            if (dataColor.getColors().size() < 2) {
                throw new IllegalArgumentException("At least 2 kaiki.ch.kaiki.nn.data color items must be provided!");
            }
            //plotBinaryClassifierDecisionBoundaries(data, iterX+1, iterY+1, minZ, maxZ);
        } else {
            if (dataColor.getColors().size() != configuration[configuration.length-1]) {
                //throw new IllegalArgumentException("Data color items " + dataColor.getColors().size() + " must match output class dimensions " + configuration[configuration.length-1] + "!");
            }
            //plotMultiClassClassifierDecisionBoundaries(forwardPropEntities, xOffset, yOffset);
        }

        if(true){

            customColors = dataColor.getColors();
            double zFactor =  1 / (cachedPadding+1);
            if (cachedPadding < 1) {
                zFactor = 0.5;
            }
            if (dataColor instanceof NNHeatMap && ((NNHeatMap) dataColor).isScaled()) {
                zMin = ((NNHeatMap) dataColor).getMin();
                zMax = ((NNHeatMap) dataColor).getMax();
            }
            step = Math.abs(zMax - zMin) / (customColors.size()-1);
            gridList = getDecisionBoundaryGrids(neuralNetwork, xMin, xMax, yMin, yMax, iterX+1, iterY+1, 1);
            render();
        }
    }
    List<Color> customColors;
    double step;
    List<double[][][]> gridList = new ArrayList<>();

    private void render() {
        matrix = getProjectionMatrix();
       // System.out.println("x rotation: " + xAngle + ", z rotateion: " + zAngle);
        clear();

        double offsetFactor = 0.05;
        double xCubeOffset = Math.abs(xMax-xMin) * offsetFactor;
        double yCubeOffset = Math.abs(yMax-yMin) * offsetFactor;
        double zCubeOffset = Math.abs(zMax-zMin) * offsetFactor;

        double xMinCube = xMin - xCubeOffset;
        double xMaxCube = xMax + xCubeOffset;
        double yMinCube = yMin - yCubeOffset;
        double yMaxCube = yMax + yCubeOffset;
        double zMinCube = zMin - zCubeOffset;
        double zMaxCube = zMax + zCubeOffset;

        double[] d0 = transform(new double[] {xMinCube, yMinCube, zMinCube});
        double[] d1 = transform(new double[] {xMinCube, yMaxCube, zMinCube});
        double[] d2 = transform(new double[] {xMaxCube, yMaxCube, zMinCube});
        double[] d3 = transform(new double[] {xMaxCube, yMinCube, zMinCube});
        double[] d4 = transform(new double[] {xMinCube, yMinCube, zMaxCube});
        double[] d5 = transform(new double[] {xMinCube, yMaxCube, zMaxCube});
        double[] d6 = transform(new double[] {xMaxCube, yMaxCube, zMaxCube});
        double[] d7 = transform(new double[] {xMaxCube, yMinCube, zMaxCube});
        double[] d8 = transform(new double[] {Math.abs(xMaxCube-xMinCube)/2+xMinCube, Math.abs(yMaxCube-yMinCube)/2+yMinCube, Math.abs(zMaxCube - zMinCube)/2+ zMinCube});

        List<Face> faces = new ArrayList<>();
        faces.add(new Face("bottom", new double[]{d0[0], d3[0], d2[0], d1[0]}, new double[]{d0[1], d3[1], d2[1], d1[1]}, d0[3]+d3[3]+d2[3]+d1[3]));
        faces.add(new Face("right", new double[]{d2[0], d6[0], d7[0], d3[0]}, new double[]{d2[1], d6[1], d7[1], d3[1]}, d2[3]+d6[3]+d7[3]+d3[3]));
        faces.add(new Face("left", new double[]{d0[0], d1[0], d5[0], d4[0]}, new double[]{d0[1], d1[1], d5[1], d4[1]}, d0[3]+d1[3]+d5[3]+d4[3]));
        faces.add(new Face("back", new double[]{d1[0], d2[0], d6[0], d5[0]}, new double[]{d1[1], d2[1], d6[1], d5[1]}, d1[3]+d2[3]+d6[3]+d5[3]));
        faces.add(new Face("front", new double[]{d0[0], d3[0], d7[0], d4[0]}, new double[]{d0[1], d3[1], d7[1], d4[1]}, d0[3]+d3[3]+d7[3]+d4[3]));
        faces.add(new Face("top", new double[]{d4[0], d5[0], d6[0], d7[0]}, new double[]{d4[1], d5[1], d6[1], d7[1]}, d4[3]+d5[3]+d6[3]+d7[3]));
        Collections.sort(faces, Comparator.comparingDouble(f -> f.z));
        context.setFill(BLACK);
        for (int i = faces.size()-1; i > 2; i--) {
            Face face = faces.get(i);
            context.fillPolygon(face.x, face.y, 4);
        }

        List<double[]> dotList = new ArrayList<>();
        dotList.addAll(Arrays.asList(d0, d1, d2, d3, d4, d5, d6, d7));


        /*
        // orientation points
        context.setFill(RED);
        double radius = 10;
        context.fillOval(d8[0]-radius/2, d8[1]-radius/2, radius,radius);
        for (double[] dot : dotList) {
            context.fillOval(dot[0]-radius/2, dot[1]-radius/2, radius, radius);
        }
        radius = 7;
        context.setFill(GREEN);
        context.fillOval(plotWidth/2-radius/2, plotHeight/2-radius/2, radius,radius);
        */

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
            polygons.addAll(getPolygons(iterX + 1, iterY + 1, zMin, zMax, pointGrid, step, 0, colorList));
            index++;
        }
        Comparator<Polygon> comparator = (Polygon::compareTo);
        polygons.sort(comparator.reversed());
        for (Polygon p : polygons) {
            //p.draw(STEELBLUE.brighter(), STEELBLUE.darker(), zMin, zMax);
            p.draw();
        }

        drawAxes(true, false, false);



    }

    class Face {
        String id;
        double z;
        double[] x;
        double[] y;
        Face(String id, double[] x, double[] y, double z) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
        }

    }




    private void prepareRanges(double[][] in) {
        data = in;

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

        dataMin = new double[] {xMin, yMin};
        dataRange = new double[] {Math.abs(xMax-xMin), Math.abs(yMax-yMin)};
    }



    private List<Polygon> getPolygons(int iterI, int iterJ, double zMin, double zMax, double[][][] pointGrid, double step, double index, List<Color> customColors) {
        List<Polygon> squares = new ArrayList<>();
        double sortMin = Double.MAX_VALUE;
        double sortMax = Double.MIN_VALUE;
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


                double sort = (a[2] + b[2] + c[2] + d[2]) / 4;
                if (sort < sortMin) {
                    sortMin = sort;
                }
                if (sort > sortMax) {
                    sortMax = sort;
                }
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
        //double stretchFactor = Math.abs(maxX-minX)/Math.abs(maxY-minY);
        double stepX = Math.abs(maxX - minX) / iterX;
        double stepY = Math.abs(maxY - minY) / iterX;
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
        return gridList;
    }

    // --------------------------------------------- matrix op ---------------------------------------------

    private double[][] getProjectionMatrix() {
        double[] camera = {0,0,-1};
        double[][] project = multiply(centralProjection(), baseProjection(camera));
        double[][] rotate = lift(multiply(multiply(xRotation(xAngle), yRotation(yAngle)), zRotation(zAngle)));

        double xRange = Math.abs(xMax - xMin);
        double yRange = Math.abs(yMax - yMin);
        double zRange = Math.abs(zMax - zMin);
        double xTranslate = xRange / 2 - xMin;
        double yTranslate = yRange / 2 - yMin;
        double zOffset = zRange/2 + zMin;                      // TODO


        double viewPortFactor = (plotHeight/plotWidth);
        boolean keepAsCube = true;

        double[][] m = scale(1,1, 1/zRange);                                     // 0. prepare z

        m = multiply(translate(-(xRange-xTranslate),-(yRange-yTranslate), -zOffset), m);   // 1. center
        m = multiply(scale(1,1/yRange*xRange, 0.5), m);                          // 2. clamp y to x and normalize z
        m = multiply(rotate, m);                                                           // 3. rotate
        m = multiply(translate(0,0, -zoom), m);                                     // 4. zoom
        m = multiply(xReflect(), m);                                                       // 5. reflect on x-axis
        m = multiply(project, m);                                                          // 6. project
        if (keepAsCube) {
            m = multiply(translate(-(xRange / 2), xRange / 2 * viewPortFactor, 0), m);                        // 7. transform back
            m = multiply(scale(1 / xRange * plotWidth, 1 / xRange * plotWidth, 1 / Math.abs(xMax - zMin)), m);     // 8. adjust to plot dimensions
        } else {
            m = multiply(translate(-(xRange / 2), xRange / 2, 0), m);                        // 7. transform back
            m = multiply(scale(1 / xRange * plotWidth, 1 / xRange * plotWidth * viewPortFactor, 1 / Math.abs(xMax - zMin)), m);     // 8. adjust to plot dimensions
        }
        m = multiply(scale(-1,1,1), m);                                           // 9. switch x

        return m;
    }

    private double[] transform(double[] v) {
        return lower(multiply(matrix, lift(v)));
    }

    public double[][] xRotation(double angle) {
        angle = Math.toRadians(angle);
        return new double[][]{{1, 0, 0}, {0, Math.cos(angle), Math.sin(angle)}, {0, -Math.sin(angle), Math.cos(angle)}};
    }

    public double[][] yRotation(double angle) {
        angle = Math.toRadians(angle);
        return new double[][]{{Math.cos(angle), 0, -Math.sin(angle)}, {0, 1, 0}, {Math.sin(angle), 0, Math.cos(angle)}};
    }

    public double[][] zRotation(double angle) {
        angle = Math.toRadians(angle);
        return new double[][]{{Math.cos(angle), -Math.sin(angle), 0}, {Math.sin(angle), Math.cos(angle), 0}, {0, 0, 1}};
    }

    public double[][] multiply(double[][] a, double[][] b) {     // outer arr is line
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

    public double[][] lift(double[][] m) {
        return new double[][] {{m[0][0], m[0][1], m[0][2], 0}, {m[1][0], m[1][1], m[1][2], 0}, {m[2][0], m[2][1], m[2][2], 0}, {0, 0, 0, 1}};
    }

    public double[] lift(double[] v) {
        double zMin = 4;
        return new double[] {v[0]*zMin, v[1]*zMin, v[2]*zMin, zMin};
    }

    public double[] lower(double[] v) {
        double val = v[3];
        if (val == 0) {
            val = 0.001;
        }
        return new double[] {v[0]/val, v[1]/val, v[2]/val, val};
    }

    public double[][] translate(double x, double y, double z) {
        return new double[][] {{1,0,0,x}, {0,1,0,y}, {0,0,1,z}, {0,0,0,1}};
    }

    public double[][] scale(double x, double y, double z) {
        return new double[][] {{x,0,0,0}, {0,y,0,0}, {0,0,z,0}, {0,0,0,1}};
    }

    public double[][] xReflect() {
        return new double[][] {{1,0,0,0}, {0,-1,0,0}, {0,0,1,0}, {0,0,0,1}};
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

    public void setZoom(double zoom) {
        this.zoom = zoom;
        matrix = getProjectionMatrix();
    }
    public void setXAngle(double xAngle) {
        this.xAngle = xAngle % 360;
        matrix = getProjectionMatrix();
    }
    public void setYAngle(double yAngle) {
        this.yAngle = yAngle % 360;
        matrix = getProjectionMatrix();
    }
    public void setZAngle(double zAngle) {
        this.zAngle = zAngle % 360;
        matrix = getProjectionMatrix();
    }

}

