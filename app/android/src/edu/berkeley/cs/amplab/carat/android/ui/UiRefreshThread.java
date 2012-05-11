package edu.berkeley.cs.amplab.carat.android.ui;

import org.apache.thrift.TException;

import android.content.Context;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.thrift.Reports;

/**
 * Communicates with the Carat Server. Sends samples stored in CaratDB every
 * COMMS_INTERVAL ms.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class UiRefreshThread extends Thread {

    private static final String TAG = "UiRefreshThread";

    private boolean isRunning = true;

    private static final String TRY_AGAIN = " will try again in "
            + (CaratApplication.FRESHNESS_TIMEOUT / 1000) + "s.";

    // FIXME: There should be a non-static way of doing this.
    private static CaratApplication app = null;
    
    private static UiRefreshThread instance = null;
    
    public static UiRefreshThread getInstance(){
        return instance;
    }

    public UiRefreshThread(CaratApplication app) {
        UiRefreshThread.app = app;
        instance = this;
    }

    public void stopRunning() {
        isRunning = false;
    }

    public void appResumed() {
        synchronized (UiRefreshThread.this) {
            UiRefreshThread.this.interrupt();
        }
    }

    public void run() {
        boolean connecting = false;
        Context c = app.getApplicationContext();
        Log.d(TAG, "Refresh thread started.");

        while (isRunning) {
            String networkStatus = SamplingLibrary.getNetworkStatus(c);
            if (networkStatus == SamplingLibrary.NETWORKSTATUS_CONNECTED) {
                try {
                    app.c.refreshAllReports();
                    Log.d(TAG, "Reports refreshed.");
                } catch (TException e1) {
                    Log.w(TAG, "Failed to refresh reports: " + e1 + TRY_AGAIN);
                    e1.printStackTrace();
                    app.c.resetConnection();
                }
                connecting = false;
            } else if (networkStatus
                    .equals(SamplingLibrary.NETWORKSTATUS_CONNECTING)) {
                Log.w(TAG, "Network status: " + networkStatus
                        + ", trying again in 10s.");
                connecting = true;
            } else {
                Log.w(TAG, "Network status: " + networkStatus + TRY_AGAIN);
                connecting = false;
            }
            // do this regardless
            setReportData();
            // Update UI elements
            CaratApplication.refreshActions();
            CaratApplication.refreshBugs();
            CaratApplication.refreshHogs();
            
            if (connecting) {
                // wait for wifi to come up
                try {
                    sleep(CaratApplication.COMMS_WIFI_WAIT);
                } catch (InterruptedException e1) {
                    // ignore
                }
                connecting = false;
            } else {
                connecting = false;
                try {
                    sleep(CaratApplication.FRESHNESS_TIMEOUT);
                } catch (InterruptedException e) {
                    // wait for wifi to come up
                    try {
                        sleep(CaratApplication.COMMS_WIFI_WAIT);
                    } catch (InterruptedException e1) {
                        // ignore
                    }
                }
            }
        }
        Log.d(TAG, "Refresh thread stopped.");
    }
    
    public static void setReportData() {
        final Reports r = app.s.getReports();
        Log.d("CaratHomeScreen", "Got reports: " + r);
        long freshness = app.s.getFreshness();
        long l = System.currentTimeMillis() - freshness;
        final long min = l / 60000;
        final long sec = (l - min * 60000) / 1000;
        double bl = 0;
        int jscore = 0;
        if (r != null) {
            double exp = r.getModel().expectedValue;
            Log.d(TAG, "Model expected value: " + exp);
            if (exp > 0.0)
                bl = 100 / r.getModel().expectedValue;
            jscore = ((int) (r.getJScore() * 100));
        }
        int blh = (int) (bl / 3600);
        bl -= blh * 3600;
        int blmin = (int) (bl / 60);
        int bls = (int) (bl - blmin * 60);
        final String blS = blh + "h " + blmin + "m " + bls + "s";
        CaratApplication.setMyDeviceText(R.id.jscore_value, jscore + "");
        if (freshness == 0)
            CaratApplication.setMyDeviceText(R.id.updated, "(Never updated)");
        else
            CaratApplication.setMyDeviceText(R.id.updated, "(Updated " + min + "m " + sec + "s ago)");
        CaratApplication.setMyDeviceText(R.id.batterylife_value, blS);
    }
}
