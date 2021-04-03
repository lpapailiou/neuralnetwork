package ui.color;

import javafx.scene.paint.Color;

public class NNBinaryClassifierColor implements NNColorSupport {

    private Color positive;
    private Color negative;
    private Color margin;

    public NNBinaryClassifierColor(Color positive, Color negative, Color margin) {
        this.positive = positive;
        this.negative = negative;
        this.margin = margin;
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
