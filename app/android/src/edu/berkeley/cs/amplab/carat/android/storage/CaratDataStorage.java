package edu.berkeley.cs.amplab.carat.android.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.android.CaratApplication.Type;
import edu.berkeley.cs.amplab.carat.thrift.HogBugReport;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;
import edu.berkeley.cs.amplab.carat.thrift.Reports;

public class CaratDataStorage {

    public static final String FILENAME = "carat-reports.dat";
    public static final String BLACKLIST_FILE = "carat-blacklist.dat";
    public static final String QUESTIONNAIRE_URL_FILE = "questionnaire-url.txt";
    public static final String BLACKLIST_FRESHNESS = "carat-blacklist-freshness.dat";
    public static final String QUICKHOGS_FRESHNESS = "carat-quickhogs-freshness.dat";
    public static final String GLOBLIST_FILE = "carat-globlist.dat";
    public static final String BUGFILE = "carat-bugs.dat";
    public static final String HOGFILE = "carat-hogs.dat";

    public static final String SAMPLES_REPORTED = "carat-samples-reported.dat";
    
    public static final String FRESHNESS = "carat-freshness.dat";
    private Application a = null;

    private long freshness = 0;
    private long blacklistFreshness = 0;
    private long quickHogsFreshness = 0;
    private long samples_reported = 0;
    private String questionnaireUrl = null;
    private WeakReference<Reports> caratData = null;
    private WeakReference<SimpleHogBug[]> bugData = null;
    private WeakReference<SimpleHogBug[]> hogData = null;
    private WeakReference<List<String>> blacklistedApps = null;
    private WeakReference<List<String>> blacklistedGlobs = null;

    public CaratDataStorage(Application a) {
        this.a = a;
        freshness = readFreshness();
        blacklistFreshness = readBlacklistFreshness();
        quickHogsFreshness = readQuickHogsFreshness();
        caratData = new WeakReference<Reports>(readReports());
        readBugReport();
        readHogReport();
        readBlacklist();
        samples_reported = readSamplesReported();
    }

    public void writeReports(Reports reports) {
        if (reports == null)
            return;
        caratData = new WeakReference<Reports>(reports);
        writeObject(reports, FILENAME);
    }

    public void writeFreshness() {
        freshness = System.currentTimeMillis();
        writeText(freshness + "", FRESHNESS);
    }
    
    public void writeBlacklistFreshness() {
        blacklistFreshness = System.currentTimeMillis();
        writeText(blacklistFreshness + "", BLACKLIST_FRESHNESS);
    }
    
    public void writeQuickHogsFreshness() {
        quickHogsFreshness = System.currentTimeMillis();
        writeText(quickHogsFreshness + "", QUICKHOGS_FRESHNESS);
    }


