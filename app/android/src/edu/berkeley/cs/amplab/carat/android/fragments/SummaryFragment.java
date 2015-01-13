package edu.berkeley.cs.amplab.carat.android.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieGraph.OnSliceClickedListener;
import com.echo.holographlibrary.PieSlice;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.Constants;
import edu.berkeley.cs.amplab.carat.android.PrefetchData;
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
	
	SharedPreferences mSharedPref;
	Resources mResources;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView() started");
//		mSharedPref = PreferenceManager.getDefaultSharedPreferences(
//				CaratApplication.getContext());
		mSharedPref = getActivity().getSharedPreferences(
				Constants.MAIN_ACTIVITY_PREFERENCE_KEY, Context.MODE_PRIVATE);
		mResources = getResources();
		Log.d(TAG, "initialized the shared preferences");
		
		View inflatedView = inflater.inflate(R.layout.summary, container, false);
        
		Log.d(TAG, "about to read the arguments");
		Bundle arguments = getArguments();
		if (arguments != null) {
			wellbehavedAppCount = arguments.getInt("wellbehaved");
			hogCount = arguments.getInt("hogs");
			bugCount = arguments.getInt("bugs");
		}
		
		if (gotDataFromMainActivity()) {
			saveStatsToSharedPref();
		} else {
			try {
			// load older data (stored in the shared preferences structure)
			// into the fields starting with the prefix "last"
			loadPrefsToFields();
			} catch (Exception e) {
				Log.d(TAG, "unable to read the info from the shared preference. No such a key.");
			}
		}
		
		handlePieGraphDrawing(inflatedView, mResources);
		
        getActivity().setTitle(mResources.getString(R.string.tab_summary));
        
        // onCreateView() method should always return the inflated view
        return inflatedView;
    }

	private void saveStatsToSharedPref() {
		SharedPreferences.Editor editor = mSharedPref.edit();
		editor.putInt(Constants.WELL_BEHAVED_APPS_COUNT_PREF_KEY, wellbehavedAppCount);
		editor.putInt(Constants.HOGS_COUNT_PREF_KEY, hogCount);
		editor.putInt(Constants.BUGS_COUNT_PREF_KEY, bugCount);
		editor.commit();
	}

	@Override
	public void onResume() {
		Log.d(TAG, "summary fragment resumed.");
		// try to fetch data again, since the user might have enabled wifi or mobile data 
		// (after being disconnected while in the splash screen (when opening the app))
		// Data will be put in the shared preferences of the MainActivity.
		new PrefetchData(getActivity()).execute();
		
		// load the data just fetched into the fields of the current class
		loadPrefsToFields();
		
		// if connected to Internet, draw the graph (read data from fields), 
		// if not connected, ask user to enable WiFi
		handlePieGraphDrawing(this.getView(), mResources);
		
		super.onResume();
	}
	
	/**
	 * @param sharedPref
	 */
	private void loadPrefsToFields() {
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				Constants.MAIN_ACTIVITY_PREFERENCE_KEY, Context.MODE_PRIVATE);
		lastWellbehavedAppCount = wellbehavedAppCount = sharedPref.getInt(Constants.WELL_BEHAVED_APPS_COUNT_PREF_KEY, 0); 
		lastHogCount = hogCount = sharedPref.getInt(Constants.HOGS_COUNT_PREF_KEY, 0);
		lastBugCount = bugCount = sharedPref.getInt(Constants.BUGS_COUNT_PREF_KEY, 0);
	}

	/**
	 * @param inflatedView
	 * @param resources
	 */
	private void handlePieGraphDrawing(View inflatedView, Resources resources) {
		if (gotDataFromMainActivity()) {
			PieGraph pireGraph = (PieGraph) inflatedView.findViewById(R.id.piegraph);
			drawPieGraph(pireGraph, resources, wellbehavedAppCount, hogCount, bugCount);
		} else if (hasOldData()) {
			PieGraph pireGraph = (PieGraph) inflatedView.findViewById(R.id.piegraph);
			drawPieGraph(pireGraph, resources, lastWellbehavedAppCount, lastHogCount, lastBugCount);
		} else {
			TextView tv = (TextView) inflatedView.findViewById(R.id.summary_screen_title);
			tv.setText(R.string.connection_error);
			tv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CaratApplication.getMainActivity().safeStart(android.provider.Settings.ACTION_WIFI_SETTINGS, getString(R.string.wifisettings));
				}
			});
		}
	}
	
	private boolean gotDataFromMainActivity() {
		return wellbehavedAppCount != 0 && hogCount != 0 && bugCount != 0;
	}
	
	private boolean hasOldData() {
		return lastWellbehavedAppCount != 0 && lastHogCount != 0 && lastBugCount != 0;
	}
	
	/**
	 * @param pg
	 * @param resources
	 * @param wellbehavedAppCount
	 * @param hogCount
	 * @param bugCount
	 */
	private void drawPieGraph(PieGraph pg, Resources resources, int wellbehavedAppCount, int hogCount, int bugCount) {
		PieSlice slice = new PieSlice();
		slice.setColor(resources.getColor(R.color.green));
		slice.setSelectedColor(resources.getColor(R.color.transparent_orange));
		slice.setValue(wellbehavedAppCount);
		slice.setTitle("first");
		pg.addSlice(slice);

		slice = new PieSlice();
		slice.setColor(resources.getColor(R.color.orange));
		slice.setValue(hogCount);
		pg.addSlice(slice);

		slice = new PieSlice();
		slice.setColor(resources.getColor(R.color.purple));
		slice.setValue(bugCount);
		pg.addSlice(slice);

		pg.setOnSliceClickedListener(new OnSliceClickedListener() {
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
		pg.setBackgroundBitmap(b);
	}
	
}
