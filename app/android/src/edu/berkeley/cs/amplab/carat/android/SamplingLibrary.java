package edu.berkeley.cs.amplab.carat.android;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Debug;
import android.provider.Settings.Secure;
import android.util.Log;

/**
 * Library class for methods that obtain information about the phone that is
 * running Carat.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public final class SamplingLibrary {
  
  /** Library class, prevent instantiation */
  private SamplingLibrary() {
  }
  
  /**
   * Returns a randomly generated unique identifier that stays constant for the lifetime of the device.
   * (May change if wiped). This is probably our best choice for a UUID across the Android landscape,
   * since it is present on both phones and non-phones.
   * @return a String that uniquely identifies this device.
   */
  public static String getUuid(Context c){
	  return Secure.getString(c.getContentResolver(), Secure.ANDROID_ID); 
  }
  
  /**
   * Returns the model of the device running Carat, for example "sdk" for the emulator,
   * Galaxy Nexus for Samsung Galaxy Nexus.
   * @return the model of the device running Carat, for example "sdk" for the emulator,
   * Galaxy Nexus for Samsung Galaxy Nexus.
   */
  public static String getModel(){
    return android.os.Build.MODEL;
  }
  
  /**
   * Returns the manufacturer of the device running Carat, for example "google" or "samsung".
   * @return the manufacturer of the device running Carat, for example "google" or "samsung".
   */
  public static String getManufacturer(){
    return android.os.Build.MANUFACTURER;
  }
  
  /**
   * Returns the OS version of the device running Carat, for example 2.3.3 or 4.0.2.
   * @return the OS version of the device running Carat, for example 2.3.3 or 4.0.2.
   */
  public static String getOsVersion(){
    return android.os.Build.VERSION.RELEASE;
  }

  /**
   * Read memory information from /proc/meminfo. Return total, used,
   * active+inactive, and active memory.
   * 
   * @return an int[] with total, used, active+inactive, and active memory, in
   *         kB, in that order.
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
      return new int[] { total, total - free, act + inact, act };
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return new int[] { 0, 0, 0, 0 };
  }

  /**
   * Read memory usage using the public Android API methods in ActivityManager,
   * such as MemoryInfo and getProcessMemoryInfo.
   * 
   * @param c
   *          the Context from the running Activity.
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
    Log.i("Mem", "Total mem:" + totalMem);
    Log.i("Mem", "Mem Used:" + memUsed);
    return new int[] { totalMem, memUsed };
  }
  
  public static List<RunningAppProcessInfo> getRunningProcessInfo(Context c) {
	  	ActivityManager man = (ActivityManager) c.getSystemService(Activity.ACTIVITY_SERVICE);
	  	 return  man.getRunningAppProcesses();
  }
  
  /**
   * Read CPU usage from /proc/stat, return a fraction of usage/(usage+idletime)
   * 
   * @return a fraction of usage/(usage+idletime)
   */
  public static float readUsage() {
    try {
      RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
      String load = reader.readLine();

      String[] toks = load.split(" ");

      long idle1 = Long.parseLong(toks[5]);
      long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
          + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

      try {
        Thread.sleep(360);
      } catch (Exception e) {
      }

      reader.seek(0);
      load = reader.readLine();
      reader.close();

      toks = load.split(" ");

      long idle2 = Long.parseLong(toks[5]);
      long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
          + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

      return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return 0;
  }
}
