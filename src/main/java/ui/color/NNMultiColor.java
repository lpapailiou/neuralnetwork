package ui.color;


import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class NNMultiColor implements NNColorSupport {

    List<Color> colors = new ArrayList<>();

    public NNMultiColor(Color... colors) {
        for (Color color : colors) {
            if (color == null) {
                color = Color.TRANSPARENT;
            }
            this.colors.add(color);
        }
    }

    public List<Color> getColors() {
        return colors;
    }
}
