package ch.kaiki.nn.ui;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.NNHeatMap;
import ch.kaiki.nn.ui.deprecated.NN3DPlot;
import ch.kaiki.nn.ui.deprecated.NNMeshGrid;
import ch.kaiki.nn.ui.icon.IconLoader;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.DepthTest;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.SceneAntialiasing.BALANCED;

public class ThreeDee3Test extends Application {

    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            primaryStage.setTitle("3D test");
            VBox root = new VBox();
            root.setDepthTest(DepthTest.ENABLE);
            root.setSpacing(10);
            HBox graphBox = new HBox();
            graphBox.setSpacing(10);
            graphBox.setPadding(new Insets(20, 20, 20, 20));
            root.getChildren().add(graphBox);
            HBox btnContainer = new HBox();
            btnContainer.setSpacing(10);
            btnContainer.setPadding(new Insets(20, 20, 20, 20));
            root.getChildren().add(btnContainer);
            VBox cBox = new VBox();
            cBox.setSpacing(10);
            cBox.setPadding(new Insets(20, 20, 20, 20));
            btnContainer.getChildren().add(cBox);
            VBox tBox = new VBox();
            tBox.setSpacing(10);
            tBox.setPadding(new Insets(20, 20, 20, 20));
            btnContainer.getChildren().add(tBox);
            VBox dBox = new VBox();
            dBox.setSpacing(10);
            dBox.setPadding(new Insets(20, 20, 20, 20));
            btnContainer.getChildren().add(dBox);
            double canvasWidth = 500;
            double canvasHeight = 500;
            double offset = 80;
            Canvas canvas = new Canvas(canvasWidth + 2 * offset, canvasHeight + 2 * offset);
            GraphicsContext context = canvas.getGraphicsContext2D();

            Canvas canvas2 = new Canvas(canvasWidth + 2 * offset, canvasWidth + 2 * offset);
            GraphicsContext context2 = canvas2.getGraphicsContext2D();
            graphBox.getChildren().add(canvas2);
            graphBox.getChildren().add(canvas);

            NeuralNetwork net = new NeuralNetwork.Builder( 2, 10, 1).setInitializer(Initializer.KAIMING)
                    .setDefaultRectifier(Rectifier.SIGMOID)
                    .setLearningRate(0.5)
                    .setLearningRateOptimizer(Optimizer.NONE).build();
            int iter = 50;
            double resolution = 0.05;
            double padding = 1;
            double step = 0.1;
            double angleStep = 5;
            net.fit(in, out, iter);
            NNHeatMap heatMap = new NNHeatMap(0,1,Color.STEELBLUE, Color.TURQUOISE, Color.YELLOW, Color.CRIMSON);
            //NNHeatMap heatMap = new NNHeatMap(0,1,Color.BLACK, Color.WHITE);
            System.out.println("3D support? "  +canvas.getDepthTest());
            AtomicReference<NN3DPlot> plot = new AtomicReference<>(new NN3DPlot(context));
            plot.get().setPadding(0,0,50, 0, padding);
            plot.get().setZoom(7*step);
            plot.get().setZAngle(-0*angleStep);
            plot.get().setXAngle(10*angleStep);
            plot.get().plot(net, in, resolution,1,false, heatMap);
            NNMeshGrid plot2 = new NNMeshGrid(context2);
            plot2.setPadding(0,0,50,0,padding);
            plot2.plotCost(net, in, resolution,1,true,true,true, heatMap);

            AtomicReference<Double> tz = new AtomicReference<>((double) 7*step);
            AtomicReference<Double> dx = new AtomicReference<>((double) 10*angleStep);
            AtomicReference<Double> dy = new AtomicReference<>((double) 0);
            AtomicReference<Double> dz = new AtomicReference<>((double) -0*angleStep);


            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 0.67, ty: 1, tz: -1.45           // padding 0
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 0.84, ty: 1.0, tz: -1.63         // padding 0.1
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 1.05, ty: 1.0, tz: -1.86         // padding 0.2
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 1.73, ty: 1.0, tz: -2.52         // padding 0.5
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 3.3, ty: 1.0, tz: -3.87          // padding 1
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 8.24, ty: 1.0, tz: -7.64         // padding 2
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 25.5, ty: 1.0, tz: -18         // padding 5

            Button train = new Button("TRAIN");
            train.setOnAction(e -> {
                net.fit(in, out, 1);
                plot.get().plot(net, in, resolution,1,false, heatMap);
                plot2.plotCost(net, in, resolution,1,true,true,true, heatMap);
            });
            cBox.getChildren().add(train);
            Button btnctz = new Button();
            btnctz.setGraphic(IconLoader.getPlus());
            btnctz.setOnAction(e -> {
                tz.updateAndGet(v -> new Double((double) (v - step)));
                plot.get().setZoom(tz.get());
                plot.get().plot(net, in, resolution,1,false, heatMap);
            });
            tBox.getChildren().add(btnctz);
            Button btnctzm = new Button();
            btnctzm.setGraphic(IconLoader.getMinus());
            btnctzm.setScaleShape(true);
            btnctzm.setOnAction(e -> {
                tz.updateAndGet(v -> new Double((double) (v + step)));
                plot.get().setZoom(tz.get());
                plot.get().plot(net, in, resolution,1,false, heatMap);
            });
            tBox.getChildren().add(btnctzm);
            Button btncdx = new Button("rotate x +");
            btncdx.setOnAction(e -> {
                dx.updateAndGet(v -> new Double((double) (v + angleStep)));
                plot.get().setXAngle(dx.get());
                plot.get().plot(net, in, resolution,1,false, heatMap);
            });
            dBox.getChildren().add(btncdx);
            Button btncdxm = new Button("rotate x -");
            btncdxm.setOnAction(e -> {
                dx.updateAndGet(v -> new Double((double) (v - angleStep)));
                plot.get().setXAngle(dx.get());
                plot.get().plot(net, in, resolution,1,false, heatMap);
            });
            dBox.getChildren().add(btncdxm);
            Button btncdz = new Button("rotate z +");
            btncdz.setOnAction(e -> {
                dz.updateAndGet(v -> new Double((double) (v + angleStep)));
                plot.get().setZAngle(dz.get());
                plot.get().plot(net, in, resolution,1,false, heatMap);
            });
            dBox.getChildren().add(btncdz);
            Button btnctdzm = new Button("rotate z -");
            btnctdzm.setOnAction(e -> {
                dz.updateAndGet(v -> new Double((double) (v - angleStep)));
                plot.get().setZAngle(dz.get());
                plot.get().plot(net, in, resolution,1,false, heatMap);
            });
            dBox.getChildren().add(btnctdzm);

            /*
            public static double[][] getProjectionMatrix2() {


        m = NN3DPlot.multiply(m, translate(0,0, camera[2]));
        m = NN3DPlot.multiply(centralProjection(), m);
        return m;
    }
             */


            //System.out.println(net);

            primaryStage.setScene(new Scene(root, root.getWidth(), root.getHeight()-150, true, BALANCED));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
