package ch.kaiki.nn.ui.color;
import javafx.scene.paint.Color;

public class NNPlotColor {
    private Color backgroundColor;
    private Color plotBackgroundColor;
    private Color axisColor;
    private Color tickColor;
    private Color gridColor;
    private Color textColor;

    public NNPlotColor(Color backgroundColor, Color plotBackgroundColor, Color plotAxisColor, Color plotGridColor, Color tickColor, Color plotTextColor, Color plotDataColor) {
        this.backgroundColor = backgroundColor == null ? Color.TRANSPARENT : backgroundColor;
        this.plotBackgroundColor = plotBackgroundColor == null ? Color.TRANSPARENT : plotBackgroundColor;
        this.axisColor = plotAxisColor == null ? Color.TRANSPARENT : plotAxisColor;
        this.tickColor = tickColor == null ? Color.TRANSPARENT : tickColor;
        this.gridColor = plotGridColor == null ? Color.TRANSPARENT : plotGridColor;
        this.textColor = plotTextColor == null ? Color.TRANSPARENT : plotTextColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getPlotBackgroundColor() {
        return plotBackgroundColor;
    }

    public Color getAxisColor() {
        return axisColor;
    }

    public Color getTickColor() {
        return tickColor;
    }

    public Color getGridColor() {
        return gridColor;
    }

    public Color getTextColor() {
        return textColor;
    }

}
