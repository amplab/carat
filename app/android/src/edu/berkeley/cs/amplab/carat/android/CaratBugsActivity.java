package edu.berkeley.cs.amplab.carat.android;

import edu.berkeley.cs.amplab.carat.android.suggestions.BugsAdapter;
import edu.berkeley.cs.amplab.carat.android.suggestions.HogsBugsAdapter;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

public class CaratBugsActivity extends Activity {
	
    private ViewFlipper vf = null;
	private int baseViewIndex = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bugs);

		vf = (ViewFlipper) findViewById(R.id.bugsFlipper);
		View baseView = findViewById(R.id.bugsList);
		baseView.setOnTouchListener(
				SwipeListener.instance);
		baseViewIndex = vf.indexOfChild(baseView);
		initBugsView();
		initGraphView();
	}

	private void initBugsView() {
		final ListView lv = (ListView) findViewById(R.id.bugsList);
		lv.setCacheColorHint(0);
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				Object o = lv.getItemAtPosition(position);
				HogsBugs fullObject = (HogsBugs) o;
				View target = findViewById(R.id.graphView);
				vf.setOutAnimation(CaratMainActivity.outtoLeft);
				vf.setInAnimation(CaratMainActivity.inFromRight);
				vf.setDisplayedChild(vf.indexOfChild(target));
				
				Toast.makeText(CaratBugsActivity.this,
						"You have chosen: " + " " + fullObject.getAppName(),
						Toast.LENGTH_SHORT).show();
			}
		});
		
	}

	private void initGraphView() {
		WebView webview = (WebView) findViewById(R.id.graphView);
		// Fixes the white flash when showing the page for the first time.
		webview.setBackgroundColor(0);
		webview.getSettings().setJavaScriptEnabled(true);
		
		webview.loadUrl("file:///android_asset/twolinechart.html");
		webview.setOnTouchListener(new FlipperBackListener(vf, vf
				.indexOfChild(findViewById(R.id.bugsList))));
	}
	
	/**
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume(){
		CaratApplication app = (CaratApplication) getApplication();
		final ListView lv = (ListView) findViewById(R.id.bugsList);
		lv.setAdapter(new BugsAdapter(app, app.s.getBugReport()));
		super.onResume();
	}
	
	@Override
	public void onBackPressed() {
		if (vf.getDisplayedChild() != baseViewIndex) {
			vf.setOutAnimation(CaratMainActivity.outtoRight);
			vf.setInAnimation(CaratMainActivity.inFromLeft);
			vf.setDisplayedChild(baseViewIndex);
		} else
			finish();
	}
}
