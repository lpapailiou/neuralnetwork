package ch.kaiki.nn.ui;

import ch.kaiki.nn.data.BackPropEntity;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.deprecated.NNPlot;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;

import java.util.function.Function;

public class CostTest extends Application {

    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            final NeuralNetwork[] neuralNetwork = {new NeuralNetwork.Builder(2, 5, 8, 5, 2).build()};

            primaryStage.setTitle("Cost test");
            VBox root = new VBox();
            root.setSpacing(10);
            root.setPadding(new Insets(20, 20, 20, 20));
            double canvasWidth = 1000;
            double canvasHeight = 300;
            double offset = 10;
            Canvas canvas = new Canvas(canvasWidth + 2 * offset, canvasHeight + 2 * offset);
            GraphicsContext context = canvas.getGraphicsContext2D();
            root.getChildren().add(canvas);
            Canvas canvas1 = new Canvas(canvasWidth + 2 * offset, canvasHeight + 2 * offset);
            GraphicsContext context1 = canvas1.getGraphicsContext2D();
            root.getChildren().add(canvas1);
            Canvas canvas2 = new Canvas(canvasWidth + 2 * offset, canvasHeight + 2 * offset);
            GraphicsContext context2 = canvas2.getGraphicsContext2D();
            root.getChildren().add(canvas2);


            NeuralNetwork net1 = new NeuralNetwork.Builder(2, 10, 1)
                    .setInitializer(Initializer.KAIMING)
                    .setLearningRate(0.8)
                    .setDefaultRectifier(Rectifier.SIGMOID)
                    .setLearningRateOptimizer(Optimizer.NONE).build();

            NeuralNetwork net2 = new NeuralNetwork.Builder(2, 10, 1)
                    .setInitializer(Initializer.KAIMING)
                    .setDropoutFactor(0.5)
                    .setLearningRate(0.8)
                    .setDefaultRectifier(Rectifier.SIGMOID)
                    .setLearningRateOptimizer(Optimizer.NONE).build();
            int iter = 500;
            net1.fit(in, out, iter);
            net2.fit(in, out, iter);

            NNPlot costPlot = new NNPlot(context);
            costPlot.plotCost(net1, false, 0);
            costPlot.setTitle("Cost of neural network during " + iter + " iterations");

            NN2DChart chart = new NN2DChart(context1);
            chart.showLegend(true);
            chart.setTitle("new thingy");
            chart.setAxisLabels("iterations", "cost");
            chart.plotLine(net1,  BackPropEntity::getCost, "cost", Color.PURPLE, 0);

            NNPlot sumPlot = new NNPlot(context2);
            sumPlot.plotCost(net2, false, 0);
            sumPlot.setTitle("With dropout over " + iter + " iterations");


            //System.out.println(net);

            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
