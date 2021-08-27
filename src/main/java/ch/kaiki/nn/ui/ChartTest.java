package ch.kaiki.nn.ui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class ChartTest extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            primaryStage.setTitle("Chart test");
            VBox root = new VBox();
            //root.setBackground(new Background(new BackgroundFill(Color.TURQUOISE, null, null)));
            NNChart chart = new NNChart(800, 500, true, true, true, 0.8);

            List<NNChart.Point> data =  Arrays.asList(new NNChart.Point(0,0), new NNChart.Point(1,2), new NNChart.Point(2,3.5),  new NNChart.Point(2,7),  new NNChart.Point(3,7),  new NNChart.Point(4,8));
            chart.setXAxisLabel("x-Axis");
            chart.setYAxisLabel("y-Axis");
            chart.plot(data);

            //chart.setXAxisLabel(null);
            //chart.setYAxisLabel(null);



            root.getChildren().add(new Group(chart));
            Scene scene = new Scene(root, 800,500);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
