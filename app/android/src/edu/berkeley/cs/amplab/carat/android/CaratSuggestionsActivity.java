package edu.berkeley.cs.amplab.carat.android;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ViewFlipper;

import java.util.List;

import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.suggestions.*;
import edu.berkeley.cs.amplab.carat.android.ui.BaseVFActivity;
import edu.berkeley.cs.amplab.carat.android.ui.FlipperBackListener;
import edu.berkeley.cs.amplab.carat.android.ui.SwipeListener;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

public class CaratSuggestionsActivity extends BaseVFActivity {

    private ViewFlipper vf = null;
    private int baseViewIndex = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.suggestions);
		vf = (ViewFlipper) findViewById(R.id.suggestionsFlipper);
        View baseView = findViewById(R.id.list);
        baseView.setOnTouchListener(
                SwipeListener.instance);
        vf.setOnTouchListener(SwipeListener.instance);
        baseViewIndex = vf.indexOfChild(baseView);

		final ListView lv = (ListView) findViewById(R.id.list);
		lv.setCacheColorHint(0);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				Object o = lv.getItemAtPosition(position);
				HogsBugs fullObject = (HogsBugs) o;
				View target = findViewById(R.id.killAppView);
                vf.setOutAnimation(CaratMainActivity.outtoLeft);
                vf.setInAnimation(CaratMainActivity.inFromRight);
                vf.setDisplayedChild(vf.indexOfChild(target));
                viewIndex = vf.indexOfChild(target);
				//killApp(fullObject.getAppName());
			}
		});

		initKillView();
		if (viewIndex == 0)
            vf.setDisplayedChild(baseViewIndex);
        else
            vf.setDisplayedChild(viewIndex);
	}
	
	private void initKillView(){
	    WebView webview = (WebView) findViewById(R.id.killAppView);
        // Fixes the white flash when showing the page for the first time.
        if (getString(R.string.blackBackground).equals("true"))
            webview.setBackgroundColor(0);
        String osVer = SamplingLibrary.getOsVersion();
        // FIXME: KLUDGE. Should be smarter with the version number.
        if (osVer.startsWith("2."))
            webview.loadUrl("file:///android_asset/killapp-2.2.html");
        else
            webview.loadUrl("file:///android_asset/killapp.html");
        webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
                .indexOfChild(findViewById(R.id.list))));
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		getRealSuggestions();
		super.onResume();
	}
	
	private void getRealSuggestions(){
		CaratApplication app = (CaratApplication) getApplication();
		final ListView lv = (ListView) findViewById(R.id.list);
		lv.setAdapter(new HogBugSuggestionsAdapter(app, app.s.getHogReport(), app.s.getBugReport()));
	}

	public void killApp(String appName) {
		List<ActivityManager.RunningAppProcessInfo> list = SamplingLibrary
				.getRunningProcessInfo(getApplicationContext());
		if (list != null) {
			for (int i = 0; i < list.size(); ++i) {
				ActivityManager.RunningAppProcessInfo pi = list.get(i);
				if (appName.matches(pi.processName)) {
					android.os.Process.killProcess(pi.pid);
				}
			}
		}
	}
	
	@Override
    public void onBackPressed() {
        if (vf.getDisplayedChild() != baseViewIndex) {
            vf.setOutAnimation(CaratMainActivity.outtoRight);
            vf.setInAnimation(CaratMainActivity.inFromLeft);
            vf.setDisplayedChild(baseViewIndex);
            viewIndex = baseViewIndex;
        } else
            finish();
    }
}
