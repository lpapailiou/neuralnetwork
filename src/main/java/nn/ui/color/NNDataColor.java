package nn.ui.color;


import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class NNDataColor implements NNColorSupport {

    private List<Color> colors = new ArrayList<>();

    public NNDataColor(Color... colors) {
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
