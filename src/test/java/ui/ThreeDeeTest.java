package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import neuralnet.NeuralNetwork;
import ui.color.NNHeatMap;
import ui.color.NNPlotColor;
import util.Initializer;
import util.Optimizer;
import util.Rectifier;

import java.util.concurrent.atomic.AtomicReference;

public class ThreeDeeTest extends Application {

    double[][] in = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    double[][] out = {{0}, {1}, {1}, {0}};

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            final NeuralNetwork[] neuralNetwork = {new NeuralNetwork(2, 5, 8, 5, 2)};

            primaryStage.setTitle("3D test");
            VBox root = new VBox();
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
            double canvasWidth = 350;
            double canvasHeight = 350;
            double offset = 80;
            Canvas canvas = new Canvas(canvasWidth + 2 * offset, canvasHeight + 2 * offset);
            GraphicsContext context = canvas.getGraphicsContext2D();

            Canvas canvas2 = new Canvas(canvasWidth + 2 * offset, canvasWidth + 2 * offset);
            GraphicsContext context2 = canvas2.getGraphicsContext2D();
            graphBox.getChildren().add(canvas2);
            graphBox.getChildren().add(canvas);

            NeuralNetwork net = new NeuralNetwork(Initializer.KAIMING, 2, 10, 1)
                    .setLearningRate(0.8)
                    .setLearningRateOptimizer(Optimizer.NONE);
            net.setRectifier(Rectifier.SIGMOID);
            int iter = 200;
            double resolution = 0.2;
            double padding = 5;
            double step = 0.1;
            double angleStep = 5;
            net.fit(in, out, iter);
            NNHeatMap heatMap = new NNHeatMap(0,1,Color.STEELBLUE, Color.TURQUOISE, Color.YELLOW, Color.CRIMSON);

            AtomicReference<NN3DPlot> plot = new AtomicReference<>(new NN3DPlot(context));
            plot.get().setPadding(0,0,50, 50, padding);
            plot.get().plot(net, in, resolution,1,true, heatMap);

            NNMeshGrid plot2 = new NNMeshGrid(context2);
            plot2.setPadding(0,0,50,50,padding);
            plot2.plot(net, in, resolution,1,true,true,true, heatMap);

            AtomicReference<Double> cx = new AtomicReference<>((double) 0);
            AtomicReference<Double> cy = new AtomicReference<>((double) 0);
            AtomicReference<Double> cz = new AtomicReference<>((double) -1); // down ++
            AtomicReference<Double> tx = new AtomicReference<>((double) 0);          // 1.02
            AtomicReference<Double> ty = new AtomicReference<>((double) 0); // up
            AtomicReference<Double> tz = new AtomicReference<>((double) 0); // down ++   padding / 2 = new tz?
            AtomicReference<Double> dx = new AtomicReference<>((double) 0);          // 1.02
            AtomicReference<Double> dy = new AtomicReference<>((double) 0); // up
            AtomicReference<Double> dz = new AtomicReference<>((double) 0);
            // // -0.5000000000000002, cy: 0.4, cz: -1.0, tx: 0.6, ty: 0.0, tz: -1.3000000000000003
            //cx: -0.0025, cy: 0.002, cz: -0.005, tx: 0.08999999999999958, ty: 0.46999999999999953, tz: -1.3499999999999999



            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 0.67, ty: 1, tz: -1.45           // padding 0
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 0.84, ty: 1.0, tz: -1.63         // padding 0.1
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 1.05, ty: 1.0, tz: -1.86         // padding 0.2
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 1.73, ty: 1.0, tz: -2.52         // padding 0.5
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 3.3, ty: 1.0, tz: -3.87          // padding 1
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 8.24, ty: 1.0, tz: -7.64         // padding 2
            // cx: -0.0025, cy: 0.002, cz: -0.005, tx: 25.5, ty: 1.0, tz: -18         // padding 5









