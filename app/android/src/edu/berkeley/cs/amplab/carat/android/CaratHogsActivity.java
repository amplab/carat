package edu.berkeley.cs.amplab.carat.android;

import java.util.Iterator;

import edu.berkeley.cs.amplab.carat.android.suggestions.HogsAdapter;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

public class CaratHogsActivity extends Activity {

    private ViewFlipper vf = null;
    private int baseViewIndex = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hogs);

        vf = (ViewFlipper) findViewById(R.id.hogsFlipper);
        View baseView = findViewById(R.id.hogsList);
        baseView.setOnTouchListener(SwipeListener.instance);
        vf.setOnTouchListener(SwipeListener.instance);
        baseViewIndex = vf.indexOfChild(baseView);
        initHogsView();
        initGraphView();
        initGraphChart();
    }

    private void initHogsView() {
        final ListView lv = (ListView) findViewById(R.id.hogsList);
        lv.setCacheColorHint(0);
    }

    private void initGraphView() {
        WebView webview = (WebView) findViewById(R.id.hogsGraphView);
        // Fixes the white flash when showing the page for the first time.
        webview.setBackgroundColor(0);
        webview.getSettings().setJavaScriptEnabled(true);
        // FIXME: Chart is not dynamic
        webview.loadUrl("file:///android_asset/twolinechart.html");
        webview.setOnTouchListener(new FlipperBackListener(vf, vf
                .indexOfChild(findViewById(R.id.hogsList))));
    }

    private void initGraphChart() {
        final DrawView w = new DrawView(getApplicationContext());
        vf.addView(w);
        w.setOnTouchListener(new FlipperBackListener(vf, vf
                .indexOfChild(findViewById(R.id.hogsList))));
        
        final ListView lv = (ListView) findViewById(R.id.hogsList);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                    long id) {
                Object o = lv.getItemAtPosition(position);
                HogsBugs fullObject = (HogsBugs) o;
                // View target = findViewById(R.id.hogsGraphView);
                View target = w;
                w.setHogsBugs(fullObject);
                w.postInvalidate();
                vf.setOutAnimation(CaratMainActivity.outtoLeft);
                vf.setInAnimation(CaratMainActivity.inFromRight);
                vf.setDisplayedChild(vf.indexOfChild(target));

                Toast.makeText(CaratHogsActivity.this,
                        "You have chosen: " + " " + fullObject.getAppName(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        CaratApplication app = (CaratApplication) getApplication();
        final ListView lv = (ListView) findViewById(R.id.hogsList);
        lv.setAdapter(new HogsAdapter(app, app.s.getHogReport()));
        // initGraphChart();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (vf.getDisplayedChild() != baseViewIndex) {
            vf.setOutAnimation(CaratMainActivity.outtoRight);
            vf.setInAnimation(CaratMainActivity.inFromLeft);
            vf.setDisplayedChild(baseViewIndex);
        } else
            finish();
    }

    class DrawView extends View {
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

        private HogsBugs thing = null;

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
            
            withTextPaint.setColor(getContext().getResources().getColor(R.color.with));
            withTextPaint.setTextSize(32);

            withoutPaint.setDither(true);
            withoutPaint.setStyle(Paint.Style.STROKE);
            withoutPaint.setStrokeJoin(Paint.Join.ROUND);
            withoutPaint.setStrokeCap(Paint.Cap.ROUND);
            withoutPaint.setColor(getContext().getResources().getColor(R.color.without));
            withoutPaint.setStrokeWidth(strokeWidth);
            
            withoutTextPaint.setColor(getContext().getResources().getColor(R.color.without));
            withoutTextPaint.setTextSize(32);
            
            textPaint.setColor(getContext().getResources().getColor(R.color.text));
            textPaint.setTextSize(32);
        }

        public void setHogsBugs(HogsBugs bugOrHog) {
            this.thing = bugOrHog;
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
            
            float xmaxX = stopX - origoX-X_LINE_MARGIN;
            float maxProb = - origoY - Y_LINE_MARGIN +startY;
            
            // X and Y axis
            canvas.drawLine(origoX, origoY, origoX, startY, axisPaint);
            canvas.drawLine(origoX, origoY, stopX, origoY, axisPaint);
            // canvas.drawLine(startX, startY, stopX, stopY, axisPaint);

            canvas.drawText("Battery drain %/s", (stopX - MARGIN_X_AXIS) / 2,
                    stopY - 20, textPaint);

            Path path = new Path();
            path.addRect(new RectF(clip), Path.Direction.CW);
            canvas.drawTextOnPath("Probability", path, w * 2 + h + h / 2, +40,
                    textPaint);
            
            canvas.drawText("With", stopX*0.75f-X_LINE_MARGIN, startY+Y_LINE_MARGIN+50, withTextPaint);
            canvas.drawText("Without", stopX*0.75f-X_LINE_MARGIN, startY+Y_LINE_MARGIN+100, withoutTextPaint);
            
            if (thing != null) {
                Iterator<Double> xes = thing.getXValsIterator();
                Iterator<Double> ys = thing.getYValsIterator();

                Iterator<Double> xesWo = thing.getXValsWithoutIterator();
                Iterator<Double> ysWo = thing.getYValsWithoutIterator();

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
                
                canvas.drawText(xmax+"", xmaxX-50, stopY-20, textPaint);
                canvas.drawText(ymax+"", startX+20, startY+50, textPaint);

                xes = thing.getXValsIterator();
                xesWo = thing.getXValsWithoutIterator();
                ys = thing.getYValsIterator();
                ysWo = thing.getYValsWithoutIterator();
                
                float lastX = 0.0f, lastY = 0.0f;
                while (xes.hasNext() && ys.hasNext()){
                    // FIXME: KLUDGE
                    float x = (float) xes.next().doubleValue();
                    x/= xmax;
                    // Now x is a fraction of max. All x values are from origoX to w - origoX - offset:
                    x = origoX + X_LINE_MARGIN + x * xmaxX;
                    
                    float y = (float) ys.next().doubleValue();
                    y/=ymax;
                    y = origoY + Y_LINE_MARGIN + y * maxProb;
                    
                    if (lastX != 0 || lastY != 0){
                        canvas.drawLine(lastX, lastY, x, y, withPaint);
                    }
                    lastX = x;
                    lastY = y;
                }
                
                lastX = 0.0f;
                lastY = 0.0f;
                while (xesWo.hasNext() && ysWo.hasNext()){
                    float x = (float) xesWo.next().doubleValue();
                    x/= xmax;
                    x = origoX + X_LINE_MARGIN + x * xmaxX;
                    
                    float y = (float) ysWo.next().doubleValue();
                    y/=ymax;
                    y = origoY + Y_LINE_MARGIN + y * maxProb;
                    
                    if (lastX != 0 || lastY != 0){
                        canvas.drawLine(lastX, lastY, x, y, withoutPaint);
                    }
                    lastX = x;
                    lastY = y;
                }
            }
        }

    }

}
