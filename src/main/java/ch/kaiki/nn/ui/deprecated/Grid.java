package ch.kaiki.nn.ui.deprecated;

import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

import java.util.Arrays;

import static javafx.scene.paint.Color.*;

public class Grid {
    private NN3DPlot plot;
    private final GridFace face;
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
    private Color zeroLineColor;
    private Color axisColor;
    private Color tickColor;
    private Color tickLabelColor;
    private Color axisLabelColor;

    private final double TICK_MARK_OFFSET = 6;
    private final double TICK_LABEL_OFFSET = 20;
    private final double AXIS_LABEL_OFFSET = 40;

    private double tickStrokeWidth = 1.5;
    private double gridStrokeWidth = 0.5;
    private double axisStrokeWidth = 1.5;
    private double zeroLineStrokeWidth = 1;

    private boolean showSecondZAxis = true;
    private boolean visualizeAsCube;

    public Grid(NN3DPlot plot, GridFace face, double[] a, double[] b, double[] c, double[] d, Color gridBackgroundColor, Color gridColor, Color axisColor, boolean visualizeAsCube) {
        this.plot = plot;
        this.face = face;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.gridBackgroundColor = gridBackgroundColor;
        this.gridColor = gridColor;
        this.zeroLineColor = gridColor;
        this.axisColor = axisColor;
        this.tickColor = axisColor;
        this.tickLabelColor = axisColor;
        this.axisLabelColor = axisColor;
        this.visualizeAsCube = visualizeAsCube;
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

        switch (face) {
            case FRONT:
            case BACK:
                min1 = a[0];
                max1 = b[0];
                min2 = b[2];
                max2 = c[2];
                cnst = a[1];
                break;
            case LEFT:
            case RIGHT:
                min1 = a[2];
                max1 = b[2];
                min2 = b[1];
                max2 = c[1];
                cnst = a[0];
                break;
            case BOTTOM:
            case TOP:
            default:
                min1 = a[0];
                max1 = b[0];
                min2 = b[1];
                max2 = c[1];
                cnst = a[2];
                break;
        }


        double range1 = Math.abs(max1 - min1);
        double range2 = Math.abs(max2 - min2);

        double step1 = plot.getInterval(range1, 1000);
        double v1 = min1 - (min1 % step1) - step1;
        double offset1 = getOffset1(range1, range2);
        double offsetTickLabelStart = min2-offset1;
        double offsetTickLabelEnd = max2+offset1;
        double offsetTickStart = min2-offset1*0.3;
        double offsetTickEnd = max2+offset1*0.3;
        double offsetLabelStart1 = min2-offset1*2;
        double offsetLabelEnd1 = max2+offset1*2;
        double offsetLabelStart2;
        double offsetLabelEnd2;
        boolean isStartZSmallerEqual = true;
        int tickCount1 = (int) (range1 / step1) + 3;
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

            switch (face){
                case FRONT:
                case BACK:
                    start = plot.transform(new double[]{v1, cnst, min2});
                    end = plot.transform(new double[]{v1, cnst, max2});
                    isStartZSmallerEqual = start[3] <= end[3];
                    break;
                case LEFT:
                case RIGHT:
                    start = plot.transform(new double[]{cnst, min2, v1});
                    end = plot.transform(new double[]{cnst, max2, v1});
                    isStartZSmallerEqual = start[3] <= end[3];
                    double[] center;
                    if (start[3] != end[3]) {
                        center = plot.transform(new double[]{cnst, isStartZSmallerEqual ? min2 : max2, v1});
                        tickLabel = plot.transform(new double[]{cnst, isStartZSmallerEqual ? offsetTickLabelStart : offsetTickLabelEnd, v1});      // z-Axis #1 (front)
                        tickLabel = getScaledVector(center, tickLabel, TICK_LABEL_OFFSET);
                        tickStart = isStartZSmallerEqual ? plot.transform(new double[]{cnst, offsetTickStart, v1}) : end;
                        tickEnd = isStartZSmallerEqual ? start : plot.transform(new double[]{cnst, max2 + range2 * 0.1 * 0.3, v1});
                        if (isStartZSmallerEqual) {
                            tickStart = getScaledVector(center, tickStart, TICK_MARK_OFFSET);
                        } else {
                            tickEnd = getScaledVector(center, tickEnd, TICK_MARK_OFFSET);
                        }
                    }
                    break;
                case TOP:
                case BOTTOM:
                default:
                    start = plot.transform(new double[]{v1, min2, cnst});
                    end = plot.transform(new double[]{v1, max2, cnst});
                    isStartZSmallerEqual = start[3] <= end[3];
                    center = plot.transform(new double[]{v1, isStartZSmallerEqual ? min2 : max2, cnst});
                    tickLabel = plot.transform(new double[]{v1, isStartZSmallerEqual ? offsetTickLabelStart : offsetTickLabelEnd, cnst});      // x-Axis
                    tickLabel = getScaledVector(center, tickLabel, TICK_LABEL_OFFSET);
                    tickStart = isStartZSmallerEqual ? plot.transform(new double[]{v1, offsetTickStart, cnst}) : end;
                    tickEnd = isStartZSmallerEqual ? start : plot.transform(new double[]{v1, offsetTickEnd, cnst});
                    if (isStartZSmallerEqual) {
                        tickStart = getScaledVector(center, tickStart, TICK_MARK_OFFSET);
                    } else {
                        tickEnd = getScaledVector(center, tickEnd, TICK_MARK_OFFSET);
                    }
                    break;
            }

            // grid line
            if (Double.parseDouble(String.format("%.2f", v1)) == 0) {
                plot.context.setStroke(zeroLineColor);
                plot.context.setLineWidth(zeroLineStrokeWidth);
            } else {
                plot.context.setStroke(gridColor);
                plot.context.setLineWidth(gridStrokeWidth);
            }
            plot.context.strokeLine(start[0], start[1], end[0], end[1]);

            // tick mark
            plot.context.setLineWidth(tickStrokeWidth);
            plot.context.setStroke(tickColor);
            plot.context.strokeLine(tickStart[0], tickStart[1], tickEnd[0], tickEnd[1]);

            // tick label
            plot.context.setFill(tickLabelColor);
            plot.context.fillText(plot.formatTickLabel(v1), tickLabel[0], tickLabel[1]);
        }

