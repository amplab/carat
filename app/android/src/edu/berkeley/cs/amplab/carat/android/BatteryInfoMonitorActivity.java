package edu.berkeley.cs.amplab.carat.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.thrift.TException;

import edu.berkeley.cs.amplab.carat.android.storage.CaratDataStorage;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

public class BatteryInfoMonitorActivity extends Activity {

	/** Called when the activity is first created. */

	TextView batteryResult;
	TextView CpuResult;
	TextView MemoryResult;
	TextView RunProcResult;
	TextView WifiSignalResult;

	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private int second;
	long totalCpuTime;
	long totalIdleTime;
	long totalCpuUsage;
	/* Running process */
	List<RunningAppProcessInfo> runningProcess;

	/* Memory Info */
	String MemoryTotalInfo;

	/* battery Variable */
	double Batterylevel = 0;
	String Batterystatus = null;
	String Batteryhealth = null;
	StringBuilder sbattery = new StringBuilder();

	/* Storage */
	// Thread myStorageThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample);
		
		TextView uuid = (TextView) findViewById(R.id.uuid);
		uuid.setText("UUID: "
				+ SamplingLibrary.getUuid(getApplicationContext()));
		CpuResult = (TextView) findViewById(R.id.CpuResult);
		MemoryResult = (TextView) findViewById(R.id.MemoryResult);
		batteryResult = (TextView) findViewById(R.id.Result);
		RunProcResult = (TextView) findViewById(R.id.runningProcesses);
		//WifiSignalResult =(TextView)findViewById(R.id.WifiSignalStrength);
	    
		//TelephonyManager TM = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        //MyPhoneStateListener cdmaStregnthInfo= new MyPhoneStateListener();
        //TM.listen(cdmaStregnthInfo ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		// Move reg to Manifest so it is active when the app is not 
        /*
		 new Thread() {
			public void run() {
				IntentFilter totalIntentFilter = new IntentFilter();
				totalIntentFilter.addAction(Intent.ACTION_TIME_TICK);
				totalIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
				totalIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
				totalIntentFilter.addAction(Intent.ACTION_BATTERY_LOW);
				totalIntentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
				totalIntentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
				totalIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
				totalIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

				// Register a Sampler that keeps sampling stuff on the
				// background
				registerReceiver(new Sampler(), totalIntentFilter);
			}
		}.start();*/
		// Allow swipe to change tabs
		findViewById(R.id.sampleScroll).setOnTouchListener(
				SwipeListener.instance);

	}
	
	/*private class MyPhoneStateListener extends PhoneStateListener
    {
      @Override
      public void onSignalStrengthsChanged(SignalStrength ss)
      {
         super.onSignalStrengthsChanged(ss);
         int ecio= ss.getCdmaEcio();
         int strength = ss.getCdmaDbm();
         Log.v("cdmaSignal","CDMA:"+strength+"CDMA ecio:"+ecio);
      }

    }*/

}
