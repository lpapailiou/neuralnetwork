package ch.kaiki.nn.ui.seriesobject;

import ch.kaiki.nn.ui.BaseChart;
import ch.kaiki.nn.ui.NN2DChart;
import ch.kaiki.nn.ui.color.NNChartColor;
import ch.kaiki.nn.ui.util.ChartMode;
import ch.kaiki.nn.ui.util.GridFace;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

import java.text.DecimalFormat;

public class ChartGrid {
    private static DecimalFormat df = new DecimalFormat("#.###");

    private final BaseChart chart;
    private final GraphicsContext context;
    private final GridFace face;
    private double[] x;
    private double[] y;
    private double z;

    private final double[] a;
    private final double[] b;
    private final double[] c;
    private final double[] d;

    private double[] t0;
    private double[] t1;
    private double[] t2;
    private double[] t3;

    private final Color axisColor;
    private final Color gridBackgroundColor;
    private final Color gridLineColor;
    private final Color zeroLineColor;
    private final Color tickMarkColor;
    private final Color tickLabelColor;
    private final Color labelColor;

    private final String[] axisLabels;

    private static final double TICK_MARK_OFFSET = 6;
    private static final double TICK_LABEL_OFFSET = 20;
    private static final double AXIS_LABEL_OFFSET = 40;

    private final double tickStrokeWidth = 1;
    private final double gridStrokeWidth = 0.25;
    private final double axisStrokeWidth = 1;
    private final double zeroLineStrokeWidth = 0.75;

    private boolean is2D;

    public ChartGrid(BaseChart chart, GridFace face, double[] a, double[] b, double[] c, double[] d, NNChartColor chartColors, String[] axisLabels) {
        this.chart = chart;
        this.context = chart.getContext();
        this.is2D = chart instanceof NN2DChart;
        this.face = face;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.axisColor = chartColors.getAxisColor();
        this.gridBackgroundColor = chartColors.getGridBackgroundColor();
        this.gridLineColor = chartColors.getGridLineColor();
        this.zeroLineColor = chartColors.getZeroLineColor();
        this.tickMarkColor = chartColors.getTickMarkColor();
        this.tickLabelColor = chartColors.getTickLabelColor();
        this.labelColor = chartColors.getLabelColor();
        this.axisLabels = axisLabels;
        if (!is2D && chart.getChartMode() == ChartMode.LINE_OR_SCATTER) {
            String y = this.axisLabels[1];
            String z = this.axisLabels[2];
            //this.axisLabels[1] = z;
            //this.axisLabels[2] = y;
            //System.out.println(face);
            //System.out.println(Arrays.toString(this.axisLabels));
            // TODO check y not working
        }
        initialTransformation();
    }

    private void initialTransformation() {
        t0 = chart.transform(a);
        t1 = chart.transform(b);
        t2 = chart.transform(c);
        t3 = chart.transform(d);
        z = t0[3]+t1[3]+t2[3]+t3[3];
    }

    public void render() {

        x = new double[]{t0[0], t1[0], t2[0], t3[0]};
        y = new double[]{t0[1], t1[1], t2[1], t3[1]};

        // grid background
        if (chart.showGridContent()) {
            context.setFill(gridBackgroundColor);
            context.fillPolygon(x, y, 4);
        }

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
                label2 = axisLabels[2];      // second z-Axis
                break;
            case LEFT:
            case RIGHT:
                indexMap1 = new int[] {2,1,0};
                indexMap2 = new int[] {1,2,0};
                hasDecoration2 = false;
                label1 = axisLabels[2];
                break;
            case TOP:
            case BOTTOM:
            default:
                indexMap1 = new int[] {0,1,2};
                indexMap2 = new int[] {1,0,2};
                label1 = axisLabels[0];
                label2 = axisLabels[1];
                break;
        }

        double range1 = Math.abs(max1 - min1);
        double range2 = Math.abs(max2 - min2);

