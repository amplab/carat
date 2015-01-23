package edu.berkeley.cs.amplab.carat.android.fragments;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieGraph.OnSliceClickedListener;
import com.echo.holographlibrary.PieSlice;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
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
	
	MainActivity mMainActivity = CaratApplication.getMainActivity();
	
	SharedPreferences mSharedPref;
	Resources mResources;
	
	private PieChart mChart;
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView() started");
//		mSharedPref = getActivity().getSharedPreferences(
//				Constants.MAIN_ACTIVITY_PREFERENCE_KEY, Context.MODE_PRIVATE);
		mResources = getResources();
//		Log.d(TAG, "initialized the shared preferences");
		
		final View inflatedView;
        
		if (mMainActivity.isStatsDataAvailable()) {
			inflatedView = inflater.inflate(R.layout.summary, container, false);
			drawPieChart(inflatedView);
			// wait a short while to read the statistics data from the shared preferences
//			new Handler().postDelayed(new Runnable(){
//			    public void run() {
//					drawPieChart(inflatedView);  // for some reason, this method doesn't work inside a handler
			    	//// handlePieGraphDrawing(inflatedView, mResources);  // this method works fine inside handler
//			    }
//			}, 600);
		} else {
			inflatedView = inflater.inflate(R.layout.summary_unavailable, container, false);
			// saveStatsToSharedPref();
		}
		
