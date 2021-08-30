package ch.kaiki.nn.ui;

import ch.kaiki.nn.data.BackPropEntity;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.util.Point;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;

import static ch.kaiki.nn.ui.color.NNColorSupport.blend;
import static javafx.scene.paint.Color.*;

public class Chart3d extends Application {

    // size of graph
    int size = 400;

    // variables for mouse interaction
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};


    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(TRANSPARENT, null, null)));

        NNChart chart = new NNChart(800, 400, true, true, true, 1);

        NeuralNetwork net1 = new NeuralNetwork.Builder(2, 10, 1)
                .setInitializer(Initializer.KAIMING)
                .setLearningRate(0.8)
                .setDefaultRectifier(Rectifier.SIGMOID)
                .setLearningRateOptimizer(Optimizer.NONE).build();
        int iter = 50;
        net1.fit(in, out, iter);
        List<Point> data1 =  new ArrayList<>();
        SortedMap<Integer, BackPropEntity> rawData = net1.getBackPropData().getMap();
        Function<BackPropEntity, Double> function = BackPropEntity::getCost;
        double x = 0;
        double y = 0;
        for (Integer key : rawData.keySet()) {
            x = (double) key;
            y += function.apply(rawData.get(key));
            data1.add(new Point(x, y));
            y = 0;
        }


        List<Point> data2 =  Arrays.asList(new Point(1,2), new Point(1.5,2), new Point(1,1));
        List<Point> data3 =  Arrays.asList(new Point(1,345));

        chart.setXAxisLabel("x-Axis");
        chart.setYAxisLabel("y-Axis");
        //rotateX.pivotXProperty().bind(Bindings.createDoubleBinding());
        chart.plot("this is test 1", data1, BLUE);
        chart.plot("test 2", data2, RED);
        root.getChildren().addAll(chart);

        // scene
        Scene scene = new Scene(root, 800, 400, true, SceneAntialiasing.DISABLED);
        chart.setStyle("-fx-border-width: 2;-fx-border-color: red");
        root.setStyle("-fx-border-width: 2;-fx-border-color: blue");
        //makeZoomable(root);
        //makeZoomable(scene, chart);

        PerspectiveCamera camera = new PerspectiveCamera(false);
        camera.setTranslateZ(-100);
        camera.setNearClip(0.1);
        camera.setFarClip(100000);
        scene.setCamera(camera);

        scene.setOnMousePressed(me -> {
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();

        });
        scene.setOnMouseDragged(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            chart.getRotateX().setAngle(chart.getRotateX().getAngle() - (mousePosY - mouseOldY));
            chart.getRotateZ().setAngle(chart.getRotateZ().getAngle() + (mousePosX - mouseOldX));
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;

        });
