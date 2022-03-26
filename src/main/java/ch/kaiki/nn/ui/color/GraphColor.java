package ch.kaiki.nn.ui.color;

import javafx.scene.paint.Color;

/**
 * This class is a helper class for the Graph. It serves as container for the javafx color palette to be used
 * to visualize a neural network on the ui.
 */
public class GraphColor {

    private final Color verticeColor;
    private final Color edgeColor;
    private final Color visitedVerticeColor;
    private final Color visitedEdgeColor;
    private final Color highlightedVerticeColor;
    private final Color highlightedEdgeColor;

    public GraphColor(Color verticeColor, Color edgeColor, Color visitedVerticeColor, Color visitedEdgeColor, Color highlightedVerticeColor, Color highlightedEdgeColor) {
        this.verticeColor = verticeColor == null ? Color.TRANSPARENT : verticeColor;
        this.edgeColor = edgeColor == null ? Color.TRANSPARENT : edgeColor;
        this.visitedVerticeColor = visitedVerticeColor == null ? Color.TRANSPARENT : visitedVerticeColor;
        this.visitedEdgeColor = visitedEdgeColor == null ? Color.TRANSPARENT : visitedEdgeColor;
        this.highlightedVerticeColor = highlightedVerticeColor == null ? Color.TRANSPARENT : highlightedVerticeColor;
        this.highlightedEdgeColor = highlightedEdgeColor == null ? Color.TRANSPARENT : highlightedEdgeColor;
    }

    public GraphColor(Color baseColor, Color visitedColor, Color highlightedColor) {
        this(baseColor, baseColor, visitedColor, visitedColor, highlightedColor, highlightedColor);
    }

    public Color getVerticeColor() {
        return verticeColor;
    }

    public Color getEdgeColor() {
        return edgeColor;
    }

    public Color getVisitedVerticeColor() {
        return visitedVerticeColor;
    }

    public Color getVisitedEdgeColor() {
        return visitedEdgeColor;
    }

    public Color getHighlightedVerticeColor() {
        return highlightedVerticeColor;
    }

    public Color getHighlightedEdgeColor() {
        return highlightedEdgeColor;
    }


}


