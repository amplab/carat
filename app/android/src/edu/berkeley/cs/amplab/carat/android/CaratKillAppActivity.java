package edu.berkeley.cs.amplab.carat.android;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class CaratKillAppActivity extends Activity {
	@Override
	public void finish() {
		//findViewById(R.id.jscoreView).startAnimation(CaratMainActivity.outtoRight);
		super.finish();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.killapp);
		WebView webview = (WebView) findViewById(R.id.killAppView);
		//Fixes the white flash when showing the page for the first time.
		if (getString(R.string.blackBackground).equals("true"))
			webview.setBackgroundColor(0);
		
		/*
		 * getWindow().requestFeature(Window.FEATURE_PROGRESS);
		 * 
		 * webview.getSettings().setJavaScriptEnabled(true);
		 */
		/*
		 * To display the amplab_logo, we need to have it stored in assets as
		 * well. If we don't want to do that, the loadConvoluted method below
		 * avoids it.
		 */
		webview.loadUrl("file:///android_asset/killapp.html");
		//findViewById(R.id.jscoreView).startAnimation(CaratMainActivity.inFromRight);
		webview.setOnTouchListener(new BackSwipeListener(this));
	}
}
