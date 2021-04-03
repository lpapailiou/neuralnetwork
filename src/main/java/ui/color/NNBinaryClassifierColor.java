package ui.color;

import javafx.scene.paint.Color;

public class NNBinaryClassifierColor implements NNColorSupport {

    private Color positive;
    private Color negative;
    private Color margin;

    public NNBinaryClassifierColor(Color positive, Color negative, Color margin) {
        this.positive = positive == null ? Color.TRANSPARENT : positive;
        this.negative = negative == null ? Color.TRANSPARENT : negative;
        this.margin = margin == null ? Color.TRANSPARENT : margin;
    }

    public Color getPositive() {
        return positive;
    }

    public Color getNegative() {
        return negative;
    }

    public Color getMargin() {
        return margin;
    }

}
