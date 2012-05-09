package edu.berkeley.cs.amplab.carat.android.ui;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.thrift.DetailScreenReport;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

public class DrawView extends View {
    Paint textPaint = new Paint();
    Paint axisPaint = new Paint();
    Paint withPaint = new Paint();
    Paint withTextPaint = new Paint();
    Paint withoutPaint = new Paint();
    Paint withoutTextPaint = new Paint();
    private static final float strokeWidth = 5.0f;
    private static final float MARGIN_X_AXIS = 50;
    private static final float MARGIN_Y_AXIS = 50;
    private static final float Y_LINE_MARGIN = -10;
    private static final float X_LINE_MARGIN = 10;
    private static final int TEXT_SIZE = 24;

    public enum Type {
        OS, MODEL, HOG, BUG, SIMILAR, JSCORE
    }

    private Type type = null;

    private List<Double> xVals = null;
    private List<Double> yVals = null;
    private List<Double> xValsWithout = null;
    private List<Double> yValsWithout = null;

    private String appName = null;

    public List<Double> getXVals() {
        return this.xVals;
    }

    public List<Double> getYVals() {
        return this.yVals;
    }

    public List<Double> getXValsWithout() {
        return this.xValsWithout;
    }

    public List<Double> getYValsWithout() {
        return this.yValsWithout;
    }

    public String getAppName() {
        return this.appName;
    }

    public DrawView(Context context) {
        super(context);
        axisPaint.setDither(true);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeJoin(Paint.Join.ROUND);
        axisPaint.setStrokeCap(Paint.Cap.ROUND);
        axisPaint.setColor(getContext().getResources().getColor(R.color.text));
        axisPaint.setStrokeWidth(strokeWidth);

        withPaint.setDither(true);
        withPaint.setStyle(Paint.Style.STROKE);
        withPaint.setStrokeJoin(Paint.Join.ROUND);
        withPaint.setStrokeCap(Paint.Cap.ROUND);
        withPaint.setColor(getContext().getResources().getColor(R.color.with));
        withPaint.setStrokeWidth(strokeWidth);

        withTextPaint.setColor(getContext().getResources().getColor(
                R.color.with));
        withTextPaint.setTextSize(TEXT_SIZE);
        withTextPaint.setTextAlign(Align.RIGHT);

        withoutPaint.setDither(true);
        withoutPaint.setStyle(Paint.Style.STROKE);
        withoutPaint.setStrokeJoin(Paint.Join.ROUND);
        withoutPaint.setStrokeCap(Paint.Cap.ROUND);
        withoutPaint.setColor(getContext().getResources().getColor(
                R.color.without));
        withoutPaint.setStrokeWidth(strokeWidth);

        withoutTextPaint.setColor(getContext().getResources().getColor(
                R.color.without));
        withoutTextPaint.setTextSize(TEXT_SIZE);
        withoutTextPaint.setTextAlign(Align.RIGHT);

        textPaint.setColor(getContext().getResources().getColor(R.color.text));
        textPaint.setTextSize(TEXT_SIZE);
    }

    public Type getType() {
        return this.type;
    }

    public void setHogsBugs(HogsBugs bugOrHog, String appName, boolean isBug) {
        this.xVals = bugOrHog.getXVals();
        this.yVals = bugOrHog.getYVals();
        this.xValsWithout = bugOrHog.getXValsWithout();
        this.yValsWithout = bugOrHog.getYValsWithout();

        this.type = isBug ? Type.BUG : Type.HOG;
        this.appName = appName;
    }

    public void setParams(Type type, String appName, List<Double> xVals,
            List<Double> yVals, List<Double> xValsWithout,
            List<Double> yValsWithout) {
        this.xVals = xVals;
        this.yVals = yVals;
        this.xValsWithout = xValsWithout;
        this.yValsWithout = yValsWithout;

        this.type = type;
        this.appName = appName;
    }

