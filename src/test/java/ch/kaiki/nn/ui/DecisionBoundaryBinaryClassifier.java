package ch.kaiki.nn.ui;

import ch.kaiki.nn.data.Dataset;
import ch.kaiki.nn.data.DatasetType;
import ch.kaiki.nn.neuralnet.BatchMode;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.NNHeatMap;
import ch.kaiki.nn.ui.util.VisualizationMode;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.SceneAntialiasing.BALANCED;
import static javafx.scene.paint.Color.BLACK;

public class DecisionBoundaryBinaryClassifier extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            primaryStage.setTitle("Decision Boundary Sample (binary)");
            VBox root = new VBox();
            root.setSpacing(10);
            root.setBackground(new Background(new BackgroundFill(BLACK, null, null)));
            HBox visualizationBox = new HBox();
            visualizationBox.setSpacing(10);
            visualizationBox.setPadding(new Insets(20, 20, 20, 20));
            root.getChildren().add(visualizationBox);
            VBox controls = new VBox();
            controls.setSpacing(10);
            controls.setPadding(new Insets(20, 20, 20, 20));
            root.getChildren().add(controls);

            double canvasWidth = 700;
            double canvasHeight = 550;
            Canvas canvasLeft = new Canvas(canvasWidth, canvasHeight);
            Canvas canvasRight = new Canvas(canvasWidth, canvasHeight);
            visualizationBox.getChildren().add(canvasLeft);
            visualizationBox.getChildren().add(canvasRight);

            Dataset dataset = new Dataset(DatasetType.XOR);
            double[][] in = dataset.getX();
            double[][] out = dataset.getY();

            NeuralNetwork neuralNetwork = new NeuralNetwork.Builder( 2, 32,32, 1).setInitializer(Initializer.KAIMING)
                    .setDefaultRectifier(Rectifier.SIGMOID)
                    //.setLastLayerRectifier(Rectifier.SOFTPLUS)  // SOFTPLUS | TANH
                    .setLearningRate(0.5)
                    .setLearningRateOptimizer(Optimizer.NONE).build();

            int iter = 100;
            int trainIter = 20;
            double resolution = 0.18;
            double padding = 3;
            int batchSize = 1;

            neuralNetwork.fit(in, out, iter, batchSize);

            NNHeatMap heatMap = new NNHeatMap(0,1,Color.STEELBLUE, Color.TURQUOISE, Color.YELLOW, Color.CRIMSON);

            NN2DPlot plot2D = new NN2DPlot(canvasLeft.getGraphicsContext2D());
            plot2D.setTitle("Decision Boundary Visualization 2D");
            plot2D.enableMouseInteraction();
            plot2D.showLegend(true);
            plot2D.setAxisLabels("x-Axis", "y-Axis");


            NN3DPlot plot3D = new NN3DPlot(canvasRight.getGraphicsContext2D());
            plot3D.setVisualizationMode(VisualizationMode.CUBE);
            plot3D.setAxisLabels("x-Axis", "y-Axis", "z-Axis");
            plot3D.setTitle("Decision Boundary Visualization 3D");
            plot3D.showLegend(true);
            plot3D.showBorder(true);
            plot3D.enableMouseInteraction();


            plot(plot2D, plot3D,  neuralNetwork, in, out, heatMap, resolution, padding);

            Button train = new Button("FIT (x" + trainIter + ")");
            train.setOnAction(e -> {
                neuralNetwork.fit(in, out, trainIter, batchSize);
                plot(plot2D, plot3D,  neuralNetwork, in, out, heatMap, resolution, padding);
            });
            controls.getChildren().add(train);


            Scene scene = new Scene(root, canvasWidth*2 + 60, canvasHeight + 120, false, BALANCED);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void plot(NN2DPlot plot2D, NN3DPlot plot3D,  NeuralNetwork net, double[][] in, double[][] out, NNHeatMap heatMap, double resolution, double padding) {
        plot2D.plotDecisionBoundaries(net, in, out, true, heatMap, resolution, padding);
        plot3D.plotDecisionBoundaries(net, in, out, true, heatMap, resolution, padding);
    }


}
