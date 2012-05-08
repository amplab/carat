package edu.berkeley.cs.amplab.carat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.cs.amplab.carat.protocol.CommunicationManager;
import edu.berkeley.cs.amplab.carat.sampling.Sampler;
import edu.berkeley.cs.amplab.carat.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.storage.CaratDataStorage;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.widget.TextView;

/**
 * Application class for Carat Android App. Place App-global static constants
 * and methods here.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class CaratApplication extends Application {

    // Sample 1 min since application start, then at 15 min intervals
    public static final long FIRST_SAMPLE_DELAY_MS = 60 * 1000;
    public static final long SAMPLE_INTERVAL_MS = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    // public static final long SAMPLE_INTERVAL_MS = FIRST_SAMPLE_DELAY_MS;

    // Alarm event for sampling when battery has not changed for
    // SAMPLE_INTERVAL_MS.
    public static final String ACTION_CARAT_SAMPLE = "edu.berkeley.cs.amplab.carat.android.ACTION_SAMPLE";
    // If true, install Sampling events to occur at boot. Currently not used.
    public static final String PREFERENCE_SAMPLE_FIRST_RUN = "carat.sample.first.run";

    // Report Freshness timeout. Default: 5 minutes
    public static final long FRESHNESS_TIMEOUT = 300000L;
    // If true, register this as a new device on the Carat server
    public static final String PREFERENCE_FIRST_RUN = "carat.first.run";

    // Send samples every 15 minutes
    public static final long COMMS_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    // When waking up from screen off, wait 10 seconds for wifi etc to come up
    public static final long COMMS_WIFI_WAIT = 10 * 1000;
    // Send up to 10 samples at a time
    public static final int COMMS_MAX_UPLOAD_BATCH = 10;

    // NOTE: This needs to be initialized before CommunicationManager.
    public CaratDataStorage s = null;
    // NOTE: The CommunicationManager requires a working instance of
    // CaratDataStorage.
    public CommunicationManager c = null;

    private static CaratMainActivity main = null;
    private static CaratMyDeviceActivity myDevice = null;

    // TODO: This may not be the best place for the icon map.
    private Map<String, Drawable> appToIcon = new HashMap<String, Drawable>();
    private Map<String, String> appToLabel = new HashMap<String, String>();
    private static final Map<Integer, String> importanceToString = new HashMap<Integer, String>();
    {
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_EMPTY,
                "Not running");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_BACKGROUND,
                "Background process");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_SERVICE,
                "Service");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_VISIBLE,
                "Visible task");
        importanceToString.put(RunningAppProcessInfo.IMPORTANCE_FOREGROUND,
                "Foreground app");
    }

    public static String importanceString(int importance) {
        return importanceToString.get(importance);
    }

    // default icon:
    public static String CARAT_PACKAGE = "edu.berkeley.cs.amplab.carat.android";

    /*
     * FIXME: Storing and retrieving totalAndused here only for testing. They
     * should really be stored in CaratDB and retrieved as part of sampling.
     */
    public int[] totalAndUsed = null;
    /*
     * FIXME: Storing and retrieving CPU here only for testing. It should really
     * be stored in CaratDB and retrieved as part of sampling.
     */
    public int cpu = 0;

    // Utility methods

    /**
     * Return a Drawable that contains an app icon for the named app. If not
     * found, return the Drawable for the Carat icon.
     * 
     * @param appName
     *            the application name
     * @return the Drawable for the application's icon
     */
    public Drawable iconForApp(String appName) {
        if (appToIcon.containsKey(appName))
            return appToIcon.get(appName);
        else
            return appToIcon.get(CARAT_PACKAGE);
    }

    /**
     * Return a human readable application label for the named app. If not
     * found, return appName.
     * 
     * @param appName
     *            the application name
     * @return the human readable application label
     */
    public String labelForApp(String appName) {
        if (appToLabel.containsKey(appName))
            return appToLabel.get(appName);
        else
            return appName;
    }

    public static void setText(final int viewId, final String text) {
        main.runOnUiThread(new Runnable() {
            public void run() {
                if (myDevice != null) {
                    TextView t = (TextView) myDevice.findViewById(viewId);
                    if (t != null)
                        t.setText(text);
                }
            }
        });
    }

    public static void setMain(CaratMainActivity a) {
        main = a;
    }

    public static void setMyDevice(CaratMyDeviceActivity a) {
        myDevice = a;
    }

    // Application overrides

    /**
     * 1. Create CaratDataStorage and read reports from disk. Does not seem too
     * slow.
     * 
     * 2. Take a sample in a new thread so that the GUI has fresh data TODO:
     * Sampling is currently delayed until we get battery stats. What to do on
     * the first time?
     * 
     * 3. Create CommunicationManager for communicating with the Carat server
     * TODO: Uses fake data at the moment. TODO: When and by which class to
     * record UUID, OS, MODEL for this?
     * 
     * 4. Communicate with the server to fetch new reports if current ones are
     * outdated, and to send old stored and the new just-recorded sample. See
     * CaratMainActivity for this task. TODO: latest sample for GUI usage.
     */
    @Override
    public void onCreate() {
        s = new CaratDataStorage(this);

        new Thread() {
            public void run() {
                totalAndUsed = SamplingLibrary.readMeminfo();
                cpu = (int) (SamplingLibrary.readUsage() * 100);

                /*
                 * Schedule recurring sampling event:
                 */
                SharedPreferences p = PreferenceManager
                        .getDefaultSharedPreferences(CaratApplication.this);
                boolean firstRun = p.getBoolean(PREFERENCE_SAMPLE_FIRST_RUN,
                        true);
                // do this always for now for debugging purposes:
                // if (firstRun) {
                // What to start when the event fires (this is unused at the
                // moment)
                Intent intent = new Intent(getApplicationContext(),
                        Sampler.class);
                intent.setAction(ACTION_CARAT_SAMPLE);
                // In reality, you would want to have a static variable for the
                // request
                // code instead of 192837
                PendingIntent sender = PendingIntent.getBroadcast(
                        CaratApplication.this, 192837, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                // Get the AlarmManager service
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                // we probably don't want the wakeup, but do battery events
                // still get delivered?
                am.setInexactRepeating(AlarmManager.RTC,
                        System.currentTimeMillis() + FIRST_SAMPLE_DELAY_MS,
                        SAMPLE_INTERVAL_MS, sender);

                // p.edit().putBoolean(PREFERENCE_SAMPLE_FIRST_RUN,
                // false).commit();
                // }
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                registerReceiver(new Sampler(), intentFilter);
            }
        }.start();

        new Thread() {
            public void run() {
                c = new CommunicationManager(CaratApplication.this);
            }
        }.start();

        /*
         * Note: this needs to be done in the main thread because icons are used
         * right away in the suggestions list.
         */
        List<android.content.pm.PackageInfo> packagelist = getPackageManager()
                .getInstalledPackages(0);
        for (android.content.pm.PackageInfo pak : packagelist) {
            String pname = pak.applicationInfo.packageName;
            String procname = pak.applicationInfo.processName;
            String label = getPackageManager().getApplicationLabel(
                    pak.applicationInfo).toString();
            Drawable icon = pak.applicationInfo.loadIcon(getPackageManager());
            appToLabel.put(pname, label);
            appToIcon.put(pname, icon);
            // Fallback / process list
            appToLabel.put(procname, label);
            appToIcon.put(procname, icon);
        }

        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        // TODO Auto-generated method stub
        super.onTerminate();
    }
}
