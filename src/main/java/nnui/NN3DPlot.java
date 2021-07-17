package nnui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import nn.NeuralNetwork;
import nnui.color.NNDataColor;
import nnui.color.NNHeatMap;

import java.util.*;

import static javafx.scene.paint.Color.*;
import static nnui.color.NNColorSupport.blend;

public class NN3DPlot extends Plot {

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

    public NN3DPlot(GraphicsContext context) {
        super(context);
        matrix = getProjectionMatrix();
    }

    public void plot(double[][] transformationMatrix, NeuralNetwork neuralNetwork, double[][] in, double resolution, double opacity, boolean axes, NNDataColor dataColor) {
        matrix = transformationMatrix;
        plot(neuralNetwork, in, resolution, opacity, axes, dataColor);
    }

    public void plot(NeuralNetwork neuralNetwork, double[][] in, double resolution, double opacity, boolean axes, NNDataColor dataColor) {
        clear();
        int[] configuration = neuralNetwork.getConfiguration();
        if (configuration[0] != 2) {
            throw new IllegalArgumentException("Decision boundaries can only be plotted for 2-dimensional inputs!");
        }

        prepareRanges(in);
        plotBackgroundColor = TRANSPARENT;
        cachedPadding = padding;
        this.dataColor = dataColor;

        processData(resolution, configuration, neuralNetwork);

        drawOverlay(opacity);
        drawAxes(axes, false, false);
        setTitle(title);

        padding = cachedPadding;
    }

