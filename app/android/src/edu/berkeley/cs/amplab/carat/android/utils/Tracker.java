package edu.berkeley.cs.amplab.carat.android.utils;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.MainActivity;
import edu.berkeley.cs.amplab.carat.android.protocol.ClickTracking;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;

public class Tracker {

	private static Tracker instance = null;
	
	public static Tracker getInstance() {
		if (instance == null)
			instance = new Tracker();
		return instance;
	}

	/*
	 * IMPORTANT: The fields "type" and "textBenefit" of the fullObject (the
	 * second parameter) must be initiated before INVOKING this method,
	 * otherwise you get NullPointerException
	 */
	public void trackUser(String label, SimpleHogBug fullObject) {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(CaratApplication.getMainActivity());
		PackageInfo pak = SamplingLibrary.getPackageInfo(CaratApplication.getMainActivity(), fullObject.getAppName());
		if (p != null) {
			String uuId = p.getString(CaratApplication.REGISTERED_UUID, "UNKNOWN");
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
			ClickTracking.track(uuId, "samplesview", options, CaratApplication.getMainActivity());
		}
	}

	public void trackUser(String whatIsGettingDone) {
		Log.d("Tracker.trackUser", whatIsGettingDone);
		MainActivity main = CaratApplication.getMainActivity();
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(main);
		if (p != null) {
			String uuId = p.getString(CaratApplication.getRegisteredUuid(), "UNKNOWN");
			HashMap<String, String> options = new HashMap<String, String>();
			options.put("status", main.getTitle().toString());
			ClickTracking.track(uuId, whatIsGettingDone, options, main);
		}
	}

}
