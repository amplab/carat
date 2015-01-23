package edu.berkeley.cs.amplab.carat.android.utils;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.MainActivity;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.protocol.ClickTracking;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;

public class Tracker {

	private static Tracker instance = null;
	private static String uuid = null;
	private static SharedPreferences p = null;
	private static MainActivity mainActivity = null;
	private static Context context = null;
	private static int jscore = 0;
	
	public static Tracker getInstance() {
		if (instance == null) {
			instance = new Tracker();
			mainActivity = CaratApplication.getMainActivity();
			context = mainActivity.getApplicationContext();
			p = PreferenceManager.getDefaultSharedPreferences(mainActivity);
			uuid = p.getString(CaratApplication.getRegisteredUuid(), "UNKNOWN");
			jscore = CaratApplication.getJscore();
		}
		return instance;
	}

	/*
	 * IMPORTANT: The fields "type" and "textBenefit" of the fullObject (the
	 * second parameter) must be initiated before INVOKING this method,
	 * otherwise you get NullPointerException
	 */
	public void trackUser(String label, SimpleHogBug fullObject) {
		PackageInfo pak = SamplingLibrary.getPackageInfo(mainActivity, fullObject.getAppName());
		if (p != null) {
			HashMap<String, String> options = new HashMap<String, String>();
			options.put("status", CaratApplication.getMainActivity().getTitle().toString());
			options.put("type", fullObject.getType().toString());
			if (pak != null) {
				options.put("app", pak.packageName);
				options.put("version", pak.versionName);
				options.put("versionCode", pak.versionCode + "");
				options.put("label", label);
			}
			options.put("benefit", fullObject.getBenefitText().replace('\u00B1', '+'));
			ClickTracking.track(uuid, "samplesview", options, CaratApplication.getMainActivity());
		}
	}

	public void trackUser(String whatIsGettingDone) {
		Log.d("Tracker.trackUser", whatIsGettingDone);
		if (p != null) {
			String uuId = p.getString(CaratApplication.getRegisteredUuid(), "UNKNOWN");
			HashMap<String, String> options = new HashMap<String, String>();
			options.put("status", mainActivity.getTitle().toString());
			ClickTracking.track(uuId, whatIsGettingDone, options, mainActivity);
		}
	}
	
	public void trackSharing() {
		if (p != null) {
			HashMap<String, String> options = new HashMap<String, String>();
			options.put("status", mainActivity.getTitle().toString());
			options.put("sharetext", mainActivity.getString(R.string.myjscoreis) + " " + jscore);
			ClickTracking.track(uuid, "caratshared", options, context);
		}
	}
	
	public void trackFeedback(String os, String model) {
		if (p != null) {
			HashMap<String, String> options = new HashMap<String, String>();
			options.put("os", os);
			options.put("model", model);
			SimpleHogBug[] b = CaratApplication.storage.getBugReport();
			int len = 0;
			if (b != null)
				len = b.length;
			options.put("bugs", len + "");
			b = CaratApplication.storage.getHogReport();
			len = 0;
			if (b != null)
				len = b.length;
			options.put("hogs", len + "");
			options.put("status", mainActivity.getTitle().toString());
			options.put("sharetext", mainActivity.getString(R.string.myjscoreis) + " " + jscore);
			ClickTracking.track(uuid, "feedbackbutton", options, context);
		}
	}

}