    private void processData(double resolution, int[] configuration, NeuralNetwork neuralNetwork) {
        int iterX = (int) Math.ceil(plotWidth * resolution);
        int iterY = (int) Math.ceil(plotHeight * resolution);
        ForwardPropEntity[][] data = new ForwardPropEntity[iterX+1][iterY+1];

        double stepX = 1.0 / iterX;
        double stepY = 1.0 / iterY;
        double x = 0;
        double y = 0;
        int outLength = configuration[configuration.length-1];
        double[] minZ = new double[outLength];
        double[] maxZ = new double[outLength];
        for (int i = 0; i < outLength; i++) {
            minZ[i] = Double.MAX_VALUE;
            maxZ[i] = Double.MIN_VALUE;
        }

        for (int i = 0; i <= iterX; i++) {
            for (int j = 0; j <= iterY; j++) {
                double[] input = {scaleX(x), scaleY(y)};
                y += stepY;
                List<Double> output = neuralNetwork.predict(input);
                for (int k = 0; k < output.size(); k++) {
                    double out = output.get(k);
                    if (out < minZ[k]) {
                        minZ[k] = out;
                    }
                    if (out > maxZ[k]) {
                        maxZ[k] = out;
                    }
                }
                data[i][j] = new ForwardPropEntity(input[0], input[1], output);
            }
            y = 0;
            x += stepX;
        }

        padding = 0;

        drawBackground();
        if (configuration[configuration.length - 1] == 1) {
            if (dataColor.getColors().size() < 2) {
                throw new IllegalArgumentException("At least 2 data color items must be provided!");
            }
            plotBinaryClassifierDecisionBoundaries(data, iterX+1, iterY+1, minZ[0], maxZ[0]);
        } else {
            if (dataColor.getColors().size() != configuration[configuration.length-1]) {
                throw new IllegalArgumentException("Count of data color items " + dataColor.getColors().size() + " must match output class dimensions " + configuration[configuration.length-1] + "!");
            }
            //plotMultiClassClassifierDecisionBoundaries(forwardPropEntities, xOffset, yOffset);
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

    private void plotBinaryClassifierDecisionBoundaries(ForwardPropEntity[][] forwardPropEntities, int iterI, int iterJ, double zMin, double zMax) {
        List<Color> customColors = dataColor.getColors();
        double zFactor =  1 / (cachedPadding+1);
        if (cachedPadding < 1) {
            zFactor = 0.5;
        }
        if (dataColor instanceof NNHeatMap && ((NNHeatMap) dataColor).isScaled()) {
            zMin = ((NNHeatMap) dataColor).getMin();
            zMax = ((NNHeatMap) dataColor).getMax();
        }
        double[][][] pointGrid = new double[iterI][iterJ][];
        double range = Math.abs(zMax - zMin);
        double step = range / (customColors.size()-1);
        for (int ii = 0; ii < iterI; ii++) {
            for (int jj = 0; jj < iterJ; jj++) {
                double output = forwardPropEntities[ii][jj].getOutput().get(0);
                double[] t = transform(new double[] {rescaleX(forwardPropEntities[ii][jj].getX()), rescaleY(forwardPropEntities[ii][jj].getY()), output * zFactor});
                pointGrid[ii][jj] = new double[]{-x(t[0]), y(t[1]), t[3], output};
            }
        }


        List<Polygon> squares = getPolygons(iterI, iterJ, zMin, zMax, pointGrid, step, 0);
        Comparator<Polygon> comparator = (Polygon::compareTo);
        squares.sort(comparator.reversed());
        for (Polygon p : squares) {
            //p.draw(STEELBLUE.brighter(), STEELBLUE.darker(), sortMin, sortMax);
            p.draw();
        }

    }

    private List<Polygon> getPolygons(int iterI, int iterJ, double zMin, double zMax, double[][][] pointGrid, double step, double index) {
        List<Color> customColors = dataColor.getColors();
        List<Polygon> squares = new ArrayList<>();
        double sortMin = Double.MAX_VALUE;
        double sortMax = Double.MIN_VALUE;
        for (int i = 0; i < iterI-1; i++) {
            for (int j = 0; j < iterJ-1; j++) {
                double[] a = pointGrid[i][j];
                double[] b = pointGrid[i+1][j];
                double[] c = pointGrid[i+1][j+1];
                double[] d = pointGrid[i][j+1];

                double pos = 0.5;
                double neg = -0.5;
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
                int stepIndex = 0;
                double value = zMin;
                for (int k = 0; k < customColors.size()-1; k++) {
                    value += step;
                    if (output <= value || k == customColors.size()-2) {
                        stepIndex = k;
                        break;
                    }
                }
                double ratio = 1 / step * Math.abs(value - output);
                if (output > zMax) {
                    ratio = 0;
                }
                color = blend(customColors.get(stepIndex), customColors.get(stepIndex+1), ratio);
                squares.add(new Polygon(context, xEs, ys, sort, color));
            }
        }
        return squares;
    }


    public double[][] getProjectionMatrix() {
        double[] camera = {0,0,-1};
        double[][] translate = translate(-0.5,0.5, -0);
        double[][] rotate = lift(multiply(multiply(xRotation(xAngle), yRotation(yAngle)), zRotation(zAngle)));

        double[][] m = rotate;
        m = multiply(m, baseProjection(camera));
        m = multiply(translate(0,0, -zoom), m);

        m = multiply(centralProjection(), m);
        m = multiply(translate, m);
        return m;
    }

    private double[] transform(double[] v) {
        return lower(multiply(matrix, lift(v)));
    }

    private void plotMultiClassClassifierDecisionBoundaries(List<ForwardPropEntity> forwardPropEntities, double xOffset, double yOffset) {
        List<Color> customColors = dataColor.getColors();
        for (ForwardPropEntity forwardPropEntity : forwardPropEntities) {
            double x = x(forwardPropEntity.getX());
            double y = y(forwardPropEntity.getY());
            List<Double> output = forwardPropEntity.getOutput();
            Color color = new Color(plotBackgroundColor.getRed(), plotBackgroundColor.getGreen(), plotBackgroundColor.getBlue(), 0);
            for (int i = 0; i < output.size(); i++) {
                double value = output.get(i);
                if (value <= 0.5) {
                    color = blend(color, customColors.get(i), 1 - (value * 2));
                } else {
                    color = blend(customColors.get(i), customColors.get(i), (value - 0.5) * 2);
                }
            }
            context.setFill(color);
            context.fillRect(x - xOffset/2 , y - yOffset/2, xOffset, yOffset);
        }
    }

    public void plotData(double[][] definedClasses, double radius) {
        List<Color> customColors = dataColor.getColors();
        //double innerRadius = radius * 0.8;
        if (data == null || data[0].length != 2) {
            throw new IllegalArgumentException("Data must be 2-dimensional!");
        }

        cachedPadding = padding;
        padding = 0;
        List<String> classes = new ArrayList<>();
        for (double[] o : definedClasses) {
            String outStr = Arrays.toString(o);
            if (!classes.contains(outStr)) {
                classes.add(outStr);
            }
        }
        List<Color> colors = new ArrayList<>();
        if (definedClasses[0].length == 1) {
            colors.add(customColors.get(0));
            colors.add(customColors.get(customColors.size()-1));

        } else {
            colors = customColors;
        }
        for (int i = 0; i < data.length; i++) {
            double[] outClass = definedClasses[i];
            Color innerColor = colors.get(classes.indexOf(Arrays.toString(outClass)));
            context.setFill(innerColor);
            context.fillOval(x(data[i][0]) - radius / 2, y(data[i][1]) - radius / 2, radius, radius);
        }

        padding = cachedPadding;
    }

    private double scaleX(double x) {
        return x * ((1 + padding) * dataRange[0]) + dataMin[0] - (dataRange[0] * (padding/2));
    }

    private double scaleY(double y) {
        return y * ((1 + padding) * dataRange[1]) + dataMin[1] - (dataRange[1] * (padding/2));
    }

    private double rescaleX(double x) {
        return (x - dataMin[0] + (dataRange[0] * (cachedPadding/2))) / ((1 + cachedPadding) * dataRange[0]) - 0.5;
    }

    private double rescaleY(double y) {
        return (y - dataMin[1] + (dataRange[1] * (cachedPadding/2))) / ((1 + cachedPadding) * dataRange[1]) - 0.5;
    }
/*
    class ForwardPropData {

        private List<ForwardPropEntity> data = new ArrayList<>();

        public void add(double[] in, List<Double> out) {
            data.add(new ForwardPropEntity(in[0], in[1], out));
        }

        public List<ForwardPropEntity> get() {
            return data;
        }

    }*/

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

