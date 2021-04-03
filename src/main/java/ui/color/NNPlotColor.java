package ui.color;

import javafx.scene.paint.Color;

public class NNPlotColor {

    private Color backgroundColor;
    private Color plotBackgroundColor;
    private Color plotAxisColor;
    private Color plotGridColor;
    private Color plotTextColor;
    private Color plotDataColor;

    public NNPlotColor(Color backgroundColor, Color plotBackgroundColor, Color plotAxisColor, Color plotGridColor, Color plotTextColor, Color plotDataColor) {
        this.backgroundColor = backgroundColor;
        this.plotBackgroundColor = plotBackgroundColor;
        this.plotAxisColor = plotAxisColor;
        this.plotGridColor = plotGridColor;
        this.plotTextColor = plotTextColor;
        this.plotDataColor = plotDataColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getPlotBackgroundColor() {
        return plotBackgroundColor;
    }

    public Color getPlotAxisColor() {
        return plotAxisColor;
    }

    public Color getPlotGridColor() {
        return plotGridColor;
    }

    public Color getPlotTextColor() {
        return plotTextColor;
    }

    public Color getPlotDataColor() {
        return plotDataColor;
    }
}
