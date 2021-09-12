package ch.kaiki.nn.ui;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.NNHeatMap;
import ch.kaiki.nn.ui.deprecated.NN3DPlot;
import ch.kaiki.nn.ui.deprecated.NNMeshGrid;
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


            Button train = new Button("TRAIN");
            train.setOnAction(e -> {
                net.fit(in, out, 1);
                plot.get().plot(net, in, resolution,1,false, heatMap);
                plot2.plotCost(net, in, resolution,1,true,true,true, heatMap);
            });
            cBox.getChildren().add(train);


            primaryStage.setScene(new Scene(root, root.getWidth(), root.getHeight()-150, true, BALANCED));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
