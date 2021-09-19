package ch.kaiki.nn.ui;

import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.deprecated.NNLinePlot;
import ch.kaiki.nn.util.Rectifier;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RectifierTest2 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            final NeuralNetwork[] neuralNetwork = {new NeuralNetwork.Builder(2, 5, 8, 5, 2).build()};

            primaryStage.setTitle("Rectifier test");
            VBox root = new VBox();
            double canvasWidth = 300;
            double canvasHeight = 300;
            int i = 0;
            HBox box = null;
            for (Rectifier rectifier : Rectifier.values()) {
            //Rectifier rectifier = Rectifier.SOFTMAX;
                if (i % 5 == 0) {
                    box = new HBox();
                    root.getChildren().add(box);
                }
                Canvas canvas = new Canvas(canvasWidth, canvasHeight);
                box.getChildren().add(canvas);
                NN2DChart plot = new NN2DChart(canvas.getGraphicsContext2D());
                plot.showLegend(false);
                plot.setTitle("Rectifier " + rectifier.name().replaceAll("_", " "));
                plot.plotLine(rectifier::derive, "derivation", Color.LIGHTGRAY, -2, 2, -1, 1);
                plot.plotLine(rectifier::activate, "activation", Color.RED, -2, 2, -1, 1);

                i++;
            }

            //System.out.println(net);

            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
