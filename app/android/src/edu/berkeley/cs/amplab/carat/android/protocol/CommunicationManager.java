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
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import com.flurry.android.FlurryAgent;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.thrift.CaratService;
import edu.berkeley.cs.amplab.carat.thrift.Feature;
import edu.berkeley.cs.amplab.carat.thrift.HogBugReport;
import edu.berkeley.cs.amplab.carat.thrift.ProcessInfo;
import edu.berkeley.cs.amplab.carat.thrift.Registration;
import edu.berkeley.cs.amplab.carat.thrift.Reports;
import edu.berkeley.cs.amplab.carat.thrift.Sample;

public class CommunicationManager {

    private static final String TAG = "CommsManager";
    private static final String DAEMONS_URL = "http://carat.cs.berkeley.edu/daemons.txt";
    private static final String QUESTIONNAIRE_URL = "http://www.cs.helsinki.fi/u/lagerspe/caratapp/questionnaire-url.txt";

    private CaratApplication a = null;

    private boolean registered = false;
    private boolean register = true;
    private boolean newuuid = false;
    private boolean timeBasedUuid = false;
    private SharedPreferences p = null;

    public CommunicationManager(CaratApplication a) {
        this.a = a;
        p = PreferenceManager.getDefaultSharedPreferences(this.a);
        
        /*Either:
         * 1. Never registered -> register
         * 2. registered, but no stored uuid -> register
         * 3. registered, with stored uuid, but uuid, model or os are different -> register
         * 4. registered, all fields equal to stored -> do not register
         */
        timeBasedUuid = p.getBoolean(CaratApplication.PREFERENCE_TIME_BASED_UUID,
                false);
        newuuid = p.getBoolean(CaratApplication.PREFERENCE_NEW_UUID,
                false);
        registered = !p.getBoolean(CaratApplication.PREFERENCE_FIRST_RUN, true);
        register = !registered;
        String storedUuid = p.getString(CaratApplication.REGISTERED_UUID, null);
        if (!register) {
            if (storedUuid == null)
                register = true;
            else {
                String storedOs = p.getString(CaratApplication.REGISTERED_OS,
                        null);
                String storedModel = p.getString(
                        CaratApplication.REGISTERED_MODEL, null);

                String uuid = storedUuid;

                String os = SamplingLibrary.getOsVersion();
                String model = SamplingLibrary.getModel();

                if (storedUuid == null
                        || os == null
                        || model == null
                        || storedModel == null
                        || storedOs == null
                        || uuid == null
                        || !(storedOs.equals(os) && storedModel.equals(model))) {
                    // need to re-reg
                    register = true;
                } else
                    register = false;
            }
        }
    }

    private void registerMe(CaratService.Client instance, String uuId,
            String os, String model) throws TException {
        if (uuId == null || os == null || model == null) {
            Log.e("registerMe", "Null uuId, os, or model given to registerMe!");
            System.exit(1);
            return;
        }
        Registration registration = new Registration(uuId);
        registration.setPlatformId(model);
        registration.setSystemVersion(os);
        registration.setTimestamp(System.currentTimeMillis() / 1000.0);
        FlurryAgent.logEvent("Registering "+uuId+","+model+","+os);
        instance.registerMe(registration);
    }

    public int uploadSamples(Collection<Sample> samples) {
        CaratService.Client instance = null;
        int succeeded = 0;
        ArrayList<Sample> samplesLeft = new ArrayList<Sample>();
        registerLocal();
        try {
            instance = ProtocolClient.open(a.getApplicationContext());
            registerOnFirstRun(instance);

            for (Sample s : samples) {
                boolean success = false;
                try {
                    success = instance.uploadSample(s);
                } catch (Throwable th) {
                    Log.e(TAG, "Error uploading sample.", th);
                }
                if (success)
                    succeeded++;
                else
                    samplesLeft.add(s);
            }

            safeClose(instance);
        } catch (Throwable th) {
            Log.e(TAG, "Error refreshing main reports.", th);
            safeClose(instance);
        }
        // Do not try again. It can cause a massive sample attack on the server.
        return succeeded;
    }
    
