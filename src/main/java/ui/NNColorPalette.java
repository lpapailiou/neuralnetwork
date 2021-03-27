package ui;

import javafx.scene.paint.Color;

/**
 * This class is a helper class for the NNVisualizer. It serves as container for the javafx color palette to be used
 * to visualize a neural network on the ui.
 */

public class NNColorPalette {

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
     * Tho colors of this calor palette can be passed by the constructor only.
     * @param backgroundColor the background color of the visualisation.
     * @param nodeColor the vertice color of the neural network graph.
     * @param lineColor the edge color of the neural network graph as well as text color.
     * @param flashedNodeColor the color of the output node with the highest value.
     * @param inactiveInputNodeColor the color of inactive input nodes.
     * @param upperAccentNodeColor the color to indicate higher node values.
     * @param lowerAccentNodeColor the color to indicate lower node values.
     * @param upperAccentWeightColor the color to indicate higher weights.
     * @param lowerAccentWeightColor the color to indicate lower weights.
     */
    public NNColorPalette(Color backgroundColor, Color nodeColor, Color lineColor, Color flashedNodeColor, Color inactiveInputNodeColor, Color upperAccentNodeColor, Color lowerAccentNodeColor, Color upperAccentWeightColor, Color lowerAccentWeightColor) {
        this.backgroundColor = backgroundColor;
        this.nodeColor = nodeColor;
        this.lineColor = lineColor;
        this.flashedNodeColor = flashedNodeColor;
        this.inactiveInputNodeColor = inactiveInputNodeColor;
        this.upperAccentNodeColor = upperAccentNodeColor;
        this.lowerAccentNodeColor = lowerAccentNodeColor;
        this.upperAccentWeightColor = upperAccentWeightColor;
        this.lowerAccentWeightColor = lowerAccentWeightColor;
    }

    Color getBackgroundColor() {
        return backgroundColor;
    }

    Color getNodeColor() {
        return nodeColor;
    }

    Color getLineColor() {
        return lineColor;
    }

    Color getFlashedNodeColor() {
        return flashedNodeColor;
    }

    Color getInactiveInputNodeColor() {
        return inactiveInputNodeColor;
    }

    Color getUpperAccentNodeColor() {
        return upperAccentNodeColor;
    }

    Color getLowerAccentNodeColor() {
        return lowerAccentNodeColor;
    }

    Color getUpperAccentWeightColor() {
        return upperAccentWeightColor;
    }

    Color getLowerAccentWeightColor() {
        return lowerAccentWeightColor;
    }

}


