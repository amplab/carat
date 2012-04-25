package edu.berkeley.cs.amplab.carat.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.cs.amplab.carat.thrift.AndroidSample;
import edu.berkeley.cs.amplab.carat.thrift.ProcessInfo;
import edu.berkeley.cs.amplab.carat.thrift.Sample;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.location.GpsStatus;
import android.location.Location;
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
    public static String NETWORKSTATUS_DISCONNECTED = "disconnected";
    public static String NETWORKSTATUS_DISCONNECTING = "disconnecting";
    public static String NETWORKSTATUS_CONNECTED = "connected";
    public static String NETWORKSTATUS_CONNECTING = "connecting";

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
    
    public static Map<String, String> getSystemDetails(){
		Map<String, String> results = new HashMap<String, String>();
		// TODO: Some of this should be added to registration to identify the device and OS.
		// Cyanogenmod and others may have different kernels etc that affect performance.
		
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
            Log.i("meminfo", "Load: " + load + " 1:" + toks[1]);
            int total = Integer.parseInt(toks[1]);
            load = reader.readLine();
            toks = load.split("\\s+");
            Log.i("meminfo", "Load: " + load + " 1:" + toks[1]);
            int free = Integer.parseInt(toks[1]);
            load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine();
            load = reader.readLine();
            toks = load.split("\\s+");
            Log.i("meminfo", "Load: " + load + " 1:" + toks[1]);
            int act = Integer.parseInt(toks[1]);
            load = reader.readLine();
            toks = load.split("\\s+");
            Log.i("meminfo", "Load: " + load + " 1:" + toks[1]);
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
        Log.i("Mem", "Total mem:" + totalMem);
        Log.i("Mem", "Mem Used:" + memUsed);
        return new int[] { totalMem, memUsed };
    }

    /**
     * Read CPU usage from /proc/stat, return a fraction of
     * usage/(usage+idletime)
     * 
     * @return a fraction of usage/(usage+idletime)
     */
    public static float readUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" ");

            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
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

            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3])
                    + Long.parseLong(toks[4]) + Long.parseLong(toks[6])
                    + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public static List<RunningAppProcessInfo> getRunningProcessInfo(
            Context context) {

        ActivityManager pActivityManager = (ActivityManager) context
                .getSystemService(Activity.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> RunningProcList = null;
        RunningProcList = pActivityManager.getRunningAppProcesses();

        return RunningProcList;
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

        // Collected in the same loop to save computation.
        int[] procMem = new int[list.size()];

        for (RunningAppProcessInfo pi : list) {
            ProcessInfo item = new ProcessInfo();
            item.setPId(pi.pid);
            item.setPName(pi.processName);
            procMem[list.indexOf(pi)] = pi.pid;
            // FIXME: More fields will need to be added here, but ProcessInfo
            // needs to change.
            /*
             * uid importance lru
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

    public static long getTotalCpuTime() throws IOException {
        long totalCpuTime = 0;
        File file = new File("/proc/stat");
        FileInputStream in = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(in),
                READ_BUFFER_SIZE);
        String str = br.readLine();
        String[] cpuTotal = str.split(" ");
        br.close();

        totalCpuTime = Long.parseLong(cpuTotal[2])
                + Long.parseLong(cpuTotal[3]) + Long.parseLong(cpuTotal[4])
                + Long.parseLong(cpuTotal[6]) + Long.parseLong(cpuTotal[7])
                + Long.parseLong(cpuTotal[8]);
        totalCpuTime /= 100;
        return totalCpuTime;
    }

    public static long getTotalIdleTime() throws IOException {
        long totalIdleTime = 0;
        File file = new File("/proc/stat");
        FileInputStream in = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(in),
                READ_BUFFER_SIZE);
        String str = br.readLine();
        String[] idleTotal = str.split(" ");
        br.close();

        totalIdleTime = Long.parseLong(idleTotal[5]);
        totalIdleTime /= 100;
        return totalIdleTime;
    }

    public static long getTotalCpuUsage() {

        String[] cpuUsage;
        long totalCpuUsage = 0;
        try {
            File file = new File("/proc/stat");
            FileInputStream in = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(in),
                    READ_BUFFER_SIZE);
            String str = br.readLine();
            cpuUsage = str.split(" ");
            br.close();

            long idle1 = Long.parseLong(cpuUsage[5]);
            long cpu1 = getTotalCpuTime();

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            File file2 = new File("/proc/stat");
            FileInputStream in2 = new FileInputStream(file2);
            BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
            str = br2.readLine();
            cpuUsage = str.split(" ");
            br2.close();

            long idle2 = Long.parseLong(cpuUsage[5]);
            long cpu2 = getTotalCpuTime();
            if (cpu2 + idle2 - (cpu1 + idle1) > 0)
                totalCpuUsage = (100 * (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1)));
            Log.v("CPUusage", String.valueOf(totalCpuUsage));
            return totalCpuUsage;
        } catch (IOException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

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

    public static String getUptime() {
        long uptime = SystemClock.elapsedRealtime();
        int seconds = (int) (uptime / 1000) % 60;
        int minutes = (int) (uptime / (1000 * 60) % 60);
        int hours = (int) (uptime / (1000 * 60 * 60) % 24);
        String tmp = "\nThe uptime is :" + hours + "hr:" + minutes + "mins:"
                + seconds + "sec.\n";
        return tmp;

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
    
    public static boolean networkAvailable(Context c){
		String network = getNetworkStatus(c);
		return network.equals(NETWORKSTATUS_CONNECTED);
	}

    /* Get current WiFi signal Strength */
    public static int getWifiSignalStrength(Context context) {

        WifiManager myWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        int wifiRssi = myWifiInfo.getRssi();
        Log.i("WifiRssi", "Rssi:" + wifiRssi);
        return wifiRssi;

    }

    /* Get current WiFi link speed */
    public static int getWifiLinkSpeed(Context context) {

        WifiManager myWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
        int linkSpeed = myWifiInfo.getLinkSpeed();

        Log.i("linkSpeed", "Link speed:" + linkSpeed);
        return linkSpeed;
    }

    /* Get Current Screen Brightness Value */
    public static float getScreenBrightness(Context context) {

        float screenBrightnessValue = 0;
        try {
            screenBrightnessValue = android.provider.Settings.System.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.i("ScreenBrightness", "Screen brightness value:"
                + screenBrightnessValue);
        return screenBrightnessValue;
    }

    /* Check whether GPS are enabled */
    public static boolean getGpsEnabled(Context context) {
        boolean gpsEnabled= false;
        LocationManager myLocationManager = (LocationManager) context  
                .getSystemService(Context.LOCATION_SERVICE);  
        gpsEnabled=myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.i("GPS", "GPS is :" +gpsEnabled);        
        return gpsEnabled;
    }

    /* Check the maximum number of satellites can be used in the satellite list */
    public static int getMaxNumSatellite(Context context) {

        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        int maxNumSatellite = gpsStatus.getMaxSatellites();

        Log.i("maxNumStatellite", "Maxmium number of satellites:"
                + maxNumSatellite);
        return maxNumSatellite;
    }
    
    /* Get call status: value 0: CALL_STATE_IDLE 1: CALL_STATE_RINGING 2: 
    CALL_STATE_OFFHOOK */
    public static int getCallState(Context context){
        TelephonyManager telManager = (TelephonyManager) context
               .getSystemService(Context.TELEPHONY_SERVICE);
            
        int callState=telManager.getCallState();
        Log.i("callstate","Call state:"+ callState);
            
        return callState;       
        }
        
    /* Get network type: value 0: NETWORK_TYPE_UNKNOWN 1: NETWORK_TYPE_GPRS 
     * 2: NETWORK_TYPE_EDGE 3: NETWORK_TYPE_UMTS 4: NETWORK_TYPE_CDMA 
     * 5: NETWORK_TYPE_EVDO_0 6: NETWORK_TYPE_EVDO_A 7: NETWORK_TYPE_1xRTT 
     * 8: NETWORK_TYPE_HSDPA 9: NETWORK_TYPE_HSUPA 10: NETWORK_TYPE_HSPA 
     * 11: NETWORK_TYPE_IDEN 12: NETWORK_TYPE_EVDO_B 13: NETWORK_TYPE_LTE 
     * 14: NETWORK_TYPE_EHRPD 15: NETWORK_TYPE_HSPAP*/      
    public static int getNetworkType(Context context){
         TelephonyManager telManager = (TelephonyManager) context
                 .getSystemService(Context.TELEPHONY_SERVICE);
            
         int netType=telManager.getNetworkType();
         Log.i("NetworkType","Network type:"+ netType);      
            
         return netType;
        }
        
    /* Check is it network roaming*/
    public static boolean getRoamingStatus(Context context){
          boolean roamStatus=false;
     
          TelephonyManager telManager = (TelephonyManager) context
                  .getSystemService(Context.TELEPHONY_SERVICE);
            
          roamStatus=telManager.isNetworkRoaming();
          Log.i("RoamingStatus","Roaming status:"+ roamStatus);          
          return roamStatus;
        }

    /* Get data state: 0: DATA_DISCONNECTED 1: DATA_CONNECTING 2: 
    DATA_CONNECTED 3: DATA_SUSPENDED*/
    public static int getDataState(Context context){
           TelephonyManager telManager = (TelephonyManager) context
                   .getSystemService(Context.TELEPHONY_SERVICE);
            
           int dataState=telManager.getDataState();
           Log.i("DataState","Data state:"+ dataState); 
           
           return dataState;
        }
        
    /* Get data activity: 0: DATA_ACTIVITY_NONE 1: DATA_ACTIVITY_IN 
    * 2: DATA_ACTIVITY_OUT 3: DATA_ACTIVITY_INOUT 4: DATA_ACTIVITY_DORMANT*/
    public static int getDataActivity(Context context){
           TelephonyManager telManager = (TelephonyManager) context
                   .getSystemService(Context.TELEPHONY_SERVICE);
            
           int dataActivity=telManager.getDataActivity();
           Log.i("DataActivity","Data activity:"+ dataActivity); 
           
           return dataActivity;
        }
        
    /* Get the current location of the device */
    public static CellLocation getDeviceLocation(Context context){
           TelephonyManager telManager = (TelephonyManager) context
                   .getSystemService(Context.TELEPHONY_SERVICE);
            
           CellLocation LocDevice=telManager.getCellLocation();
           
           Log.i("DeviceLocation","Device Location:"+ LocDevice); 
           return LocDevice;
        }
  
    public static Sample getSample(Context context, Intent intent,
            Sample lastSample) {
        String action = intent.getAction();

        // Construct sample and return it in the end
        Sample mySample = new Sample();
        AndroidSample otherInfo = new AndroidSample();
        mySample.setUuId(SamplingLibrary.getUuid(context));
        mySample.setTriggeredBy(action);
        // required always
        mySample.setTimestamp(System.currentTimeMillis() / 1000.0);

        // FIXED: Not used yet, Sample needs more fields
        String MemoryTotalInfo = SamplingLibrary.getMemoryInfo();

        // FIXED: Not used yet, Sample needs more fields
        long totalCpuTime = 0, totalIdleTime = 0;
        try {
            totalCpuTime = SamplingLibrary.getTotalCpuTime();
            totalIdleTime = SamplingLibrary.getTotalIdleTime();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // FIXED: Not used yet, Sample needs more fields
        long totalCpuUsage = SamplingLibrary.getTotalCpuUsage();

        List<ProcessInfo> processes = getRunningProcessInfoForSample(context);
        mySample.setPiList(processes);

        int wifiSignalStrength = SamplingLibrary.getWifiSignalStrength(context);
        int wifiLinkSpeed = SamplingLibrary.getWifiLinkSpeed(context);

        float screenbrightnessVal = SamplingLibrary
                .getScreenBrightness(context);
        boolean gpsEnabled=SamplingLibrary.getGpsEnabled(context);
        int maxNumSatellite = SamplingLibrary.getMaxNumSatellite(context);
        int callState=SamplingLibrary.getCallState(context);
        int networkType=SamplingLibrary.getNetworkType(context);
        boolean roamStatus=SamplingLibrary.getRoamingStatus(context);
        int dataState=SamplingLibrary.getDataState(context);
        int dataActivity=SamplingLibrary.getDataActivity(context);
        CellLocation deviceLoc=SamplingLibrary.getDeviceLocation(context);

        double level = intent.getIntExtra("level", -1);
        int health = intent.getIntExtra("health", 0);
        double scale = intent.getIntExtra("scale", 100);
        int status = intent.getIntExtra("status", 0);
        // FIXED: Not used yet, Sample needs more fields
        int voltage = intent.getIntExtra("voltage", 0);
        // FIXED: Not used yet, Sample needs more fields
        int temperature = intent.getIntExtra("temperature", 0);
        int plugged = intent.getIntExtra("plugged", 0);
        // use last known value
        double batteryLevel = 0.0;
        if (lastSample != null)
            batteryLevel = lastSample.getBatteryLevel();
        // if we have real data, change old value
        if (level > 0 && scale > 0) {
            batteryLevel = (level / scale);
            Log.i("SamplingLibrary", "BatteryLevel: " + batteryLevel);
        }

        // FIXED: Not used yet, Sample needs more fields
        String Batteryhealth = "";
        String Batterystatus = "Unknown";

        switch (health) {

        case BatteryManager.BATTERY_HEALTH_DEAD:
            Batteryhealth = "Dead";
            break;
        case BatteryManager.BATTERY_HEALTH_GOOD:
            Batteryhealth = "Good";
            break;
        case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
            Batteryhealth = "Over voltage";
            break;
        case BatteryManager.BATTERY_HEALTH_OVERHEAT:
            Batteryhealth = "Overheat";
            break;
        case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            Batteryhealth = "Unknown";
            break;
        case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
            Batteryhealth = "Unspecified failure";
            break;
        }

        switch (status) {

        case BatteryManager.BATTERY_STATUS_CHARGING:
            Batterystatus = "Charging";
            break;
        case BatteryManager.BATTERY_STATUS_DISCHARGING:
            Batterystatus = "Discharging";
            break;
        case BatteryManager.BATTERY_STATUS_FULL:
            Batterystatus = "Full";
            break;
        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
            Batterystatus = "Not charging";
            break;
        case BatteryManager.BATTERY_STATUS_UNKNOWN:
            Batterystatus = "Unknown";
            break;
        default:
            // use last known value
            if (lastSample != null)
                Batterystatus = lastSample.getBatteryState();
        }

        // FIXED: Not used yet, Sample needs more fields
        String Batteryplugged = "Unplugged";
        switch (plugged) {

        case BatteryManager.BATTERY_PLUGGED_AC:
            Batteryplugged = "Plugged AC";
            break;
        case BatteryManager.BATTERY_PLUGGED_USB:
            Batteryplugged = "Plugged USB";
            break;
        }
        otherInfo.setCPUTotalTime(totalCpuTime);
        otherInfo.setCPUIdleTime(totalIdleTime);
        otherInfo.setMemoryTotalInfo(MemoryTotalInfo);
        otherInfo.setBatteryTemperature(temperature);
        otherInfo.setBatteryVoltage(voltage);
        otherInfo.setCPUUsage(totalCpuUsage);
        otherInfo.setWifiSignalStrength(wifiSignalStrength);
        otherInfo.setWifiLinkSpeed(wifiLinkSpeed);
        otherInfo.setBatteryStatus(Batterystatus);
        otherInfo.setBatteryPlugged(Batteryplugged);
        otherInfo.setBatteryHealth(Batteryhealth);
        // Required in new Carat protocol

        mySample.setNetworkStatus(SamplingLibrary.getNetworkStatus(context));
        mySample.setBatteryLevel(batteryLevel);
        mySample.setBatteryState(Batterystatus);

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

        // Deprecated, readMeminfo gives all the 4 values
        // mySample.setMemoryFree(Integer.parseInt(SamplingLibrary.getMemoryFree()));

        return mySample;
    }

}
