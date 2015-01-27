package edu.berkeley.cs.amplab.carat.android;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import edu.berkeley.cs.amplab.carat.android.fragments.AboutFragment;
import edu.berkeley.cs.amplab.carat.android.fragments.BugsOrHogsFragment;
import edu.berkeley.cs.amplab.carat.android.fragments.CaratSettingsFragment;
import edu.berkeley.cs.amplab.carat.android.fragments.EnableInternetDialogFragment;
import edu.berkeley.cs.amplab.carat.android.fragments.MyDeviceFragment;
import edu.berkeley.cs.amplab.carat.android.fragments.SuggestionsFragment;
import edu.berkeley.cs.amplab.carat.android.fragments.SummaryFragment;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.subscreens.WebViewFragment;
import edu.berkeley.cs.amplab.carat.android.utils.JsonParser;
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
	// private static final String TAG = "MainActivity";

	public static final String ACTION_BUGS = "bugs",
							   ACTION_HOGS = "hogs";

	// Key File
	private static final String FLURRY_KEYFILE = "flurry.properties";
	
	private String fullVersion = null;
	
	private Tracker tracker = null;

	private Fragment mSummaryFragment;
	private String mSummaryFragmentLabel;
	
	private Fragment mSuggestionFragment;
	private String mSuggestionFragmentLabel;
	
	private Fragment mMyDeviceFragment;
	private String mMyDeviceFragmentLabel;
	
	private Fragment mBugsFragment;	
	private String mBugsFragmentLabel;
	
	private Bundle mArgs;
	
	private Fragment mHogsFragment;			
	private String mHogsFragmentLabel;
	
	// private Fragment mSettingsSuggestionFragment;
	// private String mSettingsSuggestionFragmentLabel;
	
	private Fragment mCaratSettingsFragment;
	private String mCaratSettingsFragmentLabel;
	
	private Fragment mAboutFragment;
	private String mAboutFragmentLabel;

	// public boolean updateSummaryFragment;
	
	// counts (general Carat statistics shown in the summary fragment)
	public int mWellbehaved = Constants.VALUE_NOT_AVAILABLE,
			mHogs = Constants.VALUE_NOT_AVAILABLE,
			mBugs = Constants.VALUE_NOT_AVAILABLE ;
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CaratApplication.setMain(this);
		tracker = Tracker.getInstance();
		// track user clicks (taps)
		tracker.trackUser("caratstarted");
		
		if (!CaratApplication.isInternetAvailable()) {
			EnableInternetDialogFragment dialog = new EnableInternetDialogFragment();
			dialog.show(getSupportFragmentManager(), "dialog");
		}

		/*
		 * Activity.getWindow.requestFeature() should get invoked only before
		 * setContentView(), otherwise it will cause an app crash The progress
		 * bar doesn't get displayed when there is no update in progress
		 */
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		
		// Log.d(TAG, "about to set the layout");
		setContentView(R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();
		setTitleNormal();
		
		// read and load the preferences specified in our xml preference file
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		// Log.d(TAG, "about to initialize fragments");
		preInittializeFragments();
		// Log.d(TAG, "done with fragment initialization");
		
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
		// Before enabling the following lines, first add an item (in the correct order) to the String-array
		// in res/values/Strings.xml (and other translation string files)
		// case 5:
		//	replaceFragment(mSettingsSuggestionFragment, mSettingsSuggestionFragmentLabel);
		//	break;
		case 5:
			replaceFragment(mCaratSettingsFragment, mCaratSettingsFragmentLabel);
			break;
		case 6:
			replaceFragment(mAboutFragment, mAboutFragmentLabel);
			break;
		}

		// update selected item and title
		mDrawerList.setItemChecked(position, true);
		setTitle(mDrawerItems[position]);
	}

	/**
	 * Avoid displaying a white screen when the back button is pressed in the summary fragment.
	 * When we are in the summary fragment, since there is only one fragment in the backstack,
	 * the fragment manager will fail to pop another fragment from the backstack,
	 * so only the framelayout (the parent/host widget for fragments (in our activity's layout)) is shown.
	 * We need to check the number of fragments present in the backstack, and act accordingly
	 */
	@Override
	public void onBackPressed() {
		FragmentManager manager = getSupportFragmentManager();
		if (manager.getBackStackEntryCount() > 1 ) {
	        // If there are back-stack entries, replace the fragment (go to the fragment)
	        manager.popBackStack();
	    } else {
	    	// if there is only one entry in the backstack, show the home screen
	    	moveTaskToBack(true);
	    }
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
				setTitle("Carat - " + s + " " + getString(R.string.samplesreported));
			} else {
				setTitle(fullVersion);
			}
		} else
			setTitle(fullVersion);
	}

	private void setFullVersion() {
		fullVersion = getString(R.string.app_name) + " " + getString(R.string.version_name);
	}
	
	public String getFulVersion()  {
		return fullVersion;
	}

	public void setTitleUpdating(String what) {
		setTitle(getString(R.string.updating) + " " + what);
	}

	public void setTitleUpdatingFailed(String what) {
		setTitle(getString(R.string.didntget) + " " + what);
	}
	
	/**
	 * Used in the system settings fragment and the summary fragment
	 * @param intentString
	 * @param thing
	 */
	public void safeStart(String intentString, String thing) {
        Intent intent = null;
        try {
            intent = new Intent(intentString);
            startActivity(intent);
        } catch (Throwable th) {
            // Log.e(TAG, "Could not start activity: " + intent, th);
            if (thing != null) {
                Toast t = Toast.makeText(this, getString(R.string.opening) + thing + getString(R.string.notsupported),
                        Toast.LENGTH_SHORT);
                t.show();
            }
        }
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
				// Log.d(TAG, "Set Flurry secret key.");
			} else {
			  // Log.e(TAG, "Could not open Flurry key file!");
			}
		} catch (IOException e) {
			// Log.e(TAG, "Could not open Flurry key file: " + e.toString());
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
		// Log.i(TAG, "Resumed. Refreshing UI");
		tracker.trackUser("caratresumed");

		// if statistics data for the summary fragment is not already fetched,
		// and the device has an Internet connection, fetch statistics and then refresh the summary fragment
		if ( (! isStatsDataAvailable()) && CaratApplication.isInternetAvailable()) {
			getStatsFromServer();
			refreshSummaryFragment();
		}
				
		((CaratApplication) getApplication()).refreshUi();

		super.onResume();
	}

	public Fragment getVisibleFragment(){
	    FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
	    List<Fragment> fragments = fragmentManager.getFragments();
	    for(Fragment fragment : fragments){
	        if(fragment != null && fragment.isVisible())
	            return fragment;
	    }
	    return null;
	}
	
	@Override
	protected void onPause() {
		// Log.i(TAG, "Paused");
		tracker.trackUser("caratpaused");
		SamplingLibrary.resetRunningProcessInfo();
		super.onPause();
	}

	@Override
	public void finish() {
		// Log.d(TAG, "Finishing up");
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
		getStatsFromServer();

		// after fetching the data needed by the summary fragment, initialize it
		initSummaryFragment();
		initSuggestionsFragment();
		initMyDeviceFragment();
		initBugsOrHogsFragment(true);
		initBugsOrHogsFragment(false);
		// enable later (after figuring out an approach for calculating the expected benefit number)
		// initSettingsSuggestionFragment();
		initCaratSettingsFragment();
		initAboutFragment();
	}

	/**
	 * Before initializing the summary fragment, we need to fetch the the data it needs from our server,
	 * in an asyncTask in a new thread
	 */
	@SuppressLint("NewApi")
	private void getStatsFromServer() {
		PrefetchData prefetchData = new PrefetchData();
		// run this asyncTask in a new thread [from the thread pool] (run in parallel to other asyncTasks)
		// (do not wait for them to finish, it takes a long time)
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
			prefetchData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		 else
			 prefetchData.execute();
	}

	private void initSummaryFragment() {
		mSummaryFragment = new SummaryFragment();
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
		mArgs = new Bundle();
		mArgs.putBoolean("isBugs", bugsFragment);
		if (bugsFragment) {
			mBugsFragment = new BugsOrHogsFragment();
			mBugsFragment.setArguments(mArgs);
			mBugsFragmentLabel = getString(R.string.tab_bugs);
		} else {
			mHogsFragment = new BugsOrHogsFragment();
			mHogsFragment.setArguments(mArgs);
			mHogsFragmentLabel = getString(R.string.tab_hogs);
		}
	}
	
	// private void initSettingsSuggestionFragment() {
	//  	mSettingsSuggestionFragment = new SettingsSuggestionsFragment();
	//  	mSettingsSuggestionFragmentLabel = getString(R.string.tab_settings);
	//}
	
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
		// use a fragment tag, so that later on we can find the currently displayed fragment
		final String FRAGMENT_TAG = fragmentNameInBackStack;
		
		// String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
		// if (tag == mSummaryFragmentLabel) {		
		//   update summary fragment's view (after enabling Internet)
		// if (FRAGMENT_TAG == mSummaryFragmentLabel) {
		//	  transaction.detach(fragment);
		//	  transaction.attach(fragment);
		//}
		
		transaction.replace(R.id.content_frame, fragment, FRAGMENT_TAG)
					.addToBackStack(fragmentNameInBackStack)
					.commit();
	}

	/**
	 * used by other classes
	 * @param fileName
	 */
	public void showHTMLFile(String fileName) {
		WebViewFragment fragment = WebViewFragment.getInstance(fileName);
		replaceFragment(fragment, fileName);
	}

	public boolean isStatsDataAvailable() {
		if (isStatsDataLoaded()) {
			// Log.i(TAG, "isStatsDataAvailable(), mWellbehaved=" + mWellbehaved + ", mHogs=" + mHogs + ", mBugs=" + mBugs);
			return true;
		} else {
			return isStatsDataStoredInPref();
		}
	}

	private boolean isStatsDataLoaded() {
		// don't check for zero, check for something unlikely, e.g. -1 (use a constant for that value, use it consistently)
		return mWellbehaved != Constants.VALUE_NOT_AVAILABLE && mHogs != Constants.VALUE_NOT_AVAILABLE  && mBugs != Constants.VALUE_NOT_AVAILABLE;
	}

	private boolean isStatsDataStoredInPref() {
		// TODO: consider a data freshness timeout (e.g. two weeks)
		int wellbehaved = CaratApplication.mPrefs.getInt(Constants.STATS_WELLBEHAVED_COUNT_PREFERENCE_KEY, Constants.VALUE_NOT_AVAILABLE);
		int hogs = CaratApplication.mPrefs.getInt(Constants.STATS_HOGS_COUNT_PREFERENCE_KEY, Constants.VALUE_NOT_AVAILABLE);
		int bugs = CaratApplication.mPrefs.getInt(Constants.STATS_BUGS_COUNT_PREFERENCE_KEY, Constants.VALUE_NOT_AVAILABLE);
		if (wellbehaved != Constants.VALUE_NOT_AVAILABLE && hogs != Constants.VALUE_NOT_AVAILABLE  && bugs != Constants.VALUE_NOT_AVAILABLE) {
			// Log.i(TAG, "isStatsDataAvailable(), wellbehaved (fetched from the pref)=" + wellbehaved);
			mWellbehaved = wellbehaved;
			mHogs = hogs;
			mBugs = bugs;
			return true;
		} else {
			return false;
		}
	}
	
	public void GoToWifiScreen() {
    	safeStart(android.provider.Settings.ACTION_WIFI_SETTINGS, getString(R.string.wifisettings));
    }
	
	public void refreshSummaryFragment() {
		if (isStatsDataAvailable()) { // blank summary fragment already attached. detach and attach for refresh. 
			// Log.d(TAG, "data for summary fragment is available. Wellbehaved=" + mWellbehaved + ", hogs=" + mHogs + ", bugs=" + mBugs);
			
			 String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
			 if (tag == mSummaryFragmentLabel) {
					FragmentManager manager = getSupportFragmentManager();
					
					// Important: initialize the mSummaryFragment field here. In selectItem() method, when the user 
					// selects an item from the nav-drawer, we replace pre-init fragments including this one.
					mSummaryFragment = manager.findFragmentByTag("Summary"); 
					
					FragmentTransaction fragTransaction = manager.beginTransaction();
					// refresh the summary fragment:
				    fragTransaction.detach(mSummaryFragment);
				    fragTransaction.attach(mSummaryFragment);
				    fragTransaction.commit();
			 } else {
				 initSummaryFragment();
			 }
			
			
		} else {
			// Log.e(TAG, "refreshSummaryFragment(): stats data not avaiable!");
		}
	    // initSummaryFragment();
	}
	
	public Fragment getHogsFragment() {
		return mHogsFragment;
	}

	public void setHogsFragment(Fragment hogsFragment) {
		mHogsFragment = hogsFragment;
	}

	public Fragment getBugsFragment() {
		return mBugsFragment;
	}

	public void setBugsFragment(Fragment bugsFragment) {
		mBugsFragment = bugsFragment;
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
	
	public class PrefetchData extends AsyncTask<Void, Void, Void> {

		String serverResponseJson = null;
		private final String TAG = "PrefetchData";
		
		@Override
	    protected Void doInBackground(Void... arg0) {
	    	// Log.d(TAG, "started doInBackground() method of the asyncTask");
	        JsonParser jsonParser = new JsonParser();
	        try {
	        	if (CaratApplication.isInternetAvailable()) {
	        		serverResponseJson = jsonParser
	        				.getJSONFromUrl("http://carat.cs.helsinki.fi/statistics-data/stats.json");
	        	}
	        } catch (Exception e) {
	        }
	        
	        if (serverResponseJson != null && serverResponseJson != "") {
	            try {
	                JSONArray jsonArray = new JSONObject(serverResponseJson).getJSONArray("android-apps");
	                // Using Java reflections to set fields by passing their name to a method
	                try {
						setIntFieldsFromJson(jsonArray, 0, "mWellbehaved");
						setIntFieldsFromJson(jsonArray, 1, "mHogs");
						setIntFieldsFromJson(jsonArray, 2, "mBugs");
						
						if (CaratApplication.mPrefs != null) {
							saveStatsToPref();
						} else {
							// Log.e(TAG, "The shared preference is null (not loaded yet. "
							//		+ "Check CaratApplication's new thread for loading the sharedPref)");
						}
						
						// Log.i(TAG, "received JSON: " + "mBugs: " + mWellbehaved 
						//		+ ", mHogs: " + mHogs + ", mBugs: " + mBugs);
					} catch (IllegalArgumentException e) {
						Log.e(TAG, "IllegalArgumentException in setFieldsFromJson()");
					} catch (IllegalAccessException e) {
						Log.e(TAG, "IllegalAccessException in setFieldsFromJson()");
					}
	            } catch (JSONException e) {
	            	// Log.e(TAG, e.getStackTrace().toString());
	            }
	        } else {
	        	// Log.d(TAG, "server response JSON is null.");
	        }
	        return null;
	    }

		@SuppressLint("NewApi")
		private void saveStatsToPref() {
			SharedPreferences.Editor editor = CaratApplication.mPrefs.edit();
			// the returned values (from setIntFieldsFromJson()
			// might be -1 (Constants.VALUE_NOT_AVAILABLE). So
			// when we are reading the following pref values, we
			// should check that condition )
			editor.putInt(Constants.STATS_WELLBEHAVED_COUNT_PREFERENCE_KEY, mWellbehaved);
			editor.putInt(Constants.STATS_HOGS_COUNT_PREFERENCE_KEY, mHogs);
			editor.putInt(Constants.STATS_BUGS_COUNT_PREFERENCE_KEY, mBugs);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				editor.apply(); // async (runs in parallel
								// in a new shared thread (off the UI thread)
			} else {
				editor.commit();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			// Log.d(TAG, "started the onPostExecute() of the asyncTask");
			super.onPostExecute(result);
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			// sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			// Log.d(TAG, "asyncTask.onPstExecute(). mWellbehaved=" + mWellbehaved);
			refreshSummaryFragment();
		}
	    
	    /**
	     * Using Java reflections to set fields by passing their name to a method
	     * @param jsonArray the json array from which we want to extract different json objects
	     * @param objIdx the index of the object in the json array
	     * @param fieldName the name of the field in the current NESTED class (PrefetchData)
	     */
		private void setIntFieldsFromJson(JSONArray jsonArray, int objIdx, String fieldName) 
				throws JSONException, IllegalArgumentException, IllegalAccessException {
			// Class<? extends PrefetchData> currentClass = this.getClass();
			Field field = null;
			int res = Constants.VALUE_NOT_AVAILABLE;
			
			try {
				// important: getField() can only get PUBLIC fields. 
				// For private fields, use another method: getDeclaredField(fieldName)
				field = /*currentClass.*/ CaratApplication.getMainActivity().getClass().getField(fieldName);
			} catch(NoSuchFieldException e) {
				// Log.e(TAG, "NoSuchFieldException when trying to get a reference to the field: " + fieldName);
			}
			
			if (field != null) {
				JSONObject jsonObject = null;
				if (jsonArray != null ) {
					jsonObject = jsonArray.getJSONObject(objIdx);
					if (jsonObject != null && jsonObject.getString("value") != null && jsonObject.getString("value") != "") {
						res = Integer.parseInt(jsonObject.getString("value"));
						field.set(CaratApplication.getMainActivity()/*this*/, res);
					} else { 
						// Log.e(TAG, "json object (server response) is null: jsonArray(" + objIdx + ")=null (or ='')");
					}
				}
			}
			// if an exception occurs, the value of the field would be -1 (Constants.VALUE_NOT_AVAILABLE)
		}
	}
}