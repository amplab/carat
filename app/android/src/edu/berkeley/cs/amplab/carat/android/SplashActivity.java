package edu.berkeley.cs.amplab.carat.android;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * This fragment reads the statistics of Android apps from the Carat stats URL,
 * behind the scene, while displaying a splash screen to show case the carat logo
 * 
 * @author Javad Sadeqzadeh
 *
 */
public class SplashActivity extends ActionBarActivity {
	final String TAG = "SplashScreen";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_splash_screen);
		
		// download the pie chart info (user statistics) in the background, while displaying the splash screen. 
		// when ready, pass the fetched info to the next activity (MainActivity) for displaying
		new PrefetchData(this).execute();
	}
	
	// do not do anything when the back button is pressed
	@Override
	public void onBackPressed() {
	}

}
