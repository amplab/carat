package edu.berkeley.cs.amplab.carat.android;

import java.lang.reflect.Field;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;
import edu.berkeley.cs.amplab.carat.android.SplashScreen.PrefetchData;
import edu.berkeley.cs.amplab.carat.android.utils.JsonParser;

/**
 * This fragment reads the statistics of Android apps from the Carat stats URL,
 * behind the scene, while displaying a splash screen to show case the carat logo
 * 
 * @author Javad Sadeqzadeh
 *
 */
public class SplashScreen extends ActionBarActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		new PrefetchData().execute();
	}

	public class PrefetchData extends AsyncTask<Void, Void, Void> {
		String serverResponseJson = null;
		private final String TAG = "SplashScreen";
		
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
            serverResponseJson = jsonParser
			    .getJSONFromUrl("http://carat.cs.helsinki.fi/statistics-data/stats.json");
 
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
            }
 
            return null;
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
 
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            if (wellbehaved != null && hogs != null && bugs != null) {
				// After completing http call
				// will close this activity and launch the main activity
				Intent intentMainActvity = new Intent(SplashScreen.this, MainActivity.class);
				intentMainActvity.putExtra("wellbehaved", wellbehaved);
				intentMainActvity.putExtra("hogs", hogs);
				intentMainActvity.putExtra("bugs", bugs);
				startActivity(intentMainActvity);
            } else {
            	Log.e(TAG, "unable to set fields: wellbehaved, hogs, and bugs (needed to be sent to the MainActivity)");
            	displayConnectionError();
            }
            
            // close this AsyncTask
			finish();
        }

		private void displayConnectionError() {
			String errorText =  getResources().getString(R.string.statserror);
			// to make the duration of the toast longer than Toast.LENGTH_LONG, 
			// looping is an easy hack
			for (int i=0; i < 2; i++) {
				Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_LONG).show();
			}
		}
	}
}
