package edu.berkeley.cs.amplab.carat.android;

import edu.berkeley.cs.amplab.carat.android.protocol.CommunicationManager;
import edu.berkeley.cs.amplab.carat.android.storage.CaratDataStorage;
import android.app.Application;

public class CaratApplication extends Application {

	
	// NOTE: This needs to be initialized before CommunicationManager.
	public CaratDataStorage s = null;
	// NOTE: The CommunicationManager requires a working instance of CaratDataStorage.
	public CommunicationManager c = null;

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
