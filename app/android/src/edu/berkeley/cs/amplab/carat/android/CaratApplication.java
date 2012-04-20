package edu.berkeley.cs.amplab.carat.android;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.cs.amplab.carat.android.protocol.CommunicationManager;
import edu.berkeley.cs.amplab.carat.android.storage.CaratDataStorage;
import android.app.Application;
import android.graphics.drawable.Drawable;

public class CaratApplication extends Application {

	
	// NOTE: This needs to be initialized before CommunicationManager.
	public CaratDataStorage s = null;
	// NOTE: The CommunicationManager requires a working instance of CaratDataStorage.
	public CommunicationManager c = null;

	// TODO: This may not be the best place for the icon map. 
	private Map<String, Drawable> appToIcon = new HashMap<String, Drawable>();
	// default icon:
	public static String CARAT_PACKAGE = "edu.berkeley.cs.amplab.carat.android";
	
	/*
	 * FIXME: Storing and retrieving totalAndused here only for testing. They
	 * should really be stored in CaratDataStorage and retrieved as part of
	 * sampling.
	 */
	public int[] totalAndUsed = null;
	/*
	 * FIXME: Storing and retrieving CPU here only for testing. It should really
	 * be stored in CaratDataStorage and retrieved as part of sampling.
	 */
	public int cpu = 0;
	
	// Utility methods
	
	/**
	 * Return a Drawable that contains an app icon for the named app.
	 * If not found, return the Drawable for the Carat icon.
	 * @param appName the application name
	 * @return the Drawable for the application's icon
	 */
	public Drawable iconForApp(String appName){
		if (appToIcon.containsKey(appName))
			return appToIcon.get(appName);
		else
			return appToIcon.get(CARAT_PACKAGE);
	}
	
	
	// Application overrides

	/**
	 * 1. Create CaratDataStorage and read reports from disk
	 * TODO: this may need to be done in a separate thread if it is slow
	 * 
	 * 2. Take a sample in a new thread so that the GUI has fresh data
	 * TODO: Sampling not implemented yet
	 * 
	 * 3. Create CommunicationManager for communicating with the Carat server
	 * TODO: Uses fake data at the moment.
	 * TODO: When and by which class to record UUID, OS, MODEL for this?
	 * 
	 * 4. Communicate with the server to fetch new reports if current
	 *    ones are outdated, and to send old stored and the new just-recorded sample.
	 * TODO: Design data storage of multiple samples.
	 *       Perhaps just a single file with repeated writeObject() and readObject() calls,
	 *       with a separate file for the latest sample for GUI usage.
	 */
	@Override
	public void onCreate() {
		s = new CaratDataStorage(this);
		
		new Thread() {
			public void run() {
				totalAndUsed = SamplingLibrary.readMeminfo();
				cpu = (int) (SamplingLibrary.readUsage() * 100);
				// Also do the icon assignment here
				List<android.content.pm.PackageInfo> packagelist = getPackageManager()
						.getInstalledPackages(0);
				for (android.content.pm.PackageInfo p : packagelist) {
					String pname = p.applicationInfo.packageName;
					Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
					appToIcon.put(pname, icon);
				}
			}
		}.start();

		new Thread() {
			public void run() {
				c = new CommunicationManager(CaratApplication.this);
				c.refreshReports();
			}
		}.start();

		super.onCreate();
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}
}
