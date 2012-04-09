package edu.berkeley.cs.amplab.carat.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import edu.berkeley.cs.amplab.carat.android.protocol.ProtocolClient;
import edu.berkeley.cs.amplab.carat.android.storage.CaratDataStorage;
import edu.berkeley.cs.amplab.carat.thrift.CaratService;
import edu.berkeley.cs.amplab.carat.thrift.Feature;
import edu.berkeley.cs.amplab.carat.thrift.Reports;
import android.app.Application;

public class CaratApplication extends Application {

	// Freshness timeout. Default: one hour
	// public static final long FRESHNESS_TIMEOUT = 3600000L;
	// 5 minutes
	public static final long FRESHNESS_TIMEOUT = 300000L;

	public CaratService.Client c = null;
	public CaratDataStorage s = null;

	/* 
	 * FIXME: Storing and retrieving totalAndused here only for testing.
	 * They should really be stored in CaratDataStorage and retrieved as part of sampling.
	 */
	public int[] totalAndUsed = null;
	/* 
	 * FIXME: Storing and retrieving CPU here only for testing.
	 * It should really be stored in CaratDataStorage and retrieved as part of sampling.
	 */
	public int cpu = 0;

	/**
	 * Create the stuff that is needed for comms.
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
				try {
					refreshReports();
				} catch (TException e) {
					e.printStackTrace();
				}
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

	private void refreshReports() throws TException {
		if (System.currentTimeMillis() - s.getFreshness() > FRESHNESS_TIMEOUT) {
			String uuId = "2DEC05A1-C2DF-4D57-BB0F-BA29B02E4ABE";
			List<Feature> features = new ArrayList<Feature>();

			Feature feature = new Feature();
			feature.setKey("Model");
			String model = "iPhone 3GS";
			feature.setValue(model);
			features.add(feature);

			feature = new Feature();
			feature.setKey("OS");
			String OS = "5.0.1";
			feature.setValue(OS);
			features.add(feature);
			c = ProtocolClient.getInstance(getApplicationContext());
			Reports r = c.getReports(uuId, features);
			ProtocolClient.close();
			s.writeReports(r);
			s.writeFreshness();
		}
	}
}
