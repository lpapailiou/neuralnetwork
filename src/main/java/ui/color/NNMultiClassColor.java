package ui.color;


import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NNMultiClassColor implements NNColorSupport {

    List<Color> colors = new ArrayList<>();

    public NNMultiClassColor(Color... colors) {
        this.colors.addAll(Arrays.asList(colors));
    }

    public List<Color> getColors() {
        return colors;
    }
}
