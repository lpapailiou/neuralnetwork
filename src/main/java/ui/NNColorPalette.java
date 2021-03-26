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
    private Color positiveAccentNodeColor;
    private Color negativeAccentNodeColor;
    private Color positiveAccentWeightColor;
    private Color negativeAccentWeightColor;

    /**
     * Tho colors of this calor palette can be passed by the constructor only.
     * @param backgroundColor the background color of the visualisation.
     * @param nodeColor the vertice color of the neural network graph.
     * @param lineColor the edge color of the neural network graph as well as text color.
     * @param flashedNodeColor the color of the output node with the highest value.
     * @param inactiveInputNodeColor the color of inactive input nodes.
     * @param positiveAccentNodeColor the color to indicate positive node values.
     * @param negativeAccentNodeColor the color to indicate negative node values.
     * @param positiveAccentWeightColor the color to indicate positive weights.
     * @param negativeAccentWeightColor the color to indicate negative weights.
     */
    public NNColorPalette(Color backgroundColor, Color nodeColor, Color lineColor, Color flashedNodeColor, Color inactiveInputNodeColor, Color positiveAccentNodeColor, Color negativeAccentNodeColor, Color positiveAccentWeightColor, Color negativeAccentWeightColor) {
        this.backgroundColor = backgroundColor;
        this.nodeColor = nodeColor;
        this.lineColor = lineColor;
        this.flashedNodeColor = flashedNodeColor;
        this.inactiveInputNodeColor = inactiveInputNodeColor;
        this.positiveAccentNodeColor = positiveAccentNodeColor;
        this.negativeAccentNodeColor = negativeAccentNodeColor;
        this.positiveAccentWeightColor = positiveAccentWeightColor;
        this.negativeAccentWeightColor = negativeAccentWeightColor;
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

    Color getPositiveAccentNodeColor() {
        return positiveAccentNodeColor;
    }

    Color getNegativeAccentNodeColor() {
        return negativeAccentNodeColor;
    }

    Color getPositiveAccentWeightColor() {
        return positiveAccentWeightColor;
    }

    Color getNegativeAccentWeightColor() {
        return negativeAccentWeightColor;
    }

}


