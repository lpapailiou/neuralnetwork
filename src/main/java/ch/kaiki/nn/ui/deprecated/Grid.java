package ch.kaiki.nn.ui.deprecated;

import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

public class Grid {
    private NN3DPlot plot;
    private String id;
    private double z;
    private double[] x;
    private double[] y;

    private double[] a;
    private double[] b;
    private double[] c;
    private double[] d;

    private double[] t0;
    private double[] t1;
    private double[] t2;
    private double[] t3;

    private Color gridBackgroundColor;
    private Color gridColor;
    private Color axisColor;
    private double tickStrokeWidth = 1;
    private double gridStrokeWidth = 0.5;
    private double axisStrokeWidht = 1;
    private double zeroLineStrokeWidth = 1;

    private boolean showSecondZAxis = true;
    private boolean keepDimensions;

    public Grid(NN3DPlot plot, String id, double[] a, double[] b, double[] c, double[] d, Color gridBackgroundColor, Color gridColor, Color axisColor, boolean keepDimensions) {
        this.plot = plot;
        this.id = id;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.gridBackgroundColor = gridBackgroundColor;
        this.gridColor = gridColor;
        this.axisColor = axisColor;
        this.keepDimensions = keepDimensions;
        transform();
    }

    private void transform() {
        t0 = plot.transform(a);
        t1 = plot.transform(b);
        t2 = plot.transform(c);
        t3 = plot.transform(d);
        z = t0[3]+t1[3]+t2[3]+t3[3];
    }

