package edu.berkeley.cs.amplab.carat.android;

import edu.berkeley.cs.amplab.carat.android.storage.CaratDB;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
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
		final Sample s = getSample(c, i);
		/* Communications are not allowed in main thread on real devices.
		 * Also, this object may be killed when we return from onReceive().
		 */
		/*
		new Thread() {
			public void run() {
				sendSample(s);
			}
		}.start();*/
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

	/*
	private void sendSample(Sample s) {
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
	/*
		try {
			CaratApplication.instance.c.uploadSample(s);
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
