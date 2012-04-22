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

import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Debug;
import android.view.KeyEvent;
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
		WifiSignalResult =(TextView)findViewById(R.id.WifiSignalStrength);

		 new Thread() {
			public void run() {
				IntentFilter tIntentFilter = new IntentFilter();
				tIntentFilter.addAction(Intent.ACTION_TIME_TICK);
				tIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

				IntentFilter bIntentFilter = new IntentFilter();
				bIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
				bIntentFilter.addAction(Intent.ACTION_BATTERY_LOW);
				bIntentFilter.addAction(Intent.ACTION_BATTERY_OKAY);

				IntentFilter totalIntentFilter = new IntentFilter();
				totalIntentFilter.addAction(Intent.ACTION_TIME_TICK);
				totalIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
				totalIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
				totalIntentFilter.addAction(Intent.ACTION_BATTERY_LOW);
				totalIntentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
				// Register a Sampler that keeps sampling stuff on the
				// background
				registerReceiver(new Sampler(
						(CaratApplication) getApplication()), totalIntentFilter);
				//registerReceiver(memoryBroadcastRecv, tIntentFilter);
				//registerReceiver(batteryBroadcastRecv, bIntentFilter);
			}
		}.start();

		// Allow swipe to change tabs
		findViewById(R.id.sampleScroll).setOnTouchListener(
				SwipeListener.instance);

	}
	

}
