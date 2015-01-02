package edu.berkeley.cs.amplab.carat.android;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.flurry.android.FlurryAgent;

import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.subscreens.CaratSettingsFragment;
import edu.berkeley.cs.amplab.carat.android.subscreens.WebViewFragment;
import edu.berkeley.cs.amplab.carat.android.utils.Tracker;

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
	private static final String TAG = "Activity (MainActivity)";

	public static final String ACTION_BUGS = "bugs",
							   ACTION_HOGS = "hogs";

	// Key File
	private static final String FLURRY_KEYFILE = "flurry.properties";

	private String fullVersion = null;

	private Tracker tracker = null;

	/* 
	 * Values of the following variables are read (from a URL) in the
	 * SplashScreen, and then passed to this activity.
	 * These values (statistics) will be sent to the summary
	 * fragment, to be shown in the chart.
	 */
	private int totalWellbehavedAppsCount,
				totalHogsCount,
				totalBugsCount;
	
	private Fragment mSummaryFragment;
	private Bundle mArgs;
	private String mSummaryFragmentLabel;
	
	private Fragment mSuggestionFragment;
	private String mSuggestionFragmentLabel;
	
	private Fragment mMyDeviceFragment;
	private String mMyDeviceFragmentLabel;
	
	private Fragment mBugsFragment;
	private String mBugsFragmentLabel;
	
	private Fragment mHogsFragment;
	private String mHogsFragmentLabel;
	
	private Fragment mSettingsSuggestionFragment;
	private String mSettingsSuggestionFragmentLabel;
	
	private Fragment mCaratSettingsFragment;
	private String mCaratSettingsFragmentLabel;
	
	private Fragment mAboutFragment;
	private String mAboutFragmentLabel;

	/**
	 * 
	 * @param savedInstanceState
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CaratApplication.setMain(this);
		tracker = Tracker.getInstance();

		/*
		 * Reading the parameters passed to the current activity from the
		 * SplashScreen. These values have been read from the Carat stats URL
		 * behind the scene in the SplashScreen.
		 */
		Intent intent = getIntent();
		totalWellbehavedAppsCount = Integer.parseInt(intent.getStringExtra("wellbehaved"));
		totalHogsCount = Integer.parseInt(intent.getStringExtra("hogs"));
		totalBugsCount = Integer.parseInt(intent.getStringExtra("bugs"));		

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
		
		// read and load the preferences specified in our xml preference file
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		// TODO: to be refactored (extract method), if we decide to keep pre-initialization of fragments
		// (doesn't make much difference in terms of smoothness/performance, but keeping all fragments in memory,
		// increases RAM usage of Carat)
		mSummaryFragment = new SummaryFragment();
		mArgs = new Bundle();
		mArgs.putInt("wellbehaved", totalWellbehavedAppsCount);
		mArgs.putInt("hogs", totalHogsCount);
		mArgs.putInt("bugs", totalBugsCount);
		mSummaryFragment.setArguments(mArgs);
		mSummaryFragmentLabel = getString(R.string.tab_summary);
		
		mSuggestionFragment = new SuggestionsFragment();
		mSuggestionFragmentLabel = getString(R.string.tab_actions);
		
		mMyDeviceFragment = new MyDeviceFragment();
		mMyDeviceFragmentLabel = getString(R.string.tab_my_device);
		
		mBugsFragment = new BugsOrHogsFragment();
		mArgs = new Bundle();
		mArgs.putBoolean("isBugs", true);
		mBugsFragment.setArguments(mArgs);
		mBugsFragmentLabel = getString(R.string.tab_bugs);
		
		mHogsFragment = new BugsOrHogsFragment();
		mArgs = new Bundle();
		mArgs.putBoolean("isBugs", false);
		mHogsFragment.setArguments(mArgs);
		mHogsFragmentLabel = getString(R.string.tab_hogs);
		
		mSettingsSuggestionFragment = new SettingsSuggestionsFragment();
		mSettingsSuggestionFragmentLabel = getString(R.string.tab_settings);
		
		mCaratSettingsFragment = new CaratSettingsFragment();
		mCaratSettingsFragmentLabel = getString(R.string.tab_carat_settings);
		
		mAboutFragment = new AboutFragment();
		mAboutFragmentLabel = getString(R.string.tab_about);

		/*
		 * Before using the field "fullVersion", first invoke setTitleNormal()
		 * or setFullVersion() to set this field
		 */
		mTitle = mDrawerTitle = fullVersion;
		mDrawerItems = getResources().getStringArray(R.array.drawer_items);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		// set a custom shadow that overlays the main content when the drawer
		// opens
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
		 * try {
		 *  HelloServer h = new HelloServer();
		 * } catch (IOException e) {
		 *  e.printStackTrace(); 
		 * }
		 */
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
			// To remove the lag in closing the drawer, don't do a fragment transaction
			// while the drawer is getting closed. 
			// First close the drawer (takes about 300ms (transition/animation time)),  
			// wait for it to get closed completely, then start replacing the fragment .
			
			// How to modify the navigation drawer closing transition time: 
			// http://stackoverflow.com/questions/19460683/speed-up-navigation-drawer-animation-speed-on-closing
			
			mDrawerLayout.closeDrawer(mDrawerList);
	        new Handler().postDelayed(new Runnable() {
	            @Override
	            public void run() {
	            	selectItem(position);
	            }
	        }, 300);
		}
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
//		Fragment fragment = null;
//		String fragmentLabel = null;
//		Bundle args = new Bundle();
		
		// TODO: remove commented out code (if we decide to keep pre-initialization of fragments)
		switch (position) {
		case 0:
			replaceFragment(mSummaryFragment, mSummaryFragmentLabel);
//			fragment = new SummaryFragment();
//			args.putInt("wellbehaved", totalWellbehavedAppsCount);
//			args.putInt("hogs", totalHogsCount);
//			args.putInt("bugs", totalBugsCount);
//			fragment.setArguments(args);			
//			fragmentLabel = getString(R.string.tab_summary);
			break;
		case 1:
			replaceFragment(mSuggestionFragment, mSuggestionFragmentLabel);
//			fragment = new SuggestionsFragment();
//			fragmentLabel = getString(R.string.tab_actions);
			break;
		case 2:
			replaceFragment(mMyDeviceFragment, mMyDeviceFragmentLabel);
//			fragment = new MyDeviceFragment();
//			fragmentLabel = getString(R.string.tab_my_device);
			break;
		case 3:
			replaceFragment(mBugsFragment, mBugsFragmentLabel);
//			args.putBoolean("isBugs", true);
//			fragment = new BugsOrHogsFragment();
//			fragment.setArguments(args);
//			fragmentLabel = getString(R.string.tab_bugs);
			break;
		case 4:
			replaceFragment(mHogsFragment, mHogsFragmentLabel);
//			args.putBoolean("isBugs", false);
//			fragment = new BugsOrHogsFragment();
//			fragment.setArguments(args);
//			fragmentLabel = getString(R.string.tab_hogs);
			break;
		case 5:
			replaceFragment(mSettingsSuggestionFragment, mSettingsSuggestionFragmentLabel);
//			fragment = new SettingsSuggestionsFragment();
//			fragmentLabel = getString(R.string.tab_settings);
			break;
		case 6:
			replaceFragment(mCaratSettingsFragment, mCaratSettingsFragmentLabel);
//			fragment = new CaratSettingsFragment();
//			fragmentLabel = getString(R.string.tab_carat_settings);
			break;
		case 7:
			replaceFragment(mAboutFragment, mAboutFragmentLabel);
//			fragment = new AboutFragment();
//			fragmentLabel = getString(R.string.tab_about);
			break;
		}

