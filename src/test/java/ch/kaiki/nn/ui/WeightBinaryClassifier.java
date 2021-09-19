package ch.kaiki.nn.ui;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.NNHeatMap;
import ch.kaiki.nn.ui.util.VisualizationMode;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.SceneAntialiasing.BALANCED;
import static javafx.scene.paint.Color.BLACK;

public class WeightBinaryClassifier extends Application {

  //double[][] in = {{0.1, 2.2}, {0.2, 0.22}, {0.3, 0.1}, {0.4, 0.7}, {0.5, 0.8}, {0.45, 0.9}, {0.8, 0.1}, {0.9, 0.15}, {0.8, 0.2}, {0.5, 1.55}};
    //double[][] out = {{1, 0, 0, 0}, {1, 0, 0, 0}, {1, 0, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 1, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
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
            double canvasWidth = 700;
            double canvasHeight = 550;
            root.setBackground(new Background(new BackgroundFill(BLACK, null, null)));
            double canW = canvasWidth;
            double canH = canvasHeight;
            Canvas canvas = new Canvas(canW, canH);
            GraphicsContext context = canvas.getGraphicsContext2D();

            Canvas canvas2 = new Canvas(canvasWidth, canvasHeight);
            GraphicsContext context2 = canvas2.getGraphicsContext2D();
            graphBox.getChildren().add(canvas2);
            graphBox.getChildren().add(canvas);
//# available values: gelu|identity|relu|leaky_relu|sigmoid|sigmoid_accurate|silu|silu_accurate|softplus|tanh|softmax.
            NeuralNetwork net = new NeuralNetwork.Builder( 2, 32,32, 16, 8, 2, 1).setInitializer(Initializer.KAIMING)
                    .setDefaultRectifier(Rectifier.SIGMOID)
                    //.setLastLayerRectifier(Rectifier.TANH)  // SOFTPLUS | TANH
                    .setLearningRate(0.8)
                    .setLearningRateOptimizer(Optimizer.NONE).build();
            int iter = 0;
            int trainIter = 1000;
            double resolution = 0.1;
            double padding = 1.5;


            net.fit(in, out, iter);
            //NNHeatMap heatMap = new NNHeatMap(0,1,Color.SALMON);
            NNHeatMap heatMap = new NNHeatMap(Color.STEELBLUE, Color.TURQUOISE, Color.YELLOW, Color.CRIMSON);
            //NNHeatMap heatMap = new NNHeatMap(Color.BLANCHEDALMOND, Color.LIGHTBLUE, Color.ROSYBROWN, Color.SALMON);
            //heatMap.setOpacity(0.3, 0.8);

            //NNHeatMap heatMap = new NNHeatMap(0,1,Color.BLACK, Color.WHITE);
            AtomicReference<NN3DChart> plot = new AtomicReference<>(new NN3DChart(context));
            plot.get().setInnerDataPadding(padding);
            plot.get().setVisualizationMode(VisualizationMode.CUBE);
            plot.get().setAxisLabels("x-Axis", "y-Axis", "z-Axis");
            plot.get().setTitle("Decision Boundary Visualization 3D");
            plot.get().enableMouseInteraction();
            plot.get().showLegend(true);
            plot.get().showBorder(true);
            plot.get().showTickMarkLabels(false);
            plot.get().showGridContent(false);
            plot.get().showGrid(true);
            //plot.get().setAnimated(true);
            //plot.get().setAnimated(true);
/*
            NNMeshGrid plot2 = new NNMeshGrid(context2);
            plot2.setPadding(0,0,50,0,padding);
            plot2.plot(net, in, resolution,1,true,true,true, heatMap);
            plot(plot, net, resolution, heatMap);*/

            NN2DChart plot2 = new NN2DChart(context2);
            plot2.setInnerDataPadding(padding);
            plot2.setTitle("Decision Boundary Visualization 2D");
            plot2.enableMouseInteraction();
            plot2.showLegend(true);
            plot2.showBorder(true);
            plot2.setAxisLabels("x-Axis", "y-Axis");
            plot2.plotWeights(net,heatMap);
            plot(plot, net, resolution, heatMap);

            Button train = new Button("TRAIN");
            train.setOnAction(e -> {
                net.fit(in, out, trainIter);
                plot(plot, net, resolution, heatMap);
                plot2.plotWeights(net,heatMap);
            });
            cBox.getChildren().add(train);

            Scene scene = new Scene(root, root.getWidth(), root.getHeight(), false, BALANCED);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void plot(AtomicReference<NN3DChart> plot, NeuralNetwork net, double resolution, NNHeatMap heatMap) {
        plot.get().plotWeights(net,heatMap);
    }


}