    void draw() {
        x = new double[]{t0[0], t1[0], t2[0], t3[0]};
        y = new double[]{t0[1], t1[1], t2[1], t3[1]};
        plot.context.setFill(gridBackgroundColor);
        plot.context.fillPolygon(x, y, 4);

        double min1;
        double max1;
        double min2;
        double max2;
        double cnst = 0;

        switch (id) {
            case "bottom":
            case "top":
                min1 = a[0];
                max1 = b[0];
                min2 = b[1];
                max2 = c[1];
                cnst = a[2];
                break;
            case "left":
            case "right":
                min1 = a[2];
                max1 = b[2];
                min2 = b[1];
                max2 = c[1];
                cnst = a[0];
                break;
            case "front":
            case "back":
            default:
                min1 = a[0];
                max1 = b[0];
                min2 = b[2];
                max2 = c[2];
                cnst = a[1];
        }

        plot.context.setStroke(gridColor);

        //double vector1 = 0;
       // double vector2 = 0;

        double range1 = Math.abs(max1 - min1);
        double range2 = Math.abs(max2 - min2);
        double step1 = plot.getInterval(range1, 1000);
        double v1 = min1 - (min1 % step1) - step1;
        int tickCount1 = (int) (range1 / step1) + 2;
        for (int i = 0; i < tickCount1; i++) {
            if (i > 0) {
                v1 += step1;
            }
            if (v1 < min1 || v1 > max1) {
                continue;
            }

            double[] start;
            double[] end;
            double[] tickStart = {0,0};
            double[] tickEnd = {0,0};
            double[] tickLabel = {0,0};
            double offset1 = getOffset1(range1, range2);
            double offsetTickLabelStart = min2-offset1;
            double offsetTickLabelEnd = max2+offset1;
            double offsetTickStart = min2-offset1*0.3;
            double offsetTickEnd = max2+offset1*0.3;
            double offsetLabelStart = min2-offset1*2;
            double offsetLabelEnd = max2+offset1*2;
            switch (id){
                case "top":
                case "bottom":
                    start = plot.transform(new double[]{v1, min2, cnst});
                    end = plot.transform(new double[]{v1, max2, cnst});
                    tickLabel = plot.transform(new double[]{v1, start[3] <= end[3] ? offsetTickLabelStart : offsetTickLabelEnd, cnst});      // x-Axis
                    tickStart = start[3] <= end[3] ? plot.transform(new double[]{v1, offsetTickStart, cnst}) : end;
                    tickEnd = start[3] <= end[3] ? start : plot.transform(new double[]{v1, offsetTickEnd, cnst});

                    break;
                case "left":
                case "right":
                    start = plot.transform(new double[]{cnst, min2, v1});
                    end = plot.transform(new double[]{cnst, max2, v1});
                    if (start[3] != end[3]) {
                        tickLabel = plot.transform(new double[]{cnst, start[3] <= end[3] ? offsetTickLabelStart : offsetTickLabelEnd, v1});      // z-Axis #1 (front)
                        tickStart = start[3] < end[3] ? plot.transform(new double[]{cnst, offsetTickStart, v1}) : end;
                        tickEnd = start[3] < end[3] ? start : plot.transform(new double[]{cnst, max2 + range2 * 0.1 * 0.3, v1});
                    }
                    break;
                case "front":
                case "back":
                default:
                    start = plot.transform(new double[]{v1, cnst, min2});
                    end = plot.transform(new double[]{v1, cnst, max2});
            }
            if (Double.parseDouble(String.format("%,.2f", v1)) == 0) {
                plot.context.setLineWidth(zeroLineStrokeWidth);
            } else {
                plot.context.setLineWidth(gridStrokeWidth);
            }
            plot.context.strokeLine(start[0], start[1], end[0], end[1]);
            plot.context.setLineWidth(tickStrokeWidth);
            plot.context.strokeLine(tickStart[0], tickStart[1], tickEnd[0], tickEnd[1]);
            plot.context.setFill(gridColor);   //TODO
            plot.context.fillText(plot.formatTickLabel(v1), tickLabel[0], tickLabel[1]);


            switch (id) {
                case "top":
                case "bottom":
                    double[] label = plot.transform(new double[]{range1/2 + min1, start[3] <= end[3] ? offsetLabelStart : offsetLabelEnd, cnst});
                    plot.context.setFill(gridColor);   //TODO
                    plot.context.fillText("x-Axis", label[0], label[1]);
                    break;
                case "left":
                case "right":
                    label = plot.transform(new double[]{cnst, start[3] <= end[3] ? offsetLabelStart : offsetLabelEnd, range1/2 + min1});
                    plot.context.setFill(gridColor);   //TODO
                    plot.context.fillText("z-Axis", label[0], label[1]);
                    break;
            }
        }
        double step2 = plot.getInterval(range2, 1000);
        double v2 = min2 - (min2 % step2) - step2;
        int tickCount2 = (int) (range2 / step2) + 2;
        for (int i = 0; i < tickCount2; i++) {
            if (i > 0) {
                v2 += step2;
            }
            if (v2 < min2 || v2 > max2) {
                continue;
            }

            double[] start;
            double[] end;
            double[] tickLabel = {0,0};
            double[] tickStart = {0,0};
            double[] tickEnd = {0,0};
            double offset2 = getOffset2(range1, range2);
            double offsetTickLabelStart = min1 - offset2;
            double offsetTickLabelEnd = max1 + offset2;
            double offsetTickStart = min1 - offset2 * 0.3;
            double offsetTickEnd = max1 + offset2 * 0.3;
            double offsetLabelStart = min1-offset2*2;
            double offsetLabelEnd = max1+offset2*2;
            switch (id){
                case "top":
                case "bottom":
                    start = plot.transform(new double[]{min1, v2, cnst});
                    end = plot.transform(new double[]{max1, v2, cnst});
                    tickLabel = plot.transform(new double[]{start[3] <= end[3] ? offsetTickLabelStart : offsetTickLabelEnd, v2, cnst});      // y-Axis
                    tickStart = start[3] <= end[3] ? plot.transform(new double[]{offsetTickStart, v2, cnst}) : end;
                    tickEnd = start[3] <= end[3] ? start : plot.transform(new double[]{offsetTickEnd, v2, cnst});
                    break;
                case "left":
                case "right":
                    start = plot.transform(new double[]{cnst, v2, min1});
                    end = plot.transform(new double[]{cnst, v2, max1});
                    break;
                case "front":
                case "back":
                default:
                    start = plot.transform(new double[]{min1, cnst, v2});
                    end = plot.transform(new double[]{max1, cnst, v2});
                    if (start[3] != end[3] && showSecondZAxis) {
                        tickLabel = plot.transform(new double[]{start[3] <= end[3] ? offsetTickLabelStart : offsetTickLabelEnd, cnst, v2});      // z-Axis #2 (back)
                        tickStart = start[3] < end[3] ? plot.transform(new double[]{offsetTickStart, cnst, v2}) : end;
                        tickEnd = start[3] < end[3] ? start : plot.transform(new double[]{offsetTickEnd, cnst, v2});
                    }
            }
            if (Double.parseDouble(String.format("%,.2f", v2)) == 0) {
                plot.context.setLineWidth(zeroLineStrokeWidth);
            } else {
                plot.context.setLineWidth(gridStrokeWidth);
            }
            plot.context.strokeLine(start[0], start[1], end[0], end[1]);
            plot.context.setLineWidth(tickStrokeWidth);
            plot.context.strokeLine(tickStart[0], tickStart[1], tickEnd[0], tickEnd[1]);
            plot.context.setFill(gridColor);   //TODO
            plot.context.fillText(plot.formatTickLabel(v2), tickLabel[0], tickLabel[1]);

            switch (id) {
                case "top":
                case "bottom":
                    double[] label = plot.transform(new double[]{start[3] <= end[3] ? offsetLabelStart : offsetLabelEnd, range2/2 + min2, cnst});
                    plot.context.setFill(gridColor);   //TODO
                    double v0x = 1;
                    double v0y = 0;
                    double v1x = Math.abs(t1[0] - t2[0]);
                    double v1y = Math.abs(t1[1] - t2[1]);


                    boolean is2D = false;
                    if (is2D) {
                        double angle = Math.toDegrees(Math.acos((v0x * v1x + v0y * v1y) / ((v0x + v0y) * Math.sqrt(v1x*v1x + v1y*v1y)))) % 180;
                        plot.context.strokeLine(0,0,v1x,v1y);
                        System.out.println();
                        System.out.println("angle: " + angle);
                        System.out.println("vector; [" + v1x + ", " + v1y + "]");
                        //double angle =-90;
                        plot.context.transform(new Affine(new Rotate(-angle, label[0],label[1])));
                        plot.context.fillText("y-Axis", label[0], label[1]);
                        plot.context.transform(new Affine(new Rotate(angle, label[0],label[1])));
                    } else {
                        plot.context.fillText("y-Axis", label[0], label[1]);
                    }
                    break;      //{min1, v2, cnst});
                case "front":
                case "back":
                    if (showSecondZAxis) {
                        label = plot.transform(new double[]{start[3] <= end[3] ? offsetLabelStart : offsetLabelEnd, cnst, range2 / 2 + min2});
                        plot.context.setFill(gridColor);   //TODO
                        plot.context.fillText("z-Axis", label[0], label[1]);
                    }
                    break;      // {min1, cnst, v2}
            }

        }



        plot.context.setLineWidth(axisStrokeWidht);
        plot.context.setStroke(axisColor);
        plot.context.strokeLine(t0[0], t0[1], t1[0], t1[1]);
        plot.context.strokeLine(t1[0], t1[1], t2[0], t2[1]);
        plot.context.strokeLine(t2[0], t2[1], t3[0], t3[1]);
        plot.context.strokeLine(t3[0], t3[1], t0[0], t0[1]);

    }

    private double getOffset1(double range1, double range2) {
        if (keepDimensions) {
            return Math.min(range1, range2) * 0.1;
        }
        return range2 * 0.1;
    }

    private double getOffset2(double range1, double range2) {
        if (keepDimensions) {
            return Math.min(range1, range2) * 0.1;
        }
        return range1 * 0.1;
    }

    public double getZ() {
        return z;
    }

}
