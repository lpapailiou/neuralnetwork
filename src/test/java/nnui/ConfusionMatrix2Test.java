package nnui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nn.NeuralNetwork;
import nnui.color.NNHeatMap;
import nnui.color.NNPlotColor;
import nnutil.Initializer;
import nnutil.Optimizer;
import nnutil.Rectifier;

import static javafx.scene.paint.Color.*;

public class ConfusionMatrix2Test extends Application {

    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{1,0}, {0,1}, {0,1}, {1,0}};

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            primaryStage.setTitle("Decision boundary test");
            VBox root = new VBox();
            root.setBackground(new Background(new BackgroundFill(BLACK, null, null)));
            HBox hbox = null;
            NNPlotColor plotColors = new NNPlotColor(BLACK, BLACK, STEELBLUE, LIGHTGRAY, STEELBLUE, RED);

            NeuralNetwork net = new NeuralNetwork(Initializer.KAIMING, 2, 15, 15, 2)
                    .setLearningRate(0.8)
                    .setRectifier(Rectifier.SIGMOID)
                    .setLearningRateOptimizer(Optimizer.NONE);
            int iterations = 0;
            net.fit(in, out, iterations);

            for (int i = 0; i < 9; i++) {
                if (i % 3 == 0) {
                    hbox = new HBox();
                    hbox.setSpacing(10);
                    hbox.setPadding(new Insets(20, 20, 20, 20));
                    root.getChildren().add(hbox);
                }
                iterations += 100;
                net.fit(in, out, 100);

                NNMeshGrid plot = new NNMeshGrid(addCanvas(300, 300, hbox));
                plot.setPadding(25, 0, 20, 30, 0);
                plot.setTitle("after " + iterations + " iterations");
                plot.setColorPalette(plotColors);
                //plot.plot(net, in, 1, 0.9, true, true, true, new NNHeatMap(0, 1, RED, YELLOW, GREEN));
                plot.plotConfusionMatrix(net, in, 1, true, true, true, new NNHeatMap(STEELBLUE, WHITE));
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
