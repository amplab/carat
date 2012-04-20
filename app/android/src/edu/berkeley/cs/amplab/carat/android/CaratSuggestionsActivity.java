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

public class CaratSuggestionsActivity extends ListActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.suggestions);

		// setListAdapter(new ArrayAdapter<String>(this, R.layout.listitem2,
		// R.id.list_content, COUNTRIES));

		// lv.setTextFilterEnabled(true);

		final ListView lv = getListView();
		lv.setCacheColorHint(0);

		ArrayList<Suggestion> searchResults = getSuggestions();

		lv.setAdapter(new SuggestionAdapter(this, searchResults));

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				Object o = lv.getItemAtPosition(position);
				Suggestion fullObject = (Suggestion) o;
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

	private ArrayList<Suggestion> getSuggestions() {
		ArrayList<Suggestion> results = new ArrayList<Suggestion>();
		List<RunningAppProcessInfo> rapps = SamplingLibrary
				.getRunningProcessInfo(getApplicationContext());
		for (RunningAppProcessInfo pi : rapps) {
			Suggestion sr1 = new Suggestion();
			sr1.setName(pi.processName);
			sr1.setBenefit("You get to kill a"
					+ ProcessInfoAdapter.importanceString(pi.importance));
			results.add(sr1);
		}
		return results;
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
