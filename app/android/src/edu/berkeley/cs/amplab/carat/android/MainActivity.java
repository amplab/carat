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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CaratApplication.setMain(this);
		tracker = Tracker.getInstance();
		// track user clicks (taps)
		tracker.trackUser("caratstarted");

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
		
		preInittializeFragments();

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
		mDrawerToggle = new ActionBarDrawerToggle(
				this, /* host Activity */
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
	        }, 300); // wait 300ms before calling selectItem()
		}
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
		switch (position) {
		case 0:
			replaceFragment(mSummaryFragment, mSummaryFragmentLabel);
			break;
		case 1:
			replaceFragment(mSuggestionFragment, mSuggestionFragmentLabel);
			break;
		case 2:
			replaceFragment(mMyDeviceFragment, mMyDeviceFragmentLabel);
			break;
		case 3:
			replaceFragment(mBugsFragment, mBugsFragmentLabel);
			break;
		case 4:
			replaceFragment(mHogsFragment, mHogsFragmentLabel);
			break;
		case 5:
			replaceFragment(mSettingsSuggestionFragment, mSettingsSuggestionFragmentLabel);
			break;
		case 6:
			replaceFragment(mCaratSettingsFragment, mCaratSettingsFragmentLabel);
			break;
		case 7:
			replaceFragment(mAboutFragment, mAboutFragmentLabel);
			break;
		}

		// update selected item and title
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
		this.setTitle(getString(R.string.updating) + " " + what);
//		this.setTitle(fullVersion + " - " + getString(R.string.updating) + " " + what);
	}

	public void setTitleUpdatingFailed(String what) {
		this.setTitle(getString(R.string.didntget) + " " + what);
	}

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
		
		/* To perform an action when our defaultSharedPreferences changes, we can do it in this listener
		 * when you uncomment this, remember to uncomment the unregistering code in the onStop() method of this activity (right below)
		 */
		// PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(getApplicationContext());
		// PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "Resumed. Refreshing UI");
		tracker.trackUser("caratresumed");

		/*
		 * Thread for refreshing the UI with new reports every 5 mins and on
		 * resume. Also sends samples and updates blacklist/questionnaire url.
		 */
		// This spawns a thread, so it does not need to be in a thread.
		/*
		 * new Thread() { public void run() {
		 */
		
		((CaratApplication) getApplication()).refreshUi();
		
		/*
		 * } }.start();
		 */

		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "Paused");
		tracker.trackUser("caratpaused");
		SamplingLibrary.resetRunningProcessInfo();
		super.onPause();
	}

	@Override
	public void finish() {
		Log.d(TAG, "Finishing up");
		tracker.trackUser("caratstopped");
		super.finish();
	}

	/**
	 * must be called in onCreate() method of the activity, before calling selectItem() method
	 * [before attaching the navigation drawer listener]
	 * pre-initialize all fragments before committing a replace fragment transaction
	 * may help for better smoothness when user selects a navigation drawer item
	 */
	private void preInittializeFragments() {
		initSummaryFragment();
		initSuggestionsFragment();
		initMyDeviceFragment();
		initBugsOrHogsFragment(true);
		initBugsOrHogsFragment(false);
		initSettingsSuggestionFragment();
		initCaratSettingsFragment();
		initAboutFragment();
	}

	private void initSummaryFragment() {
		mSummaryFragment = new SummaryFragment();
		mArgs = new Bundle();
		
		/*
		 * Reading the arguments passed to the current activity from the in the SplashScreen
		 * These values have been fetched from the Carat statistics URL in the SplashScreen.
		 * These values should be read in onCreate() of the main activity
		 */
		Intent intent = getIntent();
		int totalWellbehavedAppsCount = Integer.parseInt(intent.getStringExtra("wellbehaved"));
		int totalHogsCount = Integer.parseInt(intent.getStringExtra("hogs"));
		int totalBugsCount = Integer.parseInt(intent.getStringExtra("bugs"));
		
		mArgs.putInt("wellbehaved", totalWellbehavedAppsCount);
		mArgs.putInt("hogs", totalHogsCount);
		mArgs.putInt("bugs", totalBugsCount);
		mSummaryFragment.setArguments(mArgs);
		mSummaryFragmentLabel = getString(R.string.tab_summary);
	}
	
	private void initSuggestionsFragment() {
		mSuggestionFragment = new SuggestionsFragment();
		mSuggestionFragmentLabel = getString(R.string.tab_actions);
	}
	
	private void initAboutFragment() {
		mAboutFragment = new AboutFragment();
		mAboutFragmentLabel = getString(R.string.tab_about);
	}

	private void initMyDeviceFragment() {
		mMyDeviceFragment = new MyDeviceFragment();
		mMyDeviceFragmentLabel = getString(R.string.tab_my_device);
	}
	
	private void initBugsOrHogsFragment(boolean bugsFragment) {
		mBugsFragment = new BugsOrHogsFragment();
		mArgs = new Bundle();
		mArgs.putBoolean("isBugs", bugsFragment);
		mBugsFragment.setArguments(mArgs);
		mBugsFragmentLabel = getString(R.string.tab_bugs);
		mHogsFragmentLabel = getString(R.string.tab_hogs);
	}
	
	private void initSettingsSuggestionFragment() {
		mSettingsSuggestionFragment = new SettingsSuggestionsFragment();
		mSettingsSuggestionFragmentLabel = getString(R.string.tab_settings);
	}
	
	private void initCaratSettingsFragment() {
		mCaratSettingsFragment = new CaratSettingsFragment();
		mCaratSettingsFragmentLabel = getString(R.string.tab_carat_settings);
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

	/**
	 * used by other classes
	 * @param fileName
	 */
	public void showHTMLFile(String fileName) {
		WebViewFragment fragment = WebViewFragment.getInstance(fileName);
		replaceFragment(fragment, fileName);
	}

	/**
	 * A listener that is triggered when a value changes in our defualtSharedPreferences.
	 * Can be used to do an immediate action whenever one of our items in that hashtable (defualtSharedPreferences) changes.
	 * Should be registered (in our main activity's onStart()) and unregistered (in onStop())  
	 */
	// private OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
    //      @Override
    //      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    //          Toast.makeText(CaratApplication.getMainActivity(), String.valueOf(sharedPreferences.getBoolean(key, false)), Toast.LENGTH_SHORT).show();
    //      }
    //  };
}