    public void setOsOrModel(DetailScreenReport osOrModel,
            DetailScreenReport osOrModelWithout, String name, boolean isOs) {
        this.xVals = osOrModel.getXVals();
        this.yVals = osOrModel.getYVals();
        this.xValsWithout = osOrModelWithout.getXVals();
        this.yValsWithout = osOrModelWithout.getYVals();
        this.type = isOs ? Type.OS : Type.MODEL;
        this.appName = name;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        Rect clip = canvas.getClipBounds();
        int cw = clip.width();
        int ch = clip.height();
        if (cw > 0 && ch > 0) {
            w = cw;
            h = ch;
        }
        int inset = 20;
        float startX = 0 + inset;
        float startY = 0 + inset;
        float stopX = w - inset;
        float stopY = h - inset;

        float origoX = startX + MARGIN_X_AXIS;
        float origoY = stopY - MARGIN_Y_AXIS;

        float xmaxX = stopX - origoX - X_LINE_MARGIN;
        float maxProb = -origoY - Y_LINE_MARGIN + startY;

        // X and Y axisbug
        canvas.drawLine(origoX, origoY, origoX, startY, axisPaint);
        canvas.drawLine(origoX, origoY, stopX, origoY, axisPaint);
        // canvas.drawLine(startX, startY, stopX, stopY, axisPaint);

        canvas.drawText("Battery drain %/s", origoX, stopY - 20, textPaint);

        Path path = new Path();
        path.addRect(new RectF(clip), Path.Direction.CW);
        canvas.drawTextOnPath("Probability", path, w * 2 + h + h / 2, +40,
                textPaint);

        if (xVals == null || yVals == null) {
            canvas.drawText("With", stopX, startY + 30, withTextPaint);
            canvas.drawText("Without", stopX, startY + 60, withoutTextPaint);
        } else {
            String anShort = appName;
            int dot = anShort.lastIndexOf('.');
            if (dot > 0) {
                int lastPart = anShort.length() - dot;
                if (lastPart > 7)
                    anShort = anShort.substring(dot + 1);
                else {
                    if (anShort.length() > 15 + 3) {
                        anShort = "..."
                                + anShort.substring(anShort.length() - 15);
                    }
                }
            }

            String withString = anShort;
            String withoutString = anShort;

            switch (type) {
            case BUG:
                withString = anShort + " running here";
                withoutString = anShort + " running elsewhere";
                break;
            case HOG:
                withString = anShort + " running";
                withoutString = anShort + " not running";
                break;
            case MODEL:
                withString = "On " + anShort;
                withoutString = "On another model";
                break;
            case OS:
                withString = "With " + anShort;
                withoutString = "With another OS";
                break;
            case SIMILAR:
                withString = "With similar apps";
                withoutString = "With different apps";
                break;
            default:

            }

            canvas.drawText(withString, stopX, startY + 30, withTextPaint);
            canvas.drawText(withoutString, stopX, startY + 60, withoutTextPaint);

            Iterator<Double> xes = xVals.iterator();
            Iterator<Double> ys = yVals.iterator();

            Iterator<Double> xesWo = xValsWithout.iterator();
            Iterator<Double> ysWo = yValsWithout.iterator();

            float xmax = 0.0f;
            while (xes.hasNext()) {
                float next = (float) xes.next().doubleValue();
                if (next > xmax)
                    xmax = next;
            }
            while (xesWo.hasNext()) {
                float next = (float) xesWo.next().doubleValue();
                if (next > xmax)
                    xmax = next;
            }

            float ymax = 0.0f;
            while (ys.hasNext()) {
                float next = (float) ys.next().doubleValue();
                if (next > ymax)
                    ymax = next;
            }
            while (ysWo.hasNext()) {
                float next = (float) ysWo.next().doubleValue();
                if (next > ymax)
                    ymax = next;
            }

            String xmaxS = xmax + "";
            if (xmaxS.length() > 6)
                xmaxS = xmaxS.substring(0, 6);

            String ymaxS = ymax + "";
            if (ymaxS.length() > 4)
                ymaxS = ymaxS.substring(0, 4);

            canvas.drawText(xmaxS + "", xmaxX, stopY - 20, textPaint);
            canvas.drawText(ymaxS + "", startX, startY + 30, textPaint);

            xes = xVals.iterator();
            xesWo = xValsWithout.iterator();
            ys = yVals.iterator();
            ysWo = yValsWithout.iterator();

            float lastX = 0.0f, lastY = 0.0f;
            while (xes.hasNext() && ys.hasNext()) {
                float x = (float) xes.next().doubleValue();
                float y = (float) ys.next().doubleValue();
                if (y == 0.0)
                    continue;
                x /= xmax;
                // Now x is a fraction of max. All x values are from origoX to w
                // - origoX - offset:
                x = origoX + X_LINE_MARGIN + x * xmaxX;

                y /= ymax;
                y = origoY + Y_LINE_MARGIN + y * maxProb;

                if (lastX != 0 || lastY != 0) {
                    canvas.drawLine(lastX, lastY, x, y, withPaint);
                }
                lastX = x;
                lastY = y;
            }

            lastX = 0.0f;
            lastY = 0.0f;
            while (xesWo.hasNext() && ysWo.hasNext()) {
                float x = (float) xesWo.next().doubleValue();
                float y = (float) ysWo.next().doubleValue();
                if (y == 0.0)
                    continue;
                x /= xmax;
                x = origoX + X_LINE_MARGIN + x * xmaxX;

                y /= ymax;
                y = origoY + Y_LINE_MARGIN + y * maxProb;

                if (lastX != 0 || lastY != 0) {
                    canvas.drawLine(lastX, lastY, x, y, withoutPaint);
                }
                lastX = x;
                lastY = y;
            }
        }
    }

}
