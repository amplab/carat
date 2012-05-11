package edu.berkeley.cs.amplab.carat.android.protocol;

import java.util.SortedMap;
import java.util.Map.Entry;

import org.apache.thrift.TException;

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
    private boolean isRunning = true;

    private static final String TRY_AGAIN = " will try again in "
            + (CaratApplication.COMMS_INTERVAL / 1000) + "s.";

    CaratApplication app = null;

    public CommsThread(CaratApplication app) {
        this.app = app;
    }

    public void stopRunning() {
        isRunning = false;
    }

    public void appResumed() {
        synchronized (CommsThread.this) {
            CommsThread.this.interrupt();
        }
    }

    public void run() {
        boolean connecting = false;
        Context c = app.getApplicationContext();

        Log.i("CommsThread", "Sample sender started.");

        while (isRunning) {
            String networkStatus = SamplingLibrary.getNetworkStatus(c);
            if (networkStatus == SamplingLibrary.NETWORKSTATUS_CONNECTED) {
                SortedMap<Long, Sample> map = CaratSampleDB.getInstance(c)
                        .queryOldestSamples(
                                CaratApplication.COMMS_MAX_UPLOAD_BATCH);
                if (map.size() > 0) {
                    try {
                        StringBuilder timestamps = new StringBuilder();
                        for (Entry<Long, Sample> entry : map.entrySet()) {
                            timestamps.append(" "
                                    + entry.getValue().getTimestamp());
                        }
                        boolean success = app.c.uploadSamples(map.values());
                        if (success) {
                            Log.i("CommsThread", "Uploaded " + map.size()
                                    + " samples, timestamps:" + timestamps);
                            Sample last = map.get(map.lastKey());
                            Log.i("CommsThread",
                                    "Deleting " + map.size()
                                            + " samples older than "
                                            + last.getTimestamp());
                            int deleted = CaratSampleDB.getInstance(c)
                                    .deleteSamples(map.keySet());
                            /*
                             * .deleteOldestSamples( last.getTimestamp());
                             */
                            Log.i("CommsThread", "Deleted " + deleted
                                    + " samples.");
                        }
                    } catch (TException e1) {
                        Log.w("CommsThread", "Failed to send samples,"
                                + TRY_AGAIN);
                        app.c.resetConnection();
                        e1.printStackTrace();
                    }
                } else {
                    Log.w("CommsThread", "No samples to send." + TRY_AGAIN);
                }
            } else if (networkStatus
                    .equals(SamplingLibrary.NETWORKSTATUS_CONNECTING)) {
                Log.w("CommsThread", "Network status: " + networkStatus
                        + ", trying again in 10s.");
                connecting = true;
            } else {
                Log.w("CommsThread", "Network status: " + networkStatus
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
        }
        Log.i("CommsThread", "Sample sender stopped.");
    }
}
