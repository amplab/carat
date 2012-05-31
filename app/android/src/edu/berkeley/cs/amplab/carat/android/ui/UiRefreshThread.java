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
            CaratApplication.refreshActions();
            String networkStatus = SamplingLibrary.getNetworkStatus(c);
            if (networkStatus == SamplingLibrary.NETWORKSTATUS_CONNECTED && app.c != null) {
                int tries = 0;
                while (tries < 2) {
                    // Show we are updating...
                    CaratApplication.setActionInProgress();
                    try {
                        app.c.refreshAllReports();
                        //Log.d(TAG, "Reports refreshed.");
                        tries = 2;
                    } catch (TException e1) {
                        Log.w(TAG, "Failed to refresh reports: " + e1
                                + (tries < 1 ? "Trying again now": TRY_AGAIN));
                        e1.printStackTrace();
                        tries++;
                    } catch (Throwable th) {
                        // Any sort of malformed response, too short string,
                        // etc...
                        Log.w(TAG, "Failed to refresh reports: " + th
                                + (tries < 1 ? "Trying again now": TRY_AGAIN));
                        th.printStackTrace();
                        tries++;
                    }
                }
                connecting = false;
                
            } else if (networkStatus
                    .equals(SamplingLibrary.NETWORKSTATUS_CONNECTING)) {
                Log.w(TAG, "Network status: " + networkStatus
                        + ", trying again in 10s.");
                connecting = true;
            } else {
                Log.w(TAG, "Network status: " + networkStatus + "CommunicationManager="+ app.c + TRY_AGAIN);
                connecting = false;
            }
            // do this regardless
            setReportData();
            // Update UI elements
            CaratApplication.refreshActions();
            CaratApplication.refreshBugs();
            CaratApplication.refreshHogs();
            CaratApplication.setActionProgress(90);
            
            if (!connecting)
                CaratApplication.setActionFinished();
            
            if (connecting) {
                // wait for wifi to come up
                try {
                    sleep(CaratApplication.COMMS_WIFI_WAIT);
                } catch (InterruptedException e1) {
                    // ignore
                }
                connecting = false;
            } else {
                CaratApplication.setActionFinished();
                connecting = false;
                try {
                    sleep(CaratApplication.FRESHNESS_TIMEOUT);
                } catch (InterruptedException e) {
                    // Wake up and loop
                }
            }
        }
        Log.d(TAG, "Refresh thread stopped.");
    }
    
    public static void setReportData() {
        final Reports r = CaratApplication.s.getReports();
        Log.d("CaratHomeScreen", "Got reports: " + r);
        long freshness = CaratApplication.s.getFreshness();
        long l = System.currentTimeMillis() - freshness;
        final long h = l /360000;
        final long min = (l - h* 360000) / 60000;
        double bl = 0;
        int jscore = 0;
        if (r != null) {
            if (r.getModel() != null){
                double exp = r.getModel().expectedValue;
                Log.d(TAG, "Model expected value: " + exp);
                if (exp > 0.0)
                    bl = 100 / r.getModel().expectedValue;
            }
            jscore = ((int) (r.getJScore() * 100));
        }
        int blh = (int) (bl / 3600);
        bl -= blh * 3600;
        int blmin = (int) (bl / 60);
        int bls = (int) (bl - blmin * 60);
        final String blS = blh + "h " + blmin + "m " + bls + "s";
        CaratApplication.setMyDeviceText(R.id.jscore_value, jscore + "");
        //Log.v(TAG, "Freshness: " + freshness);
        if (freshness <= 0)
            CaratApplication.setMyDeviceText(R.id.updated, "(Never updated)");
        else if (min == 0)
        	CaratApplication.setMyDeviceText(R.id.updated, "(Updated just now)");
        else
            CaratApplication.setMyDeviceText(R.id.updated, "(Updated " + h + "h " + min + "m ago)");
        CaratApplication.setMyDeviceText(R.id.batterylife_value, blS);
    }
}
