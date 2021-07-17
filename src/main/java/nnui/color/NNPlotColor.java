package nnui.color;

import javafx.scene.paint.Color;

public class NNPlotColor {

    private Color backgroundColor;
    private Color plotBackgroundColor;
    private Color plotAxisColor;
    private Color plotGridColor;
    private Color plotTextColor;
    private Color plotDataColor;

    public NNPlotColor(Color backgroundColor, Color plotBackgroundColor, Color plotAxisColor, Color plotGridColor, Color plotTextColor, Color plotDataColor) {
        this.backgroundColor = backgroundColor == null ? Color.TRANSPARENT : backgroundColor;
        this.plotBackgroundColor = plotBackgroundColor == null ? Color.TRANSPARENT : plotBackgroundColor;
        this.plotAxisColor = plotAxisColor == null ? Color.TRANSPARENT : plotAxisColor;
        this.plotGridColor = plotGridColor == null ? Color.TRANSPARENT : plotGridColor;
        this.plotTextColor = plotTextColor == null ? Color.TRANSPARENT : plotTextColor;
        this.plotDataColor = plotDataColor == null ? Color.TRANSPARENT : plotDataColor;
    }

    public NNPlotColor(Color backgroundColor, Color plotAxisColor,  Color plotTextColor) {
        this(backgroundColor, null, plotAxisColor, null, plotTextColor, null);
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
