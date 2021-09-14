package ch.kaiki.nn.ui.deprecated;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

public class Grid {
    private NN3DPlot plot;
    private final GridFace face;
    private double[] x;
    private double[] y;
    private double z;

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

    public Grid(NN3DPlot plot, GridFace face, double[] a, double[] b, double[] c, double[] d, Color gridBackgroundColor, Color gridColor, Color axisColor) {
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
        initialTransformation();
    }

    private void initialTransformation() {
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
        double cnst;

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

        String label1 = "";
        String label2 = "";
        boolean hasDecoration1 = true;
        boolean hasDecoration2 = true;
        int[] indexMap1;     // value, range, constant
        int[] indexMap2;
        switch (face){
            case FRONT:
            case BACK:
                indexMap1 = new int[] {0,2,1};
                indexMap2 = new int[] {2,0,1};
                hasDecoration1 = false;
                label2 = "z-Axis";      // second z-Axis
                break;
            case LEFT:
            case RIGHT:
                indexMap1 = new int[] {2,1,0};
                indexMap2 = new int[] {1,2,0};
                hasDecoration2 = false;
                label1 = "z-Axis";
                break;
            case TOP:
            case BOTTOM:
            default:
                indexMap1 = new int[] {0,1,2};
                indexMap2 = new int[] {1,0,2};
                label1 = "x-Axis";
                label2 = "y-Axis";
                break;
        }

        double range1 = Math.abs(max1 - min1);
        double range2 = Math.abs(max2 - min2);

        // first grid axis
        double offsetTickStart = min2-range2;
        double offsetTickEnd = max2+range2;
        boolean isStartZSmallerEqual = drawGrid(range1, range2, min1, max1, min2, max2, cnst, offsetTickStart, offsetTickEnd, indexMap1, hasDecoration1);
        if (hasDecoration1) {
            double[] center = getTransformedVector(indexMap1, range1/2 + min1, isStartZSmallerEqual ? min2 : max2, cnst);
            double[] labelPos = getTransformedVector(indexMap1, range1/2 + min1, isStartZSmallerEqual ? offsetTickStart : offsetTickEnd, cnst);
            GridFace filter = (face == GridFace.LEFT || face == GridFace.RIGHT) ? GridFace.RIGHT : null;
            double angle = getAngle(isStartZSmallerEqual, t0, t1, t2, t3, filter);
            drawAxisLabel(center, labelPos, angle, label1);
        }

        // second grid axis
        offsetTickStart = min1 - range1;
        offsetTickEnd = max1 + range1;
        isStartZSmallerEqual = drawGrid(range2, range1, min2, max2, min1, max1, cnst, offsetTickStart, offsetTickEnd, indexMap2, hasDecoration2);
        if (hasDecoration2) {
            double[] center = getTransformedVector(indexMap2, range2/2 + min2, isStartZSmallerEqual ? min1 : max1, cnst);
            double[] labelPos = getTransformedVector(indexMap2, range2/2 + min2, isStartZSmallerEqual ? offsetTickStart : offsetTickEnd, cnst);
            GridFace filter = (face == GridFace.FRONT || face == GridFace.BACK) ? GridFace.BACK : null;
            double angle = getAngle(isStartZSmallerEqual, t3, t0, t1, t2, filter);
            drawAxisLabel(center, labelPos, angle, label2);
        }

        // axis aka border of grid
        plot.context.setLineWidth(axisStrokeWidth);
        plot.context.setStroke(axisColor);
        plot.context.strokeLine(t0[0], t0[1], t1[0], t1[1]);
        plot.context.strokeLine(t1[0], t1[1], t2[0], t2[1]);
        plot.context.strokeLine(t2[0], t2[1], t3[0], t3[1]);
        plot.context.strokeLine(t3[0], t3[1], t0[0], t0[1]);

    }

    private void drawAxisLabel(double[] center, double[] labelPos, double angle, String label) {
        labelPos = getScaledVector(center, labelPos, AXIS_LABEL_OFFSET);
        drawLabel(label, labelPos, angle);
    }

    private boolean drawGrid(double range, double rangeO, double min, double max, double minO, double maxO, double cnst, double offsetTickStart, double offsetTickEnd, int[] indexMap, boolean hasDecoration) {
        double intervalStep = plot.getInterval(range, 1000);
        double value = min - (min % intervalStep) - intervalStep;
        int tickCount = (int) (range / intervalStep) + 3;
        boolean isStartZSmallerEqual = true;
        for (int i = 0; i < tickCount; i++) {
            if (i > 0) {
                value += intervalStep;
            }
            if (value < min || value > max) {
                continue;
            }

            double[] start = getTransformedVector(indexMap, value, minO, cnst);
            double[] end = getTransformedVector(indexMap, value, maxO, cnst);
            isStartZSmallerEqual = start[3] <= end[3];

            // grid line
            if (Double.parseDouble(String.format("%.2f", value)) == 0) {
                plot.context.setStroke(zeroLineColor);
                plot.context.setLineWidth(zeroLineStrokeWidth);
            } else {
                plot.context.setStroke(gridColor);
                plot.context.setLineWidth(gridStrokeWidth);
            }
            plot.context.strokeLine(start[0], start[1], end[0], end[1]);


            if (hasDecoration) {
                double[] center = getTransformedVector(indexMap, value, isStartZSmallerEqual ? minO : maxO, cnst);
                double[] tickLabel = getTransformedVector(indexMap, value, isStartZSmallerEqual ? offsetTickStart : offsetTickEnd, cnst);
                tickLabel = getScaledVector(center, tickLabel, TICK_LABEL_OFFSET);

                double[] tickStart = isStartZSmallerEqual ? getTransformedVector(indexMap, value, offsetTickStart, cnst) : end;
                double[] tickEnd = isStartZSmallerEqual ? start : getTransformedVector(indexMap, value, offsetTickEnd, cnst);
                if (isStartZSmallerEqual) {
                    tickStart = getScaledVector(center, tickStart, TICK_MARK_OFFSET);
                } else {
                    tickEnd = getScaledVector(center, tickEnd, TICK_MARK_OFFSET);
                }

                // tick mark
                plot.context.setLineWidth(tickStrokeWidth);
                plot.context.setStroke(tickColor);
                plot.context.strokeLine(tickStart[0], tickStart[1], tickEnd[0], tickEnd[1]);

                // tick label
                plot.context.setFill(tickLabelColor);
                plot.context.fillText(plot.formatTickLabel(value), tickLabel[0], tickLabel[1]);
            }
        }
        return isStartZSmallerEqual;
    }

    private double[] getTransformedVector(int[] indexMap, double value, double range, double constant) {
        double[] result = new double[3];
        result[indexMap[0]] = value;
        result[indexMap[1]] = range;
        result[indexMap[2]] = constant;
        return plot.transform(result);
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
        Font font = plot.context.getFont();
        plot.context.setFont(new Font(null, 15));
        plot.context.setTextAlign(TextAlignment.CENTER);
        plot.context.setFill(axisLabelColor);
        plot.context.transform(new Affine(new Rotate(-angle, position[0],position[1])));
        plot.context.fillText(text, position[0], position[1]);
        plot.context.transform(new Affine(new Rotate(angle, position[0],position[1])));
        plot.context.setFont(font);
        //plot.context.fillText(text, position[0], position[1]);        // label without rotation
    }

    public double getZ() {
        return z;
    }

}
