package edu.berkeley.cs.amplab.carat.android;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class CaratJscoreActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jscoreinfo);
		WebView webview = (WebView) findViewById(R.id.jscoreView);
		//Fixes the white flash when showing the page for the first time.
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
		webview.loadUrl("file:///android_asset/jscoreinfo.html");
	}
}