        // first grid axis
        double offsetTickStart = min2-range2;
        double offsetTickEnd = max2+range2;
        boolean isStartZSmaller = drawGrid(range1, range2, min1, max1, min2, max2, cnst, offsetTickStart, offsetTickEnd, indexMap1, hasDecoration1);
        if (hasDecoration1 && chart.showAxisLabels()) {
            double[] center = getTransformedVector(indexMap1, range1/2 + min1, isStartZSmaller ? min2 : max2, cnst);
            double[] labelPos = getTransformedVector(indexMap1, range1/2 + min1, isStartZSmaller ? offsetTickStart : offsetTickEnd, cnst);
            GridFace filter = (face == GridFace.LEFT || face == GridFace.RIGHT) ? GridFace.RIGHT : null;
            double angle = getAngle(isStartZSmaller, t0, t1, t2, t3, filter);
            drawAxisLabel(center, labelPos, angle, label1);
        }

        // second grid axis
        offsetTickStart = min1 - range1;
        offsetTickEnd = max1 + range1;
        isStartZSmaller = drawGrid(range2, range1, min2, max2, min1, max1, cnst, offsetTickStart, offsetTickEnd, indexMap2, hasDecoration2);
        if (hasDecoration2 && chart.showAxisLabels()) {
            double[] center = getTransformedVector(indexMap2, range2/2 + min2, isStartZSmaller ? min1 : max1, cnst);
            double[] labelPos = getTransformedVector(indexMap2, range2/2 + min2, isStartZSmaller ? offsetTickStart : offsetTickEnd, cnst);
            GridFace filter = (face == GridFace.FRONT || face == GridFace.BACK) ? GridFace.BACK : null;
            double angle = getAngle(isStartZSmaller, t3, t0, t1, t2, filter);
            if ((face == GridFace.BOTTOM || face == GridFace.TOP) && is2D) {      // 2d hack
                angle += 180;
            }
            drawAxisLabel(center, labelPos, angle, label2);
        }

