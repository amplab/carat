package edu.berkeley.cs.amplab.carat.android;

import java.lang.reflect.Field;
import java.net.InetAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;
import edu.berkeley.cs.amplab.carat.android.utils.JsonParser;

/**
 * This fragment reads the statistics of Android apps from the Carat stats URL,
 * behind the scene, while displaying a splash screen to show case the carat logo
 * 
 * @author Javad Sadeqzadeh
 *
 */
public class SplashActivity extends ActionBarActivity {
	private final String TAG = "SplashScreen";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "about to set the content view");
		
		setContentView(R.layout.activity_splash_screen);
		
		// download the pie chart info (user statistics) in the background, while displaying the splash screen. 
		// when ready, pass the fetched info to the next activity (MainActivity) for displaying
		new PrefetchData().execute();
	}
	
	@Override
	public void onBackPressed() {
	}

	public class PrefetchData extends AsyncTask<Void, Void, Void> {
		String serverResponseJson = null;
		
		
		private String wellbehaved = null,
					   hogs = null,
					   bugs = null;
		
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
 
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
            	if (isInternetAvailable()) {
            		Log.d(TAG, "internet available");
            		serverResponseJson = jsonParser
            				.getJSONFromUrl("http://carat.cs.helsinki.fi/statistics-data/stats.json");
            	}
            } catch (Exception e) {
            	Log.d(TAG, "exception occured here");
            	Log.d("SplashActivity", e.getStackTrace().toString());
            }
            
            if (serverResponseJson != null && serverResponseJson != "") {
            	Log.d(TAG, "json not null");
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
            }
            
            Log.d(TAG, "json null, returning null");
            return null;
        }

        @Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// After completing http call
			// will close this activity and launch the main activity
			Intent intentMainActvity = new Intent(SplashActivity.this, MainActivity.class);
			Log.d(TAG, "created the intent");
			boolean gotDataSuccessfully = 
					wellbehaved != null && wellbehaved != "" 
					&& hogs != null && hogs != "" 
					&& bugs != null && bugs != "";
			if (gotDataSuccessfully) {
				Log.d(TAG, "about to set the arguments of the newly created intent");
				intentMainActvity.putExtra("wellbehaved", wellbehaved);
				intentMainActvity.putExtra("hogs", hogs);
				intentMainActvity.putExtra("bugs", bugs);
			} else {
				intentMainActvity.putExtra("wellbehaved", Constants.DATA_NOT_AVAIABLE);
				intentMainActvity.putExtra("hogs", Constants.DATA_NOT_AVAIABLE);
				intentMainActvity.putExtra("bugs", Constants.DATA_NOT_AVAIABLE);
			}
			
			Log.d(TAG, "about to start the main activity");
			startActivity(intentMainActvity);
			
			Log.d(TAG, "about to close this async task");
			// close this AsyncTask
			finish();
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
	}
	
	private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }
    }

}
