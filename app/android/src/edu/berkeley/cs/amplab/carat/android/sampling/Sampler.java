package edu.berkeley.cs.amplab.carat.android.sampling;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.storage.CaratSampleDB;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class Sampler extends BroadcastReceiver implements LocationListener{

    private static final String TAG = "Sampler";
    
    private static Sampler instance = null;
    
    public static Sampler getInstance(){
        if (instance != null)
            return instance;
        else
            return new Sampler();
    }
    
	CaratSampleDB ds = null;
	private SharedPreferences sharedPreferences;
	private Editor editor;
	private Context context = null;
	
	private double lastBatteryLevel = 0.0;
	
	public Sampler(){
	    Sampler.instance = this;
	}
	
	private void requestLocationUpdates(){
	    LocationManager lm = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
	    lm.removeUpdates(this);
        List<String> providers = SamplingLibrary.getEnabledLocationProviders(context);
        if (providers != null){
            for (String provider: providers){
            lm.requestLocationUpdates(provider, CaratApplication.FRESHNESS_TIMEOUT, 0, this);
            Log.v(TAG, "Location updates requested for " + provider);
            }
        }
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
	    /*try{
            context.unregisterReceiver(this);
        }catch(IllegalArgumentException e){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            context.registerReceiver(getInstance(), intentFilter);
        }*/
	    
		if (ds == null) {
		    this.context = context;
			ds = new CaratSampleDB(context);
			requestLocationUpdates();
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
		
		/* 
		 * Some phones receive the batteryChanged very very often.
		 * We are interested only in changes of the battery level. 
		 */ 
		
		double bl = SamplingLibrary.getBatteryLevel(context, intent);
		if (bl <= 0)
		    return;
		
		Sample lastSample = ds.getLastSample(context);
		
		if ((lastSample == null && lastBatteryLevel != bl) || (lastSample != null && lastSample.getBatteryLevel() != bl)) {
		    Log.i(TAG, "Sampling for intent="+i.getAction()+" lastSample=" + (lastSample != null ? lastSample.getBatteryLevel()+"": "null") + " current battery="+bl);
		    // Take a sample.
		    getSample(c, i, lastSample);
		    lastBatteryLevel = bl;
		}
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
		/*PendingIntent sender = PendingIntent.getBroadcast(context, 192837,
				in, PendingIntent.FLAG_UPDATE_CURRENT);
        */
		// Get the AlarmManager service
		/*AlarmManager am = (AlarmManager) context
				.getSystemService(Activity.ALARM_SERVICE);
		// 1 min first, 15 min intervals
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				CaratApplication.FIRST_SAMPLE_DELAY_MS,
				CaratApplication.SAMPLE_INTERVAL_MS, sender);
		*/
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
	private Sample getSample(Context context, Intent intent, Sample lastSample) {
	    // Update last known location...
	    if (lastKnownLocation == null)
	        lastKnownLocation = SamplingLibrary.getLastKnownLocation(context);
		Sample s = SamplingLibrary.getSample(context, intent,
				lastSample);
		// Set distance to current distance value
		if (s != null){
		    Log.v(TAG, "distanceTravelled=" + distance);
		    s.setDistanceTraveled(distance);
		    // FIX: Do not use same distance again.
		    distance = 0;
		}

		// Write to database
		// But only after first real numbers
		if (!s.getBatteryState().equals("Unknown") && s.getBatteryLevel() >= 0) {
			long id = ds.putSample(s);
			Log.d(TAG, "Took sample " + id + " for " + intent.getAction());
			//FlurryAgent.logEvent(intent.getAction());
			/*Toast.makeText(context,
					"Took sample " + id + " for " + intent.getAction(),
					Toast.LENGTH_LONG).show();*/
		}
		return s;
	}
	
	private Location lastKnownLocation = null;
	private double distance = 0.0;

    @Override
    public void onLocationChanged(Location location) {
        if (lastKnownLocation != null && location != null) {
            distance = lastKnownLocation.distanceTo(location);
            HashMap<String, Double> m = new HashMap<String, Double>();
            m.put("distanceTraveled", distance);
            //FlurryAgent.logEvent("LocationChanged", m);
            /*CharSequence text = "Location change with distance = " + distance;
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();*/
        }
        lastKnownLocation = location;
    }

    @Override
    public void onProviderDisabled(String provider) {
        requestLocationUpdates();
    }

    @Override
    public void onProviderEnabled(String provider) {
        requestLocationUpdates();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        requestLocationUpdates();
    }
}
