package edu.berkeley.cs.amplab.carat.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CaratBugsActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView textview = new TextView(this);
		textview.setFocusable(true);
		textview.setText("This is the Bugs tab");
		setContentView(textview);
		textview.getRootView().setOnTouchListener(SwipeListener.instance);
	}
}
