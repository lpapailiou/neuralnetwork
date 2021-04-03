package ui;

import data.BackPropData;
import data.BackPropEntity;
import javafx.scene.canvas.GraphicsContext;

import java.util.SortedMap;
import java.util.function.Function;

public class NNPlot extends Plot {

    private SortedMap<Integer, BackPropEntity> map;
    private Function<BackPropEntity, Double> function;

    public NNPlot(GraphicsContext context) {
        super(context);
    }

    public void plot(BackPropData data, Function<BackPropEntity, Double> function, boolean asDot, double smoothing) {
        this.map = data.getMap();
        this.function = function;
        xMin = map.keySet().stream().min(Integer::compare).get();
        xMax = map.keySet().stream().max(Integer::compare).get();
        yMin = map.values().stream().map(function).min(Double::compare).get();
        yMax = map.values().stream().map(function).max(Double::compare).get();
        if (xMin == xMax) {
            xMin = xMin - 0.5;
            xMax = xMax + 0.5;
        }
        if (yMin == yMax) {
            yMin = yMin - 0.5;
            yMax = yMax + 0.5;
        }

        drawBackground();
        drawGrid(true);

        int smooth = (int) (smoothing * 100);
        smooth = Math.max(smooth, 1);
        smooth = Math.min(smooth, map.size() - 1);
        plotData(asDot, smooth);

        drawAxes(true, true, true);
        setTitle(title);
    }

    private void plotData(boolean scatter, int modulo) {
        System.out.println("modulo : " + modulo);
        context.setStroke(plotDataColor);
        context.setFill(plotDataColor);
        context.setLineWidth(dataLineWidth);

        double y = 0;
        double oldX = 0;
        double oldY = 0;
        boolean init = true;
        int counter = 0;
        for (Integer key : map.keySet()) {
            counter++;
            double x = key;
            y += function.apply(map.get(key));
            if ((counter - 1) % modulo != 0) {
                continue;
            } else {
                y = y / modulo;
            }
            if (scatter) {
                context.fillOval(x(x) - dotRadius / 2.0, y(y) - dotRadius / 2.0, dotRadius, dotRadius);
            } else {
                if (!init) {
                    context.strokeLine(x(oldX), y(oldY), x(x), y(y));
                } else {
                    init = false;
                }
                oldX = x;
                oldY = y;
            }
            y = 0;
        }
    }
}
