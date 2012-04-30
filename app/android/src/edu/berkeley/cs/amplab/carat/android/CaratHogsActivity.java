package edu.berkeley.cs.amplab.carat.android;

import java.util.Iterator;

import edu.berkeley.cs.amplab.carat.android.suggestions.HogsAdapter;
import edu.berkeley.cs.amplab.carat.android.ui.DrawView;
import edu.berkeley.cs.amplab.carat.android.ui.FlipperBackListener;
import edu.berkeley.cs.amplab.carat.android.ui.SwipeListener;
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
                CaratApplication app = (CaratApplication) getApplication();
                String label = app.labelForApp(fullObject.getAppName());
                w.setHogsBugs(fullObject, label, false);
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
}
