package edu.berkeley.cs.amplab.carat.android;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * This class will be removed and functionality moved to suggestionsActivity,
 * which will become a ViewFlipper or ViewSwitcher.
 *  
 * @author Eemil Lagerspetz
 *
 */
public class CaratKillAppActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.killapp);
		WebView webview = (WebView) findViewById(R.id.killAppView);
		// Fixes the white flash when showing the page for the first time.
		if (getString(R.string.blackBackground).equals("true"))
			webview.setBackgroundColor(0);
		
		webview.loadUrl("file:///android_asset/killapp.html");
		webview.setOnTouchListener(new BackSwipeListener(this));
	}
}
