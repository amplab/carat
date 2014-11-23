package edu.berkeley.cs.amplab.carat.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.berkeley.cs.amplab.carat.android.utils.JsonParser;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * This fragment reads the statistics of Android apps from the Carat stats URL,
 * behind the scene, while displaying a splash screen to show case the carat logo
 * 
 * @author Javad Sadeqzadeh
 *
 */
public class SplashScreen extends ActionBarActivity {
	
    String wellbehaved, hogs, bugs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		new PrefetchData().execute();
	}

	private class PrefetchData extends AsyncTask<Void, Void, Void> {
		 
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
            String json = jsonParser
                    .getJSONFromUrl("http://carat.cs.helsinki.fi/statistics-data/stats.json");
 
            Log.e("Response: ", "> " + json);
 
            if (json != null) {
                try {
                    JSONArray jsonArray = new JSONObject(json).getJSONArray("android-apps");
                    JSONObject jsonObject;
                    jsonObject = jsonArray.getJSONObject(0);
                    wellbehaved = jsonObject.getString("value");
                    
                    jsonObject = jsonArray.getJSONObject(1);
                    hogs = jsonObject.getString("value");
                    
                    jsonObject = jsonArray.getJSONObject(2);
                    bugs = jsonObject.getString("value");
 
                    Log.e("JSON", "> " + "wellbehaved: " + wellbehaved + ", hogs: " + hogs + ", bugs: " + bugs);
 
                } catch (JSONException e) {
                	Log.e("SplashScreen", e.getStackTrace().toString());
                }
            }
 
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // After completing http call
            // will close this activity and launch the main activity
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            i.putExtra("wellbehaved", wellbehaved);
            i.putExtra("hogs", hogs);
            i.putExtra("bugs", bugs);
            startActivity(i);
 
            // close this AsyncTask
            finish();
        }
	}
 
}
