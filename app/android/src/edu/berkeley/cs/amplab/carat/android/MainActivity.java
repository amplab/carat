package edu.berkeley.cs.amplab.carat.android;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import com.flurry.android.FlurryAgent;

import edu.berkeley.cs.amplab.carat.android.protocol.ClickTracking;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.android.utils.Tracker;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar.Tab;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Carat Android App Main Activity. Is loaded right after CaratApplication.
 * Holds the Tabs that comprise the UI. Place code related to tab handling and
 * global Activity code here.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class MainActivity extends ActionBarActivity {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle,
						 mTitle;
	private String[] mDrawerItems;

	// Log tag
	private static final String TAG = "CaratMain";

	public static final String ACTION_BUGS = "bugs",
							   ACTION_HOGS = "hogs";

	// Key File
	private static final String FLURRY_KEYFILE = "flurry.properties";

	private MenuItem feedbackItem = null;

	private String fullVersion = null;

	private Tracker tracker = Tracker.getInstance();

	/* 
	 * Values of the following variables are read (from a URL) in the
	 * SplashScreen, and then passed to this activity.
	 * These values (statistics) will be sent to the summary
	 * fragment, to be shown in the chart.
	 */
	private int totalWellbehavedAppsCount,
				totalHogsCount,
				totalBugsCount;

	/**
	 * 
	 * @param savedInstanceState
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Reading the parameters passed to the current activity from the
		 * SplashScreen. These values have been read from the Carat stats URL behind the
		 * scene in the SplashScreen.
		 */
		Intent intent = getIntent();
		totalWellbehavedAppsCount = Integer.parseInt(intent.getStringExtra("wellbehaved"));
		totalHogsCount = Integer.parseInt(intent.getStringExtra("hogs"));
		totalBugsCount = Integer.parseInt(intent.getStringExtra("bugs"));

		CaratApplication.setMain(this);

		/*
		 * Activity.getWindow.requestFeature() should get invoked only before
		 * setContentView(), otherwise it will cause an app crash The progress
		 * bar doesn't get displayed when there is no update in progress
		 */
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();

		setTitleNormal();

		/*
		 * Before using the field "fullVersion", first invoke setTitleNormal()
		 * or setFullVersion() to set this field
		 */
		mTitle = mDrawerTitle = fullVersion;
		mDrawerItems = getResources().getStringArray(R.array.drawer_items);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		// Enable ActionBar app icon to behave as action to toggle navigation drawer
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		if (savedInstanceState == null) {
			selectItem(0);
		}

		setTitleNormal();

		// track user clicks (taps)
		tracker.trackUser("caratstarted");

		// Uncomment the following to enable listening on local port 8080:
		/*
		 * try { HelloServer h = new HelloServer(); } catch (IOException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); }
		 */
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		Bundle args = new Bundle();
		String fragmentLabel = "";

		switch (position) {
		case 0:
			fragment = new SummaryFragment();
			args.putInt("wellbehaved", totalWellbehavedAppsCount);
			args.putInt("hogs", totalHogsCount);
			args.putInt("bugs", totalBugsCount);
			fragment.setArguments(args);
			fragmentLabel = getString(R.string.tab_summary);
			break;
		case 1:
			fragment = new SuggestionsFragment();
			fragmentLabel = getString(R.string.tab_actions);
			break;
		case 2:
			fragment = new MyDeviceFragment();
			fragmentLabel = getString(R.string.tab_my_device);
			break;
		case 3:
			args.putBoolean("isBugs", true);
			fragment = new BugsOrHogsFragment();
			fragment.setArguments(args);
			fragmentLabel = getString(R.string.tab_bugs);
			break;
		case 4:
			args.putBoolean("isBugs", false);
			fragment = new BugsOrHogsFragment();
			fragment.setArguments(args);
			fragmentLabel = getString(R.string.tab_hogs);
			break;
		case 5:
			fragment = new AboutFragment();
			fragmentLabel = getString(R.string.tab_about);
			break;
		}

		CaratApplication.replaceFragment(fragment, fragmentLabel);

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mDrawerItems[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggle
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}

	public void setTitleNormal() {
		setFullVersion();
		if (CaratApplication.storage != null) {
			long s = CaratApplication.storage.getSamplesReported();
			Log.d("setTitleNormal", "number of samples reported=" + String.valueOf(s));
			if (s > 0) {
				this.setTitle("Carat - " + s + " " + getString(R.string.samplesreported));
			} else {
				this.setTitle(fullVersion);
			}
		} else
			this.setTitle(fullVersion);
	}

	private void setFullVersion() {
		fullVersion = getString(R.string.app_name) + " " + getString(R.string.version_name);
	}

	public void setTitleUpdating(String what) {
		this.setTitle(fullVersion + " - " + getString(R.string.updating) + " " + what);
	}

	public void setTitleUpdatingFailed(String what) {
		this.setTitle(fullVersion + " - " + getString(R.string.didntget) + " " + what);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();

		String secretKey = null;
		Properties properties = new Properties();
		try {
			InputStream raw = MainActivity.this.getAssets().open(FLURRY_KEYFILE);
			if (raw != null) {
				properties.load(raw);
				if (properties.containsKey("secretkey"))
					secretKey = properties.getProperty("secretkey", "secretkey");
				Log.d(TAG, "Set Flurry secret key.");
			} else
				Log.e(TAG, "Could not open Flurry key file!");
		} catch (IOException e) {
			Log.e(TAG, "Could not open Flurry key file: " + e.toString());
		}
		if (secretKey != null) {
			FlurryAgent.onStartSession(getApplicationContext(), secretKey);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(getApplicationContext());
	}

	/**
	 * 
	 * Starts a Thread that communicates with the server to send stored samples.
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.i(TAG, "Resumed");
		CaratApplication.setMain(this);

		/*
		 * Thread for refreshing the UI with new reports every 5 mins and on
		 * resume. Also sends samples and updates blacklist/questionnaire url.
		 */

		Log.d(TAG, "Refreshing UI");
		// This spawns a thread, so it does not need to be in a thread.
		/*
		 * new Thread() { public void run() {
		 */
		((CaratApplication) getApplication()).refreshUi();
		/*
		 * } }.start();
		 */

		super.onResume();
		tracker.trackUser("caratresumed");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onPause()
	 */
	@Override
	protected void onPause() {
		Log.i(TAG, "Paused");
		tracker.trackUser("caratpaused");
		SamplingLibrary.resetRunningProcessInfo();
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		Log.d(TAG, "Finishing up");
		tracker.trackUser("caratstopped");
		super.finish();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ActivityGroup#onDestroy()
	 */
	// @Override
	// protected void onDestroy() {
	// super.onDestroy();
	// }

	/**
	 * Show share, feedback, wifi only menu here.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		final MenuItem wifiOnly = menu.add(R.string.wifionly);
		// wifiOnly.setCheckable(true);
		// wifiOnly.setChecked(useWifiOnly);
		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		if (p.getBoolean(CaratApplication.PREFERENCE_WIFI_ONLY, false))
			wifiOnly.setTitle(R.string.wifionlyused);
		wifiOnly.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				boolean useWifiOnly = p.getBoolean(CaratApplication.PREFERENCE_WIFI_ONLY, false);
				if (useWifiOnly) {
					p.edit().putBoolean(CaratApplication.PREFERENCE_WIFI_ONLY, false).commit();
					// wifiOnly.setChecked(false);
					wifiOnly.setTitle(R.string.wifionly);
					tracker.trackUser("WifiOnlyOff");
				} else {
					p.edit().putBoolean(CaratApplication.PREFERENCE_WIFI_ONLY, true).commit();
					// wifiOnly.setChecked(true);
					wifiOnly.setTitle(R.string.wifionlyused);
					tracker.trackUser("WifiOnlyOn");
				}
				return true;
			}
		});

		MenuItem shareItem = menu.add(R.string.share);
		shareItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				int jscore = CaratApplication.getJscore();
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.setType("text/plain");
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.myjscoreis) + " " + jscore);
				sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharetext1) + " " + jscore
						+ getString(R.string.sharetext2));
				startActivity(Intent.createChooser(sendIntent, getString(R.string.sharewith)));

				SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				if (p != null) {
					String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
					HashMap<String, String> options = new HashMap<String, String>();
					options.put("status", getTitle().toString());
					options.put("sharetext", getString(R.string.myjscoreis) + " " + jscore);
					ClickTracking.track(uuId, "caratshared", options, getApplicationContext());
				}
				return true;
			}
		});

		feedbackItem = menu.add(R.string.feedback);
		feedbackItem.setOnMenuItemClickListener(new MenuListener());
		return true;
	}

	/**
	 * Class to handle feedback form.
	 * 
	 * @author Eemil Lagerspetz
	 * 
	 */
	private class MenuListener implements OnMenuItemClickListener {

		@Override
		public boolean onMenuItemClick(MenuItem arg0) {
			int jscore = CaratApplication.getJscore();
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			Context a = getApplicationContext();
			SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(a);
			String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
			String os = SamplingLibrary.getOsVersion();
			String model = SamplingLibrary.getModel();

			// Emulator does not support message/rfc822
			if (model.equals("sdk"))
				sendIntent.setType("text/plain");
			else
				sendIntent.setType("message/rfc822");
			sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "carat@eecs.berkeley.edu" });
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "[carat] [Android] " + getString(R.string.feedbackfrom) + " "
					+ model);
			sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.os) + ": " + os + "\n"
					+ getString(R.string.model) + ": " + model + "\nCarat ID: " + uuId + "\nJ-Score: " + jscore + "\n"
					+ fullVersion + "\n");
			if (p != null) {
				HashMap<String, String> options = new HashMap<String, String>();
				options.put("os", os);
				options.put("model", model);
				SimpleHogBug[] b = CaratApplication.storage.getBugReport();
				int len = 0;
				if (b != null)
					len = b.length;
				options.put("bugs", len + "");
				b = CaratApplication.storage.getHogReport();
				len = 0;
				if (b != null)
					len = b.length;
				options.put("hogs", len + "");
				options.put("status", getTitle().toString());
				options.put("sharetext", getString(R.string.myjscoreis) + " " + jscore);
				ClickTracking.track(uuId, "feedbackbutton", options, getApplicationContext());
			}
			startActivity(Intent.createChooser(sendIntent, getString(R.string.chooseemail)));
			return true;
		}
	}
}