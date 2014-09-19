package edu.berkeley.cs.amplab.carat.android;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import com.flurry.android.FlurryAgent;
import edu.berkeley.cs.amplab.carat.android.protocol.ClickTracking;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar.Tab;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * Carat Android App Main Activity. Is loaded right after CaratApplication.
 * Holds the Tabs that comprise the UI. Place code related to tab handling and
 * global Activity code here.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class CaratMainActivity extends ActionBarActivity {
	
	
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
	    private Fragment mFragment;
	    private final Activity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public TabListener(Activity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	    }

	    /* The following are each of the ActionBar.TabListener callbacks */

	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        // Check if the fragment is already initialized
	        if (mFragment == null) {
	            // If not, instantiate and add it to the activity
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	            ft.add(android.R.id.content, mFragment, mTag);
	        } else {
	            // If it exists, simply attach it in order to show it
	            ft.attach(mFragment);
	        }
	    }

	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	        if (mFragment != null) {
	            // Detach the fragment, because another one is being attached
	            ft.detach(mFragment);
	        }
	    }

	    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	        // User selected the already selected tab. Usually do nothing.
	    }
	}	
	
	
    // Log tag
    private static final String TAG = "CaratMain";

    public static final String ACTION_BUGS = "bugs";
    public static final String ACTION_HOGS = "hogs";

    // 250 ms
    public static final long ANIMATION_DURATION = 250;

    // Hold the tabs of the UI.
//    TabHost mTabHost;
//    
//    TabManager mTabManager;

    // Key File
    private static final String FLURRY_KEYFILE = "flurry.properties";

    private MenuItem feedbackItem = null;

    private String fullVersion = null;

    /**
     * 
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
    	
    	ActionBar actionBar = getSupportActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(true);
	    Tab tab;
	    
	    tab = actionBar.newTab()
                .setText(R.string.tab_actions)
                .setTabListener(new TabListener<CaratSuggestionsFragment>(
                        this, "suggestions", CaratSuggestionsFragment.class));
	    actionBar.addTab(tab);
	    
	    tab = actionBar.newTab()
                .setText(R.string.tab_bugs)
                .setTabListener(new TabListener<CaratBugsOrHogsFragment>(
                        this, "bugs", CaratBugsOrHogsFragment.class));
	    actionBar.addTab(tab);
	    
	    tab = actionBar.newTab()
                .setText(R.string.tab_hogs)
                .setTabListener(new TabListener<CaratBugsOrHogsFragment>(
                        this, "hogs", CaratBugsOrHogsFragment.class));
	    actionBar.addTab(tab);
	    
	    tab = actionBar.newTab()
                .setText(R.string.tab_about)
                .setTabListener(new TabListener<CaratAboutFragment>(
                        this, "about", CaratAboutFragment.class));
	    actionBar.addTab(tab);
	    
	    tab = actionBar.newTab()
                .setText(R.string.greetings_artist)
                .setTabListener(new TabListener<ArtistFragment>(
                        this, "artist", ArtistFragment.class));
	    actionBar.addTab(tab);

	    tab = actionBar.newTab()
	                   .setText(R.string.greetings_album)
	                   .setTabListener(new TabListener<AlbumFragment>(
	                           this, "album", AlbumFragment.class));
	    actionBar.addTab(tab);
	    
//	    MyDevice fragment causes a crash
	    tab = actionBar.newTab()
                .setText(R.string.tab_my_device)
                .setTabListener(new TabListener<CaratMyDeviceFragment>(
                        this, "my device", CaratMyDeviceFragment.class));
	    actionBar.addTab(tab);
    	
	    // The following two lines of code cause a crash, fix them or delete them
	    
        // This does not show if it is not updated
//        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        
        
        fullVersion = getString(R.string.app_name) + " " + getString(R.string.version_name);

//        Resources res = getResources(); // Resource object to get Drawables
        
        /*// Bind animations to tab changes:
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
            String oldTag = tabHost.getCurrentTabTag();

            @Override
            public void onTabChanged(String tabId) {
                String tag = tabHost.getCurrentTabTag();

                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (p != null) {
                    String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
                    HashMap<String, String> options = new HashMap<String, String>();
                    options.put("from", oldTag);
                    options.put("to", tag);
                    options.put("status", getTitle().toString());
                    ClickTracking.track(uuId, "tabswitch", options, getApplicationContext());
                }
                oldTag = tag;
            }
        });*/

        setTitleNormal();

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (p != null) {
            String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("status", getTitle().toString());
            ClickTracking.track(uuId, "caratstarted", options, getApplicationContext());
        }
        
        
