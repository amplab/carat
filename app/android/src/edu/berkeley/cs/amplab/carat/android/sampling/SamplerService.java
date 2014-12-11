package edu.berkeley.cs.amplab.carat.android.sampling;

import java.util.Date;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.MainActivity;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.storage.CaratSampleDB;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
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

public class SamplerService extends IntentService {
    
    private static final String TAG = "SamplerService";
    private double lastBatteryLevel = 0.0;
    private double distance;
    
    public SamplerService() {
        super(TAG);
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {

		String action1 = intent.getAction();
		Log.d(TAG, "action = " + action1);

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
		Log.i(TAG, "Original intent: " + action);
		
		if (action != null) {
			

			if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
				// NOTE: This is disabled to simplify how Carat behaves.
				SharedPreferences p = context.getSharedPreferences("SystemBootTime", Context.MODE_PRIVATE);
				Editor editor = p.edit();
				editor.putLong("bootTime", new Date().getTime());
				editor.commit();
				// onBoot(context);
			}

			if (action.equals(CaratApplication.ACTION_CARAT_SAMPLE)) {
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
					// No-op
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
	 *                broadcast receiver which is registered with the BATTERY_CHANGED action.
	 *                When this event occurs, Android calls the onReceive() method of your broadcast receiver
	 *                and passes this intent into that method, so you have access to this intent in there
	 *                (and can pass it to other methods) 
	 *                In our case, this broadcast receiver is 'Sampler'.                 
     * @param context
     */
	private void takeSampleIfBatteryLevelChanged(Intent intent, Context context) {
		CaratSampleDB sampleDB = CaratSampleDB.getInstance(context);
		Sample lastSample = sampleDB.getLastSample(context);
		
		if (lastBatteryLevel == 0.0)
			lastBatteryLevel = lastSample != null ? SamplingLibrary.getLastBatteryLevel(context) : 0.0;
		double currentBatteryLevel = SamplingLibrary.getCurrentBatteryLevel(intent);
		
		distance = intent.getDoubleExtra("distance", 0.0);
		
		boolean batteryLevelsNotZero = lastBatteryLevel > 0 && currentBatteryLevel > 0;
		boolean batteryPercentageIsChange = lastBatteryLevel != currentBatteryLevel;
		
		if (lastBatteryLevel == 0.0) {
			/* Ignore the battery level check, if the lastBatteryLevel is zero,
			 * that means the lastSample is null
			 * that implies the local database is empty (because this is first
			 * run of the app,or all samples have been successfully uploaded
			 * to the server and are deleted from the DB)
			 */

			// take a sample and store it in the database
			this.getSample(context, intent, lastSample, sampleDB);
			notify(context);
		} else if (batteryLevelsNotZero && batteryPercentageIsChange) {
			/* among all occurrence of the event BATTERY_CHANGED, only take a sample 
			 * whenever a battery PERCENTAGE CHANGE happens 
			 * (BATTERY_CHANGED happens whenever the battery temperature or voltage of other parameters change)
			 */
			Log.i(TAG, "about to invoke SamplerService.getSample() "
					+ "(distinguishable from SamplingLibrary.getSample())");
			// take a sample and store it in the database
			this.getSample(context, intent, lastSample, sampleDB);
			notify(context);
		} else {
			Log.d(TAG, "no battery percentage change. 'currentBatteryLevel'=" + currentBatteryLevel);
		}
		
		/*
		 * Every time all samples in the local DB get uploaded and deleted from the DB,
		 * the lastSample becomes null, and so does the lastBatteryLevel 
		 * (because it is taken from the lastSample (check SamplingLibrary.getLastBatteryLevel()).
		 * Obviously, after each successful update, the last battery level should not be reset to zero.
		 *  We manually set the last battery level here.
		 */
		lastBatteryLevel = currentBatteryLevel;
	}
        
    private void notify(Context context){
        long now = System.currentTimeMillis();
        long lastNotify = Sampler.getInstance().getLastNotify();
        
        // Do not notify if it is less than 2 days from last notification
        if (lastNotify + CaratApplication.FRESHNESS_TIMEOUT_QUICKHOGS > now)
            return;
        
        int samples = CaratSampleDB.getInstance(context).countSamples();
        if (samples >= Sampler.MAX_SAMPLES){
            Sampler.getInstance().setLastNotify(now);
        PendingIntent launchCarat = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context)
                .setSmallIcon(R.drawable.ic_launcher)
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

    /**
     * Takes a Sample and stores it in the database. Does not store the first ever samples on a device that have no battery info.
     * @param context from onReceive
     * @param intent from onReceive
     * @return the newly recorded Sample
     */
    private Sample getSample(Context context, Intent intent, Sample lastSample, CaratSampleDB sampleDB) {
//    	String action = intent.getStringExtra("OriginalAction");
//    	Log.i("SamplerService.getSample()", "Original intent: " +action);
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
            Log.d(TAG, "Took sample " + id + " for " + intent.getAction());
            //FlurryAgent.logEvent(intent.getAction());
        }
        return s;
    }
}
