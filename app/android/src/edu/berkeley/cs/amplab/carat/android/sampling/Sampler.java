package edu.berkeley.cs.amplab.carat.android.sampling;

import java.util.List;

import edu.berkeley.cs.amplab.carat.android.Constants;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;

public class Sampler extends WakefulBroadcastReceiver implements
        LocationListener {

    public static final int MAX_SAMPLES = 250;

    private static Sampler instance = null;
    private Context context = null;
    private Location lastKnownLocation = null;
    private double distance = 0.0;
    private long lastNotify;

    public static Sampler getInstance() {
    	if (instance == null)
    		Sampler.instance = new Sampler();
    	return instance;
    }
    
    private void requestLocationUpdates() {
        LocationManager lm = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        lm.removeUpdates(this);
        List<String> providers = SamplingLibrary
                .getEnabledLocationProviders(context);
        if (providers != null) {
            for (String provider : providers) {
                lm.requestLocationUpdates(provider,
                        Constants.FRESHNESS_TIMEOUT, 0, this);
                // Log.v(TAG, "Location updates requested for " + provider);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    	/* Some phones receive the batteryChanged very very often. We are interested 
         * only in changes of the battery level */
    	int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
        
		/*
		 * IMPORTANT: Android doesn't necessarily broadcast battery level info
		 * EVERY TIME a battery_changed action action happens. Sometimes these
		 * info (EXTRA_LEVEL & EXTRA_SCALE) ARE broadcasted, sometimes NOT. So
		 * it's important to make sure and check whether these extras are
		 * broadcasted and thus our variables are non-zero. Also whenever
		 * Android broadcasts these extras, it doens't necessarily mean
		 * that a battery percentage/level change has happened. So we have to
		 * check that too, to avoid overwriting our variables unnecessarily
		 * (extra memory operation). Check SamplingLibrary.setCurrentBatteryLevel().
		 */
        if (currentLevel > 0 && scale > 0) {
        	SamplingLibrary.setCurrentBatteryLevel(currentLevel, scale);
        	
        	if (this.context == null) {
                this.context = context;
                requestLocationUpdates();
            }

            // Update last known location...
            if (lastKnownLocation == null)
                lastKnownLocation = SamplingLibrary.getLastKnownLocation(context);

            Intent service = new Intent(context, SamplerService.class);
            service.putExtra("OriginalAction", intent.getAction());
            service.fillIn(intent, 0);
            service.putExtra("distance", distance);
            startWakefulService(context, service);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (lastKnownLocation != null && location != null) {
            distance = lastKnownLocation.distanceTo(location);
            /*
             * HashMap<String, Double> m = new HashMap<String, Double>();
             * m.put("distanceTraveled", distance);
             */
            // FlurryAgent.logEvent("LocationChanged", m);
            /*
             * CharSequence text = "Location change with distance = " +
             * distance; Toast.makeText(context, text,
             * Toast.LENGTH_LONG).show();
             */
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

    public long getLastNotify() {
        return lastNotify;
    }

    public void setLastNotify(long now) {
        this.lastNotify = now;
    }
}
