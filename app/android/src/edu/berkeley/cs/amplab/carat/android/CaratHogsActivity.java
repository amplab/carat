package edu.berkeley.cs.amplab.carat.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CaratHogsActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TextView textview = new TextView(this);
		textview.setText("This is the Hogs tab");
		setContentView(textview);
	}
}
