package edu.berkeley.cs.amplab.carat.android.sampling;

import java.util.Date;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.storage.CaratSampleDB;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

public class Sampler extends BroadcastReceiver {

	CaratSampleDB ds = null;
	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (ds == null) {
			ds = new CaratSampleDB(context);
		}
		final Context c = context;
		final Intent i = intent;
		
		if (i.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			//NOTE: This is disabled to simplify how Carat behaves. 
		    sharedPreferences = context.getSharedPreferences("SystemBootTime", Context.MODE_PRIVATE); 
		            editor = sharedPreferences.edit(); 
		            editor.putLong("bootTime", new Date().getTime()); 
		            editor.commit();
			//onBoot(context);
		}
		// Sample
		getSample(c, i);
	}
	
	/**
	 * Used to start Sampler on reboot even when Carat is not started.
	 * Not used at the moment to keep Carat simple.
	 * @param context
	 */

	private void onBoot(Context context){
		// Schedule recurring sampling event:
		// What to start when the event fires (this is unused at the moment)
		Intent in = new Intent(context, Sampler.class);
		in.setAction(CaratApplication.ACTION_CARAT_SAMPLE);
		// In reality, you would want to have a static variable for the
		// request code instead of 192837
		PendingIntent sender = PendingIntent.getBroadcast(context, 192837,
				in, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) context
				.getSystemService(Activity.ALARM_SERVICE);
		// 1 min first, 15 min intervals
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				CaratApplication.FIRST_SAMPLE_DELAY_MS,
				CaratApplication.SAMPLE_INTERVAL_MS, sender);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		context.registerReceiver(new Sampler(), intentFilter);
	}

	/**
	 * Get a Sample and store it in the database. Do not store the first ever samples on a device that have no battery info.
	 * @param context from onReceive
	 * @param intent from onReceive
	 * @return the newly recorded Sample
	 */
	private Sample getSample(Context context, Intent intent) {
		// FIXME: or create a takeSample(...) with more features returned than
		// in the basic Sample class

		Sample s = SamplingLibrary.getSample(context, intent,
				ds.getLastSample(context));

		// Write to database
		// But only after first real numbers
		if (!s.getBatteryState().equals("Unknown") && s.getBatteryLevel() >= 0) {
			long id = ds.putSample(s);
			Log.d("Sampler", "Took sample " + id + " for " + intent.getAction());
			/*Toast.makeText(context,
					"Took sample " + id + " for " + intent.getAction(),
					Toast.LENGTH_LONG).show();*/
		}
		return s;
	}
}
