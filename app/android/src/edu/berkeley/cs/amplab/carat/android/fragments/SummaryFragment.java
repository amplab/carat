package edu.berkeley.cs.amplab.carat.android.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import edu.berkeley.cs.amplab.carat.android.MainActivity;
import edu.berkeley.cs.amplab.carat.android.R;

/**
 * 
 * @author Javad Sadeqzadeh
 *
 */
public class SummaryFragment extends Fragment {
	private final String TAG = "SummaryFragment";
	// keys for retrieving values from the shared preference
	final String wellbehavedKey = "wellbehavedAppCount";
	final String hogCountKey = "hogCount";
	final String bugCountKey = "bugCount";
		
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "here");
		View inflatedView = inflater.inflate(R.layout.summary, container, false);
        PieGraph pg = (PieGraph) inflatedView.findViewById(R.id.piegraph);
        Resources resources = getResources();
        
		int wellbehavedAppCount = 0, lastWellbehavedAppCount = 0,
			hogCount = 0, lastHogCount = 0,
			bugCount = 0, lastBugCount = 0;
		
		Log.d(TAG, "about to read the arguments");
		Bundle arguments = getArguments();
		if (arguments != null) {
			wellbehavedAppCount = arguments.getInt("wellbehaved");
			hogCount = arguments.getInt("hogs");
			bugCount = arguments.getInt("bugs");
		}
		
		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		
		boolean gotDataFromMainActivity = wellbehavedAppCount != 0 && hogCount != 0 && bugCount != 0;
		
		if (gotDataFromMainActivity) {
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt(wellbehavedKey, wellbehavedAppCount);
			editor.putInt(hogCountKey, hogCount);
			editor.putInt(bugCountKey, bugCount);
			editor.commit();
		} else {
			try {
			lastWellbehavedAppCount = sharedPref.getInt(wellbehavedKey, 0); 
			lastHogCount = sharedPref.getInt(hogCountKey, 0);
			lastBugCount = sharedPref.getInt(bugCountKey, 0);
			} catch (Exception e) {
				Log.d(TAG, "unable to read the info from the shared preference. No such a key.");
			}
		}
		
        boolean hasOldData = lastWellbehavedAppCount != 0 && lastHogCount != 0 && lastBugCount != 0;
        
		if (gotDataFromMainActivity || hasOldData) {
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
		
        getActivity().setTitle(resources.getString(R.string.tab_summary));
        
        // onCreateView() method should always return the inflated view
        return inflatedView;
    }
	
}
