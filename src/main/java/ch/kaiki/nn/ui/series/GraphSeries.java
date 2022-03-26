package ch.kaiki.nn.ui.series;

import ch.kaiki.nn.data.*;
import ch.kaiki.nn.ui.BasePlot;
import ch.kaiki.nn.ui.NN2DPlot;
import ch.kaiki.nn.ui.NN3DPlot;
import ch.kaiki.nn.ui.color.GraphColor;
import ch.kaiki.nn.ui.seriesobject.Line;
import ch.kaiki.nn.ui.seriesobject.Point;
import ch.kaiki.nn.ui.seriesobject.SortableSeriesData;
import ch.kaiki.nn.ui.util.ChartMode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GraphSeries extends Series {

    private IGraph graph;
    private GraphColor graphColor;
    private final BasePlot chart;
    private final GraphicsContext context;
    private boolean is2D;
    private double yMinLimit = Double.MIN_VALUE;
    private double yMaxLimit = Double.MAX_VALUE;

    public GraphSeries(BasePlot chart, IGraph graph, String name, GraphColor graphColor) {
        super(Arrays.asList(name), Arrays.asList(graphColor.getEdgeColor()), ChartMode.LINE_OR_SCATTER);
        this.chart = chart;
        this.context = chart.getContext();
        this.graph = graph;
        this.graphColor = graphColor;
        this.is2D = chart instanceof NN2DPlot;
    }

    @Override
    public void compute() {
        xMin = Double.MAX_VALUE;
        xMax = Double.MIN_VALUE;
        yMin = Double.MAX_VALUE;
        yMax = Double.MIN_VALUE;
        zMin = Double.MAX_VALUE;
        zMax = Double.MIN_VALUE;
        List<IVertice> vertices = graph.getVertices();
        for (IVertice vertice : vertices) {
            double xCoordinate = vertice.getX();
            double yCoordinate = vertice.getY();
            double zCoordinate = vertice.getZ();
            if (xCoordinate < xMin) {
                xMin = xCoordinate;
            } else if (xCoordinate > xMax) {
                xMax = xCoordinate;
            }
            if (yCoordinate < yMin) {
                yMin = yCoordinate;
            } else if (yCoordinate > yMax) {
                yMax = yCoordinate;
            }
            if (zCoordinate < zMin) {
                zMin = zCoordinate;
            } else if (zCoordinate > zMax) {
                zMax = zCoordinate;
            }
        }
    }

    @Override
    public void render() {
        List<Line> lines = new ArrayList<>();
        List<Point> points = new ArrayList<>();
        for (IEdge edge : graph.getEdges()) {
            double[] a = chart.transform(edge.getFrom().getCoordinates());
            double[] b = chart.transform(edge.getTo().getCoordinates());
            Color color = edge.isHighlighted() ? graphColor.getHighlightedEdgeColor() : edge.isVisited() ? graphColor.getVisitedEdgeColor() : graphColor.getEdgeColor();
            lines.add(new Line(context, a[0], a[1], a[2], b[0], b[1], b[2], color));
        }
        for (IVertice vertice : graph.getVertices()) {
            double[] t = chart.transform(vertice.getCoordinates());
            Color color = vertice.isHighlighted() ? graphColor.getHighlightedVerticeColor() : vertice.isVisited() ? graphColor.getVisitedVerticeColor() : graphColor.getVerticeColor();
            points.add(new Point(context, t[0], t[1], t[2], color, false));
        }

        // TODO: sort before rendering
        Comparator<SortableSeriesData> comparator = (SortableSeriesData::compareTo);
        lines.sort(chart instanceof NN3DPlot ? comparator.reversed() : comparator);
        points.sort(chart instanceof NN3DPlot ? comparator.reversed() : comparator);
        for (Line line : lines) {
            line.render();
        }
        for (Point point : points) {
            point.render();
        }

    }

}
