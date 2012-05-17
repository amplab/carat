package edu.berkeley.cs.amplab.carat.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.storage.CaratDataStorage;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.thrift.DetailScreenReport;

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

    private double[] xVals = null;
    private double[] yVals = null;
    private double[] xValsWithout = null;
    private double[] yValsWithout = null;

    private String appName = null;

    public double[] getXVals() {
        return this.xVals;
    }

    public double[] getYVals() {
        return this.yVals;
    }

    public double[] getXValsWithout() {
        return this.xValsWithout;
    }

    public double[] getYValsWithout() {
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

    public void setHogsBugs(SimpleHogBug bugOrHog, String appName, boolean isBug) {
        this.xVals = bugOrHog.getxVals();
        this.yVals = bugOrHog.getyVals();
        this.xValsWithout = bugOrHog.getxValsWithout();
        this.yValsWithout = bugOrHog.getyValsWithout();

        this.type = isBug ? Type.BUG : Type.HOG;
        this.appName = appName;
    }

    public void setParams(Type type, String appName, double[] xVals,
            double[] yVals, double[] xValsWithout,
            double[] yValsWithout) {
        this.xVals = xVals;
        this.yVals = yVals;
        this.xValsWithout = xValsWithout;
        this.yValsWithout = yValsWithout;

        this.type = type;
        this.appName = appName;
    }

    public void setOsOrModel(DetailScreenReport osOrModel,
            DetailScreenReport osOrModelWithout, String name, boolean isOs) {
        this.xVals = CaratDataStorage.convert(osOrModel.getXVals());
        this.yVals = CaratDataStorage.convert(osOrModel.getYVals());
        this.xValsWithout = CaratDataStorage.convert(osOrModelWithout.getXVals());
        this.yValsWithout = CaratDataStorage.convert(osOrModelWithout.getYVals());
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
            if (anShort == null)
                anShort = "Unknown";
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
                withoutString = "On other models";
                break;
            case OS:
                withString = "With " + anShort;
                withoutString = "With other OSes";
                break;
            case SIMILAR:
                withString = "With similar apps";
                withoutString = "With different apps";
                break;
            default:

            }

            canvas.drawText(withString, stopX, startY + 30, withTextPaint);
            canvas.drawText(withoutString, stopX, startY + 60, withoutTextPaint);

            float xmax = 0.0f;
            float ymax = 0.0f;
            for (int i = 0; i < xVals.length && i < yVals.length; ++i) {
                float next = (float) xVals[i];
                float nexty = (float) yVals[i];
                if (next > xmax)
                    xmax = next;
                if (nexty > ymax)
                    ymax = nexty;
            }
            for (int i = 0; i < xValsWithout.length && i < yValsWithout.length; ++i) {
                float next = (float)  xValsWithout[i];
                float nexty = (float)  yValsWithout[i];
                if (next > xmax)
                    xmax = next;
                if (nexty > ymax)
                    ymax = nexty;
            }

            String xmaxS = xmax + "";
            if (xmaxS.length() > 6)
                xmaxS = xmaxS.substring(0, 6);

            String ymaxS = ymax + "";
            if (ymaxS.length() > 4)
                ymaxS = ymaxS.substring(0, 4);

            canvas.drawText(xmaxS + "", xmaxX, stopY - 20, textPaint);
            canvas.drawText(ymaxS + "", startX, startY + 30, textPaint);

            
            float lastX = 0.0f, lastY = 0.0f; 
            for (int i = 0; i < xVals.length && i < yVals.length; ++i){
                float x = (float) xVals[i];
                float y = (float) yVals[i];
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
            for (int i = 0; i < xValsWithout.length && i < yValsWithout.length; ++i){
                float x = (float) xValsWithout[i];
                float y = (float) yValsWithout[i];
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
