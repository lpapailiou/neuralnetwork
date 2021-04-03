package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import neuralnet.CostFunction;
import neuralnet.NeuralNetwork;
import ui.color.NNBinaryClassifierColor;
import ui.color.NNPlotColor;
import util.Initializer;
import util.Optimizer;

import java.util.concurrent.atomic.AtomicInteger;

import static javafx.scene.paint.Color.*;

public class AnimationTest extends Application {

    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            final NeuralNetwork[] neuralNetwork = {new NeuralNetwork(2, 5, 8, 5, 2)};
            primaryStage.setTitle("Hyperplane movement test");
            VBox root = new VBox();
            root.setBackground(new Background(new BackgroundFill(BLACK, null, null)));
            root.setSpacing(10);
            root.setPadding(new Insets(20, 20, 20, 20));

            NeuralNetwork net = new NeuralNetwork(Initializer.KAIMING, 2, 15, 15, 1)
                    .setLearningRate(0.8)
                    .setLearningRateOptimizer(Optimizer.NONE);
            net.costFunction = CostFunction.MSE;
            AtomicInteger iterations = new AtomicInteger();
            NNDecisionBoundaryPlot plot = new NNDecisionBoundaryPlot(addCanvas(700, 700, root));
            plot.setPadding(30, 0, 20, 30, 5);
            plot.setTitle("after " + iterations + " iterations");
            plot.setColorPalette(new NNPlotColor(BLACK, BLACK, LIGHTGRAY, LIGHTGRAY, LIGHTGRAY, RED));
            plot.plot(net, in, 1, 1, true, true, true, new NNBinaryClassifierColor(Color.GREEN, Color.RED, Color.YELLOW));


            Button btnc = new Button("step forward");
            btnc.setOnAction(e -> {
                iterations.addAndGet(1);
                net.fit(in, out, 1);
                plot.setTitle("after " + iterations + " iterations");
                plot.plot(net, in, 1, 1, true, true, true, new NNBinaryClassifierColor(Color.GREEN, Color.RED, Color.YELLOW));
            });
            root.getChildren().add(btnc);

            primaryStage.setScene(new Scene(root));
            primaryStage.show();


            System.out.println(net.predict(in[0]) + " is 0?");
            System.out.println(net.predict(in[1]) + " is 1?");
            System.out.println(net.predict(in[2]) + " is 1?");
            System.out.println(net.predict(in[3]) + " is 0?");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GraphicsContext addCanvas(double width, double height, VBox parnet) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext context = canvas.getGraphicsContext2D();
        parnet.getChildren().add(canvas);
        return context;
    }

}
