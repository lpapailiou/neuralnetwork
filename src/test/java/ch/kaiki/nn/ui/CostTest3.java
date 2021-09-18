package ch.kaiki.nn.ui;

import ch.kaiki.nn.data.BackPropEntity;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.NNHeatMap;
import ch.kaiki.nn.ui.deprecated.NNLinePlot;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import static javafx.scene.paint.Color.*;


public class CostTest3 extends Application {

    double[][] in = {{0, 0}, {2, 0}, {-0.5, -1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};
    //double[][] in = {{0.1, 2.2}, {0.2, 0.22}, {0.3, 0.1}, {0.4, 0.7}, {0.5, 0.8}, {0.45, 0.9}, {0.8, 0.1}, {0.9, 0.15}, {0.8, 0.2}, {0.5, 1.55}};
    //double[][] out = {{1, 0, 0, 0}, {1, 0, 0, 0}, {1, 0, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 1, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            primaryStage.setTitle("Cost test");
            VBox root = new VBox();
            root.setSpacing(10);
            root.setPadding(new Insets(20, 20, 20, 20));
            Canvas canvas = new Canvas(1000, 400);
            NN2DChart plot1 = new NN2DChart(canvas.getGraphicsContext2D());
            root.getChildren().add(canvas);
            NNLinePlot plot2 = new NNLinePlot(root, 1000,400, true, true, true, 0);

            NeuralNetwork net = new NeuralNetwork.Builder(2, 6,1)
                    .setInitializer(Initializer.KAIMING)
                    .setDefaultRectifier(Rectifier.SIGMOID)
                    .setLastLayerRectifier(Rectifier.TANH)
                    .setLearningRate(0.2)
                    //.setDropoutFactor(0.1)
                    .setLearningRateOptimizer(Optimizer.NONE).build();
            int iter = 200;
            net.fit(in, out, iter);

            //plot1.setTitle("Cost of Neural Network");
            //plot1.setXAxisLabel("iteration");
            //plot1.setYAxisLabel("cost");
            plot1.setInnerDataPadding(1);
            NNHeatMap heatMap = new NNHeatMap(Color.BLANCHEDALMOND, Color.LIGHTBLUE, Color.ROSYBROWN, Color.SALMON);
            heatMap.setOpacity(0.2,0.5);

            plot1.setOuterDataPadding(0.05);
            plot1.showLegend(true);
            plot1.plotLine(net,  BackPropEntity::getAccuracySum, "accuracy", RED, 0);
            plot1.plotLine(net,  BackPropEntity::getPrecisionSum, "precision", ORANGE, 0);
            plot1.plotLine(net,  BackPropEntity::getRecallSum, "recall", GREEN.brighter(), 0);

            //plot2.setTitle("Summed stats of Neural Network");
            plot2.plot(net, BackPropEntity::getAccuracySum, "accuracy", SALMON);
            plot2.plot(net, BackPropEntity::getPrecisionSum, "precision", SEAGREEN);
            plot2.plot(net, BackPropEntity::getRecallSum, "recall", BLACK);
            plot2.setLegendSide(Side.RIGHT);
;


            //System.out.println(net);
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
