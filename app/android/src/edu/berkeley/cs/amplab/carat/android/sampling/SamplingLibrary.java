package edu.berkeley.cs.amplab.carat.android.sampling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.thrift.AndroidSample;
import edu.berkeley.cs.amplab.carat.thrift.CallMonth;
import edu.berkeley.cs.amplab.carat.thrift.ProcessInfo;
import edu.berkeley.cs.amplab.carat.thrift.Sample;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Debug;
import android.os.SystemClock;

/**
 * Library class for methods that obtain information about the phone that is
 * running Carat.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public final class SamplingLibrary {
    private static final int READ_BUFFER_SIZE = 2 * 1024;
    // Network status constants
    public static String NETWORKSTATUS_DISCONNECTED = "disconnected";
    public static String NETWORKSTATUS_DISCONNECTING = "disconnecting";
    public static String NETWORKSTATUS_CONNECTED = "connected";
    public static String NETWORKSTATUS_CONNECTING = "connecting";
    // Network type constants
    public static String TYPE_UNKNOWN = "unknown";
    public static String TYPE_WIFI = "wifi";
    public static String TYPE_MOBILE = "mobile";
    public static String TYPE_WIMAX = "wimax";
    // Data State constants
    public static String DATA_DISCONNECTED = NETWORKSTATUS_DISCONNECTED;
    public static String DATA_CONNECTING = NETWORKSTATUS_CONNECTING;
    public static String DATA_CONNECTED = NETWORKSTATUS_CONNECTED;
    public static String DATA_SUSPENDED = "suspended";
    // Data Activity constants
    public static String DATA_ACTIVITY_NONE = "none";
    public static String DATA_ACTIVITY_IN = "in";
    public static String DATA_ACTIVITY_OUT = "out";
    public static String DATA_ACTIVITY_INOUT = "inout";
    public static String DATA_ACTIVITY_DORMANT = "dormant";
    // Wifi State constants
    public static String WIFI_STATE_DISABLING = "disabling";
    public static String WIFI_STATE_DISABLED = "disabled";
    public static String WIFI_STATE_ENABLING = "enabling";
    public static String WIFI_STATE_ENABLED = "enabled";
    public static String WIFI_STATE_UNKNOWN = "unknown";
    // Call state constants
    public static String CALL_STATE_IDLE = "idle";
    public static String CALL_STATE_OFFHOOK = "offhook";
    public static String CALL_STATE_RINGING = "ringing";

    private static final String STAG = "getSample";

    /** Library class, prevent instantiation */
    private SamplingLibrary() {
    }

    /**
     * Returns a randomly generated unique identifier that stays constant for
     * the lifetime of the device. (May change if wiped). This is probably our
     * best choice for a UUID across the Android landscape, since it is present
     * on both phones and non-phones.
     * 
     * @return a String that uniquely identifies this device.
     */
    public static String getUuid(Context c) {
        return Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
    }

    /**
     * Returns the model of the device running Carat, for example "sdk" for the
     * emulator, Galaxy Nexus for Samsung Galaxy Nexus.
     * 
     * @return the model of the device running Carat, for example "sdk" for the
     *         emulator, Galaxy Nexus for Samsung Galaxy Nexus.
     */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    /**
     * Returns the manufacturer of the device running Carat, for example
     * "google" or "samsung".
     * 
     * @return the manufacturer of the device running Carat, for example
     *         "google" or "samsung".
     */
    public static String getManufacturer() {
        return android.os.Build.MANUFACTURER;
    }

    /**
     * Returns the OS version of the device running Carat, for example 2.3.3 or
     * 4.0.2.
     * 
     * @return the OS version of the device running Carat, for example 2.3.3 or
     *         4.0.2.
     */
    public static String getOsVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * This may only work for 2.3 and later:
     * 
     * @return
     */

    public static String getBuildSerial() {
        // return android.os.Build.Serial;
        return System.getProperty("ro.serial", TYPE_UNKNOWN);
    }

    /**
     * Return misc system details that we might want to use later. Currently
     * does nothing.
     * 
     * @return
     */
    public static Map<String, String> getSystemDetails() {
        Map<String, String> results = new HashMap<String, String>();
        // TODO: Some of this should be added to registration to identify the
        // device and OS.
        // Cyanogenmod and others may have different kernels etc that affect
        // performance.

        /*
         * Log.i("SetModel", "board:" + android.os.Build.BOARD);
         * Log.i("SetModel", "bootloader:" + android.os.Build.BOOTLOADER);
         * Log.i("SetModel", "brand:" + android.os.Build.BRAND);
         * Log.i("SetModel", "CPU_ABI 1 and 2:" + android.os.Build.CPU_ABI +
         * ", " + android.os.Build.CPU_ABI2); Log.i("SetModel", "dev:" +
         * android.os.Build.DEVICE); Log.i("SetModel", "disp:" +
         * android.os.Build.DISPLAY); Log.i("SetModel", "FP:" +
         * android.os.Build.FINGERPRINT); Log.i("SetModel", "HW:" +
         * android.os.Build.HARDWARE); Log.i("SetModel", "host:" +
         * android.os.Build.HOST); Log.i("SetModel", "ID:" +
         * android.os.Build.ID); Log.i("SetModel", "manufacturer:" +
         * android.os.Build.MANUFACTURER); Log.i("SetModel", "prod:" +
         * android.os.Build.PRODUCT); Log.i("SetModel", "radio:" +
         * android.os.Build.RADIO); // FIXME: SERIAL not available on 2.2 //
         * Log.i("SetModel", "ser:" + android.os.Build.SERIAL);
         * Log.i("SetModel", "tags:" + android.os.Build.TAGS); Log.i("SetModel",
         * "time:" + android.os.Build.TIME); Log.i("SetModel", "type:" +
         * android.os.Build.TYPE); Log.i("SetModel", "unknown:" +
         * android.os.Build.UNKNOWN); Log.i("SetModel", "user:" +
         * android.os.Build.USER); Log.i("SetModel", "model:" +
         * android.os.Build.MODEL); Log.i("SetModel", "codename:" +
         * android.os.Build.VERSION.CODENAME); Log.i("SetModel", "release:" +
         * android.os.Build.VERSION.RELEASE);
         */

        return results;
    }

    /**
     * Read memory information from /proc/meminfo. Return used, free, inactive,
     * and active memory.
     * 
     * @return an int[] with used, free, inactive, and active memory, in kB, in
     *         that order.
     */
    public static int[] readMeminfo() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
            String load = reader.readLine();

            String[] toks = load.split("\\s+");
            // Log.v("meminfo", "Load: " + load + " 1:" + toks[1]);
            int total = Integer.parseInt(toks[1]);
            load = reader.readLine();
            toks = load.split("\\s+");
            // Log.v("meminfo", "Load: " + load + " 1:" + toks[1]);
            int free = Integer.parseInt(toks[1]);
            load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine();
            toks = load.split("\\s+");
            // Log.v("meminfo", "Load: " + load + " 1:" + toks[1]);
            int act = Integer.parseInt(toks[1]);
            load = reader.readLine();
            toks = load.split("\\s+");
            // Log.v("meminfo", "Load: " + load + " 1:" + toks[1]);
            int inact = Integer.parseInt(toks[1]);
            reader.close();
            return new int[] { total - free, free, inact, act };
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return new int[] { 0, 0, 0, 0 };
    }

    /**
     * Read memory usage using the public Android API methods in
     * ActivityManager, such as MemoryInfo and getProcessMemoryInfo.
     * 
     * @param c
     *            the Context from the running Activity.
     * @return int[] with total and used memory, in kB, in that order.
     */
    public static int[] readMemory(Context c) {
        ActivityManager man = (ActivityManager) c
                .getSystemService(Activity.ACTIVITY_SERVICE);
        /* Get available (free) memory */
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        man.getMemoryInfo(info);
        int totalMem = (int) info.availMem;

        /* Get memory used by all running processes. */

        /* Step 1: gather pids */
        List<ActivityManager.RunningAppProcessInfo> procs = man
                .getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> servs = man
                .getRunningServices(Integer.MAX_VALUE);
        int[] pids = new int[procs.size() + servs.size()];
        int i = 0;
        for (ActivityManager.RunningAppProcessInfo pinfo : procs) {
            pids[i] = pinfo.pid;
            i++;
        }
        for (ActivityManager.RunningServiceInfo pinfo : servs) {
            pids[i] = pinfo.pid;
            i++;
        }

        /*
         * Step 2: Sum up Pss values (weighted memory usage, taking into account
         * shared page usage)
         */
        android.os.Debug.MemoryInfo[] mems = man.getProcessMemoryInfo(pids);
        int memUsed = 0;
        for (android.os.Debug.MemoryInfo mem : mems) {
            memUsed += mem.getTotalPss();
        }
        Log.v("Mem", "Total mem:" + totalMem);
        Log.v("Mem", "Mem Used:" + memUsed);
        return new int[] { totalMem, memUsed };
    }

    // FIXME: Describe this. Why are there so many fields? Why is it divided by
    // 100?
    /*
     * The value of HZ varies across kernel versions and hardware platforms. On
     * i386 the situation is as follows: on kernels up to and including 2.4.x,
     * HZ was 100, giving a jiffy value of 0.01 seconds; starting with 2.6.0, HZ
     * was raised to 1000, giving a jiffy of 0.001 seconds. Since kernel 2.6.13,
     * the HZ value is a kernel configuration parameter and can be 100, 250 (the
     * default) or 1000, yielding a jiffies value of, respectively, 0.01, 0.004,
     * or 0.001 seconds. Since kernel 2.6.20, a further frequency is available:
     * 300, a number that divides evenly for the common video frame rates (PAL,
     * 25 HZ; NTSC, 30 HZ).
     * 
     * I will leave the unit of cpu time as the jiffy and we can discuss later.
     * 
     * 0 name of cpu 1 space 2 user time 3 nice time 4 sys time 5 idle time(it
     * is not include in the cpu total time) 6 iowait time 7 irg time 8 softirg
     * time
     * 
     * the idleTotal[5] is the idle time which always changes. There are two
     * spaces between cpu and user time.That is a tricky thing and messed up
     * splitting.:)
     */

    /**
     * Read CPU usage from /proc/stat, return a fraction of
     * usage/(usage+idletime)
     * 
     * @return a fraction of usage/(usage+idletime)
     */
    public static long[] readUsagePoint() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" ");

            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
                    + Long.parseLong(toks[4]) + Long.parseLong(toks[6])
                    + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return new long[] { idle1, cpu1 };
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Calculate CPU usage between the cpu and idle time given at two time
     * points.
     * 
     * @param then
     * @param now
     * @return
     */
    public static double getUsage(long[] then, long[] now) {
        if (then == null || now == null || then.length < 2 || now.length < 2)
            return 0.0;
        double idleAndCpuDiff = (now[0] + now[1]) - (then[0] + then[1]);
        return (now[1] - then[1]) / idleAndCpuDiff;
    }

    /**
     * Deprecated: We cannot sleep during sampling, since that freezes the UI.
     * Read CPU usage from /proc/stat, return a fraction of
     * usage/(usage+idletime)
     * 
     * @return a fraction of usage/(usage+idletime)
     */
    @Deprecated
    public static double readUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" ");

            double idle1 = Long.parseLong(toks[5]);
            double cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
                    + Long.parseLong(toks[4]) + Long.parseLong(toks[6])
                    + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {
            }

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" ");

            double idle2 = Long.parseLong(toks[5]);
            double cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
                    + Long.parseLong(toks[4]) + Long.parseLong(toks[6])
                    + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    private static WeakReference<List<RunningAppProcessInfo>> runningAppInfo = null;

    public static List<RunningAppProcessInfo> getRunningProcessInfo(
            Context context) {
        if (runningAppInfo == null || runningAppInfo.get() == null) {
            ActivityManager pActivityManager = (ActivityManager) context
                    .getSystemService(Activity.ACTIVITY_SERVICE);

            runningAppInfo = new WeakReference(
                    pActivityManager.getRunningAppProcesses());
        }
        return runningAppInfo.get();
    }
    
    public static boolean isRunning(Context context, String appName){
        List<RunningAppProcessInfo> runningProcs = getRunningProcessInfo(context);
        for (RunningAppProcessInfo i: runningProcs){
            if (i.processName.equals(appName) && i.importance != RunningAppProcessInfo.IMPORTANCE_EMPTY)
                return true;
        }
        return false;
    }

    /**
     * Returns a List of ProcessInfo objects for a Sample object.
     * 
     * @param context
     * @return
     */
    public static List<ProcessInfo> getRunningProcessInfoForSample(
            Context context) {
        List<RunningAppProcessInfo> list = getRunningProcessInfo(context);
        List<ProcessInfo> result = new ArrayList<ProcessInfo>();

        PackageManager pm = context.getPackageManager();
        List<android.content.pm.PackageInfo> packagelist = pm
                .getInstalledPackages(0);

        Map<String, PackageInfo> packages = new HashMap<String, PackageInfo>();
        for (PackageInfo pak : packagelist) {
            packages.put(pak.applicationInfo.name, pak);
        }

        // Collected in the same loop to save computation.
        int[] procMem = new int[list.size()];

        for (RunningAppProcessInfo pi : list) {
            ProcessInfo item = new ProcessInfo();
            PackageInfo pak = packages.get(pi.processName);
            if (pak != null) {
                ApplicationInfo info = pak.applicationInfo;
                // Human readable label (if any)
                String label = pm.getApplicationLabel(info).toString();
                if (label != null && label.length() > 0)
                    item.setApplicationLabel(label);
                // TODO: get more application details and assign to item
                int flags = pak.applicationInfo.flags;
                // Check if it is a system app
                boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
                isSystemApp = isSystemApp
                        || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;
                item.setIsSystemApp(isSystemApp);
            }
            item.setImportance(CaratApplication.importanceString(pi.importance));
            item.setPId(pi.pid);
            item.setPName(pi.processName);

            procMem[list.indexOf(pi)] = pi.pid;
            // FIXME: More fields will need to be added here, but ProcessInfo
            // needs to change.
            /*
             * uid lru
             */
            // add to result
            result.add(item);
        }

        // FIXME: These are not used yet.
        ActivityManager pActivityManager = (ActivityManager) context
                .getSystemService(Activity.ACTIVITY_SERVICE);
        Debug.MemoryInfo[] memoryInfo = pActivityManager
                .getProcessMemoryInfo(procMem);
        for (Debug.MemoryInfo info : memoryInfo) {
            // Decide which ones of info.* we want, add to a new and improved
            // ProcessInfo object
            // FIXME: Not used yet, Sample needs more fields
            int memory = info.dalvikPrivateDirty;
        }

        return result;
    }

    /**
     * Depratecated, use int[] meminfo = readMemInfo(); int totalMemory =
     * meminfo[0] + meminfo[1];
     */
    @Deprecated
    public static String getMemoryInfo() {
        String tmp = null;
        BufferedReader br = null;

        try {
            File file = new File("/proc/meminfo");
            FileInputStream in = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(in), READ_BUFFER_SIZE);

        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            tmp = br.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        StringBuilder sMemory = new StringBuilder();
        sMemory.append(tmp);

        try {
            tmp = br.readLine();
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sMemory.append("\n").append(tmp).append("\n");
        String result = "Memery Status:\n" + sMemory;
        return result;

    }

    /*
     * Deprecated, use readMemInfo()[1]
     */
    @Deprecated
    public static String getMemoryFree() {
        String tmp = null;
        BufferedReader br = null;

        try {
            File file = new File("/proc/meminfo");
            FileInputStream in = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(in), READ_BUFFER_SIZE);

        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            tmp = br.readLine();
            tmp = br.readLine();
            if (tmp != null) {
                // split by whitespace and take 2nd element, so that in:
                // MemoryFree: x kb
                // the x remains.
                String[] arr = tmp.split("\\s+");
                if (arr.length > 1)
                    tmp = arr[1];
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tmp;
    }

    /**
     * Return time in seconds since last boot.
     */
    public static double getUptime() {
        long uptime = SystemClock.elapsedRealtime();
        /*
         * int seconds = (int) (uptime / 1000) % 60; int minutes = (int) (uptime
         * / (1000 * 60) % 60); int hours = (int) (uptime / (1000 * 60 * 60) %
         * 24); String tmp = "\nThe uptime is :" + hours + "hr:" + minutes +
         * "mins:" + seconds + "sec.\n"; return tmp;
         */
        Log.v("uptime", String.valueOf(uptime));
        return uptime / 1000.0;
    }

    public static String getNetworkStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return NETWORKSTATUS_DISCONNECTED;
        NetworkInfo i = cm.getActiveNetworkInfo();
        if (i == null)
            return NETWORKSTATUS_DISCONNECTED;
        NetworkInfo.State s = i.getState();
        if (s == NetworkInfo.State.CONNECTED)
            return NETWORKSTATUS_CONNECTED;
        if (s == NetworkInfo.State.DISCONNECTED)
            return NETWORKSTATUS_DISCONNECTED;
        if (s == NetworkInfo.State.CONNECTING)
            return NETWORKSTATUS_CONNECTING;
        if (s == NetworkInfo.State.DISCONNECTING)
            return NETWORKSTATUS_DISCONNECTING;
        else
            return NETWORKSTATUS_DISCONNECTED;
    }

    public static String getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null || cm.getActiveNetworkInfo() == null)
            return TYPE_UNKNOWN;
        int type = cm.getActiveNetworkInfo().getType();
        switch (type) {
        case ConnectivityManager.TYPE_MOBILE:
            return TYPE_MOBILE;
        case ConnectivityManager.TYPE_WIFI:
            return TYPE_WIFI;
        case ConnectivityManager.TYPE_WIMAX:
            return TYPE_WIMAX;
        default:
            return TYPE_UNKNOWN;
        }
    }

    public static boolean networkAvailable(Context c) {
        String network = getNetworkStatus(c);
        return network.equals(NETWORKSTATUS_CONNECTED);
    }

    /* Get current WiFi signal Strength */
    public static int getWifiSignalStrength(Context context) {
        WifiManager myWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        int wifiRssi = myWifiInfo.getRssi();
        Log.v("WifiRssi", "Rssi:" + wifiRssi);
        return wifiRssi;

    }

    /* Get current WiFi link speed */
    public static int getWifiLinkSpeed(Context context) {
        WifiManager myWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        int linkSpeed = myWifiInfo.getLinkSpeed();

        Log.v("linkSpeed", "Link speed:" + linkSpeed);
        return linkSpeed;
    }

    /* Check whether WiFi is enabled */
    public static boolean getWifiEnabled(Context context) {
        boolean wifiEnabled = false;

        WifiManager myWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        wifiEnabled = myWifiManager.isWifiEnabled();
        Log.v("WifiEnabled", "Wifi is enabled:" + wifiEnabled);
        return wifiEnabled;
    }

    /* Get Wifi state: */
    public static String getWifiState(Context context) {
        WifiManager myWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        int wifiState = myWifiManager.getWifiState();
        switch (wifiState) {
        case WifiManager.WIFI_STATE_DISABLED:
            return WIFI_STATE_DISABLED;
        case WifiManager.WIFI_STATE_DISABLING:
            return WIFI_STATE_DISABLING;
        case WifiManager.WIFI_STATE_ENABLED:
            return WIFI_STATE_ENABLED;
        case WifiManager.WIFI_STATE_ENABLING:
            return WIFI_STATE_ENABLING;
        default:
            return WIFI_STATE_UNKNOWN;
        }
    }

    public static WifiInfo getWifiInfo(Context context) {
        WifiManager myWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = myWifiManager.getConnectionInfo();
        Log.v("WifiInfo", "Wifi information:" + connectionInfo);
        return connectionInfo;

    }

    /*
     * This method is deprecated. As of ICE_CREAM_SANDWICH, availability of
     * background data depends on several combined factors, and this method will
     * always return true. Instead, when background data is unavailable,
     * getActiveNetworkInfo() will now appear disconnected.
     */
    /* Check whether background data are enabled */
    @Deprecated
    public static boolean getBackgroundDataEnabled(Context context) {
        boolean bacDataEnabled = false;
        try {
            if (Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.BACKGROUND_DATA) == 1) {
                bacDataEnabled = true;
            }
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.v("BackgroundDataEnabled", "Background data enabled? "
                + bacDataEnabled);
        // return bacDataEnabled;
        return true;
    }

    /* Get Current Screen Brightness Value */
    public static int getScreenBrightness(Context context) {

        int screenBrightnessValue = 0;
        try {
            screenBrightnessValue = android.provider.Settings.System.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.v("ScreenBrightness", "Screen brightness value:"
                + screenBrightnessValue);
        return screenBrightnessValue;
    }

    /* Check whether GPS are enabled */
    public static boolean getGpsEnabled(Context context) {
        boolean gpsEnabled = false;
        LocationManager myLocationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        gpsEnabled = myLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.v("GPS", "GPS is :" + gpsEnabled);
        return gpsEnabled;
    }

    /**
     * Return a list of enabled LocationProviders, such as GPS, Network, etc.
     * 
     * @param context
     *            from onReceive or app.
     * @return
     */
    public static List<String> getEnabledLocationProviders(Context context) {
        List<String> res = new ArrayList<String>();
        LocationManager lm = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        List<String> allProviders = lm.getAllProviders();
        for (String provider : allProviders)
            if (lm.isProviderEnabled(provider))
                res.add(provider);

        return res;
    }

    /* Check the maximum number of satellites can be used in the satellite list */
    public static int getMaxNumSatellite(Context context) {

        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        int maxNumSatellite = gpsStatus.getMaxSatellites();

        Log.v("maxNumStatellite", "Maxmium number of satellites:"
                + maxNumSatellite);
        return maxNumSatellite;
    }

    /* Get call status */
    public static String getCallState(Context context) {
        TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        int callState = telManager.getCallState();
        switch (callState) {
        case TelephonyManager.CALL_STATE_OFFHOOK:
            return CALL_STATE_OFFHOOK;
        case TelephonyManager.CALL_STATE_RINGING:
            return CALL_STATE_RINGING;
        default:
            return CALL_STATE_IDLE;
        }
    }

    /*
     * Get network type: value 0: NETWORK_TYPE_UNKNOWN 1: NETWORK_TYPE_GPRS 2:
     * NETWORK_TYPE_EDGE 3: NETWORK_TYPE_UMTS 4: NETWORK_TYPE_CDMA 5:
     * NETWORK_TYPE_EVDO_0 6: NETWORK_TYPE_EVDO_A 7: NETWORK_TYPE_1xRTT 8:
     * NETWORK_TYPE_HSDPA 9: NETWORK_TYPE_HSUPA 10: NETWORK_TYPE_HSPA 11:
     * NETWORK_TYPE_IDEN 12: NETWORK_TYPE_EVDO_B 13: NETWORK_TYPE_LTE 14:
     * NETWORK_TYPE_EHRPD 15: NETWORK_TYPE_HSPAP
     */
    public static String getMobileNetworkType(Context context) {
        TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        int netType = telManager.getNetworkType();
        Log.v("NetworkType", "Network type:" + String.valueOf(netType));
        return String.valueOf(netType);
    }

    /* Check is it network roaming */
    public static boolean getRoamingStatus(Context context) {
        boolean roamStatus = false;

        TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        roamStatus = telManager.isNetworkRoaming();
        Log.v("RoamingStatus", "Roaming status:" + roamStatus);
        return roamStatus;
    }

    /* Get data state */
    public static String getDataState(Context context) {
        TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        int dataState = telManager.getDataState();
        switch (dataState) {
        case TelephonyManager.DATA_CONNECTED:
            return DATA_CONNECTED;
        case TelephonyManager.DATA_CONNECTING:
            return DATA_CONNECTING;
        case TelephonyManager.DATA_DISCONNECTED:
            return DATA_DISCONNECTED;
        default:
            return DATA_SUSPENDED;
        }
    }

    /* Get data activity */
    public static String getDataActivity(Context context) {
        TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        int dataActivity = telManager.getDataActivity();
        switch (dataActivity) {
        case TelephonyManager.DATA_ACTIVITY_IN:
            return DATA_ACTIVITY_IN;
        case TelephonyManager.DATA_ACTIVITY_OUT:
            return DATA_ACTIVITY_OUT;
        case TelephonyManager.DATA_ACTIVITY_INOUT:
            return DATA_ACTIVITY_INOUT;
        default:
            return DATA_ACTIVITY_NONE;
        }
    }

    /* Get the current location of the device */
    public static CellLocation getDeviceLocation(Context context) {
        TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        CellLocation LocDevice = telManager.getCellLocation();
        Log.v("DeviceLocation", "Device Location:" + LocDevice);
        return LocDevice;
    }

    /**
     * Return a long[3] with incoming call time, outgoing call time, and
     * non-call time in seconds since boot.
     * 
     * @param context
     *            from onReceive or Activity
     * @return a long[3] with incoming call time, outgoing call time, and
     *         non-call time in seconds since boot.
     */
    public static long[] getCalltimesSinceBoot(Context context) {

        long[] result = new long[3];

        long callInSeconds = 0;
        long callOutSeconds = 0;
        int type;
        long dur;
        long time = 0;

        // ms since boot
        long uptime = SystemClock.elapsedRealtime();
        long now = System.currentTimeMillis();
        long bootTime = now - uptime;

        String[] queries = new String[] { android.provider.CallLog.Calls.TYPE,
                android.provider.CallLog.Calls.DATE,
                android.provider.CallLog.Calls.DURATION };

        Cursor cur = context.getContentResolver().query(
                android.provider.CallLog.Calls.CONTENT_URI, queries,
                android.provider.CallLog.Calls.DATE + " > " + bootTime, null,
                android.provider.CallLog.Calls.DATE + " ASC");

        if (cur != null) {
            if (cur.moveToFirst()) {
                while (!cur.isAfterLast()) {
                    type = cur.getInt(0);
                    time = cur.getLong(1);
                    dur = cur.getLong(2);
                    switch (type) {
                    case android.provider.CallLog.Calls.INCOMING_TYPE:
                        callInSeconds += dur;
                        break;
                    case android.provider.CallLog.Calls.OUTGOING_TYPE:
                        callOutSeconds += dur;
                        break;
                    default:
                    }
                    cur.moveToNext();
                }
            } else {
                Log.w("CallDurFromBoot", "No calls listed");
            }
            cur.close();
        } else {
            Log.w("CallDurFromBoot", "Cursor is null");
        }

        // uptime is ms, so it needs to be divided by 1000
        long nonCallTime = uptime / 1000 - callInSeconds - callOutSeconds;
        result[0] = callInSeconds;
        result[1] = callOutSeconds;
        result[2] = nonCallTime;
        return result;
    }

    /* Get a monthly call duration record */
    public static Map<String, CallMonth> getMonthCallDur(Context context) {

        Map<String, CallMonth> callMonth = new HashMap<String, CallMonth>();
        Map<String, String> callInDur = new HashMap<String, String>();
        Map<String, String> callOutDur = new HashMap<String, String>();

        long tolCallInDur = 0;
        long tolCallOutDur = 0;
        int callType;
        long callDur;
        Date callDate;
        String tmpTime = null;
        String time;
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM");
        CallMonth curMonth = null;

        String[] queryFields = new String[] {
                android.provider.CallLog.Calls.TYPE,
                android.provider.CallLog.Calls.DATE,
                android.provider.CallLog.Calls.DURATION };

        Cursor myCursor = context.getContentResolver().query(
                android.provider.CallLog.Calls.CONTENT_URI, queryFields, null,
                null, android.provider.CallLog.Calls.DATE + " DESC");

        if (myCursor.moveToFirst()) {
            for (int i = 0; i < myCursor.getColumnCount(); i++) {
                myCursor.moveToPosition(i);
                callType = myCursor.getInt(0);
                callDate = new Date(myCursor.getLong(1));
                callDur = myCursor.getLong(2);

                time = dateformat.format(callDate);
                if (tmpTime != null && !time.equals(tmpTime)) {
                    callMonth.put(tmpTime, curMonth);
                    callInDur.clear();
                    callOutDur.clear();
                    curMonth = new CallMonth();
                }
                tmpTime = time;

                if (callType == 1) {
                    curMonth.tolCallInNum++;
                    curMonth.tolCallInDur += callDur;
                    callInDur.put("tolCallInNum",
                            String.valueOf(curMonth.tolCallInNum));
                    callInDur.put("tolCallInDur",
                            String.valueOf(curMonth.tolCallInDur));
                }
                if (callType == 2) {
                    curMonth.tolCallOutNum++;
                    curMonth.tolCallOutDur += callDur;
                    callOutDur.put("tolCallOutNum",
                            String.valueOf(curMonth.tolCallOutNum));
                    callOutDur.put("tolCallOutDur",
                            String.valueOf(curMonth.tolCallOutDur));
                }
                if (callType == 3) {
                    curMonth.tolMissedCallNum++;
                    callInDur.put("tolMissedCallNum",
                            String.valueOf(curMonth.tolMissedCallNum));
                }
            }
        } else {
            Log.v("MonthType", "callType=None");
            Log.v("MonthDate", "callDate=None");
            Log.v("MonthDuration", "callduration =None");
        }
        return callMonth;
    }

    public static CallMonth getCallMonthinfo(Context context, String time) {

        Map<String, CallMonth> callInfo;
        callInfo = SamplingLibrary.getMonthCallDur(context);
        CallMonth call = new CallMonth();
        call = callInfo.get(time);
        return call;
    }

    public static Sample getSample(Context context, Intent intent,
            Sample lastSample) {
        String action = intent.getAction();

        // Construct sample and return it in the end
        Sample mySample = new Sample();
        mySample.setUuId(SamplingLibrary.getUuid(context));
        mySample.setTriggeredBy(action);
        // required always
        long now = System.currentTimeMillis();
        mySample.setTimestamp(now / 1000.0);
        long[] idleAndCpu1 = readUsagePoint();

        List<ProcessInfo> processes = getRunningProcessInfoForSample(context);
        mySample.setPiList(processes);
        // Wifi stuff
        int wifiSignalStrength = SamplingLibrary.getWifiSignalStrength(context);
        mySample.setWifiSignalStrength(wifiSignalStrength);
        int wifiLinkSpeed = SamplingLibrary.getWifiLinkSpeed(context);
        mySample.setWifiLinkSpeed(wifiLinkSpeed);

        int screenBrightness = SamplingLibrary.getScreenBrightness(context);
        mySample.setScreenBrightness(screenBrightness);
        // boolean gpsEnabled = SamplingLibrary.getGpsEnabled(context);
        // Location providers
        List<String> enabledLocationProviders = SamplingLibrary
                .getEnabledLocationProviders(context);
        mySample.setLocationProviders(enabledLocationProviders);
        // TODO: not in Sample yet
        // int maxNumSatellite = SamplingLibrary.getMaxNumSatellite(context);
        String callState = SamplingLibrary.getCallState(context);
        // Network type
        String networkType = SamplingLibrary.getNetworkType(context);
        mySample.setNetworkType(networkType);
        String mobileNetworkType = SamplingLibrary
                .getMobileNetworkType(context);
        mySample.setMobileNetworkType(mobileNetworkType);
        boolean roamStatus = SamplingLibrary.getRoamingStatus(context);
        mySample.setRoamingEnabled(roamStatus);
        String dataState = SamplingLibrary.getDataState(context);
        mySample.setMobileDataStatus(dataState);
        String dataActivity = SamplingLibrary.getDataActivity(context);
        mySample.setMobileDataActivity(dataActivity);
        CellLocation deviceLoc = SamplingLibrary.getDeviceLocation(context);
        // TODO: cast this to GSMLocation or CDMALocation and use it
        boolean wifiEnabled = SamplingLibrary.getWifiEnabled(context);
        mySample.setWifiEnabled(wifiEnabled);
        String wifiState = SamplingLibrary.getWifiState(context);
        // mySample.setWifiState(wifiState);

        // TODO: is this used for something?
        // WifiInfo connectionInfo=SamplingLibrary.getWifiInfo(context);

        /* Calling Information */
        // List<String> callInfo;
        // callInfo=SamplingLibrary.getCallInfo(context);
        /* Total call time */
        // long totalCallTime=0;
        // totalCallTime=SamplingLibrary.getTotalCallDur(context);
        long[] incomingOutgoingIdle = getCalltimesSinceBoot(context);
        Log.i(STAG, "Call time since boot: Incoming=" + incomingOutgoingIdle[0]
                + " Outgoing=" + incomingOutgoingIdle[1] + " idle="
                + incomingOutgoingIdle[2]);

        double level = intent.getIntExtra("level", -1);
        int health = intent.getIntExtra("health", 0);
        double scale = intent.getIntExtra("scale", 100);
        int status = intent.getIntExtra("status", 0);
        // This is really an int.
        double voltage = intent.getIntExtra("voltage", 0) / 1000.0;
        // current battery voltage in volts
        // FIXED: Not used yet, Sample needs more fields
        int temperature = intent.getIntExtra("temperature", 0) / 10;
        // current battery temperature in a degree Centigrade
        int plugged = intent.getIntExtra("plugged", 0);
        String batteryTechnology = intent.getStringExtra("batteryTechnology");

        // use last known value
        double batteryLevel = 0.0;
        if (lastSample != null)
            batteryLevel = lastSample.getBatteryLevel();
        // if we have real data, change old value
        if (level > 0 && scale > 0) {
            batteryLevel = (level / scale);
            Log.i(STAG, "BatteryLevel: " + batteryLevel);
        }

        // FIXED: Not used yet, Sample needs more fields
        String batteryHealth = "Unknown";
        String batteryStatus = "Unknown";

        switch (health) {

        case BatteryManager.BATTERY_HEALTH_DEAD:
            batteryHealth = "Dead";
            break;
        case BatteryManager.BATTERY_HEALTH_GOOD:
            batteryHealth = "Good";
            break;
        case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
            batteryHealth = "Over voltage";
            break;
        case BatteryManager.BATTERY_HEALTH_OVERHEAT:
            batteryHealth = "Overheat";
            break;
        case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            batteryHealth = "Unknown";
            break;
        case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
            batteryHealth = "Unspecified failure";
            break;
        }

        switch (status) {

        case BatteryManager.BATTERY_STATUS_CHARGING:
            batteryStatus = "Charging";
            break;
        case BatteryManager.BATTERY_STATUS_DISCHARGING:
            batteryStatus = "Discharging";
            break;
        case BatteryManager.BATTERY_STATUS_FULL:
            batteryStatus = "Full";
            break;
        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
            batteryStatus = "Not charging";
            break;
        case BatteryManager.BATTERY_STATUS_UNKNOWN:
            batteryStatus = "Unknown";
            break;
        default:
            // use last known value
            if (lastSample != null)
                batteryStatus = lastSample.getBatteryState();
        }

        // FIXED: Not used yet, Sample needs more fields
        String batteryCharger = "unplugged";
        switch (plugged) {

        case BatteryManager.BATTERY_PLUGGED_AC:
            batteryCharger = "ac";
            break;
        case BatteryManager.BATTERY_PLUGGED_USB:
            batteryCharger = "usb";
            break;
        }
        if (idleAndCpu1 != null && idleAndCpu1.length > 1) {
            mySample.setIdleTime(idleAndCpu1[0]);
            mySample.setCpuTime(idleAndCpu1[1]);
        }
        // otherInfo.setCPUIdleTime(totalIdleTime);
        mySample.setBatteryTemperature(temperature);
        // otherInfo.setBatteryTemperature(temperature);
        mySample.setBatteryVoltage(voltage);
        // otherInfo.setBatteryVoltage(voltage);
        mySample.setBatteryTechnology(batteryTechnology);

        // TODO: Extended attributes should be set to mySample
        // What is totalCpuUsage? How does it compare to cpuTime and idleTime?
        // Maybe we should just have a cpu usage percentage.
        AndroidSample otherInfo = new AndroidSample();

        mySample.setUptime(getUptime());
        mySample.setBatteryCharger(batteryCharger);
        mySample.setBatteryHealth(batteryHealth);

        // Required in new Carat protocol
        mySample.setNetworkStatus(SamplingLibrary.getNetworkStatus(context));
        mySample.setBatteryLevel(batteryLevel);
        mySample.setBatteryState(batteryStatus);

        int[] usedFreeActiveInactive = SamplingLibrary.readMeminfo();
        if (usedFreeActiveInactive != null
                && usedFreeActiveInactive.length == 4) {
            mySample.setMemoryUser(usedFreeActiveInactive[0]);
            mySample.setMemoryFree(usedFreeActiveInactive[1]);
            mySample.setMemoryActive(usedFreeActiveInactive[2]);
            mySample.setMemoryInactive(usedFreeActiveInactive[3]);
        }
        // TODO: Memory Wired should have memory that is "unevictable", that
        // will always be used even when all apps are killed

        /* Calling Information */
        /*
         * CallMonth cm = new CallMonth(); cm = getCallMonthinfo(context,
         * "2012-03"); if (cm != null) Log.v(STAG,
         * "Total duration of incoming calls in March 2012=" +cm.tolCallInNum);
         */
        Log.i(STAG, "serial=" + getBuildSerial());

        // Record second data point for cpu/idle time
        now = System.currentTimeMillis();
        long[] idleAndCpu2 = readUsagePoint();
        otherInfo.setCPUUsage(getUsage(idleAndCpu1, idleAndCpu2));

        return mySample;
    }
}
