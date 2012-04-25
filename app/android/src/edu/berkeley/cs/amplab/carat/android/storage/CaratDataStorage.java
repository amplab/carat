package edu.berkeley.cs.amplab.carat.android.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.thrift.HogBugReport;
import edu.berkeley.cs.amplab.carat.thrift.HogsBugs;
import edu.berkeley.cs.amplab.carat.thrift.Reports;

public class CaratDataStorage {

	public static final String FILENAME = "carat-reports.dat";
	public static final String BUGFILE = "carat-bugs.dat";
	public static final String HOGFILE = "carat-hogs.dat";
			
	public static final String FRESHNESS = "carat-freshness.dat";
	private Application a = null;

	private long freshness = 0;
	private Reports caratData = null;
	private HogBugReport bugData = null;
	private HogBugReport hogData = null;

	public CaratDataStorage(Application a) {
		this.a = a;
		freshness = readFreshness();
		caratData = readReports();
		readBugReport();
		readHogReport();
	}

	public void writeReports(Reports reports) {
		caratData = reports;
		writeObject(reports, FILENAME);
	}

	public void writeFreshness() {
		freshness = System.currentTimeMillis();
		writeText(freshness + "", FRESHNESS);
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
		}
		return null;
	}

	private FileInputStream getFin(String fname) {
		try {
			return a.openFileInput(fname);
		} catch (FileNotFoundException e) {
			Log.e(this.getClass().getName(), "Could not open carat data file "
					+ fname + " for reading!");
			e.printStackTrace();
			return null;
		}
	}

	private FileOutputStream getFos(String fname) {
		try {
			return a.openFileOutput(fname, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			Log.e(this.getClass().getName(), "Could not open carat data file "
					+ fname + " for writing!");
			e.printStackTrace();
			return null;
		}
	}

	public long readFreshness() {
		String s = readText(FRESHNESS);
		Log.i("CaratDataStorage", "Read freshness: " + s);
		if (s != null)
			return Long.parseLong(s);
		else
			return -1;
	}

	public Reports readReports() {
		Object o = readObject(FILENAME);
		Log.i("CaratDataStorage", "Read Reports: " + o);
		if (o != null)
			return (Reports) o;
		else
			return null;
	}

	/**
	 * @return the freshness
	 */
	public long getFreshness() {
		return freshness;
	}

	/**
	 * @return the caratData
	 */
	public Reports getReports() {
		return caratData;
	}

	/**
	 * @return the bug reports
	 */
	public List<HogsBugs> getBugReport() {
		if (bugData == null)
			return null;
		return bugData.getHbList();
	}

	/**
	 * @return the hog reports
	 */
	public List<HogsBugs> getHogReport() {
		if (bugData == null)
			return null;
		return hogData.getHbList();
	}

	public void writeBugReport(HogBugReport r) {
		bugData = r;
		writeObject(r, BUGFILE);

	}

	public void writeHogReport(HogBugReport r) {
		hogData = r;
		writeObject(r, HOGFILE);

	}
	
	public HogBugReport readBugReport() {
		Object o = readObject(BUGFILE);
		Log.i("CaratDataStorage", "Read Bugs: " + o);
		if (o == null)
			return null;
		HogBugReport r = (HogBugReport) o;
		bugData = r;
		return r;
	}

	public HogBugReport readHogReport() {
		Object o = readObject(HOGFILE);
		Log.i("CaratDataStorage", "Read Hogs: " + o);
		if (o == null)
			return null;
		HogBugReport r = (HogBugReport) o;
		hogData = r;
		return r;
	}
}
