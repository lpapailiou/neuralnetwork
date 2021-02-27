package ui;

import javafx.scene.paint.Color;

public class NNColorPalette {

    private Color backgroundColor;
    private Color nodeColor;
    private Color lineColor;
    private Color flashedNodeColor;
    private Color inactiveInputNodeColor;
    private Color positiveAccentColor;
    private Color negativeAccentColor;

    public NNColorPalette(Color backgroundColor, Color nodeColor, Color lineColor, Color flashedNodeColor, Color inactiveInputNodeColor, Color positiveAccentColor, Color negativeAccentColor) {
        this.backgroundColor = backgroundColor;
        this.nodeColor = nodeColor;
        this.lineColor = lineColor;
        this.flashedNodeColor = flashedNodeColor;
        this.inactiveInputNodeColor = inactiveInputNodeColor;
        this.positiveAccentColor = positiveAccentColor;
        this.negativeAccentColor = negativeAccentColor;
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

    public Color getPositiveAccentColor() {
        return positiveAccentColor;
    }

    public Color getNegativeAccentColor() {
        return negativeAccentColor;
    }

}