//        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
//        mTabHost.setup();
//        mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);
//        addTab(getString(R.string.tab_actions), CaratSuggestionsFragment.class);
//        addTab(getString(R.string.tab_my_device), CaratMyDeviceFragment.class);
//        addTab(getString(R.string.tab_bugs), CaratBugsOrHogsFragment.class);
//        addTab(getString(R.string.tab_hogs), CaratBugsOrHogsFragment.class);
//        addTab(getString(R.string.tab_about), CaratAboutFragment.class);
//        if (savedInstanceState != null) {
//            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
//        }
//        
//        mTabHost.setCurrentTab(0);
        // Uncomment the following to enable listening on local port 8080:
        /*
         * try { HelloServer h = new HelloServer(); } catch (IOException e) { //
         * TODO Auto-generated catch block e.printStackTrace(); }
         */
    }

//    private void addTab(String desc, Class frag) {
//        mTabManager.addTab(mTabHost.newTabSpec(desc).setIndicator(desc), frag, null);
//    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putString("tab", mTabHost.getCurrentTabTag());
//    }

    /**
     * This is a helper class that implements a generic mechanism for
     * associating fragments with the tabs in a tab host. It relies on a trick.
     * Normally a tab host has a simple API for supplying a View or Intent that
     * each tab will show. This is not sufficient for switching between
     * fragments. So instead we make the content part of the tab host 0dp high
     * (it is not shown) and the TabManager supplies its own dummy view to show
     * as the tab content. It listens to changes in tabs, and takes care of
     * switch to the correct fragment shown in a separate content area whenever
     * the selected tab changes.
     */
