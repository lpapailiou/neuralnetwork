package ch.kaiki.nn.ui;

import ch.kaiki.nn.data.BackPropEntity;
import ch.kaiki.nn.data.Graph;
import ch.kaiki.nn.data.IGraph;
import ch.kaiki.nn.data.Vertice;
import ch.kaiki.nn.neuralnet.NeuralNetwork;
import ch.kaiki.nn.ui.color.GraphColor;
import ch.kaiki.nn.ui.color.NNHeatMap;
import ch.kaiki.nn.ui.util.VisualizationMode;
import ch.kaiki.nn.util.Initializer;
import ch.kaiki.nn.util.Optimizer;
import ch.kaiki.nn.util.Rectifier;
import com.sun.corba.se.spi.activation.BadServerDefinition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.DepthTest;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.SceneAntialiasing.BALANCED;
import static javafx.scene.paint.Color.*;

public class GraphChart extends Application {



    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            primaryStage.setTitle("Graph test");
            VBox root = new VBox();
            root.setDepthTest(DepthTest.ENABLE);
            root.setSpacing(10);
            HBox graphBox = new HBox();
            graphBox.setSpacing(10);
            graphBox.setPadding(new Insets(20, 20, 20, 20));
            root.getChildren().add(graphBox);
            HBox btnContainer = new HBox();
            btnContainer.setSpacing(10);
            btnContainer.setPadding(new Insets(20, 20, 20, 20));
            root.getChildren().add(btnContainer);
            VBox cBox = new VBox();
            cBox.setSpacing(10);
            cBox.setPadding(new Insets(20, 20, 20, 20));
            btnContainer.getChildren().add(cBox);
            VBox tBox = new VBox();
            tBox.setSpacing(10);
            tBox.setPadding(new Insets(20, 20, 20, 20));
            btnContainer.getChildren().add(tBox);
            VBox dBox = new VBox();
            dBox.setSpacing(10);
            dBox.setPadding(new Insets(20, 20, 20, 20));
            btnContainer.getChildren().add(dBox);
            double canvasWidth = 700;
            double canvasHeight = 550;
            root.setBackground(new Background(new BackgroundFill(BLACK, null, null)));
            double canW = canvasWidth;
            double canH = canvasHeight;
            Canvas canvas = new Canvas(canW, canH);
            GraphicsContext context = canvas.getGraphicsContext2D();

            Canvas canvas2 = new Canvas(canvasWidth, canvasHeight);
            GraphicsContext context2 = canvas2.getGraphicsContext2D();
            graphBox.getChildren().add(canvas2);
            graphBox.getChildren().add(canvas);
//# available values: gelu|identity|relu|leaky_relu|sigmoid|sigmoid_accurate|silu|silu_accurate|softplus|tanh|softmax.

            NNHeatMap heatMap = new NNHeatMap(0,1,Color.STEELBLUE, Color.TURQUOISE, Color.YELLOW, Color.CRIMSON);
            //NNHeatMap heatMap = new NNHeatMap(Color.BLANCHEDALMOND, Color.LIGHTBLUE, Color.ROSYBROWN, Color.SALMON);
            //heatMap.setOpacity(0.3, 0.8);

            //NNHeatMap heatMap = new NNHeatMap(0,1,Color.BLACK, Color.WHITE);

            IGraph graph = new Graph();
            Vertice a = new Vertice(0,0,1);
            Vertice b = new Vertice(1,10,2);
            Vertice c = new Vertice(2,1,0);
            Vertice d = new Vertice(2,3,7);
            Vertice e = new Vertice(5,1,0);
            graph.addVertices(Arrays.asList(a, b, c, d, e));
            graph.addEdge(a, b, 12);
            graph.addEdge(b, c);
            graph.addEdge(c, d);
            graph.addEdge(d, e);
            graph.addEdge(e, a);
            graph.addEdge(b, d);
            a.setVisited(true);
            b.setHighlighted(true);
            c.getEdges().stream().forEach(x -> x.setVisited(true));
            GraphColor graphColor = new GraphColor(RED, GREEN, LIGHTYELLOW);



            AtomicReference<NN3DPlot> plot = new AtomicReference<>(new NN3DPlot(context));
            plot.get().setVisualizationMode(VisualizationMode.CUBE);
            plot.get().setAxisLabels("x-Axis", "y-Axis", "z-Axis");
            plot.get().setTitle("Decision Boundary Visualization 3D");
            plot.get().enableMouseInteraction();
            plot.get().showLegend(true);
            plot.get().showBorder(true);
            plot.get().showTickMarkLabels(true);
            plot.get().showGridContent(true);
            plot.get().showGrid(true);
            plot.get().plotGraph(graph, graphColor);
            //plot.get().setAnimated(true);
            //plot.get().setAnimated(true);
/*
            NNMeshGrid plot2 = new NNMeshGrid(context2);
            plot2.setPadding(0,0,50,0,padding);
            plot2.plot(net, in, resolution,1,true,true,true, heatMap);
            plot(plot, net, resolution, heatMap);*/

            NN2DPlot plot2 = new NN2DPlot(context2);
            plot2.setTitle("Decision Boundary Visualization 2D");
            //plot2.enableMouseInteraction();
            plot2.showLegend(true);
            plot2.showBorder(true);
            plot2.plotGraph(graph, graphColor);



            Scene scene = new Scene(root, root.getWidth(), root.getHeight(), false, BALANCED);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
