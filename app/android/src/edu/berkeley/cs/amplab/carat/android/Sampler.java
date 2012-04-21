package edu.berkeley.cs.amplab.carat.android;

import org.apache.thrift.TException;

import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Sampler extends BroadcastReceiver {
	private CaratApplication app = null;

	public Sampler(CaratApplication app) {
		this.app = app;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// FIXME: or create a takeSample(...) with more features returned than
		// in the basic Sample class
		Log.i("Sampler", "Took a sample because of: " +intent.getAction());
		Sample s = SamplingLibrary.getSample(context, intent,
				app.s.getLastSample()); 
		
		// Later when this is a database, this will write there instead.
		app.s.writeSample(s);
		
		// Tell GUI to update itself if running...
		// app.reloadSample();
		// The GUI would then reload sample data in a new thread and update itself.
		// Perhaps only when the user next does something in Carat or on next onResume.
		
		/*
		 * FIXME: This should be done in a big batch when the GUI is started
		 * so we don't constantly use network bandwidth when sampling happens
		 * in the background.
		 */
		try {
			app.c.uploadSample(s);
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
