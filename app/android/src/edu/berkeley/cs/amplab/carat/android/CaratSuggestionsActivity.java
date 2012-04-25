package edu.berkeley.cs.amplab.carat.android;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.cs.amplab.carat.android.suggestions.*;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;

public class CaratSuggestionsActivity extends ListActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.suggestions);

		final ListView lv = getListView();
		lv.setCacheColorHint(0);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				Object o = lv.getItemAtPosition(position);
				HogsBugs fullObject = (HogsBugs) o;
				killApp(fullObject.getAppName());
				Toast.makeText(CaratSuggestionsActivity.this,
						"Killing: " + " " + fullObject.getAppName(),
						Toast.LENGTH_SHORT).show();
				// Intent myIntent = new Intent(v.getContext(),
				// CaratKillAppActivity.class);
				// findViewById(R.id.scrollView1).startAnimation(CaratMainActivity.outtoLeft);
				// startActivityForResult(myIntent, 0);
			}
		});

		lv.setOnTouchListener(SwipeListener.instance);
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
		final ListView lv = getListView();
		lv.setAdapter(new HogBugSuggestionsAdapter(app, app.s.getHogReport(), app.s.getBugReport()));
	}

	private void getFakeSuggestions() {
		ArrayList<Suggestion> results = new ArrayList<Suggestion>();
		List<RunningAppProcessInfo> rapps = SamplingLibrary
				.getRunningProcessInfo(getApplicationContext());
		
		CaratApplication app = (CaratApplication) getApplication();
		
		for (RunningAppProcessInfo pi : rapps) {
			/* Skip stuff that is not running (should not show in this list)
			 * and Services that will just spring back up when killed
			 * TODO: Figure out how to skip stuff like keyboards and notification icons
			 */
			if (pi.importance == RunningAppProcessInfo.IMPORTANCE_EMPTY || 
					pi.importance == RunningAppProcessInfo.IMPORTANCE_SERVICE)
				continue;
			Suggestion sugg = new Suggestion("Kill", app.labelForApp(pi.processName), CaratApplication.importanceString(pi.importance));
			sugg.setIcon(app.iconForApp(pi.processName));
			sugg.setBenefit(((int) (Math.random()*24)) + "h");
			results.add(sugg);
		}
		final ListView lv = getListView();
		lv.setAdapter(new SuggestionAdapter(this, results));
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
}
