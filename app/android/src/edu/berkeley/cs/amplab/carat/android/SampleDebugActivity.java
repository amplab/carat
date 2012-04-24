package edu.berkeley.cs.amplab.carat.android;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.berkeley.cs.amplab.carat.android.storage.CaratDB;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.app.Activity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.os.Bundle;
import android.widget.TextView;

public class SampleDebugActivity extends Activity {

	/** Called when the activity is first created. */

	TextView batteryResult;
	TextView CpuResult;
	TextView MemoryResult;
	TextView RunProcResult;
	TextView WifiSignalResult;

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
	CaratDB db = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample);
		db = new CaratDB(getApplicationContext());

		TextView uuid = (TextView) findViewById(R.id.uuid);
		uuid.setText("UUID: "
				+ SamplingLibrary.getUuid(getApplicationContext()));
		CpuResult = (TextView) findViewById(R.id.CpuResult);
		MemoryResult = (TextView) findViewById(R.id.MemoryResult);
		batteryResult = (TextView) findViewById(R.id.Result);
		RunProcResult = (TextView) findViewById(R.id.runningProcesses);
		// WifiSignalResult =(TextView)findViewById(R.id.WifiSignalStrength);

		// TelephonyManager TM = ( TelephonyManager
		// )getSystemService(Context.TELEPHONY_SERVICE);
		// MyPhoneStateListener cdmaStregnthInfo= new MyPhoneStateListener();
		// TM.listen(cdmaStregnthInfo
		// ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		// Allow swipe to change tabs
		findViewById(R.id.sampleScroll).setOnTouchListener(
				SwipeListener.instance);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		updateSampleScreen();
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		super.finish();
	}

	private void updateSampleScreen() {
		Sample[] list = db.querySamples();
		StringBuilder sb = new StringBuilder();
		String dfs = "EEE MMM dd HH:mm:ss zzz yyyy";
		SimpleDateFormat df = new SimpleDateFormat(dfs);
		for (Sample s : list){
			String trig = s.getTriggeredBy().replace("android.intent.action.", "").replace("edu.berkeley.cs.amplab.carat.android.", "");
			sb.append(df.format(new Date((long) (s.getTimestamp() * 1000)))
					+ "\n" + trig + "\n" + "Battery "
					+ s.getBatteryState() + ", " + s.getBatteryLevel() + "%\n"
					+ "Network " + s.getNetworkStatus() + "\n" + "memoryUser="
					+ s.getMemoryUser() + " Free=" + s.getMemoryFree() + "\n"
					+ "Active=" + s.getMemoryActive() + " Inactive="
					+ s.getMemoryInactive() + " Wired=" + s.getMemoryWired()
					+ "\n" + "[" + s.getPiList().size() + " processes]\n\n");
		}

		TextView v = (TextView) findViewById(R.id.Result);
		v.setText(sb.toString());
	}

	/*
	 * private class MyPhoneStateListener extends PhoneStateListener {
	 * 
	 * @Override public void onSignalStrengthsChanged(SignalStrength ss) {
	 * super.onSignalStrengthsChanged(ss); int ecio= ss.getCdmaEcio(); int
	 * strength = ss.getCdmaDbm();
	 * Log.v("cdmaSignal","CDMA:"+strength+"CDMA ecio:"+ecio); }
	 * 
	 * }
	 */
}
