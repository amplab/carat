package edu.berkeley.cs.amplab.carat.android.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.thrift.Sample;

public class CaratDB {
	
	public static final String SAMPLE_FILE = "carat-samples.dat";
			
	private Sample lastSample = null;

	public CaratDB() {
	}

	public void writeObject(Context c, Object o, String fname) {
		FileOutputStream fos = getFos(c, fname);
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

	public Object readObject(Context c, String fname) {
		FileInputStream fin = getFin(c, fname);
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

	public void writeText(Context c, String thing, String fname) {
		FileOutputStream fos = getFos(c, fname);
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

	public String readText(Context c, String fname) {
		FileInputStream fin = getFin(c, fname);
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

	private FileInputStream getFin(Context c, String fname) {
		try {
			return c.openFileInput(fname);
		} catch (FileNotFoundException e) {
			Log.e(this.getClass().getName(), "Could not open carat data file "
					+ fname + " for reading!");
			e.printStackTrace();
			return null;
		}
	}

	private FileOutputStream getFos(Context c, String fname) {
		try {
			return c.openFileOutput(fname, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			Log.e(this.getClass().getName(), "Could not open carat data file "
					+ fname + " for writing!");
			e.printStackTrace();
			return null;
		}
	}
	
	public void writeSample(Context c, Sample s) {
		lastSample = s;
		writeObject(c, s, SAMPLE_FILE);
	}
	
	public Sample readSample(Context c){
		Object o = readObject(c, SAMPLE_FILE);
		Log.i("CaratDataStorage", "Read Sample: " + o);
		if (o == null)
			return null;
		lastSample = (Sample) o;
		return lastSample;
	}
	
	public Sample getLastSample(Context c){
		if (lastSample == null)
			return readSample(c);
		return lastSample;
	}
}
