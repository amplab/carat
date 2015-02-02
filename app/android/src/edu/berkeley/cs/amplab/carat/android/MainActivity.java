package edu.berkeley.cs.amplab.carat.android;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
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

	private CharSequence mTitle;
	private String[] mDrawerItems;

	// Log tag
	// private static final String TAG = "MainActivity";

	public static final String ACTION_BUGS = "bugs",
							   ACTION_HOGS = "hogs";

	// Key File
	private static final String FLURRY_KEYFILE = "flurry.properties";
	
	private String fullVersion = null;
	
	private Tracker tracker = null;
	
	/**
	 * Dynamic way of dealing with a list of fragments that you need to keep references for.
	 */
	private Fragment[] frags = new Fragment[CaratApplication.getTitles().length];
	
	private Bundle mArgs;

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
		mDrawerItems = getResources().getStringArray(R.array.drawer_items);
		
		List<Item> items = new ArrayList<Item>();
		
//		items.add(new NavDrawerListHeader("Main"));
		items.add(new ListItem(mDrawerItems[0]));
		items.add(new ListItem(mDrawerItems[1]));
		items.add(new ListItem(mDrawerItems[2]));
		items.add(new ListItem(mDrawerItems[3]));
		items.add(new ListItem(mDrawerItems[4]));
		items.add(new NavDrawerListHeader(""));
		items.add(new ListItem(mDrawerItems[5]));
		items.add(new ListItem(mDrawerItems[6]));
		
		TextArrayAdapter adapter = new TextArrayAdapter(this, items);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(adapter);
