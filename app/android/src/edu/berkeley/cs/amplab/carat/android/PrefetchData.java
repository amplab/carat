package edu.berkeley.cs.amplab.carat.android;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.android.utils.JsonParser;

public class PrefetchData extends AsyncTask<Void, Void, Void> {


	String serverResponseJson = null;
	private final String TAG = "PrefetchData";
	
	private String wellbehaved = null,
				   hogs = null,
				   bugs = null;
	
	
	
	private SplashActivity mSplashActivity = null;
	private MainActivity mMainActivity;
	
	public PrefetchData(Activity sourceActivity){
		if (sourceActivity.getClass() == SplashActivity.class) {
			Log.d(TAG, "PrefetchData was instantiated and called from the SplashActivity");
			mSplashActivity = (SplashActivity) sourceActivity;
		} else /* if (sourceActivity.getClass() == MainActivity.class) */ {
			Log.d(TAG, "PrefetchData was instantiated and called from the MainActivity "
					+ "(called from the summary fragment's onResume() and passed MainActivity as the host)");
			mMainActivity = (MainActivity) sourceActivity;
		}
	}
	
	
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//    }

    @Override
    protected Void doInBackground(Void... arg0) {
        /*
         * Will make http call here. This call will download required data
         * before launching the app
         * example:
         * 1. Downloading and storing in SQLite
         * 2. Downloading images
         * 3. Fetching and parsing the xml / json
         * 4. Sending device information to server
         * 5. etc.,
         */
        JsonParser jsonParser = new JsonParser();
        
        try {
        	if (CaratApplication.isInternetAvailable()) {
        		serverResponseJson = jsonParser
        				.getJSONFromUrl("http://carat.cs.helsinki.fi/statistics-data/stats.json");
        	}
        } catch (Exception e) {
        	Log.d("SplashActivity", e.getStackTrace().toString());
        }
        
        if (serverResponseJson != null && serverResponseJson != "") {
            try {
                JSONArray jsonArray = new JSONObject(serverResponseJson).getJSONArray("android-apps");
                
                // Using Java reflections to set fields by passing their name to a method
                try {
					setFieldsFromJson(jsonArray, 0, "wellbehaved");
					setFieldsFromJson(jsonArray, 1, "hogs");
					setFieldsFromJson(jsonArray, 2, "bugs");
					
					Log.i(TAG, "received JSON: " + "wellbehaved: " + wellbehaved 
							+ ", hogs: " + hogs + ", bugs: " + bugs);
				} catch (IllegalArgumentException e) {
					Log.e(TAG, "IllegalArgumentException in setFieldsFromJson()");
				} catch (IllegalAccessException e) {
					Log.e(TAG, "IllegalAccessException in setFieldsFromJson()");
				}
            } catch (JSONException e) {
            	Log.e(TAG, e.getStackTrace().toString());
            }
        } else {
        	Log.d(TAG, "server respone JSON is null.");
        }
        return null;
    }

