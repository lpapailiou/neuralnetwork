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

public class ThreeDeeTestMulticlass extends Application {

    double[][] in = {{0.1, 2.2}, {0.2, 0.22}, {0.3, 0.1}, {0.4, 0.7}, {0.5, 0.8}, {0.45, 0.9}, {0.8, 0.1}, {0.9, 0.15}, {0.8, 0.2}, {0.5, 1.55}};
    double[][] out = {{1, 0, 0, 0}, {1, 0, 0, 0}, {1, 0, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 1, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};

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
            double canvasWidth = 350;
            double canvasHeight = 350;
            double offset = 80;

            double canW = canvasWidth*1 + 2 * offset;
            double canH = canvasHeight*1.5 + 2 * offset;
            Canvas canvas = new Canvas(canW, canH);
            GraphicsContext context = canvas.getGraphicsContext2D();

            Canvas canvas2 = new Canvas(canvasWidth + 2 * offset, canvasWidth + 2 * offset);
            GraphicsContext context2 = canvas2.getGraphicsContext2D();
            graphBox.getChildren().add(canvas2);
            graphBox.getChildren().add(canvas);

            NeuralNetwork net = new NeuralNetwork.Builder( 2, 10, 4).setInitializer(Initializer.KAIMING)
                    .setDefaultRectifier(Rectifier.SIGMOID)
                    .setLearningRate(0.5)
                    .setLearningRateOptimizer(Optimizer.NONE).build();
            int iter = 0;
            int trainIter = 100;
            double resolution = 0.1;
            double padding = 0;
            double step = 0.1;
            double angleStep = 5;

            net.fit(in, out, iter);
            NNHeatMap heatMap = new NNHeatMap(-1,1,Color.STEELBLUE, Color.TURQUOISE, Color.YELLOW, Color.CRIMSON);
            //NNHeatMap heatMap = new NNHeatMap(0,1,Color.BLACK, Color.WHITE);
            System.out.println("3D support? "  +canvas.getDepthTest());
            AtomicReference<NN3DPlot> plot = new AtomicReference<>(new NN3DPlot(context));
            plot.get().setPadding(0,0,0, 0, padding);

            NNMeshGrid plot2 = new NNMeshGrid(context2);
            plot2.setPadding(0,0,50,0,padding);
            plot2.plot(net, in, resolution,1,true,true,true, heatMap);
            plot(plot, net, resolution, heatMap);


            Button train = new Button("TRAIN");
            train.setOnAction(e -> {
                net.fit(in, out, trainIter);
                plot(plot, net, resolution, heatMap);
                plot2.plot(net, in, resolution,1,true,true,true, heatMap);
            });
            cBox.getChildren().add(train);


            primaryStage.setScene(new Scene(root, root.getWidth(), root.getHeight(), true, BALANCED));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void plot(AtomicReference<NN3DPlot> plot, NeuralNetwork net, double resolution, NNHeatMap heatMap) {
        plot.get().plot(net, in, resolution,1,true, heatMap);
    }


}
