package ch.kaiki.nn.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.util.Rectifier;

import java.util.function.Function;

public class RectifierTest extends Application {

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
                if (i % 5 == 0) {
                    box = new HBox();
                    root.getChildren().add(box);
                }
                NNLinePlot plot = new NNLinePlot(box, canvasWidth, canvasHeight, true, true, true, 0);
                plot.setTitleFontSize(1.1);
                plot.plot(rectifier::derive, "derivation", Color.LIGHTGRAY);
                plot.plot(rectifier::activate, "activation", Color.BLACK);
                plot.setTitle("Rectifier " + rectifier.name().replaceAll("_", " "));
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
