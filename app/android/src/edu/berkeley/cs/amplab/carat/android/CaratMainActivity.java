package edu.berkeley.cs.amplab.carat.android;

import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.thrift.TException;

import edu.berkeley.cs.amplab.carat.android.storage.CaratSampleDB;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.app.AlarmManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class CaratMainActivity extends TabActivity {
    private static final String TAG = "CaratMain";

    private CommsThread sampleSender = null;

    static TabHost tabHost = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If we want a progress bar for loading some screens at the top of the
        // title bar
        // This does not show if it is not updated
        // getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.main);
        this.setTitle(getString(R.string.app_name) + " "
                + getString(R.string.version_name));

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

        intent = new Intent().setClass(this, SampleDebugActivity.class);
        spec = tabHost
                .newTabSpec("Sample")
                .setIndicator(getString(R.string.tab_sample),
                        res.getDrawable(R.drawable.ic_tab_actions))
                .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, CaratMyDeviceActivity.class);
        spec = tabHost
                .newTabSpec("mydevice")
                .setIndicator(getString(R.string.tab_my_device),
                        res.getDrawable(R.drawable.ic_tab_mydevice))
                .setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, CaratBugsActivity.class);
        spec = tabHost
                .newTabSpec("bugs")
                .setIndicator(getString(R.string.tab_bugs),
                        res.getDrawable(R.drawable.ic_tab_bugs))
                .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, CaratHogsActivity.class);
        spec = tabHost.newTabSpec("hogs")
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
                Log.i("onTabChanged", "oldTab=" + oldTab + " old=" + old
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

    public static void changeTab(int tab) {
        tabHost.setCurrentTab(tab);
    }

    // 250 ms
    public static final long ANIMATION_DURATION = 250;

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
        if (sampleSender == null) {
            sampleSender = new CommsThread();
            sampleSender.start();
        } else{
            Log.i("CaratMainActivity", "Resuming SampleSender");
            new Thread(){ public void run(){
            sampleSender.appResumed();}}.start();
        }
        super.onResume();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#finish()
     */
    @Override
    public void finish() {
        sampleSender.stopRunning();
        synchronized (sampleSender) {
            sampleSender.notify();
        }
        Log.i(TAG, "Finishing up");
        super.finish();
    }

    // 15 minutes
    public static final long COMMS_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    // wait 10 seconds for wifi etc to come up
    public static final long COMMS_WIFI_WAIT = 10 * 1000;
    public static final int COMMS_MAX_UPLOAD_BATCH = 10;

    /**
     * Communicates with the Carat Server. Sends samples stored in CaratDB every
     * COMMS_INTERVAL ms.
     * 
     * @author Eemil Lagerspetz
     * 
     */
    private class CommsThread extends Thread {
        private boolean isRunning = true;

        private static final String TRY_AGAIN = " will try again in "
                + (COMMS_INTERVAL / 1000) + "s.";

        public void stopRunning() {
            isRunning = false;
        }

        public void appResumed() {
            synchronized (CommsThread.this) {
                CommsThread.this.interrupt();
            }
        }

        public void run() {
            boolean connecting = false;
            Log.i("CommsThread", "Sample sender started.");
            CaratApplication app = (CaratApplication) getApplication();
            Context c = getApplicationContext();
            while (isRunning) {
                String networkStatus = SamplingLibrary.getNetworkStatus(c);
                if (networkStatus == SamplingLibrary.NETWORKSTATUS_CONNECTED) {
                    SortedMap<Long, Sample> map = CaratSampleDB.getInstance(c)
                            .queryOldestSamples(COMMS_MAX_UPLOAD_BATCH);
                    if (map.size() > 0) {
                        try {
                            StringBuilder timestamps = new StringBuilder();
                            for (Entry<Long, Sample> entry : map.entrySet()) {
                                timestamps.append(" "
                                        + entry.getValue().getTimestamp());
                            }
                            boolean success = app.c.uploadSamples(map.values());
                            if (success) {
                                Log.i("CommsThread", "Uploaded " + map.size()
                                        + " samples, timestamps:" + timestamps);
                                Sample last = map.get(map.lastKey());
                                Log.i("CommsThread",
                                        "Deleting " + map.size()
                                                + " samples older than "
                                                + last.getTimestamp());
                                int deleted = CaratSampleDB.getInstance(c)
                                        .deleteSamples(map.keySet());
                                /*
                                 * .deleteOldestSamples( last.getTimestamp());
                                 */
                                Log.i("CommsThread", "Deleted " + deleted
                                        + " samples.");
                            }
                        } catch (TException e1) {
                            Log.w("CommsThread", "Failed to send samples,"
                                    + TRY_AGAIN);
                            e1.printStackTrace();
                        }
                    } else {
                        Log.w("CommsThread", "No samples to send." + TRY_AGAIN);
                    }
                } else if (networkStatus
                        .equals(SamplingLibrary.NETWORKSTATUS_CONNECTING)) {
                    Log.w("CommsThread", "Network status: " + networkStatus
                            + ", trying again in 10s.");
                    connecting = true;
                } else {
                    Log.w("CommsThread", "Network status: " + networkStatus
                            + TRY_AGAIN);
                }
                if (connecting) {
                    // wait for wifi to come up
                    try {
                        sleep(COMMS_WIFI_WAIT);
                    } catch (InterruptedException e1) {
                        // ignore
                    }
                    connecting = false;
                } else {
                    try {
                        sleep(COMMS_INTERVAL);
                    } catch (InterruptedException e) {
                        // wait for wifi to come up
                        try {
                            sleep(COMMS_WIFI_WAIT);
                        } catch (InterruptedException e1) {
                            // ignore
                        }
                    }
                }
            }
            Log.i("CommsThread", "Sample sender stopped.");
        }
    }
}