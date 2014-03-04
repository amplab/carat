package edu.berkeley.cs.amplab.carat.android.sampling;

import java.util.List;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class Sampler extends WakefulBroadcastReceiver implements
        LocationListener {

    private static final String TAG = "Sampler";

    private static Sampler instance = null;

    public static Sampler getInstance() {
        if (instance != null)
            return instance;
        else
            return new Sampler();
    }

    private Context context = null;

    private double lastBatteryLevel = 0;
    private Location lastKnownLocation = null;
    private double distance = 0.0;

    public Sampler() {
        Sampler.instance = this;
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
                        CaratApplication.FRESHNESS_TIMEOUT, 0, this);
                // Log.v(TAG, "Location updates requested for " + provider);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
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
        service.putExtra("lastBatteryLevel", lastBatteryLevel);
        service.putExtra("distance", distance);
        startWakefulService(context, service);
    }

    /**
     * Used to start Sampler on reboot even when Carat is not started. Not used
     * at the moment to keep Carat simple.
     * 
     * @param context
     */

    private void onBoot(Context context) {
        // Schedule recurring sampling event:
        // What to start when the event fires (this is unused at the moment)
        Intent in = new Intent(context, Sampler.class);
        in.setAction(CaratApplication.ACTION_CARAT_SAMPLE);
        // In reality, you would want to have a static variable for the
        // request code instead of 192837
        /*
         * PendingIntent sender = PendingIntent.getBroadcast(context, 192837,
         * in, PendingIntent.FLAG_UPDATE_CURRENT);
         */
        // Get the AlarmManager service
        /*
         * AlarmManager am = (AlarmManager) context
         * .getSystemService(Activity.ALARM_SERVICE); // 1 min first, 15 min
         * intervals am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
         * CaratApplication.FIRST_SAMPLE_DELAY_MS,
         * CaratApplication.SAMPLE_INTERVAL_MS, sender);
         */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(new Sampler(), intentFilter);
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
}
