package ui;

import data.IterationObject;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import neuralnet.Cost;
import neuralnet.NeuralNetwork;
import neuralnet.Regularizer;
import util.Descent;
import util.Initializer;
import util.Rectifier;

import java.util.SortedMap;

public class CostTest extends Application {

    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            final NeuralNetwork[] neuralNetwork = {new NeuralNetwork(2, 5, 8, 5, 2)};

            primaryStage.setTitle("Cost test");
            VBox root = new VBox();
            root.setSpacing(10);
            root.setPadding(new Insets(20, 20, 20, 20));
            double canvasWidth = 1000;
            double canvasHeight = 300;
            double offset = 10;
            Canvas canvas = new Canvas(canvasWidth+2*offset, canvasHeight+2*offset);
            GraphicsContext context = canvas.getGraphicsContext2D();
            root.getChildren().add(canvas);
            Canvas canvas2 = new Canvas(canvasWidth+2*offset, canvasHeight+2*offset);
            GraphicsContext context2 = canvas2.getGraphicsContext2D();
            root.getChildren().add(canvas2);
            Canvas canvas3 = new Canvas(canvasWidth+2*offset, canvasHeight+2*offset);
            GraphicsContext context3 = canvas3.getGraphicsContext2D();
            //root.getChildren().add(canvas3);

            /*
            public Cost costFunction = Cost.MSE;
    public Regularizer regularizer = Regularizer.NONE;
    public double regularizationLambda = 0;
             */

            NeuralNetwork net = new NeuralNetwork(Initializer.KAIMING, 2, 15,15, 1)
                    .setLearningRate(0.8)
                    .setLearningRateDescent(Descent.NONE);
            net.costFunction = Cost.MSE;
            net.regularizationLambda = 0.1;
            net.regularizer = Regularizer.NONE;
            net.setRectifier(Rectifier.SIGMOID);
            int iter = 500;
            net.fit(in, out, iter);

            SortedMap<Integer, Double> costMap = net.getCostMap();

            NNPlot plot = new NNPlot(context);
                    plot.plot(net.getBackPropData(), IterationObject::getCost, false, 0.05);
            new NNPlot(context2).plot(net.getBackPropData(), IterationObject::getCostSum, false, 0);


            System.out.println(net.predict(in[0]) + " is 0?");
            System.out.println(net.predict(in[1]) + " is 1?");
            System.out.println(net.predict(in[2]) + " is 1?");
            System.out.println(net.predict(in[3]) + " is 0?");

            //System.out.println(net);

            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