        switch (face) {
            case TOP:
            case BOTTOM:
                double[] center = plot.transform(new double[]{range1/2 + min1, isStartZSmallerEqual ? min2 : max2, cnst});
                double[] label = plot.transform(new double[]{range1/2 + min1, isStartZSmallerEqual ? offsetLabelStart1 : offsetLabelEnd1, cnst});
                label = getScaledVector(center, label, AXIS_LABEL_OFFSET);
                double angle = getAngle(isStartZSmallerEqual, t0, t1, t2, t3, null);
                drawLabel("x-Axis", label, angle);
                break;
            case LEFT:
            case RIGHT:
                center = plot.transform(new double[]{cnst, isStartZSmallerEqual ? min2 : max2, range1/2 + min1});
                label = plot.transform(new double[]{cnst, isStartZSmallerEqual ? offsetLabelStart1 : offsetLabelEnd1, range1/2 + min1});
                label = getScaledVector(center, label, AXIS_LABEL_OFFSET);
                angle = getAngle(isStartZSmallerEqual, t0, t1, t2, t3, GridFace.RIGHT);
                drawLabel("z-Axis", label, angle);
                break;
            default:
                break;
        }


        double step2 = plot.getInterval(range2, 1000);
        double v2 = min2 - (min2 % step2) - step2;
        int tickCount2 = (int) (range2 / step2) + 3;

