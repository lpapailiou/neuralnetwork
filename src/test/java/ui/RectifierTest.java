package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import neuralnet.NeuralNetwork;
import util.Rectifier;

public class RectifierTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            final NeuralNetwork[] neuralNetwork = {new NeuralNetwork(2, 5, 8, 5, 2)};

            primaryStage.setTitle("Rectifier test");
            VBox root = new VBox();
            double canvasWidth = 300;
            double canvasHeight = 300;
            double offset = 10;
            int i = 0;
            HBox box = null;
            for (Rectifier rectifier : Rectifier.values()) {
                if (i % 5 == 0) {
                    box = new HBox();
                    box.setSpacing(10);
                    box.setPadding(new Insets(20, 20, 20, 20));
                    root.getChildren().add(box);
                }
                Canvas canvas = new Canvas(canvasWidth + 2 * offset, canvasHeight + 2 * offset);
                GraphicsContext context = canvas.getGraphicsContext2D();
                box.getChildren().add(canvas);
                NNPlot plot = new NNPlot(context);
                plot.plot(rectifier, true);
                plot.setTitle("Rectifier " + rectifier.getDescription());
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
