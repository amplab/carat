package edu.berkeley.cs.amplab.carat.android.sampling;

import java.util.Date;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.android.Constants;
import edu.berkeley.cs.amplab.carat.android.MainActivity;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.storage.CaratSampleDB;
import edu.berkeley.cs.amplab.carat.thrift.Sample;

public class SamplerService extends IntentService {
    
    private static final String TAG = "SamplerService";
    private double distance;
    
    public SamplerService() {
        super(TAG);
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {

		// At this point SimpleWakefulReceiver is still holding a wake lock
		// for us. We can do whatever we need to here and then tell it that
		// it can release the wakelock. This sample just does some slow work,
		// but more complicated implementations could take their own wake
		// lock here before releasing the receiver's.
		//
		// Note that when using this approach you should be aware that if your
		// service gets killed and restarted while in the middle of such work
		// (so the Intent gets re-delivered to perform the work again), it will
		// at that point no longer be holding a wake lock since we are depending
		// on SimpleWakefulReceiver to that for us. If this is a concern, you
		// can
		// acquire a separate wake lock here.
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wl.acquire();

		Context context = getApplicationContext();

		String action = null;
		if (intent != null)
			action = intent.getStringExtra("OriginalAction");
		// Log.d(TAG, "Original intent: " + action);
		
		if (action != null) {
			if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
				// NOTE: This is disabled to simplify how Carat behaves.
				SharedPreferences p = context.getSharedPreferences("SystemBootTime", Context.MODE_PRIVATE);
				Editor editor = p.edit();
				editor.putLong("bootTime", new Date().getTime());
				editor.commit();
				// onBoot(context);
			}

			if (action.equals(Constants.ACTION_CARAT_SAMPLE)) {
				// set up sampling.
				// Let sampling happen on battery change
				IntentFilter intentFilter = new IntentFilter();
				intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
				/*
				 * intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
				 * intentFilter.addDataScheme("package"); // add addDataScheme
				 */
				Sampler sampler = Sampler.getInstance();
				// Unregister, since Carat may have been started multiple times
				// since reboot
				try {
					unregisterReceiver(sampler);
				} catch (IllegalArgumentException e) {
				}
				registerReceiver(sampler, intentFilter);
			}
        
			takeSampleIfBatteryLevelChanged(intent, context);
        }
        
        wl.release();
        Sampler.completeWakefulIntent(intent);
    }

    /**
     * Some phones receive the batteryChanged very very often. We are interested 
     * only in changes of the battery level
     * @param intent  The parent intent (the one passed from the Sampler)
	 *				  (with one extra field set, called 'distance')
	 *				  This intent should be the intent which is passed by the Android system to your 
	 *                broadcast receiver (which is registered with the BATTERY_CHANGED action).
	 *                In our case, this broadcast receiver is 'Sampler'.                 
     * @param context
     */
	private void takeSampleIfBatteryLevelChanged(Intent intent, Context context) {
		distance = intent.getDoubleExtra("distance", 0);
		
		// Make sure our new sample doesn't have a zero value as its current battery level
		if (SamplingLibrary.getCurrentBatteryLevel() > 0) {
			CaratSampleDB sampleDB = CaratSampleDB.getInstance(context);
			Sample lastSample = sampleDB.getLastSample(context);	
			
			if (lastSample != null) {
				SamplingLibrary.setLastBatteryLevel(lastSample.getBatteryLevel());
			} else if (SamplingLibrary.getLastBatteryLevel(context) == 0) {
				Log.i(TAG,
						"The last sample is null (all samples have been uploaded and deleted "
								+ "from the local DB) , and the last battery level is not set yet "
								+ "(the first ever sample). About to take a new sample. "
								+ "currentBatteryLevel=" + SamplingLibrary.getCurrentBatteryLevel());
				// before taking the first sample in a batch, first record the battery level
				SamplingLibrary.setLastBatteryLevel(SamplingLibrary.getCurrentBatteryLevel());
				// take a sample and store it in the database
				this.getSample(context, intent, lastSample, sampleDB);
				notify(context);
			}
			
			/*
			 * Read the battery levels again, they are now changed. We just
			 * changed the last battery level (in the previous block of code).
			 * The current battery level might also have been changed while the
			 * device has been taking a sample.
			 */
			boolean batteryLevelChanged = SamplingLibrary.getLastBatteryLevel(context) != SamplingLibrary.getCurrentBatteryLevel();
			
			if (batteryLevelChanged) {
				/* among all occurrence of the event BATTERY_CHANGED, only take a sample 
				 * whenever a battery PERCENTAGE CHANGE happens 
				 * (BATTERY_CHANGED happens whenever the battery temperature or voltage of other parameters change)
				 */
				Log.i(TAG, "The battery percentage changed. About to take a new sample "
						+ "(currentBatteryLevel=" + SamplingLibrary.getCurrentBatteryLevel() + ", lastBatteryLevel=" + SamplingLibrary.getLastBatteryLevel(context)+ ")");
				// take a sample and store it in the database
				this.getSample(context, intent, lastSample, sampleDB);
				notify(context);
			} else {
				Log.d(TAG, "NO battery percentage change. currentBatteryLevel=" + SamplingLibrary.getCurrentBatteryLevel());
			}
		} else {
			Log.d(TAG, "current battery level = 0");
		}
	}

    /**
     * Takes a Sample and stores it in the database. Does not store the first ever samples 
     * that have no battery info.
     * @param context from onReceive
     * @param intent from onReceive
     * @return the newly recorded Sample
     */
    private void getSample(Context context, Intent intent, Sample lastSample, CaratSampleDB sampleDB) {
    	// String action = intent.getStringExtra("OriginalAction");
    	// Log.i("SamplerService.getSample()", "Original intent: " +action);
    	String lastBatteryState = lastSample != null ? lastSample.getBatteryState() : "Unknown";
    	Sample s = SamplingLibrary.getSample(context, intent, lastBatteryState);
        // Set distance to current distance value
        if (s != null){
            s.setDistanceTraveled(distance);
            // FIX: Do not use same distance again.
            distance = 0;
        }

        // Write to database
        // But only after first real numbers
        if (!s.getBatteryState().equals("Unknown") && s.getBatteryLevel() >= 0) {
        	// store the sample into the database
            long id = sampleDB.putSample(s);
            Log.i(TAG, "Took sample " + id + " for " + intent.getAction());
            //FlurryAgent.logEvent(intent.getAction());
            //  Log.d(TAG, "current battery level (just before quitting getSample() ): " + SamplingLibrary.getCurrentBatteryLevel());
        }
        // return s;
    }
    
    private void notify(Context context){
        long now = System.currentTimeMillis();
        long lastNotify = Sampler.getInstance().getLastNotify();
        
        // Do not notify if it is less than 2 days from last notification
        if (lastNotify + Constants.FRESHNESS_TIMEOUT_QUICKHOGS > now)
            return;
        
        int samples = CaratSampleDB.getInstance(context).countSamples();
        if (samples >= Sampler.MAX_SAMPLES){
            Sampler.getInstance().setLastNotify(now);
        PendingIntent launchCarat = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle("Please open Carat")
                .setContentText("Please open Carat. Samples to send:")
                .setNumber(samples);
        mBuilder.setContentIntent(launchCarat);
        //mBuilder.setSound(null);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
        }
    }
}