        double offset2 = getOffset2(range1, range2);
        offsetTickLabelStart = min1 - offset2;
        offsetTickLabelEnd = max1 + offset2;
        offsetTickStart = min1 - offset2 * 0.3;
        offsetTickEnd = max1 + offset2 * 0.3;
        offsetLabelStart2 = min1-offset2*2;
        offsetLabelEnd2 = max1+offset2*2;
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

            switch (face){
                case LEFT:
                case RIGHT:
                    start = plot.transform(new double[]{cnst, v2, min1});
                    end = plot.transform(new double[]{cnst, v2, max1});
                    isStartZSmallerEqual = start[3] <= end[3];
                    break;
                case FRONT:
                case BACK:
                    start = plot.transform(new double[]{min1, cnst, v2});
                    end = plot.transform(new double[]{max1, cnst, v2});
                    isStartZSmallerEqual = start[3] <= end[3];
                    double[] center;
                    if (start[3] != end[3] && showSecondZAxis) {
                        center = plot.transform(new double[]{isStartZSmallerEqual ? min1 : max1, cnst, v2});
                        tickLabel = plot.transform(new double[]{isStartZSmallerEqual ? offsetTickLabelStart : offsetTickLabelEnd, cnst, v2});      // z-Axis #2 (back)
                        tickLabel = getScaledVector(center, tickLabel, TICK_LABEL_OFFSET);
                        tickStart = isStartZSmallerEqual ? plot.transform(new double[]{offsetTickStart, cnst, v2}) : end;
                        tickEnd = isStartZSmallerEqual ? start : plot.transform(new double[]{offsetTickEnd, cnst, v2});
                        if (isStartZSmallerEqual) {
                            tickStart = getScaledVector(center, tickStart, TICK_MARK_OFFSET);
                        } else {
                            tickEnd = getScaledVector(center, tickEnd, TICK_MARK_OFFSET);
                        }
                    }
                    break;
                case TOP:
                case BOTTOM:
                default:
                    start = plot.transform(new double[]{min1, v2, cnst});
                    end = plot.transform(new double[]{max1, v2, cnst});
                    isStartZSmallerEqual = start[3] <= end[3];
                    center = plot.transform(new double[]{isStartZSmallerEqual ? min1 : max1, v2, cnst});
                    tickLabel = plot.transform(new double[]{isStartZSmallerEqual ? offsetTickLabelStart : offsetTickLabelEnd, v2, cnst});      // y-Axis
                    tickLabel = getScaledVector(center, tickLabel, TICK_LABEL_OFFSET);
                    tickStart = isStartZSmallerEqual? plot.transform(new double[]{offsetTickStart, v2, cnst}) : end;
                    tickEnd = isStartZSmallerEqual ? start : plot.transform(new double[]{offsetTickEnd, v2, cnst});
                    if (isStartZSmallerEqual) {
                        tickStart = getScaledVector(center, tickStart, TICK_MARK_OFFSET);
                    } else {
                        tickEnd = getScaledVector(center, tickEnd, TICK_MARK_OFFSET);
                    }
                    break;
            }

            // grid line
            if (Double.parseDouble(String.format("%.2f", v2)) == 0) {
                plot.context.setStroke(zeroLineColor);
                plot.context.setLineWidth(zeroLineStrokeWidth);
            } else {
                plot.context.setStroke(gridColor);
                plot.context.setLineWidth(gridStrokeWidth);
            }
            plot.context.strokeLine(start[0], start[1], end[0], end[1]);

            // tick mark
            plot.context.setLineWidth(tickStrokeWidth);
            plot.context.setStroke(tickColor);
            plot.context.strokeLine(tickStart[0], tickStart[1], tickEnd[0], tickEnd[1]);

