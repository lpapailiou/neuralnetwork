package ch.kaiki.nn.ui.color;
import javafx.scene.paint.Color;

import static javafx.scene.paint.Color.TRANSPARENT;

public class NNChartColor {

    private final Color backgroundColor;

    private final Color gridBackgroundColor;
    private final Color axisColor;
    private final Color gridLineColor;
    private final Color zeroLineColor;

    private final Color tickMarkColor;
    private final Color tickLabelColor;

    private final Color labelColor;

    public NNChartColor(Color backgroundColor, Color gridBackgroundColor, Color axisColor, Color gridLineColor, Color zeroLineColor, Color tickMarkColor, Color tickLabelColor, Color labelColor) {
        this.backgroundColor = backgroundColor == null ? TRANSPARENT : backgroundColor;
        this.gridBackgroundColor = gridBackgroundColor == null ? TRANSPARENT : gridBackgroundColor;
        this.axisColor = axisColor == null ? TRANSPARENT : axisColor;
        this.gridLineColor = gridLineColor == null ? TRANSPARENT : gridLineColor;
        this.zeroLineColor = zeroLineColor == null ? TRANSPARENT : zeroLineColor;
        this.tickMarkColor = tickMarkColor == null ? TRANSPARENT : tickMarkColor;
        this.tickLabelColor = tickLabelColor == null ? TRANSPARENT : tickLabelColor;
        this.labelColor = labelColor == null ? TRANSPARENT : labelColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getAxisColor() {
        return axisColor;
    }

    public Color getGridBackgroundColor() {
        return gridBackgroundColor;
    }

    public Color getGridLineColor() {
        return gridLineColor;
    }

    public Color getZeroLineColor() {
        return zeroLineColor;
    }

    public Color getTickMarkColor() {
        return tickMarkColor;
    }

    public Color getTickLabelColor() {
        return tickLabelColor;
    }

    public Color getLabelColor() {
        return labelColor;
    }

}
