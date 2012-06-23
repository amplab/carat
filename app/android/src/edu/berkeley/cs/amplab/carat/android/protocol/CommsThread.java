package edu.berkeley.cs.amplab.carat.android.protocol;

import java.util.SortedMap;

import com.flurry.android.FlurryAgent;

import android.content.Context;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.CaratSampleDB;
import edu.berkeley.cs.amplab.carat.thrift.Sample;

/**
 * Communicates with the Carat Server. Sends samples stored in CaratDB every
 * COMMS_INTERVAL ms.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class CommsThread extends Thread {
    
    private static final String TAG = "CommsThread";
    
    private boolean isRunning = true;
    private boolean paused = false;

    private static final String TRY_AGAIN = " will try again in "
            + (CaratApplication.COMMS_INTERVAL / 1000) + "s.";

    CaratApplication app = null;

    public CommsThread(CaratApplication app) {
        this.app = app;
    }

    public void stopRunning() {
        isRunning = false;
    }
    
    public void paused() {
        synchronized (CommsThread.this) {
            paused = true;
            CommsThread.this.interrupt();
        }
    }

    public void appResumed() {
        synchronized (CommsThread.this) {
            paused = false;
            CommsThread.this.interrupt();
        }
    }

    public void run() {
        boolean connecting = false;
        Context c = app.getApplicationContext();

        Log.d(TAG, "Sample sender started.");

        while (isRunning) {
            String networkStatus = SamplingLibrary.getNetworkStatus(c);
            if (networkStatus == SamplingLibrary.NETWORKSTATUS_CONNECTED) {
                SortedMap<Long, Sample> map = CaratSampleDB.getInstance(c)
                        .queryOldestSamples(
                                CaratApplication.COMMS_MAX_UPLOAD_BATCH);
                if (map.size() > 0) {
                    if (app.c != null) {
                        int tries = 0;
                        while (tries < 2) {
                            try {
                                boolean success = app.c.uploadSamples(map
                                        .values());
                                if (success) {
                                    tries = 2;
                                    FlurryAgent.logEvent("UploadSamples", map);
                                    Log.d(TAG, "Uploaded " + map.size()
                                            + " samples.");
                                    CaratApplication.s.samplesReported(map.size());
                                    Sample last = map.get(map.lastKey());
                                    Log.d(TAG,
                                            "Deleting " + map.size()
                                                    + " samples older than "
                                                    + last.getTimestamp());
                                    int deleted = CaratSampleDB.getInstance(c)
                                            .deleteSamples(map.keySet());
                                    /*
                                     * .deleteOldestSamples(
                                     * last.getTimestamp());
                                     */
                                    Log.d(TAG, "Deleted " + deleted
                                            + " samples.");
                                }
                            } catch (Throwable th) {
                                // Any sort of malformed response, too short
                                // string, etc...
                                Log.w(TAG, "Failed to refresh reports: " + th
                                        + (tries < 1 ? "Trying again now": TRY_AGAIN), th);
                                tries++;
                            }
                        }
                    }else {
                        Log.w(TAG, "CommunicationManager is not ready yet." + TRY_AGAIN);
                    }
                } else {
                    Log.w(TAG, "No samples to send." + TRY_AGAIN);
                }
            } else if (networkStatus
                    .equals(SamplingLibrary.NETWORKSTATUS_CONNECTING)) {
                Log.w(TAG, "Network status: " + networkStatus
                        + ", trying again in 10s.");
                connecting = true;
            } else {
                Log.w(TAG, "Network status: " + networkStatus
                        + TRY_AGAIN);
                connecting = false;
            }
            if (connecting) {
                // wait for wifi to come up
                try {
                    sleep(CaratApplication.COMMS_WIFI_WAIT);
                } catch (InterruptedException e1) {
                    // ignore
                }
                connecting = false;
            } else {
                try {
                    sleep(CaratApplication.COMMS_INTERVAL);
                } catch (InterruptedException e) {
                    // wait for wifi to come up
                    try {
                        sleep(CaratApplication.COMMS_WIFI_WAIT);
                    } catch (InterruptedException e1) {
                        // ignore
                    }
                }
            }
            if (paused){
                try {
                    synchronized(CommsThread.this){
                        wait();
                    }
                } catch (InterruptedException e) {
                   // Intended behaviour
                }
            }
        }
        Log.d(TAG, "Sample sender stopped.");
    }
}