            // tick label
            plot.context.setFill(tickLabelColor);
            plot.context.fillText(plot.formatTickLabel(v2), tickLabel[0], tickLabel[1]);

        }

        switch (face) {
            case TOP:
            case BOTTOM:
                double[] center = plot.transform(new double[]{isStartZSmallerEqual ? min1 : max1, range2/2 + min2, cnst});
                double[] label = plot.transform(new double[]{isStartZSmallerEqual ? offsetLabelStart2 : offsetLabelEnd2, range2/2 + min2, cnst});
                label = getScaledVector(center, label, AXIS_LABEL_OFFSET);
                double angle = getAngle(isStartZSmallerEqual, t3, t0, t1, t2, null);
                if (center[3] == 0) {
                    angle = angle + 180;
                }
                drawLabel("y-Axis", label, angle);
                break;
            case FRONT:
            case BACK:
                if (showSecondZAxis) {
                    center = plot.transform(new double[]{isStartZSmallerEqual ? min1 : max1, cnst, range2/2 + min2});
                    label = plot.transform(new double[]{isStartZSmallerEqual ? offsetLabelStart2 : offsetLabelEnd2, cnst, range2/2 + min2});
                    label = getScaledVector(center, label, AXIS_LABEL_OFFSET);
                    angle = getAngle(isStartZSmallerEqual, t3, t0, t1, t2, GridFace.BACK);
                    drawLabel("z-Axis", label, angle);
                }
                break;
            default:
                break;
        }


        plot.context.setLineWidth(axisStrokeWidth);
        plot.context.setStroke(axisColor);
        plot.context.strokeLine(t0[0], t0[1], t1[0], t1[1]);
        plot.context.strokeLine(t1[0], t1[1], t2[0], t2[1]);
        plot.context.strokeLine(t2[0], t2[1], t3[0], t3[1]);
        plot.context.strokeLine(t3[0], t3[1], t0[0], t0[1]);

    }

    private double getOffset1(double range1, double range2) {
        if (!visualizeAsCube) {
            return Math.min(range1, range2) * 0.1;
        }
        return range2 * 0.1;
    }

    private double getOffset2(double range1, double range2) {
        if (!visualizeAsCube) {
            return Math.min(range1, range2) * 0.1;
        }
        return range1 * 0.1;
    }

    private double getAngle(boolean isStartZSmallerEqual, double[] a, double[] b, double[] c, double[] d, GridFace filter) {
        double v0x = 1;
        double v0y = 0;
        double v1x = isStartZSmallerEqual ? (a[0] - b[0]) : (c[0] - d[0]);
        double v1y = isStartZSmallerEqual ? (a[1] - b[1]) : (c[1] - d[1]);
        double angle = Math.toDegrees(Math.acos((v0x * v1x + v0y * v1y) / ((v0x + v0y) * Math.sqrt(v1x*v1x + v1y*v1y))));
        boolean dontFlip = isStartZSmallerEqual ? (a[1] < b[1]) : (c[1] < d[1]);
        if (!dontFlip) {
            angle = 180-angle;
        } else {
            angle+= 180;
        }
        if (filter != null && face == filter) {
            angle+= 180;
        }
        return angle;
    }

    private double[] getScaledVector(double[] start, double[] end, double factor) {
        double[] vector = new double[] {(start[0] - end[0]), (start[1] - end[1])};
        double length = -Math.sqrt(Math.pow(vector[0],2) + Math.pow(vector[1],2));
        return new double[] {vector[0]/length*factor + start[0], vector[1]/length*factor + start[1]};
    }

    private void drawLabel(String text, double[] position, double angle) {
        plot.context.setTextAlign(TextAlignment.CENTER);
        plot.context.setFill(axisLabelColor);
        plot.context.transform(new Affine(new Rotate(-angle, position[0],position[1])));
        plot.context.fillText(text, position[0], position[1]);
        plot.context.transform(new Affine(new Rotate(angle, position[0],position[1])));
        //plot.context.fillText(text, position[0], position[1]);
    }

    public double getZ() {
        return z;
    }

}
