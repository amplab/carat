package edu.berkeley.cs.amplab.carat.android.fragments;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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

/**
 * 
 * @author Javad Sadeqzadeh
 *
 */
public class SummaryFragment extends Fragment {
	private final String TAG = "SummaryFragment";
	
	int wellbehavedAppCount = 0, lastWellbehavedAppCount = 0,
		hogCount = 0, lastHogCount = 0,
		bugCount = 0, lastBugCount = 0;
	
	private MainActivity mMainActivity = CaratApplication.getMainActivity();
	
	private SharedPreferences mSharedPref;
	private Resources mResources;
	
	private PieChart mChart;
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView() started");
		mResources = getResources();
		
		final View inflatedView;
        
		int hogsCount = CaratApplication.storage.getHogReport().length;
		int bugsCount = CaratApplication.storage.getBugReport().length;
		
		Log.i(TAG, "isStatsDataAvailable()=" + mMainActivity.isStatsDataAvailable());
		
		if (mMainActivity.isStatsDataAvailable()) {
			inflatedView = inflater.inflate(R.layout.summary, container, false);
			drawPieChart(inflatedView);
			setClickableUserStatsText(inflatedView, hogsCount, bugsCount);
		} else {
			inflatedView = inflater.inflate(R.layout.summary_unavailable, container, false);
		}
		
        // onCreateView() method should always return the inflated view
        return inflatedView;
    }

	private void setClickableUserStatsText(final View inflatedView, int hogsCount, int bugsCount) {
		TextView hogsCountTv = (TextView) inflatedView.findViewById(R.id.summary_hogs_count);
		hogsCountTv.setText(hogsCount + " hogs");
		hogsCountTv.setTextColor(Constants.CARAT_COLORS[1]);
		
		handleHogsCountClick(hogsCountTv);
		
		TextView bugsCountTv = (TextView) inflatedView.findViewById(R.id.summary_bugs_count);
		bugsCountTv.setText(bugsCount + " bugs");
		bugsCountTv.setTextColor(Constants.CARAT_COLORS[2]);
		
		handleBugsCountClick(bugsCountTv);
	}

	private void handleHogsCountClick(View root) {
		root.findViewById(R.id.summary_hogs_count).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMainActivity.replaceFragment(mMainActivity.getHogsFragment(), getString(R.string.tab_hogs));;
			}
		});
	}
	
	private void handleBugsCountClick(View root) {
		root.findViewById(R.id.summary_bugs_count).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMainActivity.replaceFragment(mMainActivity.getBugsFragment(), getString(R.string.tab_bugs));;
			}
		});
	}
	
	private void drawPieChart(final View inflatedView) {
		mChart = (PieChart) inflatedView.findViewById(R.id.chart1);
		mChart.setDescription("");
		
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");
		
		mChart.setValueTypeface(tf);
		mChart.setCenterTextTypeface(Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf"));
		mChart.setUsePercentValues(true);
		mChart.setCenterText("Android\nApps");
		mChart.setCenterTextSize(22f);
		 
		// radius of the center hole in percent of maximum radius
		mChart.setHoleRadius(40f); 
		mChart.setTransparentCircleRadius(50f);
		
		// enable / disable drawing of x- and y-values
	    //    mChart.setDrawYValues(false);
	    //    mChart.setDrawXValues(false);
		
		mChart.setData(generatePieData());
		
		Legend l = mChart.getLegend();
		l.setPosition(LegendPosition.RIGHT_OF_CHART);
	}

	protected PieData generatePieData() {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();
        
        xVals.add(mResources.getString(R.string.chart_wellbehaved));
        xVals.add(mResources.getString(R.string.chart_hogs));
        xVals.add(mResources.getString(R.string.chart_bugs));
        
        int wellbehaved = mMainActivity.mWellbehaved;
		int hogs = mMainActivity.mHogs;
		int bugs = mMainActivity.mBugs;
        
		entries.add(new Entry((float) (wellbehaved), 1));
		entries.add(new Entry((float) (hogs), 2));
		entries.add(new Entry((float) (bugs), 3));
		
        PieDataSet ds1 = new PieDataSet(entries, "Users Statistics");
        ds1.setColors(Constants.CARAT_COLORS);
        ds1.setSliceSpace(2f);
        
        PieData d = new PieData(xVals, ds1);
        return d;
    }
	
}
