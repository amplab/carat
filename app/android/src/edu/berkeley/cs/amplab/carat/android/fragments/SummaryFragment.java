package edu.berkeley.cs.amplab.carat.android.fragments;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.Legend.LegendPosition;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.Constants;
import edu.berkeley.cs.amplab.carat.android.MainActivity;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;

/**
 * 
 * @author Javad Sadeqzadeh
 *
 */
public class SummaryFragment extends ExtendedTitleFragment {
    // private final String TAG = "SummaryFragment";
    private MainActivity mMainActivity = CaratApplication.getMainActivity();
    private PieChart mChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Handling orientation change manually (to prevent destroying and
         * recreating the activity (by Android)). We have threads and AsyncTasks
         * (e.g. for retrieving statistics from server) which try to change the
         * activity's view, and if the activity is already killed, they might
         * cause a application crash (or simply continue their work). We might
         * need to use the RoboSpice library (instead of AysncTasks) in future)
         * http://www.youtube.com/watch?v=ONaD1mB8r-A
         */
        FrameLayout frameLayout = new FrameLayout(getActivity());
        populateViewForOrientation(inflater, frameLayout);
        return frameLayout;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View subview = inflater.inflate(R.layout.summary, viewGroup);

        scheduleRefresh(subview);
        setClickableUserStatsText(subview);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("SummaryFragment", "onResume called");
        scheduleRefresh();
    }

    public void scheduleRefresh() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                View v = getView();

                if (mMainActivity.isStatsDataAvailable() && v != null) {
                    drawPieChart(v);
                }

                int hogsCount = 0;
                int bugsCount = 0;
                if (CaratApplication.storage != null && v != null) {
                    SimpleHogBug[] h = CaratApplication.storage.getHogReport();
                    SimpleHogBug[] b = CaratApplication.storage.getBugReport();
                    if (h != null)
                        hogsCount = h.length;
                    if (b != null)
                        bugsCount = b.length;
                    Button hogsCountTv = (Button) v.findViewById(R.id.summary_hogs_count);
                    hogsCountTv.setText(hogsCount + " " + getString(R.string.hogs));

                    Button bugsCountTv = (Button) v.findViewById(R.id.summary_bugs_count);
                    bugsCountTv.setText(bugsCount + " " + getString(R.string.bugs));
                    String batteryLife = CaratApplication.myDeviceData.getBatteryLife();
                    Button green = (Button) v.findViewById(R.id.active_bl);
                    green.setText(batteryLife);
                }
            }
        });

    }

    public void scheduleRefresh(final View inflatedView) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                if (mMainActivity.isStatsDataAvailable()) {
                    drawPieChart(inflatedView);
                }

                int hogsCount = 0;
                int bugsCount = 0;
                if (CaratApplication.storage != null) {
                    SimpleHogBug[] h = CaratApplication.storage.getHogReport();
                    SimpleHogBug[] b = CaratApplication.storage.getBugReport();
                    if (h != null)
                        hogsCount = h.length;
                    if (b != null)
                        bugsCount = b.length;
                }
                Button hogsCountTv = (Button) inflatedView.findViewById(R.id.summary_hogs_count);
                hogsCountTv.setText(hogsCount + " " + getString(R.string.hogs));

                Button bugsCountTv = (Button) inflatedView.findViewById(R.id.summary_bugs_count);
                bugsCountTv.setText(bugsCount + " " + getString(R.string.bugs));
            }
        });

    }

    private void setClickableUserStatsText(final View inflatedView) {
        CountClickListener l = new CountClickListener();

        Button hogsCount = (Button) inflatedView.findViewById(R.id.summary_hogs_count);
        hogsCount.setOnClickListener(l);

        Button bugsCount = (Button) inflatedView.findViewById(R.id.summary_bugs_count);
        bugsCount.setOnClickListener(l);
        
        Button green = (Button) inflatedView.findViewById(R.id.active_bl);
        green.setOnClickListener(l);
        

        /* Open Carat Statistics website on click: */
        TextView morestats = (TextView) inflatedView.findViewById(R.id.morestats);
        morestats.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.statsurl)));
                startActivity(browserIntent);
            }
        });
    }

    /**
     * Concisely handle clicks on the hogs/bugs text items.
     * 
     * @author Eemil Lagerspetz
     *
     */
    private class CountClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == v.getRootView().findViewById(R.id.summary_hogs_count)) {
                mMainActivity.replaceFragment(mMainActivity.getHogsFragment(), mMainActivity.getFragmentTag(4), true);
            } else if (v == v.getRootView().findViewById(R.id.active_bl)) {
                mMainActivity.replaceFragment(mMainActivity.getMydeviceFragment(), mMainActivity.getFragmentTag(2), true);
            }else
                mMainActivity.replaceFragment(mMainActivity.getBugsFragment(), mMainActivity.getFragmentTag(3), true);
        }
    }

    private void drawPieChart(final View inflatedView) {
        // This fixes a crash I got 2015-02-11:
        View v = inflatedView;
        if (v == null)
            v = getView();
        if (v == null)
            return;
        mChart = (PieChart) inflatedView.findViewById(R.id.chart1);
        mChart.setDescription("");

        // int orientation = getResources().getConfiguration().orientation;
        // switch (orientation) {
        // case (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE):
        // mChart.setValueTextSize(9);
        // break;
        // case (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT):
        // mChart.setValueTextSize(15);
        // break;
        // }

        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-Regular.ttf");

        mChart.setValueTypeface(tf);
        mChart.setCenterTextTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-Light.ttf"));
        mChart.setUsePercentValues(true);
        mChart.setCenterText(getString(R.string.summary_chart_center_text));
        mChart.setCenterTextSize(22f);

        // radius of the center hole in percent of maximum radius
        mChart.setHoleRadius(40f);
        mChart.setTransparentCircleRadius(50f);

        // disable click / touch / tap on the chart
        mChart.setTouchEnabled(false);

        // enable / disable drawing of x- and y-values
        // mChart.setDrawYValues(false);
        // mChart.setDrawXValues(false);

        mChart.setData(generatePieData());
        Legend l = mChart.getLegend();
        l.setPosition(LegendPosition.NONE);
    }

    protected PieData generatePieData() {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        xVals.add(getString(R.string.chart_wellbehaved));
        xVals.add(getString(R.string.chart_hogs));
        xVals.add(getString(R.string.chart_bugs));

        int wellbehaved = mMainActivity.mWellbehaved;
        int hogs = mMainActivity.mHogs;
        int bugs = mMainActivity.mBugs;

        entries.add(new Entry((float) (wellbehaved), 1));
        entries.add(new Entry((float) (hogs), 2));
        entries.add(new Entry((float) (bugs), 3));

        PieDataSet ds1 = new PieDataSet(entries, getString(R.string.summary_chart_center_text));
        ds1.setColors(Constants.CARAT_COLORS);
        ds1.setSliceSpace(2f);

        PieData d = new PieData(xVals, ds1);
        return d;
    }

}