//    public static class TabManager implements TabHost.OnTabChangeListener {
//        private final FragmentActivity mActivity;
//        private final TabHost mTabHost;
//        private final int mContainerId;
//        private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
//        TabInfo mLastTab;
//
//        static final class TabInfo {
//            private final String tag;
//            private final Class<?> clss;
//            private final Bundle args;
//            private Fragment fragment;
//
//            TabInfo(String _tag, Class<?> _class, Bundle _args) {
//                tag = _tag;
//                clss = _class;
//                args = _args;
//            }
//        }
//
//        static class DummyTabFactory implements TabHost.TabContentFactory {
//            private final Context mContext;
//
//            public DummyTabFactory(Context context) {
//                mContext = context;
//            }
//
//            @Override
//            public View createTabContent(String tag) {
//                View v = new View(mContext);
//                v.setMinimumWidth(0);
//                v.setMinimumHeight(0);
//                return v;
//            }
//        }
//
//        public TabManager(FragmentActivity activity, TabHost tabHost, int containerId) {
//            mActivity = activity;
//            mTabHost = tabHost;
//            mContainerId = containerId;
//            mTabHost.setOnTabChangedListener(this);
//        }
//
//        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
//            tabSpec.setContent(new DummyTabFactory(mActivity));
//            String tag = tabSpec.getTag();
//            TabInfo info = new TabInfo(tag, clss, args);
//            // Check to see if we already have a fragment for this tab, probably
//            // from a previously saved state. If so, deactivate it, because our
//            // initial state is that a tab isn't shown.
//            info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tag);
//            if (info.fragment != null && !info.fragment.isDetached()) {
//                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
//                ft.detach(info.fragment);
//                ft.commit();
//            }
//            mTabs.put(tag, info);
//            mTabHost.addTab(tabSpec);
//        }
//
//        @Override
//        public void onTabChanged(String tabId) {
//            TabInfo newTab = mTabs.get(tabId);
//            if (mLastTab != newTab) {
//                FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
//                if (mLastTab != null) {
//                    if (mLastTab.fragment != null) {
//                        ft.detach(mLastTab.fragment);
//                    }
//                }
//                if (newTab != null) {
//                    if (newTab.fragment == null) {
//                        Bundle args = newTab.args;
//                        if (args == null)
//                            args = new Bundle();
//                        if (tabId.equals(mActivity.getString(R.string.tab_bugs)))
//                            args.putBoolean(CaratBugsOrHogsFragment.IS_BUGS, true);
//                        else if (tabId.equals(mActivity.getString(R.string.tab_hogs)))
//                            args.putBoolean(CaratBugsOrHogsFragment.IS_BUGS, false);
//                        newTab.fragment = Fragment.instantiate(mActivity, newTab.clss.getName(), args);
//                        ft.add(mContainerId, newTab.fragment, newTab.tag);
//                    } else {
//                        ft.attach(newTab.fragment);
//                    }
//                }
//                mLastTab = newTab;
//                ft.commit();
//                mActivity.getSupportFragmentManager().executePendingTransactions();
//            }
//        }
//    }

    public void setTitleNormal() {
        if (CaratApplication.s != null) {
            long s = CaratApplication.s.getSamplesReported();
            if (s > 0)
                this.setTitle(fullVersion + " - " + s + " " + getString(R.string.samplesreported));
            else
                this.setTitle(fullVersion);
        } else
            this.setTitle(fullVersion);
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
            InputStream raw = CaratMainActivity.this.getAssets().open(FLURRY_KEYFILE);
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

//    public void changeTab(int tab) {
//        if (mTabHost == null)
//            return;
//        if (mTabHost.getChildCount() > tab && tab >= 0)
//            mTabHost.setCurrentTab(tab);
//    }

    /**
     * Animation for sliding a screen in from the right.
     * 
     * @return
     */
    public static Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    {
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
    }

    /**
     * Animation for sliding a screen out to the left.
     * 
     * @return
     */
    public static Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    {
        outtoLeft.setDuration(ANIMATION_DURATION);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
    }

    /**
     * Animation for sliding a screen in from the left.
     * 
     * @return
     */
    public static Animation inFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    {
        inFromLeft.setDuration(ANIMATION_DURATION);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
    }

    /**
     * Animation for sliding a screen out to the right.
     * 
     * @return
     */

    public static Animation outtoRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
    {
        outtoRight.setDuration(ANIMATION_DURATION);
        outtoRight.setInterpolator(new AccelerateInterpolator());
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
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (p != null) {
            String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("status", getTitle().toString());
            ClickTracking.track(uuId, "caratresumed", options, getApplicationContext());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.ActivityGroup#onPause()
     */
    @Override
    protected void onPause() {
        Log.i(TAG, "Paused");
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (p != null) {
            String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("status", getTitle().toString());
            ClickTracking.track(uuId, "caratpaused", options, getApplicationContext());
        }
        SamplingLibrary.resetRunningProcessInfo();
        super.onPause();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#finish()
     */
//    @Override
//    public void finish() {
//        Log.d(TAG, "Finishing up");
//        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        if (p != null) {
//            String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
//            HashMap<String, String> options = new HashMap<String, String>();
//            options.put("status", getTitle().toString());
//            ClickTracking.track(uuId, "caratstopped", options, getApplicationContext());
//        }
//        super.finish();
//    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.ActivityGroup#onDestroy()
     */
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }

    /**
     * Show share, feedback, wifi only menu here.
     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        final MenuItem wifiOnly = menu.add(R.string.wifionly);
//        // wifiOnly.setCheckable(true);
//        // wifiOnly.setChecked(useWifiOnly);
//        final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(CaratMainActivity.this);
//        if (p.getBoolean(CaratApplication.PREFERENCE_WIFI_ONLY, false))
//            wifiOnly.setTitle(R.string.wifionlyused);
//        wifiOnly.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem arg0) {
//                boolean useWifiOnly = p.getBoolean(CaratApplication.PREFERENCE_WIFI_ONLY, false);
//                if (useWifiOnly) {
//                    p.edit().putBoolean(CaratApplication.PREFERENCE_WIFI_ONLY, false).commit();
//                    // wifiOnly.setChecked(false);
//                    wifiOnly.setTitle(R.string.wifionly);
//                    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                    if (p != null) {
//                        String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
//                        HashMap<String, String> options = new HashMap<String, String>();
//                        options.put("status", getTitle().toString());
//                        ClickTracking.track(uuId, "wifionlyoff", options, getApplicationContext());
//                    }
//                } else {
//                    p.edit().putBoolean(CaratApplication.PREFERENCE_WIFI_ONLY, true).commit();
//                    // wifiOnly.setChecked(true);
//                    wifiOnly.setTitle(R.string.wifionlyused);
//                    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                    if (p != null) {
//                        String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
//                        HashMap<String, String> options = new HashMap<String, String>();
//                        options.put("status", getTitle().toString());
//                        ClickTracking.track(uuId, "wifionlyon", options, getApplicationContext());
//                    }
//                }
//                return true;
//            }
//        });
//
//        MenuItem shareItem = menu.add(R.string.share);
//        shareItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem arg0) {
//                int jscore = CaratApplication.getJscore();
//                Intent sendIntent = new Intent(Intent.ACTION_SEND);
//                sendIntent.setType("text/plain");
//                sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.myjscoreis) + " " + jscore);
//                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharetext1) + " " + jscore
//                        + getString(R.string.sharetext2));
//                startActivity(Intent.createChooser(sendIntent, getString(R.string.sharewith)));
//                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                if (p != null) {
//                    String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
//                    HashMap<String, String> options = new HashMap<String, String>();
//                    options.put("status", getTitle().toString());
//                    options.put("sharetext", getString(R.string.myjscoreis) + " " + jscore);
//                    ClickTracking.track(uuId, "caratshared", options, getApplicationContext());
//                }
//                return true;
//            }
//        });
//
//        feedbackItem = menu.add(R.string.feedback);
//        feedbackItem.setOnMenuItemClickListener(new MenuListener());
//        return true;
//    }

    /**
     * Class to handle feedback form.
     * 
     * @author Eemil Lagerspetz
     * 
     */
//    private class MenuListener implements OnMenuItemClickListener {
//
//        @Override
//        public boolean onMenuItemClick(MenuItem arg0) {
//            int jscore = CaratApplication.getJscore();
//            Intent sendIntent = new Intent(Intent.ACTION_SEND);
//            Context a = getApplicationContext();
//            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(a);
//            String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
//            String os = SamplingLibrary.getOsVersion();
//            String model = SamplingLibrary.getModel();
//
//            // Emulator does not support message/rfc822
//            if (model.equals("sdk"))
//                sendIntent.setType("text/plain");
//            else
//                sendIntent.setType("message/rfc822");
//            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "carat@eecs.berkeley.edu" });
//            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "[carat] [Android] " + getString(R.string.feedbackfrom) + " "
//                    + model);
//            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.os) + ": " + os + "\n"
//                    + getString(R.string.model) + ": " + model + "\nCarat ID: " + uuId + "\nJ-Score: " + jscore + "\n"
//                    + fullVersion + "\n");
//            if (p != null) {
//                HashMap<String, String> options = new HashMap<String, String>();
//                options.put("os", os);
//                options.put("model", model);
//                SimpleHogBug[] b = CaratApplication.s.getBugReport();
//                int len = 0;
//                if (b != null)
//                    len = b.length;
//                options.put("bugs", len + "");
//                b = CaratApplication.s.getHogReport();
//                len = 0;
//                if (b != null)
//                    len = b.length;
//                options.put("hogs", len + "");
//                options.put("status", getTitle().toString());
//                options.put("sharetext", getString(R.string.myjscoreis) + " " + jscore);
//                ClickTracking.track(uuId, "feedbackbutton", options, getApplicationContext());
//            }
//            startActivity(Intent.createChooser(sendIntent, getString(R.string.chooseemail)));
//            return true;
//        }
//    }
}