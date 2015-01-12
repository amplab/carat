package edu.berkeley.cs.amplab.carat.android.sampling;

import edu.berkeley.cs.amplab.carat.android.Constants;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    //private static final String TAG = "BootReceiver";

    /**
     * Used to start Sampler on reboot even when Carat is not started. Not used
     * at the moment to keep Carat simple.
     * 
     * @param context the context
     * @param Intent the intent (should be ACTION_BOOT_COMPLETED)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent2 = new Intent(context, Sampler.class);
        intent2.setAction(Constants.ACTION_CARAT_SAMPLE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 192837, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE); // 1 min first
        am.set(AlarmManager.RTC_WAKEUP, 60000, pi);
    }
}
