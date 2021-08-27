package ch.kaiki.nn.ui;

import ch.kaiki.nn.data.BackPropEntity;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static javafx.scene.paint.Color.*;


public class CostTest2 extends Application {

    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            primaryStage.setTitle("Cost test");
            VBox root = new VBox();
            root.setSpacing(10);
            root.setPadding(new Insets(20, 20, 20, 20));
            NNLinePlot plot1 = new NNLinePlot(root, 1000, 400, true, true, true, 0.02);
            NNLinePlot plot2 = new NNLinePlot(root, 1000,400, true, true, true, 0);

            NeuralNetwork net = new NeuralNetwork.Builder(2, 10, 1)
                    .setInitializer(Initializer.KAIMING)
                    .setDefaultRectifier(Rectifier.SIGMOID)
                    .setLearningRate(0.8)
                    .setLearningRateOptimizer(Optimizer.NONE).build();
            int iter = 601;
            net.fit(in, out, iter);

            plot1.setTitle("Cost of Neural Network");
            //plot1.setXAxisLabel("iteration");
            //plot1.setYAxisLabel("cost");
            plot1.plot(net, BackPropEntity::getCost, "cost", SALMON);


            //plot2.setTitle("Summed stats of Neural Network");
            plot2.plot(net, BackPropEntity::getAccuracySum, "accuracy", SALMON);
            plot2.plot(net, BackPropEntity::getPrecisionSum, "precisionnnnnnnnnnnnnnnnnnnnnnnnnnn", SEAGREEN);
            plot2.plot(net, BackPropEntity::getRecallSum, "recall", STEELBLUE);
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