    private void registerLocal() {
        if (register) {
            String uuId = p.getString(CaratApplication.REGISTERED_UUID, null);
            if (uuId == null) {
                if (registered && (!newuuid && !timeBasedUuid)) {
                    uuId = SamplingLibrary.getAndroidId(a);
                } else if (registered && !timeBasedUuid) {
                    // "new" uuid
                    uuId = SamplingLibrary.getUuid(a);
                } else {
                    // Time-based ID scheme
                    uuId = SamplingLibrary.getTimeBasedUuid(a);
                    Log.d("CommunicationManager",
                            "Generated a new time-based UUID: " + uuId);
                    // This needs to be saved now, so that if server
                    // communication
                    // fails we have a stable UUID.
                    p.edit().putString(CaratApplication.REGISTERED_UUID, uuId).commit();
                    p.edit().putBoolean(CaratApplication.PREFERENCE_TIME_BASED_UUID, true).commit();
                    timeBasedUuid = true;
                }
            }
        }
    }

    private void registerOnFirstRun(CaratService.Client instance) {
        if (register) {
            String uuId = null;
            // Only use new uuid if reg'd after this version for the first time.
            if (registered && (!newuuid && !timeBasedUuid)){
                uuId = SamplingLibrary.getAndroidId(a);
            } else if (registered && !timeBasedUuid){
            	// "new" uuid
                uuId = SamplingLibrary.getUuid(a);
            } else{
            	// Time-based ID scheme
            	uuId = SamplingLibrary.getTimeBasedUuid(a);
                Log.d("CommunicationManager",
                        "Generated a new time-based UUID: " + uuId);
                // This needs to be saved now, so that if server communication fails we have a stable UUID.
                p.edit().putString(CaratApplication.REGISTERED_UUID, uuId).commit();
                p.edit().putBoolean(CaratApplication.PREFERENCE_TIME_BASED_UUID, true).commit();
                timeBasedUuid = true;
            }
            String os = SamplingLibrary.getOsVersion();
            String model = SamplingLibrary.getModel();
            Log.d("CommunicationManager",
                    "First run, registering this device: " + uuId + ", " + os
                            + ", " + model);
            try {
                registerMe(instance, uuId, os, model);
                p.edit()
                        .putBoolean(CaratApplication.PREFERENCE_FIRST_RUN,
                                false).commit();
                register = false;
                registered = true;
                p.edit().putString(CaratApplication.REGISTERED_UUID, uuId).commit();
                p.edit().putString(CaratApplication.REGISTERED_OS, os).commit();
                p.edit().putString(CaratApplication.REGISTERED_MODEL, model).commit();
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
    public void refreshAllReports() {
        registerLocal();
        // Do not refresh if not connected
        if (!SamplingLibrary.networkAvailable(a.getApplicationContext()))
            return;
        if (System.currentTimeMillis() - CaratApplication.s.getFreshness() < CaratApplication.FRESHNESS_TIMEOUT)
            return;
        // Establish connection
        if (register) {
            CaratService.Client instance = null;
            try {
                instance = ProtocolClient.open(a.getApplicationContext());
                registerOnFirstRun(instance);
                safeClose(instance);
            } catch (Throwable th) {
                Log.e(TAG, "Error refreshing main reports.", th);
                safeClose(instance);
            }
        }

        String uuId = p.getString(CaratApplication.REGISTERED_UUID, null);
        String model = SamplingLibrary.getModel();
        String OS = SamplingLibrary.getOsVersion();

        // NOTE: Fake data for simulator
        if (model.equals("sdk")) {
            uuId = "97c542cd8e99d948"; // My S3
            model = "GT-I9300";
            OS = "4.0.4";
        }

        Log.d(TAG, "Getting reports for " + uuId + " model=" + model + " os="
                + OS);
        FlurryAgent.logEvent("Getting reports for "+uuId+","+model+","+OS);
        
        int progress = 0;

        CaratApplication.setActionProgress(progress,
                a.getString(R.string.tab_my_device), false);
        boolean success = refreshMainReports(uuId, OS, model);
        if (success) {
            progress += 20;
            CaratApplication.setActionProgress(progress,
                    a.getString(R.string.tab_bugs), false);
            Log.d(TAG, "Successfully got main report");
        } else {
            CaratApplication.setActionProgress(progress,
                    a.getString(R.string.tab_my_device), true);
            Log.d(TAG, "Failed getting main report");
        }
        success = refreshBugReports(uuId, model);
        if (success) {
            progress += 40;
            CaratApplication.setActionProgress(progress,
                    a.getString(R.string.tab_hogs), false);
            Log.d(TAG, "Successfully got bug report");
        } else {
            CaratApplication.setActionProgress(progress,
                    a.getString(R.string.tab_bugs), true);
            Log.d(TAG, "Failed getting bug report");
        }
        success = refreshHogReports(uuId, model);

        boolean bl = true;
        if (System.currentTimeMillis()
                - CaratApplication.s.getBlacklistFreshness() < CaratApplication.FRESHNESS_TIMEOUT_BLACKLIST)
            bl = false;

        if (success) {
            progress += 20;
            CaratApplication.setActionProgress(
                    progress,
                    bl ? a.getString(R.string.blacklist) : a
                            .getString(R.string.finishing), false);
            Log.d(TAG, "Successfully got hog report");
        } else {
            CaratApplication.setActionProgress(progress,
                    a.getString(R.string.tab_hogs), true);
            Log.d(TAG, "Failed getting hog report");
        }
        
        // NOTE: Check for having a J-Score, and in case there is none, send the new message
        Reports r = CaratApplication.s.getReports();
        if (r == null || r.jScoreWith == null || r.jScoreWith.expectedValue <= 0){
            success = getQuickHogsAndMaybeRegister(uuId, OS, model);
            if (success)
                Log.d(TAG, "Got quickHogs.");
            else
                Log.d(TAG, "Failed getting GuickHogs.");
        }
        
        if (bl) {
            refreshBlacklist();
            refreshQuestionnaireLink();
        }

        CaratApplication.s.writeFreshness();
        Log.d(TAG, "Wrote freshness");
    }

    private boolean refreshMainReports(String uuid, String os, String model) {
        if (System.currentTimeMillis() - CaratApplication.s.getFreshness() < CaratApplication.FRESHNESS_TIMEOUT)
            return false;
        CaratService.Client instance = null;
        try {
            instance = ProtocolClient.open(a.getApplicationContext());
            Reports r = instance.getReports(uuid,
                    getFeatures("Model", model, "OS", os));
            // Assume multiple invocations, do not close
            // ProtocolClient.close();
            if (r != null)
                CaratApplication.s.writeReports(r);
            // Assume freshness written by caller.
            // s.writeFreshness();
            safeClose(instance);
            return true;
        } catch (Throwable th) {
            Log.e(TAG, "Error refreshing main reports.", th);
            safeClose(instance);
        }
        return false;
    }

    private boolean refreshBugReports(String uuid, String model) {
        if (System.currentTimeMillis() - CaratApplication.s.getFreshness() < CaratApplication.FRESHNESS_TIMEOUT)
            return false;
        CaratService.Client instance = null;
        try {
            instance = ProtocolClient.open(a.getApplicationContext());
            HogBugReport r = instance.getHogOrBugReport(uuid,
                    getFeatures("ReportType", "Bug", "Model", model));
            // Assume multiple invocations, do not close
            // ProtocolClient.close();
            if (r != null)
                CaratApplication.s.writeBugReport(r);
            safeClose(instance);
            return true;
        } catch (Throwable th) {
            Log.e(TAG, "Error refreshing bug reports.", th);
            safeClose(instance);
        }
        return false;
    }

    private boolean refreshHogReports(String uuid, String model) {
        if (System.currentTimeMillis() - CaratApplication.s.getFreshness() < CaratApplication.FRESHNESS_TIMEOUT)
            return false;
        CaratService.Client instance = null;
        try {
            instance = ProtocolClient.open(a.getApplicationContext());
            HogBugReport r = instance.getHogOrBugReport(uuid,
                    getFeatures("ReportType", "Hog", "Model", model));

            // Assume multiple invocations, do not close
            // ProtocolClient.close();
            if (r != null)
                CaratApplication.s.writeHogReport(r);
            // Assume freshness written by caller.
            // s.writeFreshness();
            safeClose(instance);
            return true;
        } catch (Throwable th) {
            Log.e(TAG, "Error refreshing hog reports.", th);
            safeClose(instance);
        }
        return false;
    }

    private void refreshBlacklist() {
        // I/O, let's do it on the background.
        new Thread() {
            public void run() {
                final List<String> blacklist = new ArrayList<String>();
                final List<String> globlist = new ArrayList<String>();
                try {
                    URL u = new URL(DAEMONS_URL);
                    URLConnection c = u.openConnection();
                    InputStream is = c.getInputStream();
                    if (is != null) {
                        BufferedReader rd = new BufferedReader(
                                new InputStreamReader(is));
                        String s = rd.readLine();
                        while (s != null) {
                            // Optimization for android: Only add names that
                            // have a dot
                            // Does not work, since for example "system" has no
                            // dots.
                            blacklist.add(s);
                            if (s.endsWith("*") || s.startsWith("*"))
                                globlist.add(s);
                            s = rd.readLine();
                        }
                        rd.close();
                        Log.v(TAG, "Downloaded blacklist: " + blacklist);
                        Log.v(TAG, "Downloaded globlist: " + globlist);
                        CaratApplication.s.writeBlacklist(blacklist);
                        // List of *something or something* expressions:
                        if (globlist.size() > 0)
                            CaratApplication.s.writeGloblist(globlist);

                    }
                } catch (Throwable th) {
                    Log.e(TAG, "Could not retrieve blacklist!", th);
                }
                // So we don't try again too often.
                CaratApplication.s.writeBlacklistFreshness();
            }
        }.start();
    }
    
    private void refreshQuestionnaireLink() {
        // I/O, let's do it on the background.
        new Thread() {
            public void run() {
                String s = null;
                try {
                    URL u = new URL(QUESTIONNAIRE_URL);
                    URLConnection c = u.openConnection();
                    InputStream is = c.getInputStream();
                    if (is != null) {
                        BufferedReader rd = new BufferedReader(
                                new InputStreamReader(is));
                        s = rd.readLine();
                        rd.close();
                        if (s != null && s.length() > 0)
                            CaratApplication.s.writeQuestionnaireUrl(s);
                    }
                } catch (Throwable th) {
                    Log.e(TAG, "Could not retrieve blacklist!", th);
                }
            }
        }.start();
    }
    
    private boolean getQuickHogsAndMaybeRegister(String uuid, String os, String model) {
        if (System.currentTimeMillis() - CaratApplication.s.getQuickHogsFreshness() < CaratApplication.FRESHNESS_TIMEOUT_QUICKHOGS)
            return false;
        CaratService.Client instance = null;
        try {
            instance = ProtocolClient.open(a.getApplicationContext());
            Registration registration = new Registration(uuid);
            registration.setPlatformId(model);
            registration.setSystemVersion(os);
            registration.setTimestamp(System.currentTimeMillis() / 1000.0);
            List<ProcessInfo> pi = SamplingLibrary.getRunningAppInfo(a.getApplicationContext());
            List<String> processList = new ArrayList<String>();
            for (ProcessInfo p: pi)
                processList.add(p.pName);
            
            HogBugReport r = instance.getQuickHogsAndMaybeRegister(registration, processList);
            // Assume multiple invocations, do not close
            // ProtocolClient.close();
            if (r != null){
                CaratApplication.s.writeHogReport(r);
                CaratApplication.s.writeQuickHogsFreshness();
            }
            // Assume freshness written by caller.
            // s.writeFreshness();
            safeClose(instance);
            return true;
        } catch (Throwable th) {
            Log.e(TAG, "Error refreshing main reports.", th);
            safeClose(instance);
        }
        return false;
    }

    public static void safeClose(CaratService.Client c) {
        if (c == null)
            return;
        TProtocol i = c.getInputProtocol();
        TProtocol o = c.getOutputProtocol();
        if (i != null) {
            TTransport it = i.getTransport();
            if (it != null)
                it.close();
        }
        if (o != null) {
            TTransport it = o.getTransport();
            if (it != null)
                it.close();
        }
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