//			try {
//			// load older data (stored in the shared preferences structure)
//			// into the fields starting with the prefix "last"
//			loadPrefsToFields();
//			} catch (Exception e) {
//				Log.d(TAG, "unable to read the info from the shared preference. No such a key.");
//			}
		
        // onCreateView() method should always return the inflated view
        return inflatedView;
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
//	        mChart.setDrawYValues(false);
//	        mChart.setDrawXValues(false);
		
		mChart.setData(generatePieData());
		
		Legend l = mChart.getLegend();
		l.setPosition(LegendPosition.RIGHT_OF_CHART);
	}

	protected PieData generatePieData() {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();
        
        xVals.add("Wellbahved");
        xVals.add("Hogs");
        xVals.add("Bugs");
        
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
	
//	@Override
//	public void onResume() {
//		CaratApplication.getMainActivity().reAttachSummaryFragment = true;
//		
//		getActivity().setTitle(mResources.getString(R.string.tab_summary));
////		Log.d(TAG, "summary fragment resumed.");
//		Log.d(TAG, "I'm here");
//		// if the pie graph is not drawn (because of being disconnected),
//		// check again if the user has enabled the Internet meanwhile, if so, 
//		// fetch the graph data and draw the graph.
//		PieGraph pireGraph = (PieGraph) getView().findViewById(R.id.piegraph);
//		
//		Log.d(TAG, "CaratApplication.isInternetAvailable()=" + CaratApplication.isInternetAvailable());
//		Log.d(TAG, "pireGraph.getSlices().size()=" + pireGraph.getSlices().size());
//		boolean debugging = gotDataFromMainActivity();
//		
//		// Don't redraw (refresh) the graph if it is already drawn (if we already have the data)
//		if (!gotDataFromMainActivity()) {
//			Log.d(TAG, "about to draw the graph");
//			// data will be put in MainActivity's shared preferences. TAKES TIME (network operation is slow)
//			new PrefetchData(getActivity()).execute();
//			
////			new Handler().postDelayed(new Runnable() {
////	            @Override
////	            public void run() {
//	            	// load the data just fetched into the fields of the current class
//	            	loadPrefsToFields();
//	            	// read the data from fields and draw the graph
//	    			handlePieGraphDrawing(null, mResources);
////	            }
////	        }, 5000); // wait AT LEAST 2 seconds for the data to be fetched over (slow) network before trying to read the fields and draw
//			// some users might have slower Internet speed (256 Kb/s mobile data, for example)
//			
//			
//		}
//		
//		super.onResume();
//	}
	
	private void saveStatsToSharedPref() {
		SharedPreferences.Editor editor = mSharedPref.edit();
		editor.putInt(Constants.WELL_BEHAVED_APPS_COUNT_PREF_KEY, wellbehavedAppCount);
		editor.putInt(Constants.HOGS_COUNT_PREF_KEY, hogCount);
		editor.putInt(Constants.BUGS_COUNT_PREF_KEY, bugCount);
		editor.commit();
		
		Log.d(TAG, "saveStatsToSharedPref() was called. Saved wellbehavedAppsCount=" + wellbehavedAppCount);
	}
	
	/**
	 * @param sharedPref
	 */
	private void loadPrefsToFields() {
		lastWellbehavedAppCount = wellbehavedAppCount = mSharedPref.getInt(Constants.WELL_BEHAVED_APPS_COUNT_PREF_KEY, 0); 
		lastHogCount = hogCount = mSharedPref.getInt(Constants.HOGS_COUNT_PREF_KEY, 0);
		lastBugCount = bugCount = mSharedPref.getInt(Constants.BUGS_COUNT_PREF_KEY, 0);
		
		Log.d(TAG, "loadPrefsToFields() invoked. Read data from the pref: "
				+ "lastWellbehavedAppCount=wellbehavedAppCount=" + lastWellbehavedAppCount);
	}

	/**
	 * If connected to Internet, draw the graph (read data from fields),
	 * if not connected, ask the user to enable WiFi.
	 * @param inflatedView
	 * @param resources
	 */
	/*private void handlePieGraphDrawing(View inflatedV, Resources resources) {
		View inflatedView;
		
		if (inflatedV == null) {
			inflatedView = this.getView();
		} else {
			inflatedView = inflatedV;
		}
		
////		if (gotDataFromMainActivity()) {
		
		// main code: from here
//			PieGraph pieGraph = (PieGraph) inflatedView.findViewById(R.id.piegraph);
//			int wellbehaved = mMainActivity.mWellbehaved;
//			int hogs = mMainActivity.mHogs;
//			int bugs = mMainActivity.mBugs;
//			Log.d(TAG, "about to draw the graph. wellbehavedAppCount=" + wellbehaved);
//			drawPieGraph(pieGraph, resources, wellbehaved, hogs, bugs);
		// main code: to here
			
////		} else if (hasOldData()) {
////			PieGraph pireGraph = (PieGraph) inflatedView.findViewById(R.id.piegraph);
////			Log.d(TAG, "about to draw the graph from old data. lastWellbehavedAppCount=" + lastWellbehavedAppCount);
////			drawPieGraph(pireGraph, resources, lastWellbehavedAppCount, lastHogCount, lastBugCount);
////		} else {
////			Log.d(TAG, "data unavailable.");
////			TextView tv = (TextView) inflatedView.findViewById(R.id.summary_screen_title);
////			tv.setText(R.string.connection_error);
////			tv.setOnClickListener(new OnClickListener() {
////				@Override
////				public void onClick(View v) {
////					CaratApplication.getMainActivity().safeStart(android.provider.Settings.ACTION_WIFI_SETTINGS, getString(R.string.wifisettings));
////				}
////			});
////		}
	}*/
	
//	private boolean gotDataFromMainActivity() {
//		boolean result = wellbehavedAppCount != 0 && hogCount != 0 && bugCount != 0;
//		Log.d(TAG, "gotDataFromMainActivity() returns" + result + ". wellbehavedAppCount=" + wellbehavedAppCount);
//		return result;
//	}
//	
//	private boolean hasOldData() {
//		return lastWellbehavedAppCount != 0 && lastHogCount != 0 && lastBugCount != 0;
//	}
	
	/**
	 * @param pieGraph
	 * @param resources
	 * @param wellbehavedAppCount
	 * @param hogCount
	 * @param bugCount
	 */
	private void drawPieGraph(PieGraph pieGraph, Resources resources, int wellbehavedAppCount, int hogCount, int bugCount) {
		PieSlice slice = new PieSlice();
		slice.setColor(resources.getColor(R.color.green));
		slice.setSelectedColor(resources.getColor(R.color.transparent_orange));
		slice.setValue(wellbehavedAppCount);
		slice.setTitle("first");
		pieGraph.addSlice(slice);
		
		slice = new PieSlice();
		slice.setColor(resources.getColor(R.color.orange));
		slice.setValue(hogCount);
		pieGraph.addSlice(slice);

		slice = new PieSlice();
		slice.setColor(resources.getColor(R.color.purple));
		slice.setValue(bugCount);
		pieGraph.addSlice(slice);

		pieGraph.setOnSliceClickedListener(new OnSliceClickedListener() {
			@Override
			public void onClick(int index) {
				switch (index) {
				case 0:
					Toast.makeText(getActivity(), R.string.userswithoutany, Toast.LENGTH_SHORT).show();
					break;
				case 1:
					Toast.makeText(getActivity(), R.string.userswithhogs, Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(getActivity(), R.string.userswithbugs, Toast.LENGTH_SHORT).show();
					break;
				}
			}
		});

		Bitmap b = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher);
		pieGraph.setBackgroundBitmap(b);
	}
	
}
