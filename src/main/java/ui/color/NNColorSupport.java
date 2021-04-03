package ui.color;

import javafx.scene.paint.Color;

import java.text.DecimalFormat;

import static javafx.scene.paint.Color.TRANSPARENT;

public interface NNColorSupport {

    static DecimalFormat df = new DecimalFormat("#.###");

    public static Color randomColor(boolean includeOpacity) {
        return new Color(Math.random(), Math.random(), Math.random(), includeOpacity ? Math.random() : 1);
    }

    public static Color blend(Color c1, Color c2, double ratio) {
        if (ratio > 1.0)  {
            ratio = 1;
        } else if (ratio < 0.0) {
            ratio = 0;
        }
        double iRatio = 1.0 - ratio;

        Color rgb1 = c1 == TRANSPARENT ? c2 : c1;
        Color rgb2 = c2 == TRANSPARENT ? c1 : c2;

        int r1 = isolateComponent(rgb1.getRed());
        int g1 = isolateComponent(rgb1.getGreen());
        int b1 = isolateComponent(rgb1.getBlue());
        int a1 = isolateComponent(c1.getOpacity());

        int r2 = isolateComponent(rgb2.getRed());
        int g2 = isolateComponent(rgb2.getGreen());
        int b2 = isolateComponent(rgb2.getBlue());
        int a2 = isolateComponent(c2.getOpacity());

        double r = convertComponent((r1 * ratio) + (r2 * iRatio));
        double g = convertComponent((g1 * ratio) + (g2 * iRatio));
        double b = convertComponent((b1 * ratio) + (b2 * iRatio));
        double a = convertComponent((a1 * ratio) + (a2 * iRatio));

        return new Color(r, g, b, a);
    }

    static double convertComponent(double value) {
        return Double.parseDouble(df.format(value / 255));
    }

    static int isolateComponent(double component) {
        return Integer.parseInt(Integer.toHexString(((int) (component * 255))), 16);
    }
}
