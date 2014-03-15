package edu.berkeley.cs.amplab.carat.android.sampling;

import java.util.Date;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.CaratMainActivity;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.storage.CaratSampleDB;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

public class SamplerService extends IntentService {
    
    private static final String TAG = "SamplerService";
    
    public SamplerService() {
        super(TAG);
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        // At this point SimpleWakefulReceiver is still holding a wake lock
        // for us.  We can do whatever we need to here and then tell it that
        // it can release the wakelock.  This sample just does some slow work,
        // but more complicated implementations could take their own wake
        // lock here before releasing the receiver's.
        //
        // Note that when using this approach you should be aware that if your
        // service gets killed and restarted while in the middle of such work
        // (so the Intent gets re-delivered to perform the work again), it will
        // at that point no longer be holding a wake lock since we are depending
        // on SimpleWakefulReceiver to that for us.  If this is a concern, you can
        // acquire a separate wake lock here.
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wl.acquire();
        
        Context context = getApplicationContext();
        
        String action = intent.getStringExtra("OriginalAction");
        //Log.i(TAG, "Original intent: " +action);
        if (action != null){
        double lastBatteryLevel = intent.getDoubleExtra("lastBatteryLevel", 0.0);
        double distance = intent.getDoubleExtra("distance", 0.0);

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // NOTE: This is disabled to simplify how Carat behaves.
            SharedPreferences p = context.getSharedPreferences("SystemBootTime",
                    Context.MODE_PRIVATE);
            Editor editor = p.edit();
            editor.putLong("bootTime", new Date().getTime());
            editor.commit();
            // onBoot(context);
        }

        if (action.equals(CaratApplication.ACTION_CARAT_SAMPLE)){
            // set up sampling.
            // Let sampling happen on battery change
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            /*intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            intentFilter.addDataScheme("package"); // add addDataScheme*/
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
        
        CaratSampleDB ds = CaratSampleDB.getInstance(context);
        
        /*
         * Some phones receive the batteryChanged very very often. We are
         * interested only in changes of the battery level.
         */

        double bl = SamplingLibrary.getBatteryLevel(context, intent);
            if (bl > 0) {

                Sample lastSample = ds.getLastSample(context);

                if ((lastSample == null && lastBatteryLevel != bl)
                        || (lastSample != null && lastSample.getBatteryLevel() != bl)) {
                    // Log.i(TAG,
                    // "Sampling for intent="+i.getAction()+" lastSample=" +
                    // (lastSample != null ? lastSample.getBatteryLevel()+"":
                    // "null") + " current battery="+bl);
                    // Take a sample.
                    getSample(ds, context, intent, lastSample, distance);
                    lastBatteryLevel = bl;
                    
                    notify(context);
                }
            }
        }
        
        wl.release();
        Sampler.completeWakefulIntent(intent);
    }
    
    
    private void notify(Context context){
        int samples = CaratSampleDB.getInstance(context).countSamples();
        if (samples >= 200){
        PendingIntent launchCarat = PendingIntent.getActivity(context, 0,
                new Intent(context, CaratMainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Please open Carat")
                .setContentText("Please open Carat. Samples to send:")
                .setNumber(samples);
        mBuilder.setContentIntent(launchCarat);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
        }
    }

    /**
     * Get a Sample and store it in the database. Do not store the first ever samples on a device that have no battery info.
     * @param context from onReceive
     * @param intent from onReceive
     * @return the newly recorded Sample
     */
    private Sample getSample(CaratSampleDB ds, Context context, Intent intent, Sample lastSample, double distance) {
        Sample s = SamplingLibrary.getSample(context, intent,
                lastSample);
        // Set distance to current distance value
        if (s != null){
            //Log.v(TAG, "distanceTravelled=" + distance);
            s.setDistanceTraveled(distance);
            // FIX: Do not use same distance again.
            distance = 0;
        }

        // Write to database
        // But only after first real numbers
        if (!s.getBatteryState().equals("Unknown") && s.getBatteryLevel() >= 0) {
            long id = ds.putSample(s);
            //Log.d(TAG, "Took sample " + id + " for " + intent.getAction());
            //FlurryAgent.logEvent(intent.getAction());
        }
        return s;
    }
}