    public void writeObject(Object o, String fname) {
        FileOutputStream fos = getFos(fname);
        if (fos == null)
            return;
        try {
            ObjectOutputStream dos = new ObjectOutputStream(fos);
            dos.writeObject(o);
            dos.close();
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Could not write object:" + o
                    + "!");
            e.printStackTrace();
        } catch (Throwable th){
        	Log.e(this.getClass().getName(), "Problem writing object", th);
        }
    }

    public Object readObject(String fname) {
        FileInputStream fin = getFin(fname);
        if (fin == null)
            return null;
        try {
            ObjectInputStream din = new ObjectInputStream(fin);
            Object o = din.readObject();
            din.close();
            return o;
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Could not read object from "
                    + fname + "!");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e(this.getClass().getName(),
                    "Could not find class: " + e.getMessage()
                            + " reading from " + fname + "!");
            e.printStackTrace();
        } catch (Throwable th){
        	Log.e(this.getClass().getName(), "Problem reading object", th);
        }
        return null;
    }

    public void writeText(String thing, String fname) {
        FileOutputStream fos = getFos(fname);
        if (fos == null)
            return;
        try {
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeUTF(thing);
            dos.close();
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Could not write text:" + thing
                    + "!");
            e.printStackTrace();
        } catch (Throwable th){
        	Log.e(this.getClass().getName(), "Problem writing text in "+fname, th);
        }
    }

    public String readText(String fname) {
        FileInputStream fin = getFin(fname);
        if (fin == null)
            return null;
        try {
            DataInputStream din = new DataInputStream(fin);
            String s = din.readUTF();
            din.close();
            return s;
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Could not read text from "
                    + fname + "!");
            e.printStackTrace();
        } catch (Throwable th){
        	Log.e(this.getClass().getName(), "Problem reading text", th);
        }
        return null;
    }

    private FileInputStream getFin(String fname) {
        try {
            return a.openFileInput(fname);
        } catch (FileNotFoundException e) {
            Log.w(this.getClass().getName(), "File "
                    + fname + " does not exist yet. Wait for reports.");
            // e.printStackTrace();
            return null;
        } catch (Throwable th){
        	Log.e(this.getClass().getName(), "Problem opening file "+fname+" for input", th);
        	return null;
        }
    }

    private FileOutputStream getFos(String fname) {
        try {
            return a.openFileOutput(fname, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Log.w(this.getClass().getName(), "File "
                    + fname + " does not exist yet. Wait for reports.");
            // e.printStackTrace();
            return null;
        } catch (Throwable th){
        	Log.e(this.getClass().getName(), "Problem opening file "+fname+" for output", th);
        	return null;
        }
    }

    public long readFreshness() {
        String s = readText(FRESHNESS);
        Log.d("CaratDataStorage", "Read freshness: " + s);
        if (s != null)
            return Long.parseLong(s);
        else
            return -1;
    }

    public Reports readReports() {
        Object o = readObject(FILENAME);
        Log.d("CaratDataStorage", "Read Reports: " + o);
        if (o != null) {
            caratData = new WeakReference<Reports>((Reports) o);
            return (Reports) o;
        } else
            return null;
    }

    /**
     * @return the freshness
     */
    public long getFreshness() {
        return freshness;
    }
    
    
    public long readBlacklistFreshness() {
        String s = readText(BLACKLIST_FRESHNESS);
        Log.d("CaratDataStorage", "Read freshness: " + s);
        if (s != null)
            return Long.parseLong(s);
        else
            return -1;
    }
    
    public long getBlacklistFreshness() {
        return blacklistFreshness;
    }
    
    public long readQuickHogsFreshness() {
        String s = readText(QUICKHOGS_FRESHNESS);
        Log.d("CaratDataStorage", "Read freshness: " + s);
        if (s != null)
            return Long.parseLong(s);
        else
            return -1;
    }
    
    public long getQuickHogsFreshness() {
        return quickHogsFreshness;
    }
    
    /**
     * @return a list of blacklisted apps
     */
    public List<String> getBlacklist() {
        if (blacklistedApps != null && blacklistedApps.get() != null)
            return blacklistedApps.get();
        else
            return readBlacklist();
    }
    
    /**
     * @return a list of blacklisted expressions
     */
    public List<String> getGloblist() {
        if (blacklistedGlobs != null && blacklistedGlobs.get() != null)
            return blacklistedGlobs.get();
        else
            return readGloblist();
    }
    
    /**
     * 
     * @return a list of blacklisted apps
     */
    public List<String> readBlacklist() {
        Object o = readObject(BLACKLIST_FILE);
        //Log.d("CaratDataStorage", "Read blacklist: " + o);
        if (o != null) {
            blacklistedApps = new WeakReference<List<String>>((List<String>) o);
            return (List<String>) o;
        } else
            return null;
    }
    
    /**
     * 
     * @param blacklist the list of blacklisted apps to write.
     */
    public void writeBlacklist(List<String> blacklist) {
        if (blacklist == null)
            return;
        blacklistedApps = new WeakReference<List<String>>(blacklist);
        writeObject(blacklist, BLACKLIST_FILE);
    }
    
    /**
     * 
     * @return a list of blacklisted expressions
     */
    public List<String> readGloblist() {
        Object o = readObject(GLOBLIST_FILE);
        //Log.d("CaratDataStorage", "Read glob blacklist: " + o);
        if (o != null) {
            blacklistedGlobs = new WeakReference<List<String>>((List<String>) o);
            return (List<String>) o;
        } else
            return null;
    }
    
    /**
     * 
     * @param globlist the list of blacklisted expressions to write.
     */
    public void writeGloblist(List<String> globlist) {
        if (globlist == null)
            return;
        blacklistedGlobs = new WeakReference<List<String>>(globlist);
        writeObject(globlist, GLOBLIST_FILE);
    }
    
    /**
     * @return the questionnaire base URL, read from storage or memory.
     */
    public String getQuestionnaireUrl() {
        if (questionnaireUrl != null)
            return questionnaireUrl;
        else
            return readQuestionnaireUrl();
    }
    
    /**
     * 
     * @return the questionnaire base URL, read from storage.
     */
    public String readQuestionnaireUrl() {
        String url = readText(QUESTIONNAIRE_URL_FILE);
        //Log.d("CaratDataStorage", "Read blacklist: " + o);
        if (url != null) {
            return url;
        } else
            return null;
    }
    
    /**
     * Write questionnaire URL to permanent storage.
     * @param url the questionnaire base URL.
     */
    public void writeQuestionnaireUrl(String url) {
        if (url == null)
            return;
        questionnaireUrl = url;
        writeText(url, QUESTIONNAIRE_URL_FILE);
    }
    
    
    public void samplesReported(int howmany) {
    	samples_reported += howmany;
        writeText(samples_reported+ "", SAMPLES_REPORTED);
    }
    
    public long readSamplesReported() {
        String s = readText(SAMPLES_REPORTED);
        Log.d("CaratDataStorage", "Read samples reported: " + s);
        if (s != null)
            return Long.parseLong(s);
        else
            return -1;
    }

    /**
     * @return the freshness
     */
    public long getSamplesReported() {
        return samples_reported;
    }

    /**
     * @return the caratData
     */
    public Reports getReports() {
        if (caratData != null && caratData.get() != null)
            return caratData.get();
        else
            return readReports();
    }

    /**
     * @return the bug reports
     */
    public SimpleHogBug[] getBugReport() {
        if (bugData == null || bugData.get() == null) {
            readBugReport();
        }
        if (bugData == null || bugData.get() == null)
            return null;
        return bugData.get();
    }

    /**
     * @return the hog reports
     */
    public SimpleHogBug[] getHogReport() {
        if (hogData == null || hogData.get() == null) {
            readHogReport();
        }
        if (hogData == null || hogData.get() == null)
            return null;
        return hogData.get();
    }

    public void writeBugReport(HogBugReport r) {
        if (r != null) {
            SimpleHogBug[] list = convert(r.getHbList(), true);
            if (list != null){
                bugData = new WeakReference<SimpleHogBug[]>(list);
                writeObject(list, BUGFILE);
            }
        }
    }

    public void writeHogReport(HogBugReport r) {
        if (r != null) {
            SimpleHogBug[] list = convert(r.getHbList(), false);
            if (list != null){
                hogData = new WeakReference<SimpleHogBug[]>(list);
                writeObject(list, HOGFILE);
            }
        }
    }

    private SimpleHogBug[] convert(List<HogsBugs> list, boolean isBug) {
        if (list == null)
            return null;
        SimpleHogBug[] result = new SimpleHogBug[list.size()];
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            HogsBugs item = list.get(i);
            result[i] = new SimpleHogBug(fixName(item.getAppName()), isBug ? Type.BUG:Type.HOG);
            result[i].setAppLabel(item.getAppLabel());
            String priority = item.getAppPriority();
            if (priority == null || priority.length() == 0)
                priority = "Foreground app";
            result[i].setAppPriority(priority);
            result[i].setExpectedValue(item.getExpectedValue());
            result[i].setExpectedValueWithout(item.getExpectedValueWithout());
            result[i].setwDistance(item.getWDistance());
            //result[i].setxVals(convert(item.getXVals()));
            result[i].setError(item.getError());
            result[i].setErrorWithout(item.getErrorWithout());
            result[i].setSamples(item.getSamples());
            result[i].setSamplesWithout(item.getSamplesWithout());
            //result[i].setSignificance(item.getSignificance());
            /*result[i].setyVals(convert(item.getYVals()));
            result[i].setxValsWithout(convert(item.getXValsWithout()));
            result[i].setyValsWithout(convert(item.getYValsWithout()));*/
        }
        return result;
    }

    public static double[] convert(List<Double> dbls) {
        if (dbls == null)
            return new double[0];
        for (int j = 0; j < dbls.size(); ++j) {
            if (dbls.get(j) == 0.0) {
                dbls.remove(j);
                j--;
            }
        }
        double[] arr = new double[dbls.size()];
        for (int j = 0; j < dbls.size(); ++j) {
            arr[j] = dbls.get(j);
        }
        return arr;
    }

    private String fixName(String name) {
        if (name == null)
            return null;
        int idx = name.lastIndexOf(':');
        if (idx <= 0)
            idx = name.length();
        String n = name.substring(0, idx);
        return n;
    }

    public SimpleHogBug[] readBugReport() {
        Object o = readObject(BUGFILE);
        //Log.d("CaratDataStorage", "Read Bugs: " + o);
        if (o == null || !(o instanceof SimpleHogBug[]))
            return null;
        SimpleHogBug[] r = (SimpleHogBug[]) o;
        bugData = new WeakReference<SimpleHogBug[]>(r);
        return r;
    }

    public SimpleHogBug[] readHogReport() {
        Object o = readObject(HOGFILE);
        //Log.d("CaratDataStorage", "Read Hogs: " + o);
        if (o == null || !(o instanceof SimpleHogBug[]))
            return null;
        SimpleHogBug[] r = (SimpleHogBug[]) o;
        hogData = new WeakReference<SimpleHogBug[]>(r);
        return r;
    }
}
