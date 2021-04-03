package ui;

import data.ForwardPropData;
import data.IterationObject;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import neuralnet.Cost;
import neuralnet.NeuralNetwork;
import neuralnet.Regularizer;
import org.junit.Test;
import ui.color.NNBinaryClassifierColor;
import util.Descent;
import util.Initializer;
import util.Rectifier;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;

public class DecisionBoundaryTest extends Application {

    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            final NeuralNetwork[] neuralNetwork = {new NeuralNetwork(2, 5, 8, 5, 2)};

            primaryStage.setTitle("Decision boundary test");
            HBox root = new HBox();
            root.setSpacing(10);
            root.setPadding(new Insets(20, 20, 20, 20));

            NeuralNetwork net = new NeuralNetwork(Initializer.KAIMING, 2, 15,15, 1)
                    .setLearningRate(0.8)
                    .setLearningRateDescent(Descent.NONE);
            net.costFunction = Cost.MSE;

            net.fit(in, out, 50);

            for (int i = 0; i < 8; i++) {
                net.fit(in, out, 20);

                NNPlot plot = new NNPlot(addCanvas(300,300, root));
                plot.setPadding(0,0,20,30, 5);
                plot.plot(net, in, 1, 1, true, true, true, new NNBinaryClassifierColor(Color.GREEN, Color.RED, Color.YELLOW));
                //plot.plot2DData(out, 12);
            }


            System.out.println(net.predict(in[0]) + " is 0?");
            System.out.println(net.predict(in[1]) + " is 1?");
            System.out.println(net.predict(in[2]) + " is 1?");
            System.out.println(net.predict(in[3]) + " is 0?");

            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GraphicsContext addCanvas(double width, double height, HBox parnet) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext context = canvas.getGraphicsContext2D();
        parnet.getChildren().add(canvas);
        return context;
    }

}
