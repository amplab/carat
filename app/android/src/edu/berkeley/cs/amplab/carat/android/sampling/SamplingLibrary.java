package edu.berkeley.cs.amplab.carat.android.sampling;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import com.flurry.android.FlurryAgent;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.thrift.BatteryDetails;
import edu.berkeley.cs.amplab.carat.thrift.CallMonth;
import edu.berkeley.cs.amplab.carat.thrift.CellInfo;
import edu.berkeley.cs.amplab.carat.thrift.CpuStatus;
import edu.berkeley.cs.amplab.carat.thrift.NetworkDetails;
import edu.berkeley.cs.amplab.carat.thrift.ProcessInfo;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.SystemClock;

/**
 * Library class for methods that obtain information about the phone that is
 * running Carat.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public final class SamplingLibrary {
    private static final boolean collectSignatures = true;
    public static final String SIG_SENT = "sig-sent:";
    public static final String SIG_SENT_256 = "sigs-sent:";
    public static final String INSTALLED = "installed:";
    public static final String REPLACED = "replaced:";
    public static final String UNINSTALLED = "uninstalled:";

    private static final int READ_BUFFER_SIZE = 2 * 1024;
    // Network status constants
    public static String NETWORKSTATUS_DISCONNECTED = "disconnected";
    public static String NETWORKSTATUS_DISCONNECTING = "disconnecting";
    public static String NETWORKSTATUS_CONNECTED = "connected";
    public static String NETWORKSTATUS_CONNECTING = "connecting";
    // Network type constants
    public static String TYPE_UNKNOWN = "unknown";
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

    // Mobile network constants
    /*
     * we cannot find network types:EVDO_B,LTE,EHRPD,HSPAP from TelephonyManager
     * now
     */
    public static String NETWORK_TYPE_UNKNOWN = "unknown";
    public static String NETWORK_TYPE_GPRS = "gprs";
    public static String NETWORK_TYPE_EDGE = "edge";
    public static String NETWORK_TYPE_UMTS = "utms";
    public static String NETWORK_TYPE_CDMA = "cdma";
    public static String NETWORK_TYPE_EVDO_0 = "evdo_0";
    public static String NETWORK_TYPE_EVDO_A = "evdo_a";
     public static String NETWORK_TYPE_EVDO_B="evdo_b";
    public static String NETWORK_TYPE_1xRTT = "1xrtt";
    public static String NETWORK_TYPE_HSDPA = "hsdpa";
    public static String NETWORK_TYPE_HSUPA = "hsupa";
    public static String NETWORK_TYPE_HSPA = "hspa";
    public static String NETWORK_TYPE_IDEN = "iden";
    public static String NETWORK_TYPE_LTE="lte";
    public static String NETWORK_TYPE_EHRPD="ehrpd";
    public static String NETWORK_TYPE_HSPAP="hspap";
    
    private static final int EVDO_B = 12;
    private static final int LTE = 13;
    private static final int EHRPD = 14;
    private static final int HSPAP = 15;
    
    
    // Phone type constants
    public static String PHONE_TYPE_CDMA = "cdma";
    public static String PHONE_TYPE_GSM = "gsm";
    // public static String PHONE_TYPE_SIP="sip";
    public static String PHONE_TYPE_NONE = "none";

    public static double startLatitude = 0;
    public static double startLongitude = 0;
    public static double distance = 0;

    private static final String STAG = "getSample";
    // private static final String TAG="FeaturesPowerConsumption";

    public static final int UUID_LENGTH = 16;

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
    public static String getAndroidId(Context c) {
        return Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
    }

    public static String getUuid(Context c) {
        return getTimeBasedUuid(c, false);
    }

    public static String getTimeBasedUuid(Context c) {
        return getTimeBasedUuid(c, true);
    }

    /**
     * Generate a time-based, random identifier.
     * 
     * @param c
     *            the app's Context
     * @return a time-based, random identifier.
     */
    public static String getTimeBasedUuid(Context c, boolean includeTimestamp) {
        String aID = getAndroidId(c);
        String wifiMac = getWifiMacAddress(c);
        String devid = getDeviceId(c);
        String concat = "";
        if (aID != null)
            concat = aID;
        else
            concat = "0000000000000000";
        if (wifiMac != null)
            concat += wifiMac;
        else
            concat += "00:00:00:00:00:00";

        // IMEI is 15 characters, decimal, while MEID is 14 characters, hex. Add
        // a space if length is less than 15:
        if (devid != null) {
            concat += devid;
            if (devid.length() < 15)
                concat += " ";
        } else
            concat += "000000000000000";
        if (includeTimestamp) {
            long timestamp = System.currentTimeMillis();
            concat += timestamp;
        }

        // Log.d(STAG,
        // "AID="+aID+" wifiMac="+wifiMac+" devid="+devid+" rawUUID=" +concat );
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(concat.getBytes());
            byte[] mdbytes = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < mdbytes.length; i++) {
                String hx = Integer.toHexString(0xFF & mdbytes[i]);
                if (hx.equals("0"))
                    hexString.append("00");
                else
                    hexString.append(hx);
            }
            String uuid = hexString.toString().substring(0, UUID_LENGTH);
            // FlurryAgent.logEvent("ANDROID_ID=" + aID +" UUID=" + uuid);
            return uuid;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return aID;
        }
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
     * Returns the product name.
     * 
     * @return the product name.
     */
    public static String getProductName() {
        return android.os.Build.PRODUCT;
    }

	/**
	 * Returns the kernel version, e.g. 3.4-1101.
	 * 
	 * @return the kernel version, e.g. 3.4-1101.
	 */
	public static String getKernelVersion() {
		return System.getProperty("os.version", TYPE_UNKNOWN);
	}

	/**
	 * Returns the build serial number. May only work for 2.3 and up.
	 * 
	 * @return the build serial number.
	 */
	public static String getBuildSerial() {
		// return android.os.Build.Serial;
		return System.getProperty("ro.serial", TYPE_UNKNOWN);
	}

	/**
	 * Print all system properties for debugging.
	 * 
	 */
	public static void printAllProperties() {
		Properties list = System.getProperties();
		Enumeration<Object> keys = list.keys();
		while (keys.hasMoreElements()) {
			String k = (String) keys.nextElement();
			String v = list.getProperty(k);
			Log.d("PROPS", k + "=" + v);
		}
	}

	/**
	 * Returns the brand for which the device is customized, e.g. Verizon.
	 * 
	 * @return the brand for which the device is customized, e.g. Verizon.
	 */
	public static String getBrand() {
		return android.os.Build.BRAND;
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
         * Log.d("SetModel", "board:" + android.os.Build.BOARD);
         * Log.d("SetModel", "bootloader:" + android.os.Build.BOOTLOADER);
         * Log.d("SetModel", "brand:" + android.os.Build.BRAND);
         * Log.d("SetModel", "CPU_ABI 1 and 2:" + android.os.Build.CPU_ABI +
         * ", " + android.os.Build.CPU_ABI2); Log.d("SetModel", "dev:" +
         * android.os.Build.DEVICE); Log.d("SetModel", "disp:" +
         * android.os.Build.DISPLAY); Log.d("SetModel", "FP:" +
         * android.os.Build.FINGERPRINT); Log.d("SetModel", "HW:" +
         * android.os.Build.HARDWARE); Log.d("SetModel", "host:" +
         * android.os.Build.HOST); Log.d("SetModel", "ID:" +
         * android.os.Build.ID); Log.d("SetModel", "manufacturer:" +
         * android.os.Build.MANUFACTURER); Log.d("SetModel", "prod:" +
         * android.os.Build.PRODUCT); Log.d("SetModel", "radio:" +
         * android.os.Build.RADIO); // FIXME: SERIAL not available on 2.2 //
         * Log.d("SetModel", "ser:" + android.os.Build.SERIAL);
         * Log.d("SetModel", "tags:" + android.os.Build.TAGS); Log.d("SetModel",
         * "time:" + android.os.Build.TIME); Log.d("SetModel", "type:" +
         * android.os.Build.TYPE); Log.d("SetModel", "unknown:" +
         * android.os.Build.UNKNOWN); Log.d("SetModel", "user:" +
         * android.os.Build.USER); Log.d("SetModel", "model:" +
         * android.os.Build.MODEL); Log.d("SetModel", "codename:" +
         * android.os.Build.VERSION.CODENAME); Log.d("SetModel", "release:" +
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
        ActivityManager man = (ActivityManager) c.getSystemService(Activity.ACTIVITY_SERVICE);
        /* Get available (free) memory */
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        man.getMemoryInfo(info);
        int totalMem = (int) info.availMem;

        /* Get memory used by all running processes. */

        /* Step 1: gather pids */
        List<ActivityManager.RunningAppProcessInfo> procs = man.getRunningAppProcesses();
        List<ActivityManager.RunningServiceInfo> servs = man.getRunningServices(Integer.MAX_VALUE);
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
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            reader.close();
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

    private static WeakReference<List<RunningAppProcessInfo>> runningAppInfo = null;

    public static List<ProcessInfo> getRunningAppInfo(Context c) {
        List<RunningAppProcessInfo> runningProcs = getRunningProcessInfo(c);
        List<RunningServiceInfo> runningServices = getRunningServiceInfo(c);

        List<ProcessInfo> l = new ArrayList<ProcessInfo>();

        if (runningProcs != null) {
            for (RunningAppProcessInfo pi : runningProcs) {
                if (pi == null)
                    continue;
                ProcessInfo item = new ProcessInfo();
                item.setImportance(CaratApplication.importanceString(pi.importance));
                item.setPId(pi.pid);
                item.setPName(pi.processName);
                l.add(item);
            }
        }

        if (runningServices != null) {
            for (RunningServiceInfo pi : runningServices) {
                if (pi == null)
                    continue;
                ProcessInfo item = new ProcessInfo();
                item.setImportance(pi.foreground ? "Foreground app" : "Service");
                item.setPId(pi.pid);
                item.setPName(pi.clientPackage);
                l.add(item);
            }
        }

        return l;
    }

    private static List<RunningAppProcessInfo> getRunningProcessInfo(Context context) {
        if (runningAppInfo == null || runningAppInfo.get() == null) {
            ActivityManager pActivityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> runningProcs = pActivityManager.getRunningAppProcesses();
            /*
             * TODO: Is this the right thing to do? Remove part after ":" in
             * process names
             */
            for (RunningAppProcessInfo i : runningProcs) {
                if (i != null && i.processName != null) {
                    int idx = i.processName.lastIndexOf(':');
                    if (idx <= 0)
                        idx = i.processName.length();
                    i.processName = i.processName.substring(0, idx);
                }
            }

            runningAppInfo = new WeakReference<List<RunningAppProcessInfo>>(runningProcs);
        }
        return runningAppInfo.get();
    }

    public static List<RunningServiceInfo> getRunningServiceInfo(Context c) {
        ActivityManager pActivityManager = (ActivityManager) c.getSystemService(Activity.ACTIVITY_SERVICE);
        return pActivityManager.getRunningServices(0);
    }

    public static boolean isRunning(Context context, String appName) {
        List<RunningAppProcessInfo> runningProcs = getRunningProcessInfo(context);
        for (RunningAppProcessInfo i : runningProcs) {
            if (i.processName.equals(appName) && i.importance != RunningAppProcessInfo.IMPORTANCE_EMPTY)
                return true;
        }
        return false;
    }

    public static void resetRunningProcessInfo() {
        runningAppInfo = null;
    }

    static WeakReference<Map<String, PackageInfo>> packages = null;

    public static boolean isHidden(Context c, String processName) {
        boolean isSystem = isSystem(c, processName);
        boolean blocked = (isSystem && !isWhiteListed(c, processName));
        return blocked || isBlacklisted(c, processName);
    }

    /**
     * For debugging always returns true.
     * 
     * @param c
     * @param processName
     * @return
     */
    private static boolean isWhiteListed(Context c, String processName) {
        return !isBlacklisted(c, processName);
    }

    /**
     * For debugging always returns true.
     * 
     * @param c
     * @param processName
     * @return
     */
    private static boolean isBlacklisted(Context c, String processName) {
        /*
         * Whitelist: Messaging, Voice Search, Bluetooth Share
         * 
         * Blacklist: Key chain, google partner set up, package installer,
         * package access helper
         */
        if (CaratApplication.storage != null) {
            List<String> blacklist = CaratApplication.storage.getBlacklist();
            if (blacklist != null && blacklist.size() > 0 && processName != null && blacklist.contains(processName)) {
                return true;
            }

            blacklist = CaratApplication.storage.getGloblist();
            if (blacklist != null && blacklist.size() > 0 && processName != null) {
                for (String glob : blacklist) {
                    if (glob == null)
                        continue;
                    // something*
                    if (glob.endsWith("*") && processName.startsWith(glob.substring(0, glob.length() - 1)))
                        return true;
                    // *something
                    if (glob.startsWith("*") && processName.endsWith(glob.substring(1)))
                        return true;
                }
            }
        }
        String label = CaratApplication.labelForApp(c, processName);

        if (processName != null && label != null && label.equals(processName)) {
            // Log.v("Hiding uninstalled", processName);
            return true;
        }

        // FlurryAgent.logEvent("Whitelisted "+processName + " \""+ label+"\"");
        return false;
    }

    private static boolean isSystem(Context context, String processName) {
        PackageInfo pak = getPackageInfo(context, processName);
        if (pak != null) {
            ApplicationInfo i = pak.applicationInfo;
            int flags = i.flags;
            boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
            isSystemApp = isSystemApp || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;
            // Log.v(STAG, processName + " is System app? " + isSystemApp);
            return isSystemApp;
        }
        return false;
    }
    
    /**
     * Helper to ensure the WeakReferenced `packages` is populated.
     * @param context
     * @return The content of `packages` or null in case of failure.
     */
    private static Map<String, PackageInfo> getPackages(Context context){
        List<android.content.pm.PackageInfo> packagelist = null;

        if (packages == null || packages.get() == null || packages.get().size() == 0) {
            Map<String, PackageInfo> mp = new HashMap<String, PackageInfo>();
            PackageManager pm = context.getPackageManager();
            if (pm == null)
                return null;

            try {
                if (collectSignatures)
                    packagelist = pm.getInstalledPackages(PackageManager.GET_SIGNATURES
                            | PackageManager.GET_PERMISSIONS);
                else
                    packagelist = pm.getInstalledPackages(0);
            } catch (Throwable th) {
                // Forget about it...
            }
            if (packagelist == null)
                return null;
            for (PackageInfo pak : packagelist) {
                if (pak == null || pak.applicationInfo == null || pak.applicationInfo.processName == null)
                    continue;
                mp.put(pak.applicationInfo.processName, pak);
            }

            packages = new WeakReference<Map<String, PackageInfo>>(mp);

            if (mp == null || mp.size() == 0)
                return null;
            return mp;
        } else {
            if (packages == null)
                return null;
            Map<String, PackageInfo> p = packages.get();
            if (p == null || p.size() == 0)
                return null;
            return p;
        }
    }

    /**
     * Get info for a single package from the WeakReferenced packagelist.
     * @param context
     * @param processName The package to get info for.
     * @return info for a single package from the WeakReferenced packagelist.
     */
    public static PackageInfo getPackageInfo(Context context, String processName) {
        Map<String, PackageInfo> mp = getPackages(context);
            if (mp == null || !mp.containsKey(processName))
                return null;
            PackageInfo pak = mp.get(processName);
            return pak;
    }
    
    /**
     * Returns a list of installed packages on the device.
     * Will be called for the first Carat sample on a phone, to get signatures for the
     * malware detection project. Later on, single package information is got by receiving the package installed intent. 
     * 
     * @param context
     * @param filterSystem if true, exclude system packages.
     * @return a list of installed packages on the device.
     */
    public static Map<String, ProcessInfo> getInstalledPackages(Context context, boolean filterSystem) {
        Map<String, PackageInfo> packageMap = getPackages(context);
        PackageManager pm = context.getPackageManager();
        if (pm == null)
            return null;

        Map<String, ProcessInfo> result = new HashMap<String, ProcessInfo>();

        for (Entry<String, PackageInfo> pentry: packageMap.entrySet()) {
            try {
                String pkg = pentry.getKey();
                PackageInfo pak = pentry.getValue();
                if (pak != null) {
                    int vc = pak.versionCode;
                    ApplicationInfo info = pak.applicationInfo;
                    String label = pm.getApplicationLabel(info).toString();
                    int flags = pak.applicationInfo.flags;
                    // Check if it is a system app
                    boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
                    isSystemApp = isSystemApp || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;
                    if (filterSystem & isSystemApp)
                        continue;
                    if (pak.signatures.length > 0) {
                        List<String> sigList = getSignatures(pak);
                        ProcessInfo pi = new ProcessInfo();
                        pi.setPName(pkg);
                        pi.setApplicationLabel(label);
                        pi.setVersionCode(vc);
                        pi.setPId(-1);
                        pi.setIsSystemApp(isSystemApp);
                        pi.setAppSignatures(sigList);
                        pi.setImportance(CaratApplication.IMPORTANCE_NOT_RUNNING);
                        pi.setInstallationPkg(pm.getInstallerPackageName(pkg));
                        pi.setVersionName(pak.versionName);
                        result.put(pkg, pi);
                    }
                }
            } catch (Throwable th) {
                // Forget about it...
            }
        }
        return result;
    }
    
    /**
     * Returns info about an installed package.
     * Will be called when receiving the PACKAGE_ADDED or PACKAGE_REPLACED intent. 
     * 
     * @param context
     * @param filterSystem if true, exclude system packages.
     * @return a list of installed packages on the device.
     */
    public static ProcessInfo getInstalledPackage(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        if (pm == null)
            return null;
        PackageInfo pak;
        try {
            pak = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES | PackageManager.GET_PERMISSIONS);
        } catch (NameNotFoundException e) {
            return null;
        }
        if (pak == null)
            return null;

        ProcessInfo pi = new ProcessInfo();
        int vc = pak.versionCode;
        ApplicationInfo info = pak.applicationInfo;
        String label = pm.getApplicationLabel(info).toString();
        int flags = pak.applicationInfo.flags;
        // Check if it is a system app
        boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
        isSystemApp = isSystemApp || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;

        if (pak.signatures.length > 0) {
            List<String> sigList = getSignatures(pak);
            pi.setPName(pkg);
            pi.setApplicationLabel(label);
            pi.setVersionCode(vc);
            pi.setPId(-1);
            pi.setIsSystemApp(isSystemApp);
            pi.setAppSignatures(sigList);
            pi.setImportance(CaratApplication.IMPORTANCE_NOT_RUNNING);
            pi.setInstallationPkg(pm.getInstallerPackageName(pkg));
            pi.setVersionName(pak.versionName);
        }
        return pi;
    }

    /**
     * Returns a List of ProcessInfo objects for a Sample object.
     * 
     * @param context
     * @return
     */
    private static List<ProcessInfo> getRunningProcessInfoForSample(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);

        // Reset list for each sample
        runningAppInfo = null;
        List<ProcessInfo> list = getRunningAppInfo(context);
        List<ProcessInfo> result = new ArrayList<ProcessInfo>();

        PackageManager pm = context.getPackageManager();
        // Collected in the same loop to save computation.
        int[] procMem = new int[list.size()];

        Set<String> procs = new HashSet<String>();
        
        
        boolean inst = p.getBoolean(CaratApplication.PREFERENCE_SEND_INSTALLED_PACKAGES, true);
        
        Map<String, ProcessInfo> ipkg = null;
        if (inst)
            ipkg = getInstalledPackages(context, false);
        
        for (ProcessInfo pi : list) {
            String pname = pi.getPName();
            if (ipkg != null && ipkg.containsKey(pname))
                ipkg.remove(pname);
            procs.add(pname);
            ProcessInfo item = new ProcessInfo();
            PackageInfo pak = getPackageInfo(context, pname);
            if (pak != null) {
                String ver = pak.versionName;
                int vc = pak.versionCode;
                item.setVersionName(ver);
                item.setVersionCode(vc);
                ApplicationInfo info = pak.applicationInfo;

                // Human readable label (if any)
                String label = pm.getApplicationLabel(info).toString();
                if (label != null && label.length() > 0)
                    item.setApplicationLabel(label);
                int flags = pak.applicationInfo.flags;
                // Check if it is a system app
                boolean isSystemApp = (flags & ApplicationInfo.FLAG_SYSTEM) > 0;
                isSystemApp = isSystemApp || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;
                item.setIsSystemApp(isSystemApp);
                /*boolean sigSent = p.getBoolean(SIG_SENT_256 + pname, false);
                if (collectSignatures && !sigSent && pak.signatures != null && pak.signatures.length > 0) {
                    List<String> sigList = getSignatures(pak);
                    boolean sigSentOld = p.getBoolean(SIG_SENT + pname, false);
                    if (sigSentOld)
                        p.edit().remove(SIG_SENT + pname);
                    p.edit().putBoolean(SIG_SENT_256 + pname, true).commit();
                    item.setAppSignatures(sigList);
                }*/
            }
            item.setImportance(pi.getImportance());
            item.setPId(pi.getPId());
            item.setPName(pname);
            
            String installationSource = null;
            if (!pi.isSystemApp) {
                try{
                //Log.w(STAG, "Calling getInstallerPackageName with: " + pname);
                installationSource = pm.getInstallerPackageName(pname);
                }catch (IllegalArgumentException iae){
                    Log.e(STAG, "Could not get installer for " + pname);
                }
            }
            if (installationSource == null)
                installationSource = "null";
            item.setInstallationPkg(installationSource);

            //procMem[list.indexOf(pi)] = pi.getPId();
            // FIXME: More fields will need to be added here, but ProcessInfo
            // needs to change.
            /*
             * uid lru
             */
            // add to result
            result.add(item);
        }
        
        // Send installed packages if we were to do so.
        if (ipkg != null && ipkg.size() > 0){
            result.addAll(ipkg.values());
            p.edit().putBoolean(CaratApplication.PREFERENCE_SEND_INSTALLED_PACKAGES, false).commit();
        }
        
        // Go through the preferences and look for UNINSTALL, INSTALL and REPLACE keys set by InstallReceiver.
        Set<String> ap = p.getAll().keySet();
        SharedPreferences.Editor e = p.edit();
        boolean edited = false;
        for (String pref : ap) {
            if (pref.startsWith(INSTALLED)) {
                String pname = pref.substring(INSTALLED.length());
                boolean installed = p.getBoolean(pref, false);
                if (installed) {
                    Log.i(STAG, "Installed:" + pname);
                    ProcessInfo i = getInstalledPackage(context, pname);
                    if (i != null){
                        i.setImportance(CaratApplication.IMPORTANCE_INSTALLED);
                        result.add(i);
                        e.remove(pref);
                        edited = true;
                    }
                }
            } else if (pref.startsWith(REPLACED)) {
                String pname = pref.substring(REPLACED.length());
                boolean replaced = p.getBoolean(pref, false);
                if (replaced) {
                    Log.i(STAG, "Replaced:" + pname);
                    ProcessInfo i = getInstalledPackage(context, pname);
                    if (i != null) {
                        i.setImportance(CaratApplication.IMPORTANCE_REPLACED);
                        result.add(i);
                        e.remove(pref);
                        edited = true;
                    }
                }
            } else if (pref.startsWith(UNINSTALLED)) {
                String pname = pref.substring(UNINSTALLED.length());
                boolean uninstalled = p.getBoolean(pref, false);
                if (uninstalled) {
                    Log.i(STAG, "Uninstalled:" + pname);
                    result.add(uninstalledItem(pname, pref, e));
                    edited = true;
                }
            }
        }
        if (edited)
            e.commit();

        // FIXME: These are not used yet.
        /*
         * ActivityManager pActivityManager = (ActivityManager) context
         * .getSystemService(Activity.ACTIVITY_SERVICE); Debug.MemoryInfo[]
         * memoryInfo = pActivityManager .getProcessMemoryInfo(procMem); for
         * (Debug.MemoryInfo info : memoryInfo) { // Decide which ones of info.*
         * we want, add to a new and improved // ProcessInfo object // FIXME:
         * Not used yet, Sample needs more fields // FIXME: Which memory fields
         * to choose? //int memory = info.dalvikPrivateDirty; }
         */

        return result;
    }
    
    private static ProcessInfo uninstalledItem(String pname, String pref, SharedPreferences.Editor e) {
        ProcessInfo item = new ProcessInfo();
        item.setPName(pname);
        List<String> sigs = new LinkedList<String>();
        sigs.add("uninstalled");
        item.setAppSignatures(sigs);
        item.setPId(-1);
        item.setImportance(CaratApplication.IMPORTANCE_UNINSTALLED);
        // Remember to remove it so we do not send
        // multiple uninstall events
        e.remove(pref);
        return item;
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
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return TYPE_UNKNOWN;
        NetworkInfo i = cm.getActiveNetworkInfo();
        if (i == null)
            return TYPE_UNKNOWN;
        return i.getTypeName();
    }

    public static boolean networkAvailable(Context c) {
        String network = getNetworkStatus(c);
        return network.equals(NETWORKSTATUS_CONNECTED);
    }

    /* Get current WiFi signal Strength */
    public static int getWifiSignalStrength(Context context) {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        int wifiRssi = myWifiInfo.getRssi();
        //Log.v("WifiRssi", "Rssi:" + wifiRssi);
        return wifiRssi;

    }

    /**
     * Get Wifi MAC ADDR. Hashed and used in UUID calculation.
     */
    private static String getWifiMacAddress(Context context) {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (myWifiManager == null)
            return null;
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        if (myWifiInfo == null)
            return null;
        return myWifiInfo.getMacAddress();
    }

    /* Get current WiFi link speed */
    public static int getWifiLinkSpeed(Context context) {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        int linkSpeed = myWifiInfo.getLinkSpeed();

        //Log.v("linkSpeed", "Link speed:" + linkSpeed);
        return linkSpeed;
    }

    /* Check whether WiFi is enabled */
    public static boolean getWifiEnabled(Context context) {
        boolean wifiEnabled = false;

        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiEnabled = myWifiManager.isWifiEnabled();
        //Log.v("WifiEnabled", "Wifi is enabled:" + wifiEnabled);
        return wifiEnabled;
    }

    /* Get Wifi state: */
    public static String getWifiState(Context context) {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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
        WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = myWifiManager.getConnectionInfo();
        //Log.v("WifiInfo", "Wifi information:" + connectionInfo);
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
            if (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.BACKGROUND_DATA) == 1) {
                bacDataEnabled = true;
            }
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //Log.v("BackgroundDataEnabled", "Background data enabled? " + bacDataEnabled);
        // return bacDataEnabled;
        return true;
    }

    /* Get Current Screen Brightness Value */
    public static int getScreenBrightness(Context context) {

        int screenBrightnessValue = 0;
        try {
            screenBrightnessValue = android.provider.Settings.System.getInt(context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Log.v("ScreenBrightness", "Screen brightness value:" + screenBrightnessValue);
        return screenBrightnessValue;
    }

    public static boolean isAutoBrightness(Context context) {
        boolean autoBrightness = false;
        try {
            autoBrightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        //Log.v("AutoScreenBrightness", "Automatic Screen brightness mode is enabled:" + autoBrightness);
        return autoBrightness;
    }

    /* Check whether GPS are enabled */
    public static boolean getGpsEnabled(Context context) {
        boolean gpsEnabled = false;
        LocationManager myLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        gpsEnabled = myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //Log.v("GPS", "GPS is :" + gpsEnabled);
        return gpsEnabled;
    }

    /* check the GSM cell information */
    public static CellInfo getCellInfo(Context context) {
        CellInfo curCell = new CellInfo();

        TelephonyManager myTelManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String netOperator = myTelManager.getNetworkOperator();

        // Fix crash when not connected to network (airplane mode, underground,
        // etc)
        if (netOperator == null || netOperator.length() < 3) {
            return curCell;
        }

        /*
         * FIXME: Actually check for mobile network status == connected before
         * doing this stuff.
         */

        if (SamplingLibrary.getPhoneType(context) == PHONE_TYPE_CDMA) {
            CdmaCellLocation cdmaLocation = (CdmaCellLocation) myTelManager.getCellLocation();
            if (cdmaLocation == null) {
                //Log.v("cdmaLocation", "CDMA Location:" + cdmaLocation);
            } else {
                int cid = cdmaLocation.getBaseStationId();
                int lac = cdmaLocation.getNetworkId();
                int mnc = cdmaLocation.getSystemId();
                int mcc = Integer.parseInt(netOperator.substring(0, 3));

                curCell.CID = cid;
                curCell.LAC = lac;
                curCell.MNC = mnc;
                curCell.MCC = mcc;
                curCell.radioType = SamplingLibrary.getMobileNetworkType(context);

                //Log.v("MCC", "MCC is:" + mcc);
                //Log.v("MNC", "MNC is:" + mnc);
                //Log.v("CID", "CID is:" + cid);
                //Log.v("LAC", "LAC is:" + lac);
            }

        } else if (SamplingLibrary.getPhoneType(context) == PHONE_TYPE_GSM) {
            GsmCellLocation gsmLocation = (GsmCellLocation) myTelManager.getCellLocation();

            if (gsmLocation == null) {
                //Log.v("gsmLocation", "GSM Location:" + gsmLocation);
            } else {
                int cid = gsmLocation.getCid();
                int lac = gsmLocation.getLac();
                int mcc = Integer.parseInt(netOperator.substring(0, 3));
                int mnc = Integer.parseInt(netOperator.substring(3));

                curCell.MCC = mcc;
                curCell.MNC = mnc;
                curCell.LAC = lac;
                curCell.CID = cid;
                curCell.radioType = SamplingLibrary.getMobileNetworkType(context);

                //Log.v("MCC", "MCC is:" + mcc);
                //Log.v("MNC", "MNC is:" + mnc);
                //Log.v("CID", "CID is:" + cid);
                //Log.v("LAC", "LAC is:" + lac);
            }
        }
        return curCell;
    }

    /**
     * Return distance between <code>lastKnownLocation</code> and a newly
     * obtained location from any available provider.
     * 
     * @param c
     *            from Intent or Application.
     * @return
     */
    public static double getDistance(Context c) {
        Location l = getLastKnownLocation(c);
        double distance = 0.0;
        if (lastKnownLocation != null && l != null) {
            distance = lastKnownLocation.distanceTo(l);
        }
        lastKnownLocation = l;
        return distance;
    }

    public static Location getLastKnownLocation(Context c) {
        String provider = getBestProvider(c);
        // FIXME: Some buggy device is giving GPS to us, even though we cannot
        // use it.
        if (provider != null && !provider.equals("gps")) {
            Location l = getLastKnownLocation(c, provider);
            return l;
        }
        return null;
    }

    private static Location getLastKnownLocation(Context context, String provider) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location l = lm.getLastKnownLocation(provider);
        return l;
    }

    /* Get the distance users between two locations */
    public static double getDistance(double startLatitude, double startLongitude, double endLatitude,
            double endLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return results[0];
    }

    /**
     * Return a list of enabled LocationProviders, such as GPS, Network, etc.
     * 
     * @param context
     *            from onReceive or app.
     * @return
     */
    public static List<String> getEnabledLocationProviders(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.getProviders(true);
    }

    public static String getBestProvider(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_COARSE);
        c.setPowerRequirement(Criteria.POWER_LOW);
        String provider = lm.getBestProvider(c, true);
        return provider;
    }

    /* Check the maximum number of satellites can be used in the satellite list */
    public static int getMaxNumSatellite(Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        int maxNumSatellite = gpsStatus.getMaxSatellites();

        //Log.v("maxNumStatellite", "Maxmium number of satellites:" + maxNumSatellite);
        return maxNumSatellite;
    }

    /* Get call status */
    public static String getCallState(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

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

    private static String getDeviceId(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telManager == null)
            return null;
        return telManager.getDeviceId();
    }

    /* Get network type */
    public static String getMobileNetworkType(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        int netType = telManager.getNetworkType();
        switch (netType) {
        case TelephonyManager.NETWORK_TYPE_1xRTT:
            return NETWORK_TYPE_1xRTT;
        case TelephonyManager.NETWORK_TYPE_CDMA:
            return NETWORK_TYPE_CDMA;
        case TelephonyManager.NETWORK_TYPE_EDGE:
            return NETWORK_TYPE_EDGE;
        case EHRPD:
            return NETWORK_TYPE_EHRPD;
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
            return NETWORK_TYPE_EVDO_0;
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
            return NETWORK_TYPE_EVDO_A;
        case EVDO_B:
            return NETWORK_TYPE_EVDO_B;
        case TelephonyManager.NETWORK_TYPE_GPRS:
            return NETWORK_TYPE_GPRS;
        case TelephonyManager.NETWORK_TYPE_HSDPA:
            return NETWORK_TYPE_HSDPA;
        case TelephonyManager.NETWORK_TYPE_HSPA:
            return NETWORK_TYPE_HSPA;
        case HSPAP:
            return NETWORK_TYPE_HSPAP;
        case TelephonyManager.NETWORK_TYPE_HSUPA:
            return NETWORK_TYPE_HSUPA;
        case TelephonyManager.NETWORK_TYPE_IDEN:
            return NETWORK_TYPE_IDEN;
        case LTE:
            return NETWORK_TYPE_LTE;
        case TelephonyManager.NETWORK_TYPE_UMTS:
            return NETWORK_TYPE_UMTS;
        default:
            // If we don't know the type, just return the number and let the backend take care of it
            return netType+"";
        }
    }

    /* Get Phone Type */
    public static String getPhoneType(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        int phoneType = telManager.getPhoneType();
        switch (phoneType) {
        case TelephonyManager.PHONE_TYPE_CDMA:
            return PHONE_TYPE_CDMA;
        case TelephonyManager.PHONE_TYPE_GSM:
            return PHONE_TYPE_GSM;
        default:
            return PHONE_TYPE_NONE;
        }
    }

    /* Check is it network roaming */
    public static boolean getRoamingStatus(Context context) {
        boolean roamStatus = false;

        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        roamStatus = telManager.isNetworkRoaming();
        //Log.v("RoamingStatus", "Roaming status:" + roamStatus);
        return roamStatus;
    }

    /* Get data state */
    public static String getDataState(Context context) {
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

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
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

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
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        CellLocation LocDevice = telManager.getCellLocation();
        //Log.v("DeviceLocation", "Device Location:" + LocDevice);
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

        // ms since boot
        long uptime = SystemClock.elapsedRealtime();
        long now = System.currentTimeMillis();
        long bootTime = now - uptime;

        String[] queries = new String[] { android.provider.CallLog.Calls.TYPE, android.provider.CallLog.Calls.DATE,
                android.provider.CallLog.Calls.DURATION };

        Cursor cur = context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, queries,
                android.provider.CallLog.Calls.DATE + " > " + bootTime, null,
                android.provider.CallLog.Calls.DATE + " ASC");

        if (cur != null) {
            if (cur.moveToFirst()) {
                while (!cur.isAfterLast()) {
                    type = cur.getInt(0);
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

        int callType;
        long callDur;
        Date callDate;
        String tmpTime = null;
        String time;
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM");
        CallMonth curMonth = null;

        String[] queryFields = new String[] { android.provider.CallLog.Calls.TYPE, android.provider.CallLog.Calls.DATE,
                android.provider.CallLog.Calls.DURATION };

        Cursor myCursor = context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, queryFields,
                null, null, android.provider.CallLog.Calls.DATE + " DESC");

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
                    callInDur.put("tolCallInNum", String.valueOf(curMonth.tolCallInNum));
                    callInDur.put("tolCallInDur", String.valueOf(curMonth.tolCallInDur));
                }
                if (callType == 2) {
                    curMonth.tolCallOutNum++;
                    curMonth.tolCallOutDur += callDur;
                    callOutDur.put("tolCallOutNum", String.valueOf(curMonth.tolCallOutNum));
                    callOutDur.put("tolCallOutDur", String.valueOf(curMonth.tolCallOutDur));
                }
                if (callType == 3) {
                    curMonth.tolMissedCallNum++;
                    callInDur.put("tolMissedCallNum", String.valueOf(curMonth.tolMissedCallNum));
                }
            }
        } else {
            //Log.v("MonthType", "callType=None");
            //Log.v("MonthDate", "callDate=None");
            //Log.v("MonthDuration", "callduration =None");
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

    private static Location lastKnownLocation = null;

    public static double getBatteryLevel(Context context, Intent intent) {
        double level = intent.getIntExtra("level", -1);
        double scale = intent.getIntExtra("scale", 100);

        // use last known value
        double batteryLevel = 0.0;
        // if we have real data, change old value
        if (level > 0 && scale > 0) {
            batteryLevel = (level / scale);
        }
        return batteryLevel;
    }
    
    /**
     * Get whether the screen is on or off.
     * @return true if the screen is on.
     */
    public static int isScreenOn(Context context){
        android.os.PowerManager powerManager =
                (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null)
            if (powerManager.isScreenOn())
                return 1;
        return 0;
    }
    
    /**
     * Get the current timezone of the device.
     */

    public static String getTimeZone(Context context) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        return tz.getDisplayName();
    }
    
    /**
     * 
     * @param context
     * @return true when app installation from unknown sources is enabled. 
     */
    public static int allowUnknownSources(Context context){
        ContentResolver res = context.getContentResolver();
        int unknownSources = Settings.Secure.getInt(res, Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
        return unknownSources;
    }
    
    /**
     * 
     * @param context
     * @return true when developer mode is enabled. 
     */
    public static int isDeveloperModeOn(Context context){
        ContentResolver res = context.getContentResolver();
        int adb = Settings.Secure.getInt(res, Settings.Secure.ADB_ENABLED, 0);
        // In API level 17, this is Settings.Global.ADB_ENABLED.
        return adb;
    }
    
    
    /*
     * TODO:
     * Make the app running when the system reboots, and provide a stop button.
     * CPU and Memory info per application
     * CPU core/ frequency, CPU governors usage
     * How to motivate user to upload more samples.
     */
    

    /**
     * Safely terminate (kill) the given app.
     * @param context
     * @param packageName
     * @param label
     * @return
     */
    public static boolean killApp(Context context, String packageName, String label) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        if (am != null) {
            try {
                PackageInfo p = getPackageInfo(context, packageName);
                //Log.v(STAG, "Trying to kill proc=" + packageName + " pak=" + p.packageName);
                FlurryAgent.logEvent("Killing app=" + (label == null ? "null" : label) + " proc=" + packageName
                        + " pak=" + (p == null ? "null" : p.packageName));
                am.killBackgroundProcesses(packageName);

                return true;
            } catch (Throwable th) {
                Log.e(STAG, "Could not kill process: " + packageName, th);
            }
        }
        return false;
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
                        : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static Sample getSample(Context context, Intent intent, Sample lastSample) {
        String action = intent.getAction();

        // Construct sample and return it in the end
        Sample mySample = new Sample();
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        String uuId = p.getString(CaratApplication.REGISTERED_UUID, null);
        mySample.setUuId(uuId);
        mySample.setTriggeredBy(action);
        // required always
        long now = System.currentTimeMillis();
        mySample.setTimestamp(now / 1000.0);

        // Record first data point for CPU usage
        long[] idleAndCpu1 = readUsagePoint();

        List<ProcessInfo> processes = getRunningProcessInfoForSample(context);
        mySample.setPiList(processes);

        int screenBrightness = SamplingLibrary.getScreenBrightness(context);
        mySample.setScreenBrightness(screenBrightness);
        boolean autoScreenBrightness = SamplingLibrary.isAutoBrightness(context);
        if (autoScreenBrightness)
            mySample.setScreenBrightness(-1); // Auto
        // boolean gpsEnabled = SamplingLibrary.getGpsEnabled(context);
        // Location providers
        List<String> enabledLocationProviders = SamplingLibrary.getEnabledLocationProviders(context);
        mySample.setLocationProviders(enabledLocationProviders);

        // TODO: not in Sample yet
        // int maxNumSatellite = SamplingLibrary.getMaxNumSatellite(context);

        String network = SamplingLibrary.getNetworkStatus(context);
        String networkType = SamplingLibrary.getNetworkType(context);
        String mobileNetworkType = SamplingLibrary.getMobileNetworkType(context);

        // Required in new Carat protocol
        if (network.equals(NETWORKSTATUS_CONNECTED)) {
            if (networkType.equals("WIFI"))
                mySample.setNetworkStatus(networkType);
            else
                mySample.setNetworkStatus(mobileNetworkType);
        } else
            mySample.setNetworkStatus(network);

        // String ns = mySample.getNetworkStatus();
        // Log.d(STAG, "Set networkStatus="+ns);

        // Network details
        NetworkDetails nd = new NetworkDetails();

        // Network type
        nd.setNetworkType(networkType);
        nd.setMobileNetworkType(mobileNetworkType);
        boolean roamStatus = SamplingLibrary.getRoamingStatus(context);
        nd.setRoamingEnabled(roamStatus);
        String dataState = SamplingLibrary.getDataState(context);
        nd.setMobileDataStatus(dataState);
        String dataActivity = SamplingLibrary.getDataActivity(context);
        nd.setMobileDataActivity(dataActivity);

        // Wifi stuff
        String wifiState = SamplingLibrary.getWifiState(context);
        nd.setWifiStatus(wifiState);
        int wifiSignalStrength = SamplingLibrary.getWifiSignalStrength(context);
        nd.setWifiSignalStrength(wifiSignalStrength);
        int wifiLinkSpeed = SamplingLibrary.getWifiLinkSpeed(context);
        nd.setWifiLinkSpeed(wifiLinkSpeed);
        // Add NetworkDetails substruct to Sample
        mySample.setNetworkDetails(nd);

        /* Calling Information */
        // List<String> callInfo;
        // callInfo=SamplingLibrary.getCallInfo(context);
        /* Total call time */
        // long totalCallTime=0;
        // totalCallTime=SamplingLibrary.getTotalCallDur(context);

        /*
         * long[] incomingOutgoingIdle = getCalltimesSinceBoot(context);
         * Log.d(STAG, "Call time since boot: Incoming=" +
         * incomingOutgoingIdle[0] + " Outgoing=" + incomingOutgoingIdle[1] +
         * " idle=" + incomingOutgoingIdle[2]);
         * 
         * // Summary Call info CallInfo ci = new CallInfo(); String callState =
         * SamplingLibrary.getCallState(context); ci.setCallStatus(callState);
         * ci.setIncomingCallTime(incomingOutgoingIdle[0]);
         * ci.setOutgoingCallTime(incomingOutgoingIdle[1]);
         * ci.setNonCallTime(incomingOutgoingIdle[2]);
         * 
         * mySample.setCallInfo(ci);
         */

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
            //Log.d(STAG, "BatteryLevel: " + batteryLevel);
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

        BatteryDetails bd = new BatteryDetails();
        // otherInfo.setCPUIdleTime(totalIdleTime);
        bd.setBatteryTemperature(temperature);
        // otherInfo.setBatteryTemperature(temperature);
        bd.setBatteryVoltage(voltage);
        // otherInfo.setBatteryVoltage(voltage);
        bd.setBatteryTechnology(batteryTechnology);

        bd.setBatteryCharger(batteryCharger);
        bd.setBatteryHealth(batteryHealth);

        mySample.setBatteryDetails(bd);

        mySample.setBatteryLevel(batteryLevel);
        mySample.setBatteryState(batteryStatus);

        int[] usedFreeActiveInactive = SamplingLibrary.readMeminfo();
        if (usedFreeActiveInactive != null && usedFreeActiveInactive.length == 4) {
            mySample.setMemoryUser(usedFreeActiveInactive[0]);
            mySample.setMemoryFree(usedFreeActiveInactive[1]);
            mySample.setMemoryActive(usedFreeActiveInactive[2]);
            mySample.setMemoryInactive(usedFreeActiveInactive[3]);
        }
        // TODO: Memory Wired should have memory that is "unevictable", that
        // will always be used even when all apps are killed

        // Log.d(STAG, "serial=" + getBuildSerial());

        // Record second data point for cpu/idle time
        now = System.currentTimeMillis();
        long[] idleAndCpu2 = readUsagePoint();

        CpuStatus cs = new CpuStatus();

        cs.setCpuUsage(getUsage(idleAndCpu1, idleAndCpu2));
        cs.setUptime(getUptime());
        mySample.setCpuStatus(cs);

        
        mySample.setDeveloperMode(isDeveloperModeOn(context));
        mySample.setUnknownSources(allowUnknownSources(context));
        mySample.setScreenOn(isScreenOn(context));
        mySample.setTimeZone(getTimeZone(context));
        // printAverageFeaturePower(context);

        return mySample;
    }
    
    public static List<String> getSignatures(PackageInfo pak) {
        List<String> sigList = new LinkedList<String>();
        String[] pmInfos = pak.requestedPermissions;
        if (pmInfos != null) {
            byte[] bytes = getPermissionBytes(pmInfos);
            String hexB = convertToHex(bytes);
            sigList.add(hexB);
        }
        Signature[] sigs = pak.signatures;

        for (Signature s : sigs) {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-1");
                md.update(s.toByteArray());
                byte[] dig = md.digest();
                // Add SHA-1
                sigList.add(convertToHex(dig));

                CertificateFactory fac = CertificateFactory.getInstance("X.509");
                if (fac == null)
                    continue;
                X509Certificate cert = (X509Certificate) fac.generateCertificate(new ByteArrayInputStream(s.toByteArray()));
                if (cert == null)
                    continue;
                PublicKey pkPublic = cert.getPublicKey();
                if (pkPublic == null)
                    continue;
                String al = pkPublic.getAlgorithm();
                if (al.equals("RSA")) {
                    md = MessageDigest.getInstance("SHA-256");
                    RSAPublicKey rsa = (RSAPublicKey) pkPublic;
                    byte[] data = rsa.getModulus().toByteArray();
                    if (data[0] == 0) {
                        byte[] copy = new byte[data.length - 1];
                        System.arraycopy(data, 1, copy, 0, data.length - 1);
                        md.update(copy);
                    } else
                        md.update(data);
                    dig = md.digest();
                    // Add SHA-256 of modulus
                    sigList.add(convertToHex(dig));
                } else if (al.equals("DSA")) {
                    DSAPublicKey dsa = (DSAPublicKey) pkPublic;
                    md = MessageDigest.getInstance("SHA-256");
                    byte[] data = dsa.getY().toByteArray();
                    if (data[0] == 0) {
                        byte[] copy = new byte[data.length - 1];
                        System.arraycopy(data, 1, copy, 0, data.length - 1);
                        md.update(copy);
                    } else
                        md.update(data);
                    dig = md.digest();
                    // Add SHA-256 of public key (DSA)
                    sigList.add(convertToHex(dig));
                } else {
                    Log.e("SamplingLibrary", "Weird algorithm: " + al + " for " + pak.packageName);
                }
            } catch (NoSuchAlgorithmException e) {
                // Do nothing
            } catch (CertificateException e) {
                // Do nothing
            }

        }
        return sigList;
    }

    public static byte[] getPermissionBytes(String[] perms) {
        if (perms == null)
            return null;
        if (permList.size() == 0)
            populatePermList();
        //Log.i(STAG, "PermList Size: " + permList.size());
        byte[] bytes = new byte[permList.size() / 8 + 1];
        for (String p : perms) {
            int idx = permList.indexOf(p);
            if (idx > 0){
                int i = idx / 8;
                idx = (int) Math.pow(2, idx - i * 8);
                bytes[i] = (byte) (bytes[i] | idx);
            }
        }
        return bytes;
    }

    private static final ArrayList<String> permList = new ArrayList<String>();

    private static void populatePermList() {
        final String[] permArray = { "android.permission.ACCESS_CHECKIN_PROPERTIES",
                "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS", "android.permission.ACCESS_MOCK_LOCATION",
                "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_SURFACE_FLINGER",
                "android.permission.ACCESS_WIFI_STATE", "android.permission.ACCOUNT_MANAGER",
                "android.permission.AUTHENTICATE_ACCOUNTS", "android.permission.BATTERY_STATS",
                "android.permission.BIND_APPWIDGET", "android.permission.BIND_DEVICE_ADMIN",
                "android.permission.BIND_INPUT_METHOD", "android.permission.BIND_WALLPAPER",
                "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.BRICK",
                "android.permission.BROADCAST_PACKAGE_REMOVED", "android.permission.BROADCAST_SMS",
                "android.permission.BROADCAST_STICKY", "android.permission.BROADCAST_WAP_PUSH",
                "android.permission.CALL_PHONE", "android.permission.CALL_PRIVILEGED", "android.permission.CAMERA",
                "android.permission.CHANGE_COMPONENT_ENABLED_STATE", "android.permission.CHANGE_CONFIGURATION",
                "android.permission.CHANGE_NETWORK_STATE", "android.permission.CHANGE_WIFI_MULTICAST_STATE",
                "android.permission.CHANGE_WIFI_STATE", "android.permission.CLEAR_APP_CACHE",
                "android.permission.CLEAR_APP_USER_DATA", "android.permission.CONTROL_LOCATION_UPDATES",
                "android.permission.DELETE_CACHE_FILES", "android.permission.DELETE_PACKAGES",
                "android.permission.DEVICE_POWER", "android.permission.DIAGNOSTIC",
                "android.permission.DISABLE_KEYGUARD", "android.permission.DUMP",
                "android.permission.EXPAND_STATUS_BAR", "android.permission.FACTORY_TEST",
                "android.permission.FLASHLIGHT", "android.permission.FORCE_BACK", "android.permission.GET_ACCOUNTS",
                "android.permission.GET_PACKAGE_SIZE", "android.permission.GET_TASKS",
                "android.permission.GLOBAL_SEARCH", "android.permission.HARDWARE_TEST",
                "android.permission.INJECT_EVENTS", "android.permission.INSTALL_LOCATION_PROVIDER",
                "android.permission.INSTALL_PACKAGES", "android.permission.INTERNAL_SYSTEM_WINDOW",
                "android.permission.INTERNET", "android.permission.KILL_BACKGROUND_PROCESSES",
                "android.permission.MANAGE_ACCOUNTS", "android.permission.MANAGE_APP_TOKENS",
                "android.permission.MASTER_CLEAR", "android.permission.MODIFY_AUDIO_SETTINGS",
                "android.permission.MODIFY_PHONE_STATE", "android.permission.MOUNT_FORMAT_FILESYSTEMS",
                "android.permission.MOUNT_UNMOUNT_FILESYSTEMS", "android.permission.PERSISTENT_ACTIVITY",
                "android.permission.PROCESS_OUTGOING_CALLS", "android.permission.READ_CALENDAR",
                "android.permission.READ_CONTACTS", "android.permission.READ_FRAME_BUFFER",
                "com.android.browser.permission.READ_HISTORY_BOOKMARKS", "android.permission.READ_INPUT_STATE",
                "android.permission.READ_LOGS", "android.permission.READ_OWNER_DATA",
                "android.permission.READ_PHONE_STATE", "android.permission.READ_SMS",
                "android.permission.READ_SYNC_SETTINGS", "android.permission.READ_SYNC_STATS",
                "android.permission.REBOOT", "android.permission.RECEIVE_BOOT_COMPLETED",
                "android.permission.RECEIVE_MMS", "android.permission.RECEIVE_SMS",
                "android.permission.RECEIVE_WAP_PUSH", "android.permission.RECORD_AUDIO",
                "android.permission.REORDER_TASKS", "android.permission.RESTART_PACKAGES",
                "android.permission.SEND_SMS", "android.permission.SET_ACTIVITY_WATCHER",
                "android.permission.SET_ALWAYS_FINISH", "android.permission.SET_ANIMATION_SCALE",
                "android.permission.SET_DEBUG_APP", "android.permission.SET_ORIENTATION",
                "android.permission.SET_PREFERRED_APPLICATIONS", "android.permission.SET_PROCESS_LIMIT",
                "android.permission.SET_TIME", "android.permission.SET_TIME_ZONE", "android.permission.SET_WALLPAPER",
                "android.permission.SET_WALLPAPER_HINTS", "android.permission.SIGNAL_PERSISTENT_PROCESSES",
                "android.permission.STATUS_BAR", "android.permission.SUBSCRIBED_FEEDS_READ",
                "android.permission.SUBSCRIBED_FEEDS_WRITE", "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.UPDATE_DEVICE_STATS", "android.permission.USE_CREDENTIALS",
                "android.permission.VIBRATE", "android.permission.WAKE_LOCK", "android.permission.WRITE_APN_SETTINGS",
                "android.permission.WRITE_CALENDAR", "android.permission.WRITE_CONTACTS",
                "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_GSERVICES",
                "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS", "android.permission.WRITE_OWNER_DATA",
                "android.permission.WRITE_SECURE_SETTINGS", "android.permission.WRITE_SETTINGS",
                "android.permission.WRITE_SMS", "android.permission.WRITE_SYNC_SETTINGS" };

        for (String s : permArray)
            permList.add(s);
    }
}