        // axis aka border of grid
        context.setLineWidth(axisStrokeWidth);
        context.setStroke(axisColor);
        context.strokeLine(t0[0], t0[1], t1[0], t1[1]);
        context.strokeLine(t1[0], t1[1], t2[0], t2[1]);
        context.strokeLine(t2[0], t2[1], t3[0], t3[1]);
        context.strokeLine(t3[0], t3[1], t0[0], t0[1]);

    }

    private void drawAxisLabel(double[] center, double[] labelPos, double angle, String label) {
        double offset = chart.showTickMarkLabels() ? AXIS_LABEL_OFFSET : AXIS_LABEL_OFFSET - TICK_LABEL_OFFSET;
        labelPos = getScaledVector(center, labelPos, offset);
        drawLabel(label, labelPos, angle);
    }

    private boolean drawGrid(double range, double rangeO, double min, double max, double minO, double maxO, double cnst, double offsetTickStart, double offsetTickEnd, int[] indexMap, boolean hasDecoration) {
        //double intervalStep = chart.getInterval(range, 1000);
        double intervalStep = getInterval(range, 500);

        double value = min - (min % intervalStep) - intervalStep;
        int tickCount = (int) (range / intervalStep) + 3;
        boolean isStartZSmaller = true;
        for (int i = 0; i < tickCount; i++) {
            if (i > 0) {
                value += intervalStep;
            }
            if (value < min || value > max) {
                continue;
            }

            double[] start = getTransformedVector(indexMap, value, minO, cnst);
            double[] end = getTransformedVector(indexMap, value, maxO, cnst);
            isStartZSmaller = ((face == GridFace.BOTTOM || face == GridFace.TOP) && is2D) ? start[3] <= end[3] : start[3] < end[3];

            // grid line
            if (chart.showGridContent()) {
                context.setStroke(gridLineColor);
                context.setLineWidth(gridStrokeWidth);
                context.strokeLine(start[0], start[1], end[0], end[1]);

                // zero line
                if (Double.parseDouble(String.format("%.2f", value)) == 0) {
                    context.setStroke(zeroLineColor);
                    context.setLineWidth(zeroLineStrokeWidth);
                    context.strokeLine(start[0], start[1], end[0], end[1]);
                }
            }


            if (hasDecoration) {
                double[] center = getTransformedVector(indexMap, value, isStartZSmaller ? minO : maxO, cnst);
                double[] tickLabel = getTransformedVector(indexMap, value, isStartZSmaller ? offsetTickStart : offsetTickEnd, cnst);
                tickLabel = getScaledVector(center, tickLabel, TICK_LABEL_OFFSET);

                double[] tickStart = isStartZSmaller ? getTransformedVector(indexMap, value, offsetTickStart, cnst) : end;
                double[] tickEnd = isStartZSmaller ? start : getTransformedVector(indexMap, value, offsetTickEnd, cnst);
                if (isStartZSmaller) {
                    tickStart = getScaledVector(center, tickStart, TICK_MARK_OFFSET);
                } else {
                    tickEnd = getScaledVector(center, tickEnd, TICK_MARK_OFFSET);
                }

                // tick mark
                if (chart.showTickMarks()) {
                    context.setLineWidth(tickStrokeWidth);
                    context.setStroke(tickMarkColor);
                    context.strokeLine(tickStart[0], tickStart[1], tickEnd[0], tickEnd[1]);
                }

                // tick label
                if (chart.showTickMarkLabels()) {
                    Font font = context.getFont();
                    context.setFont(new Font(null, 11));
                    context.setTextAlign(TextAlignment.CENTER);
                    context.setTextBaseline(VPos.CENTER);
                    context.setFill(tickLabelColor);
                    context.fillText(formatTickLabel(value), tickLabel[0], tickLabel[1]);
                    context.setTextBaseline(VPos.BASELINE);
                    context.setFont(font);
                }
            }
        }
        return isStartZSmaller;
    }

    private double[] getTransformedVector(int[] indexMap, double value, double range, double constant) {
        double[] result = new double[3];
        result[indexMap[0]] = value;
        result[indexMap[1]] = range;
        result[indexMap[2]] = constant;
        return chart.transform(result);
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
        Font font = context.getFont();
        context.setFont(new Font(null, 15));
        context.setTextAlign(TextAlignment.CENTER);
        context.setTextBaseline(VPos.CENTER);
        context.setFill(labelColor);
        context.transform(new Affine(new Rotate(-angle, position[0],position[1])));
        context.fillText(text, position[0], position[1]);
        context.transform(new Affine(new Rotate(angle, position[0],position[1])));
        context.setFont(font);
        context.setTextBaseline(VPos.BASELINE);
    }

    public double getZ() {
        return z;
    }
    protected String formatTickLabel(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)){
            return "" + value;
        }

        double formatted = Double.parseDouble(df.format(value));
        if (formatted % 1 == 0) {
            return Integer.toString((int) formatted);
        }
        return Double.toString(formatted);
    }

    protected double getInterval(double range, double threshold) {
        if (range <= threshold) {
            return calculateIntervalSmall(range);
        }
        return calculateIntervalLarge(range);
    }

    private double calculateIntervalSmall(double range) {
        double x = Math.pow(10.0, Math.floor(Math.log10(range)));
        if (range / x >= 5) {
            return x;
        } else if (range / (x / 2.0) >= 5) {
            return x / 2.0;
        }
        return x / 5.0;
    }

    private double calculateIntervalLarge(double range) {
        double x = Math.pow(10.0, Math.floor(Math.log10(range)));
        if (range / (x / 2.0) >= 10) {
            return x / 2.0;
        } else if (range / (x / 5.0) >= 10) {
            return x / 5.0;
        }
        return x / 10.0;
    }
}
