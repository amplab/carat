package edu.berkeley.cs.amplab.carat.android;

import edu.berkeley.cs.amplab.carat.thrift.Reports;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;

/**
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class CaratMyDeviceActivity extends Activity {

	
	private CaratApplication app = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mydevice);
		app = (CaratApplication) this.getApplication();
		findViewById(R.id.scrollView1).setOnTouchListener(SwipeListener.instance);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		new Thread(){
			public void run(){
				app.c.refreshReports();
			}
		}.start();
		
		setModelAndVersion();
		setMemory();
		setReportData();
		super.onResume();
	}

	/**
	 * Called when View Process List is clicked.
	 * 
	 * @param v
	 *            The source of the click.
	 */
	public void onClickViewProcessList(View v) {
		//toggleColors();
	}
	
	/**
	 * Called when Jscore additional info button is clicked.
	 * 
	 * @param v
	 *            The source of the click.
	 */
	public void showJscoreInfo(View v) {
		 Intent myIntent = new Intent(v.getContext(), CaratJscoreActivity.class);
		 //findViewById(R.id.scrollView1).startAnimation(CaratMainActivity.outtoLeft);
         startActivityForResult(myIntent, 0);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		findViewById(R.id.scrollView1).startAnimation(CaratMainActivity.inFromLeft);
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setModelAndVersion() {
		// Device model
		String model = SamplingLibrary.getModel();

		// Android version
		String version = SamplingLibrary.getOsVersion();

		Window win = this.getWindow();
		// The info icon needs to change from dark to light.
		TextView mText = (TextView) win.findViewById(R.id.dev_value);
		mText.setText(model);
		mText = (TextView) win.findViewById(R.id.os_ver_value);
		mText.setText(version);

		/*
		Log.i("SetModel", "board:" + android.os.Build.BOARD);
		Log.i("SetModel", "bootloader:" + android.os.Build.BOOTLOADER);
		Log.i("SetModel", "brand:" + android.os.Build.BRAND);
		Log.i("SetModel", "CPU_ABI 1 and 2:" + android.os.Build.CPU_ABI + ", "
				+ android.os.Build.CPU_ABI2);
		Log.i("SetModel", "dev:" + android.os.Build.DEVICE);
		Log.i("SetModel", "disp:" + android.os.Build.DISPLAY);
		Log.i("SetModel", "FP:" + android.os.Build.FINGERPRINT);
		Log.i("SetModel", "HW:" + android.os.Build.HARDWARE);
		Log.i("SetModel", "host:" + android.os.Build.HOST);
		Log.i("SetModel", "ID:" + android.os.Build.ID);
		Log.i("SetModel", "manufacturer:" + android.os.Build.MANUFACTURER);
		Log.i("SetModel", "prod:" + android.os.Build.PRODUCT);
		Log.i("SetModel", "radio:" + android.os.Build.RADIO);
		// FIXME: SERIAL not available on 2.2
		// Log.i("SetModel", "ser:" + android.os.Build.SERIAL);
		Log.i("SetModel", "tags:" + android.os.Build.TAGS);
		Log.i("SetModel", "time:" + android.os.Build.TIME);
		Log.i("SetModel", "type:" + android.os.Build.TYPE);
		Log.i("SetModel", "unknown:" + android.os.Build.UNKNOWN);
		Log.i("SetModel", "user:" + android.os.Build.USER);
		Log.i("SetModel", "model:" + android.os.Build.MODEL);
		Log.i("SetModel", "codename:" + android.os.Build.VERSION.CODENAME);
		Log.i("SetModel", "release:" + android.os.Build.VERSION.RELEASE);
		*/
	}

	private void setMemory() {
		Window win = this.getWindow();
		// Set memory values to the progress bar.
		ProgressBar mText = (ProgressBar) win.findViewById(R.id.progressBar1);
		mText.setMax(app.totalAndUsed[0]);
		mText.setProgress(app.totalAndUsed[1]);
		mText = (ProgressBar) win.findViewById(R.id.progressBar2);

		if (app.totalAndUsed.length > 2) {
			mText.setMax(app.totalAndUsed[2]);
			mText.setProgress(app.totalAndUsed[3]);
		}

		/* CPU usage */
		mText = (ProgressBar) win.findViewById(R.id.cpubar);
		mText.setMax(100);
		mText.setProgress(app.cpu);
	}

	private void setReportData() {
		final Reports r = app.s.getReports();
		Log.i("CaratHomeScreen", "Got reports: " + r);
		long l = System.currentTimeMillis() - app.s.getFreshness();
		final long min = l / 60000;
		final long sec = (l - min * 60000) / 1000;
		double bl = 0;
		int jscore = 0;
		if (r != null){
			bl = 100 / r.getModel().expectedValue;
			jscore = ((int) (r.getJScore() * 100));
		}
		int blh = (int) (bl / 3600);
		bl -= blh * 3600;
		int blmin = (int) (bl / 60);
		int bls = (int) (bl - blmin * 60);
		final String blS = blh + "h " + blmin + "m " + bls + "s";
		setText(R.id.jscore_value, jscore + "");
		setText(R.id.updated, "(Updated " + min + "m " + sec + "s ago)");
		setText(R.id.batterylife_value, blS);
	}

	private void setText(int viewId, String text) {
		Window win = this.getWindow();
		TextView t = (TextView) win.findViewById(viewId);
		t.setText(text);
	}

	private int lastColor = R.color.black;

	private void toggleColors() {
		/*
		 * Use arrays to make code easier to understand below. These elements
		 * need to change from a shade of brown/green to another.
		 */
		int[] browns = { R.id.jscore, R.id.updated, R.id.batterylife,
				R.id.apps, R.id.memactive, R.id.memused, R.id.dev, R.id.os_ver,
				R.id.cpu };

		int[] greens = { R.id.jscore_value, R.id.batterylife_value,
				R.id.dev_value, R.id.os_ver_value };

		Window win = this.getWindow();
		// The info icon needs to change from dark to light.
		ImageView infoIcon = (ImageView) win.findViewById(R.id.moreinfo);

		int green = R.color.green;
		int brown = R.color.brown_dark;
		int w = R.color.white;
		int b = R.color.black;

		/* Change from dark to light or the other way */
		if (lastColor == b) {
			green = R.color.green;
			brown = R.color.brown;
			win.setBackgroundDrawableResource(w);
			infoIcon.setImageResource(R.drawable.infoicon);
			lastColor = w;
		} else {
			win.setBackgroundDrawableResource(b);
			infoIcon.setImageResource(R.drawable.infoicon_dark);
			lastColor = b;
		}

		/* Handle the green/brown shades */
		for (int k : browns)
			changeColor(win, k, brown);
		for (int k : greens)
			changeColor(win, k, green);
	}

	/**
	 * Utility method to change the textColor
	 */
	private void changeColor(Window w, int viewId, int colorId) {
		TextView target = ((TextView) w.findViewById(viewId));
		target.setTextColor(getResources().getColor(colorId));
	}
}
