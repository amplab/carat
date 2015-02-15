package edu.berkeley.cs.amplab.carat.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.R;

/**
 * View to Draw pie.
 */
public class BatteryLayoutView extends LinearLayout {

    private static final String TAG = "BatteryLayout";

    private Bitmap fullPie = null;

    public BatteryLayoutView(Context context) {
        super(context);
    }

    public BatteryLayoutView(Context context, AttributeSet ar) {
        super(context, ar);
    }

    private void drawBattery() {
        if (fullPie != null)
            return;
        fullPie = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(fullPie);

        Paint p = new Paint();
        p.setColor(bgColor);

        // 30% battery tip
        if (w > h) {
            float size = h * 0.3f;
            float from = (h - size) / 2;
            float to = h - from;
            c.drawRect(0, 0, w - 20, h, p);
            c.drawRect(0, from, w, to, p);
        } else {
            float size = w * 0.3f;
            float from = (w - size) / 2;
            float to = w - from;
            c.drawRect(0, 20, w, h, p);
            c.drawRect(from, 0, to, h, p);
        }
    }

    private int bgColor = 0xFF808080;

    private int w = 0;
    private int h = 0;

    private Paint p;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0)
            return;

        this.w = w;
        this.h = h;

        p = new Paint();
        p.setAntiAlias(true);
        p.setColor(bgColor);
        p.setTextSize(100);
        p.setStyle(Style.FILL);
        drawBattery();
        this.setBackground(new BitmapDrawable(fullPie));
    }
}
