package edu.berkeley.cs.amplab.carat.android;

import java.util.List;

import edu.berkeley.cs.amplab.carat.android.lists.BugsAdapter;
import edu.berkeley.cs.amplab.carat.android.lists.HogsAdapter;
import edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity;
import edu.berkeley.cs.amplab.carat.android.ui.DrawView;
import edu.berkeley.cs.amplab.carat.android.ui.FlipperBackListener;
import edu.berkeley.cs.amplab.carat.android.ui.SwipeListener;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

public class CaratHogsActivity extends BaseVFActivity {

    private DrawView w = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hogs);

        vf = (ViewFlipper) findViewById(R.id.hogsFlipper);
        View baseView = findViewById(R.id.hogsList);
        baseView.setOnTouchListener(SwipeListener.instance);
        vf.setOnTouchListener(SwipeListener.instance);
        baseViewIndex = vf.indexOfChild(baseView);
        initHogsView();
        //initGraphView();
        initGraphChart();
        
        Object o = getLastNonConfigurationInstance();
        if (o != null){
            CaratHogsActivity previous = (CaratHogsActivity) o;
            List<Double> xVals = previous.w.getXVals();
            List<Double> yVals = previous.w.getYVals();
            List<Double> xValsWithout = previous.w.getXValsWithout();
            List<Double> yValsWithout = previous.w.getYValsWithout();
            String appName = previous.w.getAppName();
            w.setParams(DrawView.Type.HOG, appName, xVals, yVals, xValsWithout, yValsWithout);
            w.postInvalidate();
        }
        
        if (viewIndex == 0)
            vf.setDisplayedChild(baseViewIndex);
        else
            vf.setDisplayedChild(viewIndex);
    }

    private void initHogsView() {
        final ListView lv = (ListView) findViewById(R.id.hogsList);
        lv.setCacheColorHint(0);
    }

    /*
    private void initGraphView() {
        WebView webview = (WebView) findViewById(R.id.hogsGraphView);
        // Fixes the white flash when showing the page for the first time.
        webview.setBackgroundColor(0);
        webview.getSettings().setJavaScriptEnabled(true);
        // FIXME: Chart is not dynamic
        webview.loadUrl("file:///android_asset/twolinechart.html");
        webview.setOnTouchListener(new FlipperBackListener(vf, vf
                .indexOfChild(findViewById(R.id.hogsList))));
    }*/

    private void initGraphChart() {
        w = new DrawView(getApplicationContext());
        vf.addView(w);
        w.setOnTouchListener(new FlipperBackListener(this, vf, vf
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
                switchView(target);
                /*
                Toast.makeText(CaratHogsActivity.this,
                        "You have chosen: " + " " + fullObject.getAppName(),
                        Toast.LENGTH_SHORT).show();*/
            }
        });
    }
    
    
    public void refresh(){
        CaratApplication app = (CaratApplication) getApplication();
        final ListView lv = (ListView) findViewById(R.id.hogsList);
        lv.setAdapter(new HogsAdapter(app, app.s.getHogReport()));
    }

    /**
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        CaratApplication.setHogs(this);
        refresh();
        super.onResume();
    }
}
