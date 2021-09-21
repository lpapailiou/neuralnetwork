package ch.kaiki.nn.ui;

import ch.kaiki.nn.util.Rectifier;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RectifierSample extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            primaryStage.setTitle("Rectifier Sample");
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
                Canvas canvas = new Canvas(canvasWidth, canvasHeight);
                box.getChildren().add(canvas);
                NN2DPlot plot = new NN2DPlot(canvas.getGraphicsContext2D());
                plot.showLegend(false);
                plot.setTitle("Rectifier " + rectifier.name().replaceAll("_", " "));
                plot.plotLine(rectifier::derive, "derivation", Color.LIGHTGRAY, -2, 2, -1, 1);
                plot.plotLine(rectifier::activate, "activation", Color.RED, -2, 2, -1, 1);

                i++;
            }

            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