//		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerItems));
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
						//getSupportActionBar().setTitle(mTitle);
					}

					public void onDrawerOpened(View drawerView) {
						getSupportActionBar().setTitle(mTitle);
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
			
			// TODO: use a dynamic approach
			// consider headers when selecting an item
			final int newPosition = (position <= 4) ? position : position - 1;
			
			mDrawerLayout.closeDrawer(mDrawerList);
	        new Handler().postDelayed(new Runnable() {
	            @Override
	            public void run() {
	            	selectItem(newPosition);
	            }
	        }, 300); // wait 300ms before calling selectItem()
		}
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
		replaceFragment(frags[position], mDrawerItems[position], true);

		mDrawerList.setItemChecked(position, true);
	}
	
	/**
	 * 
	 * @param index 0-based index of the navigation drawer entries (e.g. 3 for bugs fragment)
	 * @return the tag of the fragment corresponding to the index
	 */
	public String getFragmentTag(int index) {
		return mDrawerItems[index];
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
		
		// If we will pop a top level screen, show drawer indicator again
		int stackTop = manager.getBackStackEntryCount()-1;
		
		BackStackEntry entry = manager.getBackStackEntryAt(stackTop);
		String name = entry.getName();
		String[] titles = CaratApplication.getTitles();
		boolean found = false;
		for (String t: titles){
			if (!found)
				found = t.equals(name);
		}
		if (found){
			// Restore menu
			mDrawerToggle.setDrawerIndicatorEnabled(true);
		}
		if (stackTop > 0 ) {
	        // If there are back-stack entries, replace the fragment (go to the fragment)
	        manager.popBackStack();
	    }else
	        finish();
	}
	
	@Override
	public void setTitle(CharSequence title) {
		getSupportActionBar().setTitle(title);
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
		
		// In case we are at a sublevel, enable going back by clicking title.
		if (item.getItemId() == android.R.id.home && !mDrawerToggle.isDrawerIndicatorEnabled()){
			onBackPressed();
			return true;
		}
		
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
				mTitle = fullVersion + " - "+ s + " " + getString(R.string.samplesreported);
			} else {
				mTitle = fullVersion;
			}
		}
		setTitle(mTitle);
	}
	
	private void setFullVersion() {
		fullVersion = getString(R.string.app_name) + " " + getString(R.string.version_name);
	}
	
	public String getFullVersion()  {
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
		int idx = 0;
		frags[idx] = new SummaryFragment();
		idx++;
		frags[idx] = new SuggestionsFragment();
		idx++;
		frags[idx] = new MyDeviceFragment();
		idx++;
		frags[idx] = new BugsOrHogsFragment();
		mArgs = new Bundle();
		mArgs.putBoolean("isBugs", true);
		frags[idx].setArguments(mArgs);
		idx++;
		frags[idx] = new BugsOrHogsFragment();
		mArgs = new Bundle();
		mArgs.putBoolean("isBugs", false);
		frags[idx].setArguments(mArgs);
		idx++;
		// enable later (after figuring out an approach for calculating the expected benefit number)
		// initSettingsSuggestionFragment();
		frags[idx] = new CaratSettingsFragment();
		idx++;
		frags[idx] = new AboutFragment();
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
	
	/*
	 * shows the fragment using a fragment transaction (replaces the FrameLayout
	 * (a placeholder in the main activity's layout file) with the passed-in fragment)
	 * 
	 * @param fragment the fragment that should be shown
	 * @param tag a name for the fragment to be shown in the
	 * fragment (task) stack
	 */
	public void replaceFragment(Fragment fragment, String tag, boolean showDrawerIndicator) {
		// replace the fragment, using a fragment transaction
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		// use a fragment tag, so that later on we can find the currently displayed fragment
		final String FRAGMENT_TAG = tag;
		
		transaction.replace(R.id.content_frame, fragment, FRAGMENT_TAG)
					.addToBackStack(FRAGMENT_TAG)
					.commit();
		mDrawerToggle.setDrawerIndicatorEnabled(showDrawerIndicator);
	}
	
	/**
	 * used by other classes
	 * @param fileName
	 */
	public void showHTMLFile(String fileName, String title, boolean showDrawerIndicator) {
		WebViewFragment fragment = WebViewFragment.getInstance(fileName);
		replaceFragment(fragment, title, showDrawerIndicator);
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
			int idx = 0;
			String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
			if (tag == mDrawerItems[idx]) {
				FragmentManager manager = getSupportFragmentManager();
				// Important: initialize the mSummaryFragment field here. In selectItem() method, when the user 
				// selects an item from the nav-drawer, we replace pre-init fragments including this one.
				frags[idx] = manager.findFragmentByTag(mDrawerItems[idx]); 
				
				FragmentTransaction fragTransaction = manager.beginTransaction();
				// refresh the summary fragment:
			    fragTransaction.detach(frags[idx]);
			    fragTransaction.attach(frags[idx]);
			    fragTransaction.commit();
			 } else {
				 frags[idx] = new SummaryFragment();
			 }
		} else {
			// Log.e(TAG, "refreshSummaryFragment(): stats data not avaiable!");
		}
	    // initSummaryFragment();
	}
	
	public Fragment getHogsFragment() {
		return frags[4];
	}

	public void setHogsFragment(Fragment hogsFragment) {
		frags[4] = hogsFragment;
	}

	public Fragment getBugsFragment() {
		return frags[3];
	}

	public void setBugsFragment(Fragment bugsFragment) {
		frags[3] = bugsFragment;
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
	
	/**
	 * Handle physical menu button (e.g. Samsung devices).
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
            if (drawerOpen) {
                mDrawerLayout.closeDrawers();
            } else {
            	// FIXME: Gravity.Start is not available in API Level 8, so hack below.
            	int grav = Gravity.TOP|Gravity.LEFT;
            	if (isRTL())
            		grav = Gravity.TOP|Gravity.RIGHT;
                mDrawerLayout.openDrawer(grav);
            }
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }
	
	private static boolean isRTL() {
	    return isRTL(Locale.getDefault());
	}

	private static boolean isRTL(Locale locale) {
	    final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
	    return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
	           directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
	}
}