package edu.berkeley.cs.amplab.carat.android.protocol;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.R;
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
public class SampleSender {
    
    private static final String TAG = "sendSamples";
    
    private static final String TRY_AGAIN = " will try again in "
            + (CaratApplication.COMMS_INTERVAL / 1000) + "s.";

    CaratApplication app = null;

    // Prevent instantiation
    private SampleSender(){}
    
    public static void sendSamples(boolean connecting, CaratApplication app) {
        Context c = app.getApplicationContext();

        String networkStatus = SamplingLibrary.getNetworkStatus(c);
        String networkType = SamplingLibrary.getNetworkType(c);

        final SharedPreferences p = PreferenceManager
                .getDefaultSharedPreferences(c);
        final boolean useWifiOnly = p.getBoolean(
                CaratApplication.PREFERENCE_WIFI_ONLY, false);

        boolean connected = (!useWifiOnly && networkStatus == SamplingLibrary.NETWORKSTATUS_CONNECTED)
                || networkType.equals("WIFI");

        if (connected) {
            CaratSampleDB db = CaratSampleDB.getInstance(c);
            int samples = db.countSamples();
            int successSum = 0;
            for (int batches = 0; batches < CaratApplication.COMMS_MAX_BATCHES
                    && batches < samples
                            / CaratApplication.COMMS_MAX_UPLOAD_BATCH + 1; batches++) {
                SortedMap<Long, Sample> map = CaratSampleDB.getInstance(c)
                        .queryOldestSamples(
                                CaratApplication.COMMS_MAX_UPLOAD_BATCH);
                if (map.size() > 0) {
                    CaratApplication.setActionProgress(0, successSum + "/" + samples
                            +" "+app.getString(R.string.samplesreported), false);
                    if (app.c != null) {
                        int tries = 0;
                        while (tries < 2) {
                            try {
                                int success = app.c.uploadSamples(map.values());

                                tries = 2;
                                // FlurryAgent.logEvent("UploadSamples");
                                Log.d(TAG, "Uploaded " + success
                                        + " samples out of " + map.size());
                                if (success > 0)
                                    CaratApplication.s.samplesReported(success);
                                Sample last = map.get(map.lastKey());
                                Log.d(TAG,
                                        "Deleting " + success
                                                + " samples older than "
                                                + last.getTimestamp());
                                /*
                                 * Log.i(TAG, "Sent samples:"); for (Sample k:
                                 * map.values()){ Log.i(TAG, k.getTimestamp() +
                                 * " " + k.getBatteryLevel()); }
                                 */
                                SortedSet<Long> uploaded = new TreeSet<Long>();
                                int i = 0;
                                for (Long s : map.keySet()) {
                                    if (i < success)
                                        uploaded.add(s);
                                    i += 1;
                                }
                                int deleted = CaratSampleDB.getInstance(c)
                                        .deleteSamples(uploaded);
                                /*
                                 * .deleteOldestSamples( last.getTimestamp());
                                 */
                                Log.d(TAG, "Deleted " + deleted + " samples.");
                                successSum += success;
                            } catch (Throwable th) {
                                // Any sort of malformed response, too short
                                // string, etc...
                                Log.w(TAG, "Failed to refresh reports: "
                                        + th
                                        + (tries < 1 ? "Trying again now"
                                                : TRY_AGAIN), th);
                                tries++;
                            }
                        }
                    } else {
                        Log.w(TAG, "CommunicationManager is not ready yet."
                                + TRY_AGAIN);
                    }
                } else {
                    Log.w(TAG, "No samples to send." + TRY_AGAIN);
                }
                int progress = (int) (successSum * 1.0 / samples * 100.0);
                CaratApplication.setActionProgress(progress, successSum + "/"
                        + samples +" "+ app.getString(R.string.samplesreported), false);
            }
        } else if (networkStatus
                .equals(SamplingLibrary.NETWORKSTATUS_CONNECTING)) {
            Log.w(TAG, "Network status: " + networkStatus
                    + ", trying again in 10s.");
            connecting = true;
        } else {
            Log.w(TAG, "Network status: " + networkStatus + TRY_AGAIN);
            connecting = false;
        }
        if (connecting) {
            // wait for wifi to come up
            try {
                Thread.sleep(CaratApplication.COMMS_WIFI_WAIT);
            } catch (InterruptedException e1) {
                // ignore
            }
            connecting = false;
        } else {
            try {
                Thread.sleep(CaratApplication.COMMS_INTERVAL);
            } catch (InterruptedException e) {
                // wait for wifi to come up
                try {
                    Thread.sleep(CaratApplication.COMMS_WIFI_WAIT);
                } catch (InterruptedException e1) {
                    // ignore
                }
            }
        }
    }
}
