package ch.kaiki.nn.ui;

import ch.kaiki.nn.ui.deprecated.NNMeshGrid;
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
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.NNHeatMap;
import ch.kaiki.nn.ui.color.NNPlotColorDeprecated;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;

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
            NNPlotColorDeprecated plotColors = new NNPlotColorDeprecated(BLACK, BLACK, STEELBLUE, LIGHTGRAY, STEELBLUE, RED);

            NeuralNetwork net = new NeuralNetwork.Builder(2, 15, 15, 2).setInitializer(Initializer.KAIMING)
                    .setLearningRate(0.8)
                    .setDefaultRectifier(Rectifier.SIGMOID)
                    .setLearningRateOptimizer(Optimizer.NONE).build();
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