            final double[][][] matrix = {plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get())};
            Button btnccx = new Button("+ cx");
            btnccx.setOnAction(e -> {
                cx.updateAndGet(v -> new Double((double) (v + step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            cBox.getChildren().add(btnccx);
            Button btnccxm = new Button("- cx");
            btnccxm.setOnAction(e -> {
                cx.updateAndGet(v -> new Double((double) (v - step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            cBox.getChildren().add(btnccxm);
            Button btnccy = new Button("+ cy");
            btnccy.setOnAction(e -> {
                cy.updateAndGet(v -> new Double((double) (v + step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            cBox.getChildren().add(btnccy);
            Button btnccym = new Button("- cy");
            btnccym.setOnAction(e -> {
                cy.updateAndGet(v -> new Double((double) (v - step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            cBox.getChildren().add(btnccym);
            Button btnccz = new Button("+ cz .");
            btnccz.setOnAction(e -> {
                cz.updateAndGet(v -> new Double((double) (v + step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            cBox.getChildren().add(btnccz);
            Button btncczm = new Button("- cz .");
            btncczm.setOnAction(e -> {
                cz.updateAndGet(v -> new Double((double) (v - step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            cBox.getChildren().add(btncczm);
            Button btnctx = new Button("move right");
            btnctx.setOnAction(e -> {
                tx.updateAndGet(v -> new Double((double) (v + step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            tBox.getChildren().add(btnctx);
            Button btnctxm = new Button("move left");
            btnctxm.setOnAction(e -> {
                tx.updateAndGet(v -> new Double((double) (v - step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            tBox.getChildren().add(btnctxm);
            Button btncty = new Button("move up");
            btncty.setOnAction(e -> {
                ty.updateAndGet(v -> new Double((double) (v + step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            tBox.getChildren().add(btncty);
            Button btnctym = new Button("move down");
            btnctym.setOnAction(e -> {
                ty.updateAndGet(v -> new Double((double) (v - step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            tBox.getChildren().add(btnctym);
            Button btnctz = new Button("zoom in");
            btnctz.setOnAction(e -> {
                tz.updateAndGet(v -> new Double((double) (v + step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            tBox.getChildren().add(btnctz);
            Button btnctzm = new Button("zoom out");
            btnctzm.setOnAction(e -> {
                tz.updateAndGet(v -> new Double((double) (v - step)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            tBox.getChildren().add(btnctzm);
            Button btncdx = new Button("rotate x +");
            btncdx.setOnAction(e -> {
                dx.updateAndGet(v -> new Double((double) (v + angleStep)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            dBox.getChildren().add(btncdx);
            Button btncdxm = new Button("rotate x -");
            btncdxm.setOnAction(e -> {
                dx.updateAndGet(v -> new Double((double) (v - angleStep)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            dBox.getChildren().add(btncdxm);
            Button btncdy = new Button("rotate y +");
            btncdy.setOnAction(e -> {
                dy.updateAndGet(v -> new Double((double) (v + angleStep)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            dBox.getChildren().add(btncdy);
            Button btncdym = new Button("rotate y -");
            btncdym.setOnAction(e -> {
                dy.updateAndGet(v -> new Double((double) (v - angleStep)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            dBox.getChildren().add(btncdym);
            Button btncdz = new Button("rotate z +");
            btncdz.setOnAction(e -> {
                dz.updateAndGet(v -> new Double((double) (v + angleStep)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            dBox.getChildren().add(btncdz);
            Button btnctdzm = new Button("rotate z -");
            btnctdzm.setOnAction(e -> {
                dz.updateAndGet(v -> new Double((double) (v - angleStep)));
                matrix[0] = plot.get().getProjectionMatrix2(cx.get(), cy.get(), cz.get(), tx.get(), ty.get(), tz.get(), dx.get(), dy.get(), dz.get());
                plot.get().plot(matrix[0], net, in, resolution,1,true, heatMap);
                System.out.println("cx: " + cx + ", cy: " + cy + ", cz: " + cz + ", tx: " + tx + ", ty: " + ty +  ", tz: " + tz);
            });
            dBox.getChildren().add(btnctdzm);


            /*
            public static double[][] getProjectionMatrix2() {


        m = NN3DPlot.multiply(m, translate(0,0, camera[2]));
        m = NN3DPlot.multiply(centralProjection(), m);
        return m;
    }
             */
            double[] camera = {0,0,-10};


            //System.out.println(net);

            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
