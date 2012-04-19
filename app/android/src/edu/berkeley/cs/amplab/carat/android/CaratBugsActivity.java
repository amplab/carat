package edu.berkeley.cs.amplab.carat.android;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class CaratBugsActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bugs);
		WebView webview = (WebView) findViewById(R.id.bugsView);
		//Fixes the white flash when showing the page for the first time.
		webview.setBackgroundColor(0);
		webview.getSettings().setJavaScriptEnabled(true);
		/*
		 * getWindow().requestFeature(Window.FEATURE_PROGRESS);
		 * 
		 * 
		 */
		/*
		 * To display the amplab_logo, we need to have it stored in assets as
		 * well. If we don't want to do that, the loadConvoluted method below
		 * avoids it.
		 */
		webview.loadUrl("file:///android_asset/twolinechart.html");
		webview.setOnTouchListener(SwipeListener.instance);
		
		/*TextView textview = new TextView(this);
		textview.setFocusable(true);
		textview.setText("This is the Bugs tab");
		setContentView(textview);
		textview.getRootView().setOnTouchListener(SwipeListener.instance);*/
	}
}
