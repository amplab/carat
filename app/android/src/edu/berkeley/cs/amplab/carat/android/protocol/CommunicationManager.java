package edu.berkeley.cs.amplab.carat.android.protocol;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.thrift.TException;



import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.thrift.Feature;
import edu.berkeley.cs.amplab.carat.thrift.HogBugReport;
import edu.berkeley.cs.amplab.carat.thrift.Registration;
import edu.berkeley.cs.amplab.carat.thrift.Reports;
import edu.berkeley.cs.amplab.carat.thrift.Sample;

public class CommunicationManager {

    private static final String TAG = "CommsManager";
    private static final String DAEMONS_URL = "http://carat.cs.berkeley.edu/daemons.txt";

    private CaratApplication a = null;

    private boolean register = true;
    private SharedPreferences p = null;

    public CommunicationManager(CaratApplication a) {
        this.a = a;
        p = PreferenceManager.getDefaultSharedPreferences(this.a);
        register = p.getBoolean(CaratApplication.PREFERENCE_FIRST_RUN, true);
    }

    private void registerMe(String uuId, String os, String model)
            throws TException {
        if (uuId == null || os == null || model == null) {
            Log.e("registerMe", "Null uuId, os, or model given to registerMe!");
            System.exit(1);
            return;
        }
        Registration registration = new Registration(uuId);
        registration.setPlatformId(model);
        registration.setSystemVersion(os);
        registration.setTimestamp(System.currentTimeMillis() / 1000.0);
        ProtocolClient.registerMe(a.getApplicationContext(), registration);
    }

    public boolean uploadSamples(Collection<Sample> samples) throws TException {
        ProtocolClient.open(a.getApplicationContext());
        registerOnFirstRun();
        
        for (Sample s : samples)
            ProtocolClient.uploadSample(a.getApplicationContext(), s);
        ProtocolClient.close();
        return true;
    }

    private void registerOnFirstRun() {
        if (register) {
            String uuId = SamplingLibrary.getUuid(a.getApplicationContext());
            String os = SamplingLibrary.getOsVersion();
            String model = SamplingLibrary.getModel();
            Log.d("CommunicationManager",
                    "First run, registering this device: " + uuId + ", " + os
                            + ", " + model);
            try {
                registerMe(uuId, os, model);
                p.edit()
                        .putBoolean(CaratApplication.PREFERENCE_FIRST_RUN,
                                false).commit();
                register = false;
            } catch (TException e) {
                Log.e("CommunicationManager",
                        "Registration failed, will try again next time: " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Used by UiRefreshThread which needs to know about exceptions.
     * 
     * @throws TException
     */
    public void refreshAllReports() throws TException {
        // Do not refresh if not connected
        if (!SamplingLibrary.networkAvailable(a.getApplicationContext()))
            return;
        if (System.currentTimeMillis() - a.s.getFreshness() < CaratApplication.FRESHNESS_TIMEOUT)
            return;
        // Establish connection
        ProtocolClient.open(a.getApplicationContext());
        registerOnFirstRun();

        String uuId = SamplingLibrary.getUuid(a);
        String model = SamplingLibrary.getModel();
        String OS = SamplingLibrary.getOsVersion();

        // NOTE: Fake data for simulator
        /*
         * if (model.equals("sdk")) { uuId =
         * "2DEC05A1-C2DF-4D57-BB0F-BA29B02E4ABE"; model = "iPhone 3GS"; OS =
         * "5.0.1"; }
         */
        
        Log.d(TAG, "Getting reports for "+uuId + " model=" + model +" os="+OS);

        if (model.equals("sdk") || uuId.equals("ce9af33c736adbf7")) {
            uuId = "304e45cf1d3cf68b"; // My Galaxy Nexus
            model = "Galaxy Nexus";
            OS = "4.0.4";
        }

        refreshMainReports(uuId, OS, model);
        refreshBugReports(uuId, model);
        refreshHogReports(uuId, model);
        refreshBlacklist();
        ProtocolClient.close();
        a.s.writeFreshness();
    }

    private void refreshMainReports(String uuid, String os, String model)
            throws TException {
        if (System.currentTimeMillis() - a.s.getFreshness() < CaratApplication.FRESHNESS_TIMEOUT)
            return;
        Reports r = ProtocolClient.getReports(a.getApplicationContext(), uuid, getFeatures("Model", model, "OS", os));
        // Assume multiple invocations, do not close
        // ProtocolClient.close();
        if (r != null)
            a.s.writeReports(r);
        // Assume freshness written by caller.
        // s.writeFreshness();
    }

    private void refreshBugReports(String uuid, String model) throws TException {
        if (System.currentTimeMillis() - a.s.getFreshness() < CaratApplication.FRESHNESS_TIMEOUT)
            return;
        HogBugReport r = ProtocolClient.getHogOrBugReport(a.getApplicationContext(), uuid,
                getFeatures("ReportType", "Bug", "Model", model));
        // Assume multiple invocations, do not close
        // ProtocolClient.close();
        if (r != null)
            a.s.writeBugReport(r);
        // Assume freshness written by caller.
        // s.writeFreshness();
    }

    private void refreshHogReports(String uuid, String model) throws TException {
        if (System.currentTimeMillis() - a.s.getFreshness() < CaratApplication.FRESHNESS_TIMEOUT)
            return;
        HogBugReport r = ProtocolClient.getHogOrBugReport(a.getApplicationContext(), uuid,
                getFeatures("ReportType", "Hog", "Model", model));

        // Assume multiple invocations, do not close
        // ProtocolClient.close();
        if (r != null)
            a.s.writeHogReport(r);
        // Assume freshness written by caller.
        // s.writeFreshness();
    }
    
    private void refreshBlacklist(){
        try{
        List<String> blacklist = new ArrayList<String>();
                  URL u = new URL(DAEMONS_URL);
                  URLConnection c = u.openConnection();
                  InputStream is = c.getInputStream();
                  if (is != null) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String s = rd.readLine();
                    while (s != null) {
                        // Optimization for android: Only add names that have a dot
                        if (s.contains("."))
                            blacklist.add(s);
                      s = rd.readLine();
                    }
                    rd.close();
                    Log.v(TAG, "Downloaded blacklist: " + blacklist);
                    a.s.writeBlacklist(blacklist);
                  }
        } catch (Throwable th){
            Log.e(TAG, "Could not retrieve blacklist!", th);
        }
    }
    
    public static void resetConnection(){
        try{
        ProtocolClient.resetConnection();
        } catch (Throwable th){
            // Null pointer from Thrift?
            Log.e(TAG, "Got exception from thrift while resetting:", th);
        }
    }
    
    public static void close(){
        ProtocolClient.close();
    }

    private List<Feature> getFeatures(String key1, String val1, String key2,
            String val2) {
        List<Feature> features = new ArrayList<Feature>();
        if (key1 == null || val1 == null || key2 == null || val2 == null) {
            Log.e("getFeatures", "Null key or value given to getFeatures!");
            System.exit(1);
            return features;
        }
        Feature feature = new Feature();
        feature.setKey(key1);
        feature.setValue(val1);
        features.add(feature);

        feature = new Feature();
        feature.setKey(key2);
        feature.setValue(val2);
        features.add(feature);
        return features;
    }
}
