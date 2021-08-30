package ch.kaiki.nn.ui;

import ch.kaiki.nn.ui.util.Point;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

import static javafx.scene.paint.Color.*;

public class ChartTest extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            double mousePosX, mousePosY;
            double mouseOldX, mouseOldY;
            final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
            final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);
            primaryStage.setTitle("Chart test");
            VBox root = new VBox();


            NNChart chart = new NNChart(800, 400, true, true, true, 1);

            List<Point> data1 =  Arrays.asList(new Point(-1,-1), new Point(0,0), new Point(2,2));
            List<Point> data2 =  Arrays.asList(new Point(1,2), new Point(1.5,2), new Point(0,2));
            List<Point> data3 =  Arrays.asList(new Point(1,345));

            chart.setXAxisLabel("x-Axis");
            chart.setYAxisLabel("y-Axis");

            chart.plot("this is test 1", data1, BLUE);
            chart.plot("test 2", data2, RED);
            chart.plot("test 3", data3, GREEN);

            //chart.setXAxisLabel(null);
            //chart.setYAxisLabel(null);

            chart.setTitle("This is my title");
            //chart.setTitle(null);



            root.getChildren().add(new Group(chart));
            Scene scene = new Scene(root, 800,400, true, SceneAntialiasing.BALANCED);

            scene.setCamera(new PerspectiveCamera());


            primaryStage.setResizable(false);
            primaryStage.setScene(scene);


            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
