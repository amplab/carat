package edu.berkeley.cs.amplab.carat.android;

import edu.berkeley.cs.amplab.carat.android.ui.LocalizedWebView;
import edu.berkeley.cs.amplab.carat.android.ui.SwipeListener;
import android.app.Activity;
import android.os.Bundle;

public class CaratAboutActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		LocalizedWebView webview = (LocalizedWebView) findViewById(R.id.aboutView);
		
		webview.loadUrl("file:///android_asset/about.html");
		webview.setOnTouchListener(SwipeListener.instance);
	}
}
