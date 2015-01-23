package edu.berkeley.cs.amplab.carat.android;

import java.net.InetAddress;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import edu.berkeley.cs.amplab.carat.android.fragments.BugsOrHogsFragment;
import edu.berkeley.cs.amplab.carat.android.fragments.SuggestionsFragment;
import edu.berkeley.cs.amplab.carat.android.model_classes.MyDeviceData;
import edu.berkeley.cs.amplab.carat.android.protocol.CommunicationManager;
import edu.berkeley.cs.amplab.carat.android.protocol.SampleSender;
import edu.berkeley.cs.amplab.carat.android.sampling.Sampler;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.CaratDataStorage;
import edu.berkeley.cs.amplab.carat.thrift.Reports;

/**
 * Application class for Carat Android App. Place App-global static constants
 * and methods here.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class CaratApplication extends Application {

	private static CaratApplication mInstance;
	// Used for logging
	private static final String TAG = "CaratApp";
	
	public static Context mAppContext = null;
	public static SharedPreferences mPrefs = null;
	
	// Used to map importances to human readable strings for sending samples to
	// the server, and showing them in the process list.
	private static final SparseArray<String> importanceToString = new SparseArray<String>();
	
	{
		importanceToString.put(RunningAppProcessInfo.IMPORTANCE_EMPTY, "Not running");
		importanceToString.put(RunningAppProcessInfo.IMPORTANCE_BACKGROUND, "Background process");
		importanceToString.put(RunningAppProcessInfo.IMPORTANCE_SERVICE, "Service");
		importanceToString.put(RunningAppProcessInfo.IMPORTANCE_VISIBLE, "Visible task");
		importanceToString.put(RunningAppProcessInfo.IMPORTANCE_FOREGROUND, "Foreground app");

		importanceToString.put(Constants.IMPORTANCE_PERCEPTIBLE, "Perceptible task");
		importanceToString.put(Constants.IMPORTANCE_SUGGESTION, "Suggestion");
		
		mInstance = this;
	}

	public static Context getContext() {
    	return mInstance;
    }
	
	// NOTE: This needs to be initialized before CommunicationManager.
	public static CaratDataStorage storage = null;
	// NOTE: The CommunicationManager requires a working instance of
	// CaratDataStorage.
	public CommunicationManager commManager = null;

	// Activity pointers so that all activity UIs can be updated with a callback
	// to CaratApplication
	static MainActivity main = null;
	private static BugsOrHogsFragment bugsActivity = null;
	private static BugsOrHogsFragment hogsActivity = null;
	private static SuggestionsFragment actionList = null;
	// The Sampler samples the battery level when it changes.
	private static Sampler sampler = null;
	
	public static MyDeviceData myDeviceData = new MyDeviceData();

	// Application overrides:
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}	
	
	/**
	 * 1. Create CaratDataStorage and read reports from disk. Does not seem too
	 * slow.
	 * 
	 * 2. Take a sample in a new thread so that the GUI has fresh data.
	 * 
	 * 3. Create CommunicationManager for communicating with the Carat server.
	 * 
	 * 4. Communicate with the server to fetch new reports if current ones are
	 * outdated, and to send old stored and the new just-recorded sample. See
	 * MainActivity for this task.
	 */
	@Override
	public void onCreate() {
		// use a private preference file (created and used only by CaratApp). 
		// don't use PreferenceManager.getDefaultSharedPreferences (it might cause problem in different OS versions).
		new Thread() {
			public void run() {
				mPrefs = CaratApplication.this.getSharedPreferences(Constants.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
			}
		}.start();
		
		storage = new CaratDataStorage(this);

		new Thread() {
			private IntentFilter intentFilter;

			public void run() {
				/*
				 * Schedule recurring sampling event: (currently not used)
				 */
				/*
				 * SharedPreferences p = PreferenceManager
				 * 			.getDefaultSharedPreferences(CaratApplication.this); 
				 * boolean firstRun = p.getBoolean(PREFERENCE_SAMPLE_FIRST_RUN, true);
				 */
				// do this always for now for debugging purposes:
				// if (firstRun) {
				// What to start when the event fires (this is unused at the
				// moment)
				/*
				 * Intent intent = new Intent(getApplicationContext(), Sampler.class); 
				 * intent.setAction(ACTION_CARAT_SAMPLE); 
				 * // In reality, you would want to have a static variable for the 
				 * // request code instead of 192837 
				 * PendingIntent sender =
				 * 			PendingIntent.getBroadcast( CaratApplication.this, 192837,
				 * 							intent, PendingIntent.FLAG_UPDATE_CURRENT); 
				 * // Cancel if this has been set up. 
				 * // Do not use timer at all any more.
				 *  sender.cancel();
				 */

				// Let sampling happen on battery change
				intentFilter = new IntentFilter();
				intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
				/*
				 * intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
				 * intentFilter.addDataScheme("package"); // add addDataScheme
				 */
				sampler = Sampler.getInstance();
				// Unregister, since Carat may have been started multiple times
				// since reboot
				try {
					unregisterReceiver(sampler);
				} catch (IllegalArgumentException e) {
				}
				registerReceiver(sampler, intentFilter);

				// register for screen_on and screen-off as well

				// for the debugging purpose, let's comment out these actions
				// TODO: re-enable
				// intentFilter.addAction(Intent.ACTION_SCREEN_ON);
				// registerReceiver(sampler, intentFilter);
				// intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
				// registerReceiver(sampler, intentFilter);
			}
		}.start();

		new Thread() {
			public void run() {
				commManager = new CommunicationManager(CaratApplication.this);
			}
		}.start();

		super.onCreate();
	}
	
	// Utility methods
	
	/**
	 * Converts <code>importance</code> to a human readable string.
	 * 
	 * @param importance
	 *            the importance from Android process info.
	 * @return a human readable String describing the importance.
	 */
	public static String importanceString(int importance) {
		String s = importanceToString.get(importance);
		if (s == null || s.length() == 0) {
			Log.e("Importance not found:", "" + importance);
			s = "Unknown";
		}
		return s;
	}

	public static MainActivity getMainActivity() {
		return main;
	}

	public static String translatedPriority(String importanceString) {
		if (main != null) {
			if (importanceString == null)
				return main.getString(R.string.priorityDefault);
			if (importanceString.equals("Not running")) {
				return main.getString(R.string.prioritynotrunning);
			} else if (importanceString.equals("Background process")) {
				return main.getString(R.string.prioritybackground);
			} else if (importanceString.equals("Service")) {
				return main.getString(R.string.priorityservice);
			} else if (importanceString.equals("Visible task")) {
				return main.getString(R.string.priorityvisible);
			} else if (importanceString.equals("Foreground app")) {
				return main.getString(R.string.priorityforeground);
			} else if (importanceString.equals("Perceptible task")) {
				return main.getString(R.string.priorityperceptible);
			} else if (importanceString.equals("Suggestion")) {
				return main.getString(R.string.prioritysuggestion);
			} else
				return main.getString(R.string.priorityDefault);
		} else
			return importanceString;
	}

	/**
	 * Return a Drawable that contains an app icon for the named app. If not
	 * found, return the Drawable for the Carat icon.
	 * 
	 * @param appName
	 *            the application name
	 * @return the Drawable for the application's icon
	 */
	public static Drawable iconForApp(Context context, String appName) {
		try {
			return context.getPackageManager().getApplicationIcon(appName);
		} catch (NameNotFoundException e) {
			return context.getResources().getDrawable(R.drawable.ic_launcher);
		}
	}

	/**
	 * Return a human readable application label for the named app. If not
	 * found, return appName.
	 * 
	 * @param appName
	 *            the application name
	 * @return the human readable application label
	 */
	public static String labelForApp(Context context, String appName) {
		if (appName == null)
			return "Unknown";
		try {
			ApplicationInfo i = context.getPackageManager().getApplicationInfo(appName, 0);
			if (i != null)
				return context.getPackageManager().getApplicationLabel(i).toString();
			else
				return appName;
		} catch (NameNotFoundException e) {
			return appName;
		}
	}


	public static int getJscore() {
		final Reports reports = storage.getReports();
		int jscore = 0;
		if (reports != null) {
			jscore = ((int) (reports.getJScore() * 100));
		}
		return jscore;
	}

	public static void refreshBugs() {
		if (bugsActivity != null) {
			main.runOnUiThread(new Runnable() {
				public void run() {
					bugsActivity.refresh();
				}

			});
		}
	}

	public static void refreshHogs() {
		if (hogsActivity != null) {
			main.runOnUiThread(new Runnable() {
				public void run() {
					hogsActivity.refresh();
				}

			});
		}
	}

	public static void setActionInProgress() {
		if (main != null) {
			main.runOnUiThread(new Runnable() {
				public void run() {
					// Updating done
					main.setTitleUpdating(main.getString(R.string.tab_my_device));
					main.setProgress(0);
					main.setProgressBarVisibility(true);
					main.setProgressBarIndeterminateVisibility(true);
				}
			});
		}
	}

	public static void refreshActions() {
		if (actionList != null) {
			main.runOnUiThread(new Runnable() {
				public void run() {
					actionList.refresh();
				}
			});
		}
	}

	public static void setActionProgress(final int progress, final String what, final boolean fail) {
		if (main != null) {
			main.runOnUiThread(new Runnable() {
				public void run() {
					if (fail)
						main.setTitleUpdatingFailed(what);
					else
						main.setTitleUpdating(what);
					main.setProgress(progress * 100);
				}
			});
		}
	}

	public static void setActionFinished() {
		if (main != null) {
			main.runOnUiThread(new Runnable() {
				public void run() {
					// Updating done
					main.setTitleNormal();
					main.setProgress(100);
					main.setProgressBarVisibility(false);
					main.setProgressBarIndeterminateVisibility(false);
				}
			});
		}
	}

	public static void setMain(MainActivity mainActivity) {
		main = mainActivity;
	}

	public static void setBugs(BugsOrHogsFragment bugsOrHogsFragment) {
		bugsActivity = bugsOrHogsFragment;
	}

	public static void setHogs(BugsOrHogsFragment bugsOrHogsFragment) {
		hogsActivity = bugsOrHogsFragment;
	}

	public static void setActionList(SuggestionsFragment suggestionsFragment) {
		actionList = suggestionsFragment;
	}

	public static String getRegisteredUuid() {
		return Constants.REGISTERED_UUID;
	}


	public void refreshUi() {
		new Thread() {
			public void run() {
				boolean connecting = false;
				Context co = getApplicationContext();
				// TODO: using a shared preferences object might cause problem in different OS versions. replace with a private one. see MainActivity.AsyncTask.doInBackground().
				final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(co);
				final boolean useWifiOnly = p.getBoolean(Constants.WIFI_ONLY_PREFERENCE_KEY, false);
				String networkStatus = SamplingLibrary.getNetworkStatus(getApplicationContext());
				String networkType = SamplingLibrary.getNetworkType(co);

				boolean connected = (!useWifiOnly && networkStatus == SamplingLibrary.NETWORKSTATUS_CONNECTED)
						|| networkType.equals("WIFI");

				if (connected && commManager != null) {
					// Show we are updating...
					CaratApplication.setActionInProgress();
					try {
						commManager.refreshAllReports();
						// Log.d(TAG, "Reports refreshed.");
					} catch (Throwable th) {
						// Any sort of malformed response, too short string,
						// etc...
						Log.w(TAG, "Failed to refresh reports: " + th + Constants.MSG_TRY_AGAIN);
						th.printStackTrace();
					}
					connecting = false;

				} else if (networkStatus.equals(SamplingLibrary.NETWORKSTATUS_CONNECTING)) {
					Log.w(TAG, "Network status: " + networkStatus + ", trying again in 10s.");
					connecting = true;
				}

				// do this regardless
				setReportData();

				CaratApplication.setActionProgress(90, getString(R.string.finishing), false);

				if (!connecting)
					CaratApplication.setActionFinished();

				if (connecting) {
					// wait for WiFi to come up
					try {
						Thread.sleep(Constants.COMMS_WIFI_WAIT);
					} catch (InterruptedException e1) {
						// ignore
					}
					connecting = false;

					// Show we are updating...
					CaratApplication.setActionInProgress();
					try {
						commManager.refreshAllReports();
						// Log.d(TAG, "Reports refreshed.");
					} catch (Throwable th) {
						// Any sort of malformed response, too short string,
						// etc...
						Log.w(TAG, "Failed to refresh reports: " + th + Constants.MSG_TRY_AGAIN);
						th.printStackTrace();
					}
					connecting = false;

					// do this regardless
					setReportData();
					
					setActionProgress(90, getString(R.string.finishing), false);
				}
				CaratApplication.setActionFinished();
				SampleSender.sendSamples(CaratApplication.this);
				CaratApplication.setActionFinished();
			}
		}.start();
	}

	public static boolean isInternetAvailable() {
	    try {
	        InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
	        if (ipAddr.equals("")) {
	            return false;
	        } else {
	            return true;
	        }
	
	    } catch (Exception e) {
	        return false;
	    }
	}

	public static void setReportData() {
		final Reports r = storage.getReports();
		Log.d(TAG, "Got reports.");
		long freshness = CaratApplication.storage.getFreshness();
		long l = System.currentTimeMillis() - freshness;
		final long h = l / 3600000;
		final long min = (l - h * 3600000) / 60000;
		double bl = 0;
		double error = 0;

		if (r != null) {
			Log.d(TAG, "r (reports) not null.");
			// Try exact battery life
			if (r.jScoreWith != null) {
				// Log.d(TAG, "jscoreWith not null.");
				double exp = r.jScoreWith.expectedValue;
				if (exp > 0.0) {
					bl = 100 / exp;
					error = 100 / (exp + r.jScoreWith.error);
				} else if (r.getModel() != null) {
					exp = r.getModel().expectedValue;
					Log.d(TAG, "Model expected value: " + exp);
					if (exp > 0.0) {
						bl = 100 / exp;
						error = 100 / (exp + r.getModel().error);
					}
				}
				// If not possible, try model battery life
			}
		}


		// Only take the error part
		error = bl - error;

		int blh = (int) (bl / 3600);
		bl -= blh * 3600;
		int blmin = (int) (bl / 60);

		int errorH = 0;
		int errorMin = 0;
		if (error > 7200) {
			errorH = (int) (error / 3600);
			error -= errorH * 3600;
		}

		errorMin = (int) (error / 60);

		final String blS = blh + "h " + blmin + "m \u00B1 " + (errorH > 0 ? errorH + "h " : "") + errorMin + " m";

		/*
		 * we removed direct manipulation of MyDevice fragment,
		 * and moved the data pertaining to this fragment to a class field, called myDeviceData.
		 * In the onResume() method of MyDeviceFragment, we fetch this data and show (see setViewData())
		 * The reason for this movement is that we migrated from tabs to fragments.
		 * We cannot change a fragment's view while it's not in the foreground
		 * (fragments get replaced by a fragment transaction:
		 * the parent activity which hosts a frame-layout
		 * (a placeholder for fragment's layout), replaces the frame-layout with
		 * the new fragment's layout)
		 */ 
		
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(getMainActivity());
		String caratId = p.getString(Constants.REGISTERED_UUID, "0");
		
		myDeviceData.setAllFields(freshness, h, min, caratId, blS);
	}		
}