    @Override
	protected void onPostExecute(Void result) {
    	super.onPostExecute(result);
		
		if (mSplashActivity != null) {
			// After completing http call
			// close the host activity (SplashActivity) and launch the main activity
			Intent intentMainActvity = new Intent(mSplashActivity, MainActivity.class);
			Log.d(TAG, "created the intent");
			
			if (gotDataSuccessfully()) {
				Log.d(TAG, "about to set the arguments of the newly created intent");
				intentMainActvity.putExtra("wellbehaved", wellbehaved);
				intentMainActvity.putExtra("hogs", hogs);
				intentMainActvity.putExtra("bugs", bugs);
			} else {
				Log.d(TAG, "about to set the extras of the main ACTIVITY intent to not_available");
				intentMainActvity.putExtra("wellbehaved", Constants.DATA_NOT_AVAIABLE);
				intentMainActvity.putExtra("hogs", Constants.DATA_NOT_AVAIABLE);
				intentMainActvity.putExtra("bugs", Constants.DATA_NOT_AVAIABLE);
			}

			Log.d(TAG, "about to start the main activity");
			mSplashActivity.startActivity(intentMainActvity);

			Log.d(TAG, "about to close this async task");
			// close this AsyncTask
			mSplashActivity.finish();
		} 
		
		if (mMainActivity != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			
			Log.d(TAG, "gotDataSuccessfully()=" + gotDataSuccessfully() + "time: " + sdf.format(new Date()));
			
			if (gotDataSuccessfully()) {
				mMainActivity.mWellbehaved = Integer.parseInt(wellbehaved);
				mMainActivity.mHogs = Integer.parseInt(hogs);
				mMainActivity.mBugs = Integer.parseInt(bugs);
			}
//				
//						
//				SummaryFragment summaryFragment = new SummaryFragment();
//				Bundle args = new Bundle();
//				
//				args.putInt("wellbehaved", Integer.parseInt(wellbehaved));
//				args.putInt("hogs", Integer.parseInt(hogs));
//				args.putInt("bugs", Integer.parseInt(bugs));
//				
//				Log.d(TAG, "put the arguments into the bundle. totalWellbehavedAppsCount=" + wellbehaved);
//				
//				summaryFragment.setArguments(args);
//				Log.d(TAG, "set the arguments of the summary fragment");
//				
//				String summaryFragmentLabel = mMainActivity.getString(R.string.tab_summary);
//				
//				mMainActivity.replaceFragment(summaryFragment, summaryFragmentLabel);
				
			
			
//			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(
//					CaratApplication.getContext());
////			SharedPreferences sharedPref = mMainActivity.getSharedPreferences(
////					Constants.MAIN_ACTIVITY_PREFERENCE_KEY, Context.MODE_PRIVATE);
//			
//			Log.d(TAG, "gotDataSuccessfully()=" + gotDataSuccessfully());
//			
//			if (gotDataSuccessfully()) {
//				SharedPreferences.Editor editor = sharedPref.edit();
//				editor.putInt(Constants.WELL_BEHAVED_APPS_COUNT_PREF_KEY, Integer.parseInt(wellbehaved));
//				editor.putInt(Constants.HOGS_COUNT_PREF_KEY, Integer.parseInt(hogs));
//				editor.putInt(Constants.HOGS_COUNT_PREF_KEY, Integer.parseInt(bugs));
//				editor.commit();
//				
//				int wellBehaved = sharedPref.getInt(Constants.WELL_BEHAVED_APPS_COUNT_PREF_KEY, 0);
//				Log.d(TAG, "successfully stored the fetched value of wellBehaved:" + wellBehaved);
//			}
			
		}
		
		
	}
    
    /**
     * Using Java reflections to set fields by passing their name to a method
     * @param jsonArray the json array from which we want to extract different json objects
     * @param objIdx the index of the object in the json array
     * @param fieldName the name of the field in the current NESTED class (PrefetchData)
     */
	private void setFieldsFromJson(JSONArray jsonArray, int objIdx, String fieldName) 
			throws JSONException, IllegalArgumentException, IllegalAccessException {
		Class<? extends PrefetchData> currentClass = this.getClass();
		Field field = null;
		
		try {
			// important: getField() can only get PUBLIC fields. 
			// For private fields, use getDeclaredField()
			field = currentClass.getDeclaredField(fieldName);
		} catch(NoSuchFieldException e) {
			Log.e(TAG, "NoSuchFieldException when trying to get a reference to the field: " + fieldName);
		}
		
		if (field != null) {
			JSONObject jsonObject;
			if (jsonArray != null ) {
				jsonObject = jsonArray.getJSONObject(objIdx);
				if (jsonObject != null && jsonObject.getString("value") != null && jsonObject.getString("value") != "")
					field.set(this, jsonObject.getString("value"));
				else 
					Log.e(TAG, "json object (server response) is null: jsonArray(" + objIdx + ")=null (or ='')");
			}
		}
		
	}
	
	private boolean gotDataSuccessfully() {
		return wellbehaved != null && wellbehaved != "" && hogs != null && hogs != ""
				&& bugs != null && bugs != "";
	}
	
}