package edu.berkeley.cs.amplab.carat.android;

import java.util.List;

import edu.berkeley.cs.amplab.carat.android.lists.BugsAdapter;
import edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity;
import edu.berkeley.cs.amplab.carat.android.ui.DrawView;
import edu.berkeley.cs.amplab.carat.android.ui.FlipperBackListener;
import edu.berkeley.cs.amplab.carat.android.ui.SwipeListener;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

public class CaratBugsActivity extends BaseVFActivity {

    private DrawView w = null;
    private View detailPage = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bugs);
        vf = (ViewFlipper) findViewById(R.id.bugsFlipper);
        View baseView = findViewById(R.id.bugsList);
        baseView.setOnTouchListener(SwipeListener.instance);
        vf.setOnTouchListener(SwipeListener.instance);
        baseViewIndex = vf.indexOfChild(baseView);
        // initBugsView();
        // initGraphView();
        initGraphChart();
        initDetailView();

        Object o = getLastNonConfigurationInstance();
        if (o != null) {
            CaratBugsActivity previous = (CaratBugsActivity) o;
            TextView pn = (TextView) previous.detailPage.findViewById(R.id.name);
            ImageView pi = (ImageView) previous.detailPage.findViewById(R.id.appIcon);
            ProgressBar pp = (ProgressBar) previous.detailPage.findViewById(R.id.confidenceBar);
            
            ((TextView) detailPage.findViewById(R.id.name)).setText(pn.getText());
            ((ImageView) detailPage.findViewById(R.id.appIcon))
                    .setImageDrawable(pi.getDrawable());
            ((ProgressBar) detailPage.findViewById(R.id.confidenceBar))
                    .setProgress(pp.getProgress());
            
            List<Double> xVals = previous.w.getXVals();
            List<Double> yVals = previous.w.getYVals();
            List<Double> xValsWithout = previous.w.getXValsWithout();
            List<Double> yValsWithout = previous.w.getYValsWithout();
            String appName = previous.w.getAppName();
            w.setParams(DrawView.Type.BUG, appName, xVals, yVals, xValsWithout,
                    yValsWithout);
            w.postInvalidate();
        }

        if (viewIndex == 0)
            vf.setDisplayedChild(baseViewIndex);
        else
            vf.setDisplayedChild(viewIndex);
    }

    private void initGraphChart() {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        detailPage = inflater.inflate(R.layout.graph, null);
        ViewGroup g = (ViewGroup) detailPage;
        w = new DrawView(getApplicationContext());
        g.addView(w);
        vf.addView(detailPage);

        View moreinfo = detailPage.findViewById(R.id.moreinfo);
        moreinfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                switchView(R.id.detailView);
            }
        });

        detailPage.setOnTouchListener(new FlipperBackListener(this, vf,
                baseViewIndex, true));

        final ListView lv = (ListView) findViewById(R.id.bugsList);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                    long id) {
                Object o = lv.getItemAtPosition(position);
                HogsBugs fullObject = (HogsBugs) o;
                // View target = findViewById(R.id.hogsGraphView);
                View target = detailPage;
                CaratApplication app = (CaratApplication) getApplication();
                String label = app.labelForApp(fullObject.getAppName());
                Drawable icon = app.iconForApp(fullObject.getAppName());
                ((TextView) detailPage.findViewById(R.id.name)).setText(label);
                ((ImageView) detailPage.findViewById(R.id.appIcon))
                        .setImageDrawable(icon);
                ((ProgressBar) detailPage.findViewById(R.id.confidenceBar))
                        .setProgress((int) (fullObject.getWDistance() * 100));
                w.setHogsBugs(fullObject, label, true);
                w.postInvalidate();
                switchView(target);
                /*
                 * Toast.makeText(CaratBugsActivity.this, "You have chosen: " +
                 * " " + fullObject.getAppName(), Toast.LENGTH_SHORT).show();
                 */
            }
        });
    }

    private void initDetailView() {
        WebView webview = (WebView) findViewById(R.id.detailView);
        // Fixes the white flash when showing the page for the first time.
        if (getString(R.string.blackBackground).equals("true"))
            webview.setBackgroundColor(0);

        webview.loadUrl("file:///android_asset/detailinfo.html");
        webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
                .indexOfChild(detailPage), false));
    }

    public void refresh() {
        CaratApplication app = (CaratApplication) getApplication();
        final ListView lv = (ListView) findViewById(R.id.bugsList);
        lv.setAdapter(new BugsAdapter(app, app.s.getBugReport()));
    }

    /**
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        CaratApplication.setBugs(this);
        refresh();
        super.onResume();
    }
}
