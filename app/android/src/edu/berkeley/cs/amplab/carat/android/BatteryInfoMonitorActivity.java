package edu.berkeley.cs.amplab.carat.android;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

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
import android.os.SystemClock;
import android.widget.TextView;

public class BatteryInfoMonitorActivity extends Activity {

	/** Called when the activity is first created. */

	TextView batteryResult;
	TextView CpuResult;
	TextView MemoryResult;
	TextView RunningProcessResult;
	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private int second;
	private long ut;
	long totalCpuTime = 0;
	long totalIdleTime = 0;
	long totalCpuUsage = 0;
	long myProcessCpuUsage = 0;
	private ActivityManager pActivityManager = null;
	private List<RunningAppProcessInfo> RunningProcList = null;
	private Sample mySample = new Sample();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample);
		TextView uuid = (TextView) findViewById(R.id.uuid);
		mySample.setUuId(SamplingLibrary.getUuid(getApplicationContext()));
		uuid.setText("UUID: " + mySample.getUuId());

		CpuResult = (TextView) findViewById(R.id.CpuResult);
		MemoryResult = (TextView) findViewById(R.id.MemoryResult);
		batteryResult = (TextView) findViewById(R.id.Result);
		RunningProcessResult = (TextView) findViewById(R.id.RuningProcessResult);
		new Thread() {
			public void run() {
				IntentFilter tIntentFilter = new IntentFilter();
				tIntentFilter.addAction(Intent.ACTION_TIME_TICK);
				tIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

				IntentFilter bIntentFilter = new IntentFilter();
				bIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
				bIntentFilter.addAction(Intent.ACTION_BATTERY_LOW);
				bIntentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
				// bIntentFilter.addAction(Intent.ACTION_TIME_TICK);

				registerReceiver(memoryBroadcastRecv, tIntentFilter);
				registerReceiver(batteryBroadcastRecv, bIntentFilter);
			}
		}.start();

		// Allow swipe to change tabs
		findViewById(R.id.sampleScroll).setOnTouchListener(
				SwipeListener.instance);
	}

	protected void onResume() {
		super.onResume();
		cpuInfo();
		memoryInfo();
		getRunningProcess();
	}

	protected void onRestart() {
		super.onRestart();
		cpuInfo();
		memoryInfo();
		getRunningProcess();
	}

	protected void onDestroy() {
		super.onDestroy();
		CaratApplication app = (CaratApplication) this.getApplication();
		app.s.writeObject(mySample, CaratDataStorage.SAMPLE_FILE);
		unregisterReceiver(batteryBroadcastRecv);
		unregisterReceiver(memoryBroadcastRecv);
	}

	protected long getUptime() {
		long uptime = SystemClock.elapsedRealtime();
		return uptime;

	}

	private BroadcastReceiver memoryBroadcastRecv = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			final String ac = intent.getAction();

			if (ac.equals(Intent.ACTION_TIME_TICK)
					|| ac.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				/* The tick interval is ONE MINUTE only and can not be changed. */
				memoryInfo();
				cpuInfo();
				getRunningProcess();
			}
		}
	};

	private void memoryInfo() {
		MemoryInfoDetails memory = new MemoryInfoDetails();
		String tmp = memory.getMemoryInfo();
		MemoryResult.setText(tmp);
	}

	private void cpuInfo() {

		CpuInfoDetails cpuInfoUsage = new CpuInfoDetails();

		try {
			totalCpuTime = cpuInfoUsage.getTotalCpuTime();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			totalIdleTime = cpuInfoUsage.getTotalIdleTime();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		totalCpuUsage = cpuInfoUsage.getTotalCpuUsage();

		ut = getUptime();
		int seconds = (int) (ut / 1000) % 60;
		int minutes = (int) (ut / (1000 * 60) % 60);
		int hours = (int) (ut / (1000 * 60 * 60) % 24);

		String tmp = "CPU Use Details:  \n" + "Total cpu usage time:"
				+ String.valueOf(totalCpuTime) + "\n" + "Total cpu Idle time:"
				+ String.valueOf(totalIdleTime) + "\nTotal cpu usage:"
				+ String.valueOf(totalCpuUsage) + "%\n" + "Uptime: "
				+ hours + "h " + minutes + "m " + seconds + "s.\n";
		CpuResult.setText(tmp);

	}

	public void getRunningProcess() {

		pActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		RunningProcList = pActivityManager.getRunningAppProcesses();
		int processNUM = RunningProcList.size();

		StringBuffer rProcess = new StringBuffer();
		rProcess.append("Running Process Details:\n")
				.append("Current total number of processes:")
				.append(processNUM);

		for (int i = 0; i < RunningProcList.size(); i++) {

			int processPID = RunningProcList.get(i).pid;
			int userUID = RunningProcList.get(i).uid;
			String processNAME = RunningProcList.get(i).processName;

			int[] procMem = new int[] { processPID };
			Debug.MemoryInfo[] memoryInfo = pActivityManager
					.getProcessMemoryInfo(procMem);
			int memeorySIZE = memoryInfo[0].dalvikPrivateDirty;

			rProcess.append("\nProcess ID:").append(processPID)
					.append("\nUser ID:").append(userUID)
					.append("\nProcess Name:").append(processNAME)
					.append("\nOccupied memory size:").append(memeorySIZE)
					.append("kb\n");
			RunningProcessResult.setText(rProcess);
		}
	}

	private BroadcastReceiver batteryBroadcastRecv = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			String ac = intent.getAction();

			if (ac.equals(Intent.ACTION_BATTERY_CHANGED)) {

				StringBuilder sbattery = new StringBuilder();

				double initiallevel = intent.getIntExtra("level", 0);
				int health = intent.getIntExtra("health", 0);
				double scale = intent.getIntExtra("scale", 100);
				int status = intent.getIntExtra("status", 0);
				int voltage = intent.getIntExtra("voltage", 0);
				int temperature = intent.getIntExtra("temperature", 0);
				int plugged = intent.getIntExtra("plugged", 0);
				double level = 0;

				if (initiallevel > 0 && scale > 0) {
					level = (initiallevel * 100 / scale);
				}

				sbattery.append("The battery level is:").append(level)
						.append("%");

				String Batteryhealth = null;

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

				sbattery.append("\nThe Battery health is:").append(
						Batteryhealth);
				sbattery.append("\nThe battery scale is:").append(scale);

				String Batterystatus = null;

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
				}

				sbattery.append("\nThe battery status is:").append(
						Batterystatus);
				sbattery.append("\nThe battery voltage is:").append(voltage);
				sbattery.append("\nThe battery temperature is:").append(
						temperature);

				String Batteryplugged = null;

				switch (plugged) {

				case BatteryManager.BATTERY_PLUGGED_AC:
					Batteryplugged = "Plugged AC";
					break;
				case BatteryManager.BATTERY_PLUGGED_USB:
					Batteryplugged = "Plugged USB";
					break;
				}

				sbattery.append("\nPlugged in: ").append(
						Batteryplugged);

				Calendar date = Calendar.getInstance();

				year = date.get(Calendar.YEAR);
				month = date.get(Calendar.MONTH);
				day = date.get(Calendar.DAY_OF_MONTH);
				hour = date.get(Calendar.HOUR_OF_DAY);
				minute = date.get(Calendar.MINUTE);
				second = date.get(Calendar.SECOND);

				String tmp = "Current Time:" + Integer.toString(year) + "."
						+ Integer.toString(month) + "." + Integer.toString(day)
						+ "," + Integer.toString(hour) + ":"
						+ Integer.toString(minute) + ":"
						+ Integer.toString(second) + "\n" + sbattery + "\n";
				batteryResult.setText(tmp);
				mySample.setBatteryLevel(level);
				mySample.setBatteryState(Batterystatus);
			}

		}
	};
}
