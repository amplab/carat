package edu.berkeley.cs.amplab.carat.android;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.flurry.android.FlurryAgent;
import com.zubhium.ZubhiumSDK;

import edu.berkeley.cs.amplab.carat.android.protocol.CommsThread;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

/**
 * Carat Android App Main Activity. Is loaded right after CaratApplication.
 * Holds the Tabs that comprise the UI. Place code related to tab handling and
 * global Activity code here.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class CaratMainActivity extends TabActivity {
    // Log tag
    private static final String TAG = "CaratMain";

    public static final String ACTION_BUGS = "bugs";
    public static final String ACTION_HOGS = "hogs";

    // 250 ms
    public static final long ANIMATION_DURATION = 250;

    // Thread that sends samples when phone is woken up, GUI is started, or at
    // 15 min intervals.
    private CommsThread sampleSender = null;
    // private distanceThread distanceInfo = null;
    //private UiRefreshThread uiRefreshThread = null;

    // Hold the tabs of the UI.
    public static TabHost tabHost = null;

    // Zubhium SDK
    ZubhiumSDK sdk = null;

    // Key File
    private static final String ZUBHIUM_KEYFILE = "zubhium.properties";
    private static final String FLURRY_KEYFILE = "flurry.properties";

    private MenuItem feedbackItem = null;

    private String fullVersion = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If we want a progress bar for loading some screens at the top of the
        // title bar
        // This does not show if it is not updated
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.main);

        fullVersion = getString(R.string.app_name) + " "
                + getString(R.string.version_name);

        String secretKey = null;
        Properties properties = new Properties();
        try {
            InputStream raw = CaratMainActivity.this.getAssets().open(
                    ZUBHIUM_KEYFILE);
            if (raw != null) {
                properties.load(raw);
                if (properties.containsKey("secretkey"))
                    secretKey = properties
                            .getProperty("secretkey", "secretkey");
                Log.d(TAG, "Set secret key.");
            } else
                Log.e(TAG, "Could not open zubhium key file!");
        } catch (IOException e) {
            Log.e(TAG, "Could not open zubhium key file: " + e.toString());
        }
        if (secretKey != null) {
            sdk = ZubhiumSDK.getZubhiumSDKInstance(getApplicationContext(),
                    secretKey, fullVersion);
            sdk.registerUpdateReceiver(CaratMainActivity.this);
        }
        setTitleNormal();

        Resources res = getResources(); // Resource object to get Drawables
        tabHost = getTabHost(); // The activity TabHost
        TabHost.TabSpec spec; // Resusable TabSpec for each tab
        Intent intent; // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)

        // Initialize a TabSpec for each tab and add it to the TabHost
        intent = new Intent().setClass(this, CaratSuggestionsActivity.class);
        spec = tabHost
                .newTabSpec("actions")
                .setIndicator(getString(R.string.tab_actions),
                        res.getDrawable(R.drawable.ic_tab_actions))
                .setContent(intent);
        tabHost.addTab(spec);

        /*
         * intent = new Intent().setClass(this, SampleDebugActivity.class); spec
         * = tabHost .newTabSpec("Sample")
         * .setIndicator(getString(R.string.tab_sample),
         * res.getDrawable(R.drawable.ic_tab_actions)) .setContent(intent);
         * tabHost.addTab(spec);
         */
        intent = new Intent().setClass(this, CaratMyDeviceActivity.class);
        spec = tabHost
                .newTabSpec("mydevice")
                .setIndicator(getString(R.string.tab_my_device),
                        res.getDrawable(R.drawable.ic_tab_mydevice))
                .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, CaratBugsOrHogsActivity.class);
        intent.setAction(ACTION_BUGS);
        spec = tabHost
                .newTabSpec(ACTION_BUGS)
                .setIndicator(getString(R.string.tab_bugs),
                        res.getDrawable(R.drawable.ic_tab_bugs))
                .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, CaratBugsOrHogsActivity.class);
        intent.setAction(ACTION_HOGS);
        spec = tabHost.newTabSpec(ACTION_HOGS)
                .setIndicator("Hogs", res.getDrawable(R.drawable.ic_tab_hogs))
                .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, CaratAboutActivity.class);
        spec = tabHost
                .newTabSpec("about")
                .setIndicator("About", res.getDrawable(R.drawable.ic_tab_about))
                .setContent(intent);
        tabHost.addTab(spec);

        // Bind animations to tab changes:
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
            int oldTab = tabHost.getCurrentTab();

            @Override
            public void onTabChanged(String tabId) {
                int newTab = tabHost.getCurrentTab();
                View old = tabHost.getTabContentView().getChildAt(oldTab);
                View newView = tabHost.getTabContentView().getChildAt(newTab);
                Log.d("onTabChanged", "oldTab=" + oldTab + " old=" + old
                        + " newTabId=" + tabId + " newTab=" + newTab
                        + " newView=" + newView);
                /*
                 * if (old != null && newView != null) { if (oldTab < newTab) {
                 * old.setAnimation(outtoLeft);
                 * newView.setAnimation(inFromRight); } else {
                 * newView.setAnimation(inFromLeft);
                 * old.setAnimation(outtoRight); } }
                 */
                oldTab = newTab;
            }
        });

        tabHost.setCurrentTab(0);
    }

    public void setTitleNormal() {
        long s = CaratApplication.s.getSamplesReported();
        if (s > 0)
            this.setTitle(fullVersion + " - " + s + " samples reported");
        else
            this.setTitle(fullVersion);
    }

    public void setTitleUpdating(String what) {
        this.setTitle(fullVersion + " - Updating " + what);
    }

    public void setTitleUpdatingFailed(String what) {
        this.setTitle(fullVersion + " - got no " + what);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

        String secretKey = null;
        Properties properties = new Properties();
        try {
            InputStream raw = CaratMainActivity.this.getAssets().open(
                    FLURRY_KEYFILE);
            if (raw != null) {
                properties.load(raw);
                if (properties.containsKey("secretkey"))
                    secretKey = properties
                            .getProperty("secretkey", "secretkey");
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
        // TODO Auto-generated method stub
        super.onStop();
        FlurryAgent.onEndSession(getApplicationContext());
    }

    public static void changeTab(int tab) {
        tabHost.setCurrentTab(tab);
    }

    /**
     * Animation for sliding a screen in from the right.
     * 
     * @return
     */
    public static Animation inFromRight = new TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT,
            0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f);
    {
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
    }

    /**
     * Animation for sliding a screen out to the left.
     * 
     * @return
     */
    public static Animation outtoLeft = new TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
            -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f);
    {
        outtoLeft.setDuration(ANIMATION_DURATION);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
    }

    /**
     * Animation for sliding a screen in from the left.
     * 
     * @return
     */
    public static Animation inFromLeft = new TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT,
            0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f);
    {
        inFromLeft.setDuration(ANIMATION_DURATION);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
    }

    /**
     * Animation for sliding a screen out to the right.
     * 
     * @return
     */

    public static Animation outtoRight = new TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
            +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f);
    {
        outtoRight.setDuration(ANIMATION_DURATION);
        outtoRight.setInterpolator(new AccelerateInterpolator());
    }

    /**
     * 
     * Starts a Thread that communicates with the server to send stored samples.
     * TODO: latest sample for GUI usage.
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        Log.i(TAG, "Resumed");
        CaratApplication.setMain(this);
        // Thread for sending samples every 15 mins

        if (sampleSender == null) {
            sampleSender = new CommsThread((CaratApplication) getApplication());
            sampleSender.start();
        } else {
            Log.d("CaratMainActivity", "Resuming SampleSender");
            new Thread() {
                public void run() {
                    sampleSender.appResumed();
                }
            }.start();
        }

        /*
         * if (distanceInfo == null) { distanceInfo= new
         * distanceThread((CaratApplication) getApplication());
         * distanceInfo.start(); } else { Log.d("CaratMainActivity",
         * "Resuming location distance calculation!"); new Thread() { public
         * void run() { distanceInfo.appResumed(); } }.start(); }
         */
        // Thread for refreshing the UI with new reports every 5 mins and on
        // resume
        
        Log.d(TAG, "Refreshing UI");
        new Thread() {
            public void run() {
                ((CaratApplication) getApplication()).refreshUi();
            }
        }.start();
        super.onResume();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.ActivityGroup#onPause()
     */
    @Override
    protected void onPause() {
        Log.i(TAG, "Paused");
        SamplingLibrary.resetRunningProcessInfo();
        sampleSender.paused();
        super.onPause();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#finish()
     */
    @Override
    public void finish() {
        sampleSender.stopRunning();
        sampleSender.appResumed();
        // distanceInfo.appResumed();
        // distanceInfo.stopRunning();
        //uiRefreshThread.stopRunning();
        //uiRefreshThread.appResumed();

        Log.d(TAG, "Finishing up");
        super.finish();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.ActivityGroup#onDestroy()
     */
    @Override
    protected void onDestroy() {
        if (sdk != null)
            sdk.unRegisterUpdateReceiver();
        super.onDestroy();
    }

    /**
     * Show Zubhium menu here.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	/*MenuItem tweetItem = menu.add(R.string.tweet);
        tweetItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
            	String tweet = "My J-Score is "+ CaratApplication.getJscore() +"!\nWhat's yours?\nFind out here:";
            	String tweetUrl = "https://twitter.com/intent/tweet?text="+Uri.encode(tweet)+"&url="+Uri.encode("http://carat.cs.berkeley.edu/");
            	Uri uri = Uri.parse(tweetUrl);
            	startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            }
        });*/
        /*
        MenuItem facebookItem = menu.add(R.string.facebook);
        facebookItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                }
            }
        );*/
        
        MenuItem shareItem = menu.add(R.string.share);
        shareItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                int jscore = CaratApplication.getJscore();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "My J-Score is "+jscore);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Carat is improving my battery life. My J-Score is "+jscore +"!\nWhat's yours?\nFind out here: \nhttp://carat.cs.berkeley.edu/");
                startActivity(Intent.createChooser(sendIntent, "Share with:"));
                return true;
            }
        });
        
        feedbackItem = menu.add(R.string.feedback);
        feedbackItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                if (sdk != null)
                    sdk.openFeedbackDialog(CaratMainActivity.this);
                return true;
            }
        });
        return true;
    }

}