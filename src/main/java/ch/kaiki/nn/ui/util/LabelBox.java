package ch.kaiki.nn.ui.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

import static javafx.scene.paint.Color.TRANSPARENT;

public class LabelBox extends Pane {


    Side side = Side.RIGHT;
    VBox vBox = new VBox();

    Font axisLabelFont;
    Color labelColor;

    public LabelBox(Font axisLabelFont, Color labelColor) {
        this.axisLabelFont = axisLabelFont;
        this.labelColor = labelColor;

        vBox.setPadding(new Insets(8));
        vBox.setAlignment(Pos.CENTER);
        vBox.prefHeightProperty().bind(this.heightProperty());
        this.getChildren().add(vBox);

    }


    void add(Series series) {
        HBox hBox = new HBox();

        Circle circle = new Circle();
        circle.setRadius(4.2);
        circle.setFill(TRANSPARENT);
        circle.setStroke(series.getColor());
        circle.setStrokeWidth(3.5);
        circle.setTranslateY(2.4);
        hBox.getChildren().add(circle);

        Region region = new Region();
        region.setMinWidth(5);
        hBox.getChildren().add(region);

        Label label = new Label(series.getName());
        label.setFont(axisLabelFont);
        label.setTextFill(labelColor);
        hBox.getChildren().add(label);

        vBox.getChildren().add(hBox);
    }

    void remove(Series series) {
        // TODO
    }
}
