package ch.kaiki.nn.ui;

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
import javafx.stage.Stage;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.NNGraphColor;
import ch.kaiki.nn.util.Initializer;

import java.util.*;

import static ch.kaiki.nn.ui.color.NNColor.randomColor;

public class NNGraphTest extends Application {

    private double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    private double[][] out = {{1, 0}, {0, 1}, {0, 1}, {1, 0}};
    private Label inLabel = new Label();
    private Label outLabel = new Label();
    private Label successLabel = new Label();
    private NNGraph graph;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            final NeuralNetwork[] neuralNetwork = {new NeuralNetwork.Builder( 2, 5, 8, 5, 2).setInitializer(Initializer.KAIMING).build()};

            primaryStage.setTitle("NNGraph test");
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
                neuralNetwork[0].fit(in, out, 1000, 16);
                graph.refresh();
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
                int[][] config = new int[][]{{2, 5, 8, 5, 2}, {2, 2}, {2, 10, 10, 10, 10, 2}, {2, 3, 4, 5, 4, 3, 2}, {2, 4, 2}, {2, 2, 1, 2, 2}, {2, 4, 8, 16, 8, 4, 2}, {2, 4, 12, 7, 2}, {2, 12, 3, 2}};
                int[] configs = config[new Random().nextInt(config.length)];
                neuralNetwork[0] = new NeuralNetwork.Builder(configs).setInitializer(Initializer.KAIMING).build();
                graph.setNeuralNetwork(neuralNetwork[0]);
                graph.setGraphInputNodeCount(2, new HashSet<>());
                graph.setInputNodeLabels(new String[]{"a", "b"});
                graph.setOutputNodeLabels(null);
            });
            btnBox.getChildren().add(btnr);

            Button btnc = new Button("new color palette");
            btnc.setOnAction(e -> {
                graph.setColorPalette(randomPalette());
            });
            btnBox.getChildren().add(btnc);

            box.getChildren().add(new Label("Every prediction will perform an xor test on this neural network. It can be trained for 1000 epochs by clicking on 'fit'."));
            box.getChildren().add(inLabel);
            box.getChildren().add(outLabel);
            box.getChildren().add(successLabel);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawNeuralNetwork(GraphicsContext context, NeuralNetwork neuralNetwork) {
        graph = new NNGraph(context).setNeuralNetwork(neuralNetwork);
        Set<Integer> inactiveNodes = new HashSet<>();
        inactiveNodes.add(0);
        graph.setGraphInputNodeCount(3, inactiveNodes);
        graph.setOutputNodeLabels(new String[]{"0", "1"});
        graph.setInputNodeLabels(new String[]{"(this node is not in use)", "a", "b"});
        graph.setPadding(0, 0, 0, 120);
        //graph.setFontProperties(true, true, 12);
    }

    private NNGraphColor randomPalette() {
        return new NNGraphColor(randomColor(false), randomColor(false), randomColor(false), randomColor(false), randomColor(false), randomColor(false), randomColor(false), randomColor(false), randomColor(false));
    }

    public void stop() {
        Platform.exit();
        System.exit(0);
    }
}
