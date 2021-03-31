package ui;

import data.ForwardPropData;
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
import org.junit.Test;
import util.Descent;
import util.Initializer;
import util.Rectifier;

import java.util.List;
import java.util.SortedMap;

public class DecisionBoundaryTest extends Application {

    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};

    @Test
    public void test() {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            final NeuralNetwork[] neuralNetwork = {new NeuralNetwork(2, 5, 8, 5, 2)};

            primaryStage.setTitle("Decision boundary test");
            VBox root = new VBox();
            root.setSpacing(10);
            root.setPadding(new Insets(20, 20, 20, 20));
            double canvasWidth = 500;
            double canvasHeight = 500;
            double offset = 10;
            Canvas canvas = new Canvas(canvasWidth+2*offset, canvasHeight+2*offset);
            GraphicsContext context = canvas.getGraphicsContext2D();
            root.getChildren().add(canvas);


            NeuralNetwork net = new NeuralNetwork(Initializer.KAIMING, 2, 15,15, 1)
                    .setLearningRate(0.8)
                    .setLearningRateDescent(Descent.NONE);
            net.costFunction = Cost.MSE;
            net.regularizationLambda = 0.1;
            net.regularizer = Regularizer.NONE;
            net.setRectifier(Rectifier.SIGMOID);
            int iter = 3000;
            net.fit(in, out, iter);

            ForwardPropData data = new ForwardPropData();
            for (int i = 0; i < 25000; i++) {
                double[] input = {Math.random() * 2 -1, Math.random() * 2 -1};
                List<Double> output = net.predict(input);
                data.add(input, output);
            }

            NNPlot plot = new NNPlot(context);
            plot.setPadding(20,20,60,60);
            plot.plot(data);

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
            /*
            int minX = costMap.keySet().stream().min(Integer::compare).get();;
            int maxX = costMap.keySet().stream().max(Integer::compare).get();
            double minY = costMap.values().stream().min(Double::compare).get();
            double maxY = costMap.values().stream().max(Double::compare).get();
            double radius = 5;
            double lineWidth = 2;
            context.setFill(Color.BLACK);
            context.setStroke(Color.BLACK);
            context.setLineWidth(lineWidth);
            double x = 0;
            double y = 0;
            int counter = 0;
            int threshold = iter/((int)canvasWidth/5);
            boolean dot = false;
            for (Integer key : costMap.keySet()) {
                counter++;
                if (counter % threshold == 0) {
                    double newx = (double) key / maxX * canvasWidth + offset;
                    double newy = ((canvasHeight + offset) - (costMap.get(key) / maxY * canvasHeight));
                    if (!dot) {

                        context.strokeLine(x, y, newx, newy);
                        x = newx;
                        y = newy;

                    } else {
                        context.fillOval(newx, newy, radius, radius);
                    }
                    System.out.println(key + " --> " + costMap.get(key));
                }
            } */