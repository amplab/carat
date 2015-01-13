package edu.berkeley.cs.amplab.carat.android;

import android.app.AlarmManager;

public class Constants {

	// Report Freshness timeout. Default: 15 minutes
	// public static final long FRESHNESS_TIMEOUT = 30 * 1000;
	public static final long FRESHNESS_TIMEOUT = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
	// Blacklist freshness timeout. Default 24h.
	// public static final long FRESHNESS_TIMEOUT_BLACKLIST = 30 * 1000;
	public static final long FRESHNESS_TIMEOUT_BLACKLIST = 24 * 3600 * 1000;
	// Blacklist freshness timeout. Default 2 days.
	// public static final long FRESHNESS_TIMEOUT_QUICKHOGS = 30 * 1000;
	public static final long FRESHNESS_TIMEOUT_QUICKHOGS = 2 * 24 * 3600 * 1000;

	// If this preference is true, register this as a new device on the Carat
	// server.
	public static final String PREFERENCE_FIRST_RUN = "carat.first.run";
	static final String REGISTERED_UUID = "carat.registered.uuid";
	public static final String REGISTERED_OS = "carat.registered.os";
	public static final String REGISTERED_MODEL = "carat.registered.model";

	// if you change the preference key of any of our preference widgets (in
	// res/xml/preferences.xml),
	// update the following constants as well
	public static final String WIFI_ONLY_PREFERENCE_KEY = "wifiOnlyPreferenceKey";
	public static final String SHARE_PREFERENCE_KEY = "sharePreferenceKey";
	public static final String FEEDBACK_PREFERENCE_KEY = "feedbackPreferenceKey";

	public static final String PREFERENCE_NEW_UUID = "carat.new.uuid";
	public static final String PREFERENCE_TIME_BASED_UUID = "carat.uuid.timebased";

	// Check for and send new samples at most every 15 minutes, but only when
	// the user wakes up/starts Carat
	public static final long COMMS_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
	// When waking up from screen off, wait 5 seconds for wifi etc to come up
	public static final long COMMS_WIFI_WAIT = 5 * 1000;
	// Send up to 10 samples at a time
	public static final int COMMS_MAX_UPLOAD_BATCH = 10;

	// Alarm event for sampling when battery has not changed for
	// SAMPLE_INTERVAL_MS. Currently not used.
	public static final String ACTION_CARAT_SAMPLE = "edu.berkeley.cs.amplab.carat.android.ACTION_SAMPLE";
	// If true, install Sampling events to occur at boot. Currently not used.
	public static final String PREFERENCE_SAMPLE_FIRST_RUN = "carat.sample.first.run";
	public static final String PREFERENCE_SEND_INSTALLED_PACKAGES = "carat.sample.send.installed";

	// default icon and Carat package name:
	public static final String CARAT_PACKAGE_NAME = "edu.berkeley.cs.amplab.carat.android";
	// Used to blacklist old Carat
	public static final String CARAT_OLD = "edu.berkeley.cs.amplab.carat";

	// Not in Android 2.2, but needed for app importances
	public static final int IMPORTANCE_PERCEPTIBLE = 130;
	// Used for non-app suggestions
	public static final int IMPORTANCE_SUGGESTION = 123456789;

	public static final String IMPORTANCE_NOT_RUNNING = "Not Running";
	public static final String IMPORTANCE_UNINSTALLED = "uninstalled";
	public static final String IMPORTANCE_INSTALLED = "installed";
	public static final String IMPORTANCE_REPLACED = "replaced";

	public static final int COMMS_MAX_BATCHES = 50;

	// Used for bugs and hogs, and EnergyDetails sub-screen (previously known as drawing)
	public static enum Type {
		OS, MODEL, HOG, BUG, SIMILAR, JSCORE, OTHER, BRIGHTNESS, WIFI, MOBILEDATA
	}

	// used in the SplashActivity and MainActivity
	// (to check whether the users statistics are fetched from the server)
	public static final String DATA_NOT_AVAIABLE = "not_available";
	
	public static final String MAIN_ACTIVITY_PREFERENCE_KEY = "Main_Activity_Shared_Preferences_Key";
	
	// keys for retrieving values from the shared preference
	public static final String WELL_BEHAVED_APPS_COUNT_PREF_KEY = "wellbehavedAppCount";
	public static final String HOGS_COUNT_PREF_KEY = "hogCount";
	public static final String BUGS_COUNT_PREF_KEY = "bugCount";

	// Used for messages in comms threads
	static final String MSG_TRY_AGAIN = " will try again in " + (FRESHNESS_TIMEOUT / 1000) + "s.";

}
