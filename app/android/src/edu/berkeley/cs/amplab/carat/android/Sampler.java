package edu.berkeley.cs.amplab.carat.android;

import edu.berkeley.cs.amplab.carat.android.storage.CaratDB;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class Sampler extends BroadcastReceiver {

	CaratDB ds = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ds == null) {
			ds = new CaratDB(context);
		}
		final Context c = context;
		final Intent i = intent;
		if (i.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			/*
			 * Schedule recurring sampling event:
			 */
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
		// Sample
		getSample(c, i);
	}

	private Sample getSample(Context context, Intent intent) {
		// FIXME: or create a takeSample(...) with more features returned than
		// in the basic Sample class

		Sample s = SamplingLibrary.getSample(context, intent,
				ds.getLastSample(context));

		// Write to database
		// But only after first real numbers
		if (!s.getBatteryState().equals("Unknown") && s.getBatteryLevel() >= 0) {
			long id = ds.putSample(s);
			Log.i("Sampler", "Took sample " + id + " for " + intent.getAction());
			Toast.makeText(context,
					"Took sample " + id + " for " + intent.getAction(),
					Toast.LENGTH_LONG).show();
		}
		return s;
	}
}
