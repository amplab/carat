package edu.berkeley.cs.amplab.carat.android;

import org.apache.thrift.TException;

import edu.berkeley.cs.amplab.carat.android.protocol.ProtocolClient;
import edu.berkeley.cs.amplab.carat.android.storage.CaratDB;
import edu.berkeley.cs.amplab.carat.thrift.CaratService.Client;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Sampler extends BroadcastReceiver {
	
	CaratDB ds = null;
	
	public Sampler(){
		ds = new CaratDB();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final Context c = context;
		final Intent i = intent;
		if (i.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			// start alarm to keep Sampler sampling
			/*
			 * Schedule recurring sampling event:*/
			// What to start when the event fires (this is unused at the moment)
			 Intent in = new Intent(context, Sampler.class);
			 in.setAction(CaratApplication.ACTION_CARAT_SAMPLE);
			 // In reality, you would want to have a static variable for the request code instead of 192837
			 PendingIntent sender = PendingIntent.getBroadcast(context, 192837, in, PendingIntent.FLAG_UPDATE_CURRENT);
			 
			 // Get the AlarmManager service
			 AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
			 // 1 min first, 15 min intervals
			 am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1*60*1000, AlarmManager.INTERVAL_FIFTEEN_MINUTES, sender);
		}
		
		final Sample s = getSample(c, i);
		/* Communications are not allowed in main thread on real devices.
		 * Also, this object may be killed when we return from onReceive().
		 */
		new Thread() {
			public void run() {
				sendSample(c, s);
			}
		}.start();
	}

	private Sample getSample(Context context, Intent intent) {
		// FIXME: or create a takeSample(...) with more features returned than
		// in the basic Sample class
		Log.i("Sampler", "Took a sample because of: " + intent.getAction());
		Toast.makeText(context,
				"Took a sample because of: " + intent.getAction(),
				Toast.LENGTH_SHORT).show();

		Sample s = SamplingLibrary.getSample(context, intent,
				ds.getLastSample(context));

		// Later when this is a database, this will write there instead.
		ds.writeSample(context, s);
		return s;
	}

	
	private void sendSample(Context c, Sample s) {
		// Tell GUI to update itself if running...
		// app.reloadSample();
		// The GUI would then reload sample data in a new thread and update
		// itself.
		// Perhaps only when the user next does something in Carat or on next
		// onResume.
		/*
		 * FIXME: This should be done in a big batch when the GUI is started so
		 * we don't constantly use network bandwidth when sampling happens in
		 * the background.
		 */
	
		try {
			Client instance = ProtocolClient.getInstance(c);
			instance.uploadSample(s);
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
