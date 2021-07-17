package nnui.color;

import javafx.scene.paint.Color;

/**
 * This class is a helper class for the NNGraph. It serves as container for the javafx color palette to be used
 * to visualize a neural network on the ui.
 */
public class NNGraphColor {

    private Color backgroundColor;
    private Color nodeColor;
    private Color lineColor;
    private Color flashedNodeColor;
    private Color inactiveInputNodeColor;
    private Color upperAccentNodeColor;
    private Color lowerAccentNodeColor;
    private Color upperAccentWeightColor;
    private Color lowerAccentWeightColor;

    /**
     * Tho colors of this color palette can be passed by the constructor only.
     * If null is passed, it will be translated to Color.TRANSPARENT.
     *
     * @param backgroundColor        the background color of the visualisation.
     * @param nodeColor              the vertice color of the neural network graph.
     * @param lineColor              the edge color of the neural network graph as well as text color.
     * @param flashedNodeColor       the color of the output node with the highest value.
     * @param inactiveInputNodeColor the color of inactive input nodes.
     * @param upperAccentNodeColor   the color to indicate higher node values.
     * @param lowerAccentNodeColor   the color to indicate lower node values.
     * @param upperAccentWeightColor the color to indicate higher weights.
     * @param lowerAccentWeightColor the color to indicate lower weights.
     */
    public NNGraphColor(Color backgroundColor, Color nodeColor, Color lineColor, Color flashedNodeColor, Color inactiveInputNodeColor, Color upperAccentNodeColor, Color lowerAccentNodeColor, Color upperAccentWeightColor, Color lowerAccentWeightColor) {
        this.backgroundColor = backgroundColor == null ? Color.TRANSPARENT : backgroundColor;
        this.nodeColor = nodeColor == null ? Color.TRANSPARENT : nodeColor;
        this.lineColor = lineColor == null ? Color.TRANSPARENT : lineColor;
        this.flashedNodeColor = flashedNodeColor == null ? Color.TRANSPARENT : flashedNodeColor;
        this.inactiveInputNodeColor = inactiveInputNodeColor == null ? Color.TRANSPARENT : inactiveInputNodeColor;
        this.upperAccentNodeColor = upperAccentNodeColor == null ? Color.TRANSPARENT : upperAccentNodeColor;
        this.lowerAccentNodeColor = lowerAccentNodeColor == null ? Color.TRANSPARENT : lowerAccentNodeColor;
        this.upperAccentWeightColor = upperAccentWeightColor == null ? Color.TRANSPARENT : upperAccentWeightColor;
        this.lowerAccentWeightColor = lowerAccentWeightColor == null ? Color.TRANSPARENT : lowerAccentWeightColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getNodeColor() {
        return nodeColor;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public Color getFlashedNodeColor() {
        return flashedNodeColor;
    }

    public Color getInactiveInputNodeColor() {
        return inactiveInputNodeColor;
    }

    public Color getUpperAccentNodeColor() {
        return upperAccentNodeColor;
    }

    public Color getLowerAccentNodeColor() {
        return lowerAccentNodeColor;
    }

    public Color getUpperAccentWeightColor() {
        return upperAccentWeightColor;
    }

    public Color getLowerAccentWeightColor() {
        return lowerAccentWeightColor;
    }

}