/*
        scene.setOnMousePressed(e -> {
            mousePosX = e.getSceneX();
            mousePosY = e.getSceneY();
            data1.add(new Point(mouseOldX, mouseOldY));
            chart.plot("test 2", data1, RED);
        });*/
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);



        primaryStage.show();


    }
    public void makeZoomable(Scene scene, Pane control) {
        final double MAX_SCALE = 20.0;
        final double MIN_SCALE = 0.1;
        scene.addEventFilter(ScrollEvent.ANY, event -> {
            double delta = 1.2;
            double scale = control.getScaleX();
            if (event.getDeltaY() < 0) {
                scale /= delta;
            } else {
                scale *= delta;
            }
            scale = clamp(scale, MIN_SCALE, MAX_SCALE);
            control.setScaleX(scale);
            System.out.println(control.getScaleX());
            control.setScaleY(scale);
            //event.consume();
        });
    }
    /**
     * Create texture for uv mapping
     * @param size
     * @param noise
     * @return
     */
    public Image createImage(double size, float[][] noise) {

        int width = (int) size;
        int height = (int) size;

        WritableImage wr = new WritableImage(width, height);
        PixelWriter pw = wr.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                float value = noise[x][y];

                double gray = normalizeValue(value, -.5, .5, 0., 1.);

                gray = clamp(gray, 0, 1);

                Color color = RED.interpolate(Color.YELLOW, gray);

                pw.setColor(x, y, color);

            }
        }

        return wr;

    }

    /**
     * Axis wall
     */
    public static class Axis extends Pane {

        Rectangle wall;

        public Axis(double size) {

            // wall
            // first the wall, then the lines => overlapping of lines over walls
            // works
            wall = new Rectangle(size, size);
            getChildren().add(wall);

            // grid
            double zTranslate = 0;
            double lineWidth = 1.0;
            Color gridColor = Color.WHITE;

            for (int y = 0; y <= size; y += size / 10) {

                Line line = new Line(0, 0, size, 0);
                line.setStroke(gridColor);
                line.setFill(gridColor);
                line.setTranslateY(y);
                line.setTranslateZ(zTranslate);
                line.setStrokeWidth(lineWidth);

                getChildren().addAll(line);

            }

            for (int x = 0; x <= size; x += size / 10) {

                Line line = new Line(0, 0, 0, size);
                line.setStroke(gridColor);
                line.setFill(gridColor);
                line.setTranslateX(x);
                line.setTranslateZ(zTranslate);
                line.setStrokeWidth(lineWidth);

                getChildren().addAll(line);

            }

            // labels
            // TODO: for some reason the text makes the wall have an offset
            // for( int y=0; y <= size; y+=size/10) {
            //
            // Text text = new Text( ""+y);
            // text.setTranslateX(size + 10);
            //
            // text.setTranslateY(y);
            // text.setTranslateZ(zTranslate);
            //
            // getChildren().addAll(text);
            //
            // }

        }

        public void setFill(Paint paint) {
            wall.setFill(paint);
        }

    }



    /**
     * Create axis walls
     * @param size
     * @return
     */
    private Group createCube(int size) {

        Group cube = new Group();

        // size of the cube
        Color color = blend(LIGHTGRAY.brighter(), TRANSPARENT, 0.05);

        List<Axis> cubeFaces = new ArrayList<>();
        Axis face;

        // back face
        face = new Axis(size);
        face.setId("back");
        face.setFill(color.deriveColor(0.0, 1.0, (1 - 0.5 * 1), 1.0));
        face.setTranslateX(-0.5 * size);
        face.setTranslateY(-0.5 * size);
        face.setTranslateZ(0.5 * size);
        cubeFaces.add(face);

        // bottom face
        face = new Axis(size);
        face.setId("bottom");
        face.setFill(color.deriveColor(0.0, 1.0, (1 - 0.4 * 1), 1.0));
        face.setTranslateX(-0.5 * size);
        face.setTranslateY(0);
        face.setRotationAxis(Rotate.X_AXIS);
        face.setRotate(90);

        cubeFaces.add(face);

        // right face
        face = new Axis(size);
        face.setId("right");
        face.setFill(color.deriveColor(0.0, 1.0, (1 - 0.3 * 1), 1.0));
        face.setTranslateX(-1 * size);
        face.setTranslateY(-0.5 * size);
        face.setRotationAxis(Rotate.Y_AXIS);
        face.setRotate(90);

         cubeFaces.add(face);   // TODO: disable

        // left face
        face = new Axis(size);
        face.setId("left");
        face.setFill(color.deriveColor(0.0, 1.0, (1 - 0.2 * 1), 1.0));
        face.setTranslateX(0);
        face.setTranslateY(-0.5 * size);
        face.setRotationAxis(Rotate.Y_AXIS);
        face.setRotate(90);

        cubeFaces.add(face);

        // top face
        face = new Axis(size);
        face.setId("top");
        face.setFill(color.deriveColor(0.0, 1.0, (1 - 0.1 * 1), 1.0));

        face.setTranslateX(-0.5 * size);
        face.setTranslateY(-1 * size);
        face.setRotationAxis(Rotate.X_AXIS);
        face.setRotate(90);

         cubeFaces.add(face);  // TODO: disable

        // front face
        face = new Axis(size);
        face.setId("front");
        face.setFill(color.deriveColor(0.0, 1.0, (1 - 0.1 * 1), 1.0));
        face.setFill(RED);
        face.setTranslateX(-0.5 * size);
        face.setTranslateY(-0.5 * size);
        face.setTranslateZ(-0.5 * size);

        cubeFaces.add(face);   // TODO: disable

        cube.getChildren().addAll(cubeFaces);

        return cube;
    }

    /**
     * Create an array of the given size with values of perlin noise
     * @param size
     * @return
     */
    private float[][] createNoise( int size) {
        float[][] noiseArray = new float[(int) size][(int) size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {

                double frequency = 10.0 / (double) size;

                double noise = ImprovedNoise.noise(x * frequency, y * frequency, 0);

                noiseArray[x][y] = (float) noise;
            }
        }

        return noiseArray;

    }

    public static double normalizeValue(double value, double min, double max, double newMin, double newMax) {

        return (value - min) * (newMax - newMin) / (max - min) + newMin;

    }

    public static double clamp(double value, double min, double max) {
        if (Double.compare(value, min) < 0)
            return min;
        if (Double.compare(value, max) > 0)
            return max;
        return value;
    }


    /**
     * Perlin noise generator
     *
     * // JAVA REFERENCE IMPLEMENTATION OF IMPROVED NOISE - COPYRIGHT 2002 KEN PERLIN.
     * // http://mrl.nyu.edu/~perlin/paper445.pdf
     * // http://mrl.nyu.edu/~perlin/noise/
     */
    public final static class ImprovedNoise {
        static public double noise(double x, double y, double z) {
            int X = (int)Math.floor(x) & 255,                  // FIND UNIT CUBE THAT
                    Y = (int)Math.floor(y) & 255,                  // CONTAINS POINT.
                    Z = (int)Math.floor(z) & 255;
            x -= Math.floor(x);                                // FIND RELATIVE X,Y,Z
            y -= Math.floor(y);                                // OF POINT IN CUBE.
            z -= Math.floor(z);
            double u = fade(x),                                // COMPUTE FADE CURVES
                    v = fade(y),                                // FOR EACH OF X,Y,Z.
                    w = fade(z);
            int A = p[X  ]+Y, AA = p[A]+Z, AB = p[A+1]+Z,      // HASH COORDINATES OF
                    B = p[X+1]+Y, BA = p[B]+Z, BB = p[B+1]+Z;      // THE 8 CUBE CORNERS,

            return lerp(w, lerp(v, lerp(u, grad(p[AA  ], x  , y  , z   ),  // AND ADD
                    grad(p[BA  ], x-1, y  , z   )), // BLENDED
                    lerp(u, grad(p[AB  ], x  , y-1, z   ),  // RESULTS
                            grad(p[BB  ], x-1, y-1, z   ))),// FROM  8
                    lerp(v, lerp(u, grad(p[AA+1], x  , y  , z-1 ),  // CORNERS
                            grad(p[BA+1], x-1, y  , z-1 )), // OF CUBE
                            lerp(u, grad(p[AB+1], x  , y-1, z-1 ),
                                    grad(p[BB+1], x-1, y-1, z-1 ))));
        }
        static double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
        static double lerp(double t, double a, double b) { return a + t * (b - a); }
        static double grad(int hash, double x, double y, double z) {
            int h = hash & 15;                      // CONVERT LO 4 BITS OF HASH CODE
            double u = h<8 ? x : y,                 // INTO 12 GRADIENT DIRECTIONS.
                    v = h<4 ? y : h==12||h==14 ? x : z;
            return ((h&1) == 0 ? u : -u) + ((h&2) == 0 ? v : -v);
        }
        static final int p[] = new int[512], permutation[] = { 151,160,137,91,90,15,
                131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
                190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
                88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
                77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
                102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
                135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
                5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
                223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
                129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
                251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
                49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
                138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
        };
        static { for (int i=0; i < 256 ; i++) p[256+i] = p[i] = permutation[i]; }
    }

    public static void main(String[] args) {
        launch(args);
    }


}