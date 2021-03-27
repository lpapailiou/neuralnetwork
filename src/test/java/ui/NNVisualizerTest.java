package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import neuralnet.NeuralNetwork;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NNVisualizerTest extends Application {

    private double[][] in = {{0,0}, {1,0}, {0,1}, {1,1}};
    private double[][] out = {{1,0}, {0,1}, {0,1}, {1,0}};
    private Label inLabel = new Label();
    private Label outLabel = new Label();
    private Label successLabel = new Label();
    private NNVisualizer visualizer;

    @Override
    public void start(Stage primaryStage) throws Exception {
        final NeuralNetwork[] neuralNetwork = {new NeuralNetwork(2, 5, 8, 5, 2)};

        primaryStage.setTitle("NNVisualizer test");
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(20, 20, 20, 20));
        Canvas canvas = new Canvas(600, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawNeuralNetwork(gc, neuralNetwork[0]);
        root.getChildren().add(canvas);

        VBox box = new VBox();
        HBox btnBox = new HBox();
        btnBox.setSpacing(10);
        root.getChildren().add(box);
        box.getChildren().add(btnBox);
        box.setSpacing(10);

        Button btnf = new Button("fit");
        btnf.setOnAction(e -> {
            neuralNetwork[0].fit(in, out, 1000);
            visualizer.refresh();
            inLabel.setText("");
            outLabel.setText("");
            successLabel.setText("");
        });
        btnBox.getChildren().add(btnf);

        Button btnp = new Button("predict");
        btnp.setOnAction(e -> {
            double[] test = in[new Random().nextInt(in.length)];
            List<Double> result = neuralNetwork[0].predict(test);
            inLabel.setText("test with input: " + Arrays.toString(test));
            outLabel.setText("output: " + result.toString());
            boolean maxIsFirst = Collections.max(result) == result.get(0);
            boolean success = ((maxIsFirst && (Arrays.equals(test, in[0]) || Arrays.equals(test, in[3]))) || (!maxIsFirst && (Arrays.equals(test, in[1]) || Arrays.equals(test, in[2]))));
            successLabel.setText(success ? "SUCCESS" : "FAIL");
        });
        btnBox.getChildren().add(btnp);

        Button btnr = new Button("new neural network");
        btnr.setOnAction(e -> {
            int[][] config = new int[][] {{2, 5, 8, 5, 2}, {2,2}, {2,10,10,10,10,2}, {2,3,4,5,4,3,2}, {2,4,2}, {2,2,1,2,2}, {2,4,8,16,8,4,2}, {2,4,12,7,2}, {2,12,3,2}};
            neuralNetwork[0] = new NeuralNetwork(config[new Random().nextInt(config.length)]);
            visualizer.setNeuralNetwork(neuralNetwork[0]);
            visualizer.setInputNodeLabels(new String[] {"a", "b"});
        });
        btnBox.getChildren().add(btnr);

        Button btnc = new Button("new color palette");
        btnc.setOnAction(e -> {
            visualizer.setColorPalette(randomPalette());
        });
        btnBox.getChildren().add(btnc);

        box.getChildren().add(new Label("Every prediction will perform an xor test on this neural network. It can be trained for 1000 epochs by clicking on 'fit'."));
        box.getChildren().add(inLabel);
        box.getChildren().add(outLabel);
        box.getChildren().add(successLabel);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void drawNeuralNetwork(GraphicsContext context, NeuralNetwork neuralNetwork) {
        visualizer = new NNVisualizer(context).setNeuralNetwork(neuralNetwork);
        visualizer.setGraphInputNodeCount(3, 0);
        visualizer.setOutputNodeLabels(new String[] {"0", "1"});
        visualizer.setInputNodeLabels(new String[] {"(this node is not in use)", "a", "b"});
        visualizer.setWidthOffset(120);
    }

    private NNColorPalette randomPalette() {
        return new NNColorPalette(randomColor(), randomColor(), randomColor(), randomColor(), randomColor(), randomColor(), randomColor(), randomColor(), randomColor());
    }

    private Color randomColor() {
        return Color.color(Math.random(), Math.random(), Math.random());
    }

    public void stop() {
        Platform.exit();
        System.exit(0);
    }
}
