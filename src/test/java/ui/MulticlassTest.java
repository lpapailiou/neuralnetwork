package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import neuralnet.Cost;
import neuralnet.NeuralNetwork;
import ui.color.NNMultiClassColor;
import util.Descent;
import util.Initializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javafx.scene.paint.Color.*;

public class MulticlassTest extends Application {

    double[][] in = {{0.1, 2.2}, {0.2, 0.22}, {0.3,0.1}, {0.4,0.7}, {0.5,0.8}, {0.45,0.9}, {0.8,0.1}, {0.9,0.15}, {0.8,0.2}, {0.5,0.55}};
    double[][] out = {{1,0,0,0}, {1,0,0,0}, {1,0,0,0}, {0,1,0,0}, {0,1,0,0}, {0,1,0,0}, {0,0,1,0}, {0,0,1,0}, {0,0,1,0}, {0,0,0,1}};

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            final NeuralNetwork[] neuralNetwork = {new NeuralNetwork(2, 5, 8, 5, 2)};

            primaryStage.setTitle("Decision boundary test");
            HBox root = new HBox();
            root.setSpacing(10);
            root.setPadding(new Insets(20, 20, 20, 20));

            NeuralNetwork net = new NeuralNetwork(Initializer.KAIMING, 2, 15,15, 4)
                    .setLearningRate(0.8)
                    .setLearningRateDescent(Descent.NONE);
            net.costFunction = Cost.MSE;

            net.fit(in, out, 200);

            for (int i = 0; i < 3; i++) {
                net.fit(in, out, 200);

                NNPlot plot = new NNPlot(addCanvas(300,300, root));
                plot.setPadding(0,0,20,30, 0.1);
                plot.plot(net, in, 1, 0.6, true, true, true, new NNMultiClassColor(web("#eeb76b"), web("#e2703a"), web("#9c3d54"), web("#310b0b")));
                plot.plot2DData(out, 12);
            }


            System.out.println(net.predict(in[0]) + " is 1 0 0 0 ?");
            System.out.println(net.predict(in[3]) + " is 0 1 0 0 ?");
            System.out.println(net.predict(in[6]) + " is 0 0 1 0 ?");
            System.out.println(net.predict(in[9]) + " is 0 0 0 1 ?");

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