//		replaceFragment(mFragment, mFragmentLabel);

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mDrawerItems[position]);
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
	
	public String getFulVersion()  {
		return fullVersion;
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
		
		// if we need to do something when our default shared preferences change, we can do it in this listener
		// when you uncomment this, remember to uncomment the unregistering code in the onStop() listener of this activity
		// PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
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
		// PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
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

		/*
		 * Thread for refreshing the UI with new reports every 5 mins and on
		 * resume. Also sends samples and updates blacklist/questionnaire url.
		 */

		Log.i(TAG, "Refreshing UI");
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
	 * shows the fragment using a fragment transaction (replaces the FrameLayout
	 * (a placeholder in the main activity's layout file) with the passed-in fragment)
	 * 
	 * @param fragment the fragment that should be shown
	 * 
	 * @param fragmentNameInBackStack a name for the fragment to be shown in the
	 * fragment (task) stack
	 */
	public void replaceFragment(Fragment fragment, String fragmentNameInBackStack) {
		// replace the fragment, using a fragment transaction
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.content_frame, fragment).addToBackStack(fragmentNameInBackStack).commit();
	}

	public void showHTMLFile(String fileName) {
		WebViewFragment fragment = WebViewFragment.getInstance(fileName);
		replaceFragment(fragment, fileName);
	}

	/**
	 * A listener that is triggered when a value changes in our defualtSharedPreferences.
	 * Can be used to do an immediate action whenever one of our items in that hashtable (defualtSharedPreferences) changes.
	 * Should be registered (in our main activity's onStart()) and unregistered (in onStop())  
	 */
//	private OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
//        @Override
//        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//            Toast.makeText(CaratApplication.getMainActivity(), String.valueOf(sharedPreferences.getBoolean(key, false)), Toast.LENGTH_SHORT).show();
//        }
//    };
}