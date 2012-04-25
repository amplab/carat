package edu.berkeley.cs.amplab.carat.android.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;
import edu.berkeley.cs.amplab.carat.thrift.ProcessInfo;
import edu.berkeley.cs.amplab.carat.thrift.Sample;

/**
 * Stores samples in a SQLite database until sent.
 * If you add COLUMNS in the database, check all the TODO:s for places you need to update.
 * 
 * @author Eemil Lagerspetz
 *
 */
public class CaratDB {

	private static final String TAG = "CaratDB";

	public static final String COLUMN_UUID = "uuid";
	public static final String COLUMN_TIMESTAMP = "timestamp";
	public static final String COLUMN_TRIGGERED_BY = "triggeredby";
	public static final String COLUMN_BATTERY_LEVEL = "batterylevel";
	public static final String COLUMN_BATTERY_STATE = "batterystate";
	public static final String COLUMN_NETWORKSTATUS = "networkstatus";
	public static final String COLUMN_MEMORY_WIRED = "memorywired";
	public static final String COLUMN_DISTANCE_TRAVELED = "distancetraveled";
	public static final String COLUMN_MEMORY_ACTIVE = "memoryactive";
	public static final String COLUMN_MEMORY_USER = "memoryuser";
	public static final String COLUMN_MEMORY_FREE = "memoryfree";
	public static final String COLUMN_MEMORY_INACTIVE = "memoryinactive";
	public static final String COLUMN_PILIST = "pilist";

	public static final String DATABASE_NAME = "caratdata";
	public static final String SAMPLES_VIRTUAL_TABLE = "samples";
	private static final int DATABASE_VERSION = 2;

	private static final HashMap<String, String> mColumnMap = buildColumnMap();

	private Sample lastSample = null;

	private DictionaryOpenHelper mDatabaseOpenHelper = null;
	
	private static CaratDB instance = null;
	
	public static CaratDB getInstance(Context c){
		if (instance == null)
			instance = new CaratDB(c);
		return instance;
	}

	public CaratDB(Context context) {
		if (mDatabaseOpenHelper == null)
			mDatabaseOpenHelper = new DictionaryOpenHelper(context);
	}

	/**
	 * 
	 * Builds a map for all columns that may be requested, which will be given
	 * to the SQLiteQueryBuilder. This is a good way to define aliases for
	 * column names, but must include all columns, even if the value is the key.
	 * This allows the ContentProvider to request columns w/o the need to know
	 * real column names and create the alias itself.
	 * 
	 * TODO: Needs to be updated when fields update.
	 */
	private static HashMap<String, String> buildColumnMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(COLUMN_BATTERY_LEVEL, COLUMN_BATTERY_LEVEL);
		map.put(COLUMN_BATTERY_STATE, COLUMN_BATTERY_STATE);
		map.put(COLUMN_DISTANCE_TRAVELED, COLUMN_DISTANCE_TRAVELED);
		map.put(COLUMN_MEMORY_ACTIVE, COLUMN_MEMORY_ACTIVE);
		map.put(COLUMN_MEMORY_INACTIVE, COLUMN_MEMORY_INACTIVE);
		map.put(COLUMN_MEMORY_USER, COLUMN_MEMORY_USER);
		map.put(COLUMN_MEMORY_FREE, COLUMN_MEMORY_FREE);
		map.put(COLUMN_MEMORY_WIRED, COLUMN_MEMORY_WIRED);
		map.put(COLUMN_NETWORKSTATUS, COLUMN_NETWORKSTATUS);
		map.put(COLUMN_PILIST, COLUMN_PILIST);
		map.put(COLUMN_TIMESTAMP, COLUMN_TIMESTAMP);
		map.put(COLUMN_TRIGGERED_BY, COLUMN_TRIGGERED_BY);
		map.put(COLUMN_UUID, COLUMN_UUID);
		map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
		return map;
	}

	/**
	 * Performs a database query.
	 * 
	 * @param selection
	 *            The selection clause
	 * @param selectionArgs
	 *            Selection arguments for "?" components in the selection
	 * @param columns
	 *            The columns to return
	 * @return A Cursor over all rows matching the query
	 */
	private Cursor query(String selection, String[] selectionArgs,
			String[] columns, String groupBy, String having, String sortOrder) {
		/*
		 * The SQLiteBuilder provides a map for all possible columns requested
		 * to actual columns in the database, creating a simple column alias
		 * mechanism by which the ContentProvider does not need to know the real
		 * column names
		 */
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(SAMPLES_VIRTUAL_TABLE);
		builder.setProjectionMap(mColumnMap);

		Cursor cursor = builder.query(
				mDatabaseOpenHelper.getReadableDatabase(), columns, selection,
				selectionArgs, groupBy, having, sortOrder);

		if (cursor == null) {
			return null;
		} else if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		return cursor;
	}

	/**
	 * Searches the database and returns Samples not yet sent.
	 */

	public Sample[] querySamples() {
		String[] columns = mColumnMap.keySet().toArray(
				new String[mColumnMap.size()]);

		Cursor cursor = query(null, null, columns,null,null,null);

		if (cursor == null) {
			// There are no results
			return new Sample[0];
		} else {
			// Display the number of results
			int count = cursor.getCount();

			Sample[] results = new Sample[count];
			int i = 0;

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Sample s = fillSample(cursor);
				results[i] = s;
				i++;
				cursor.moveToNext();
			}
			cursor.close();
			return results;
		}
	}
	
	public Sample[] queryOldestSamples(int howmany) {
		String[] columns = mColumnMap.keySet().toArray(
				new String[mColumnMap.size()]);

		Cursor cursor = query(null, null, columns,null,null,COLUMN_TIMESTAMP +" ASC LIMIT "+howmany);

		if (cursor == null) {
			// There are no results
			return new Sample[0];
		} else {
			// Display the number of results
			int count = cursor.getCount();

			Sample[] results = new Sample[count];
			int i = 0;

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Sample s = fillSample(cursor);
				results[i] = s;
				i++;
				cursor.moveToNext();
			}
			cursor.close();
			return results;
		}
	}
	
	public int delete(String whereClause, String[] whereArgs){
		SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
		int deleted =  db.delete(SAMPLES_VIRTUAL_TABLE, whereClause, whereArgs);
		db.close();
		return deleted;
	}
	
	public int deleteOldestSamples(double timestamp){
		return delete(COLUMN_TIMESTAMP+" <= ?", new String[]{timestamp +""});
	}

	public Sample queryLastSample() {
		String[] columns = mColumnMap.keySet().toArray(
				new String[mColumnMap.size()]);

		Cursor cursor = query(null, null, columns, null, null, COLUMN_TIMESTAMP+" DESC LIMIT 1");

		if (cursor == null) {
			// There are no results
			return null;
		} else {
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				Sample s = fillSample(cursor);
				cursor.close();
				lastSample = s;
				return s;
			}
			return null;
		}
	}

	/*
	 * Read a sample from the current position of the cursor. TODO: Needs to be
	 * updated when fields update.
	 */
	private Sample fillSample(Cursor cursor) {
		Sample s = new Sample();
		s.setUuId(cursor.getString(cursor.getColumnIndex(CaratDB.COLUMN_UUID)));
		s.setTimestamp(cursor.getDouble(cursor
				.getColumnIndex(CaratDB.COLUMN_TIMESTAMP)));
		s.setTriggeredBy(cursor.getString(cursor
				.getColumnIndex(CaratDB.COLUMN_TRIGGERED_BY)));
		s.setBatteryLevel(cursor.getDouble(cursor
				.getColumnIndex(CaratDB.COLUMN_BATTERY_LEVEL)));
		s.setBatteryState(cursor.getString(cursor
				.getColumnIndex(CaratDB.COLUMN_BATTERY_STATE)));
		s.setNetworkStatus(cursor.getString(cursor
				.getColumnIndex(CaratDB.COLUMN_NETWORKSTATUS)));
		s.setMemoryActive(cursor.getInt(cursor
				.getColumnIndex(CaratDB.COLUMN_MEMORY_ACTIVE)));
		s.setMemoryInactive(cursor.getInt(cursor
				.getColumnIndex(CaratDB.COLUMN_MEMORY_INACTIVE)));
		s.setMemoryUser(cursor.getInt(cursor
				.getColumnIndex(CaratDB.COLUMN_MEMORY_USER)));
		s.setMemoryFree(cursor.getInt(cursor
				.getColumnIndex(CaratDB.COLUMN_MEMORY_FREE)));
		s.setMemoryWired(cursor.getInt(cursor
				.getColumnIndex(CaratDB.COLUMN_MEMORY_WIRED)));
		s.setDistanceTraveled(cursor.getDouble(cursor
				.getColumnIndex(CaratDB.COLUMN_DISTANCE_TRAVELED)));
		byte[] pidlistB = cursor.getBlob(cursor
				.getColumnIndex(CaratDB.COLUMN_PILIST));
		if (pidlistB != null) {
			ObjectInputStream oi;
			try {
				oi = new ObjectInputStream(new ByteArrayInputStream(pidlistB));
				Object o = oi.readObject();
				if (o != null)
					s.setPiList((List<ProcessInfo>) o);
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// s.setDistanceTraveled(distanceTraveled)
		// s.setMemoryActive(memoryActive)
		// s.setMemoryFree(memoryFree)
		// s.setMemoryInactive(memoryInactive)
		// s.setMemoryUser(memoryUser)
		// s.setMemoryWired(memoryWired)
		// s.setNetworkStatus(networkStatus)

		// cursor.getX(columnIndex) ...
		return s;
	}

	public Sample getLastSample(Context c) {
		if (lastSample == null)
			queryLastSample();
		return lastSample;
	}

	public long putSample(Sample s) {
		// force init
		mDatabaseOpenHelper.getReadableDatabase();
		long id = mDatabaseOpenHelper.addSample(s);
		mDatabaseOpenHelper.close();
		return id;
	}

	/**
	 * This creates/opens the database.
	 */
	private static class DictionaryOpenHelper extends SQLiteOpenHelper {
		private SQLiteDatabase mDatabase;

		/*
		 * Note that FTS3 does not support column constraints and thus, you
		 * cannot declare a primary key. However, "rowid" is automatically used
		 * as a unique identifier, so when making requests, we will use "_id" as
		 * an alias for "rowid"
		 */
		private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE "
				+ SAMPLES_VIRTUAL_TABLE + " USING fts3 (" + createStatement()
				+ ");";

		private static final String createStatement() {
			Set<String> set = mColumnMap.keySet();
			StringBuilder b = new StringBuilder();
			int i = 0;
			int size = set.size() - 1;
			for (String s : set) {
				if (s.equals(BaseColumns._ID))
					continue;
				if (i + 1 == size)
					b.append(s);
				else
					b.append(s + ", ");
				i++;
			}
			return b.toString();
		}

		DictionaryOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			mDatabase = db;
			mDatabase.execSQL(FTS_TABLE_CREATE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.
		 * sqlite.SQLiteDatabase)
		 */
		@Override
		public void onOpen(SQLiteDatabase db) {
			mDatabase = db;
			super.onOpen(db);
		}

		/**
		 * Add a sample to the database. TODO: Needs to be updated when fields
		 * update.
		 * 
		 * @return rowId or -1 if failed
		 */
		public long addSample(Sample s) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(COLUMN_TIMESTAMP, s.getTimestamp());
			initialValues.put(COLUMN_UUID, s.getUuId());
			initialValues
					.put(COLUMN_DISTANCE_TRAVELED, s.getDistanceTraveled());
			initialValues.put(COLUMN_MEMORY_WIRED, s.getMemoryWired());
			initialValues.put(COLUMN_MEMORY_USER, s.getMemoryUser());
			initialValues.put(COLUMN_MEMORY_FREE, s.getMemoryFree());
			initialValues.put(COLUMN_MEMORY_ACTIVE, s.getMemoryActive());
			initialValues.put(COLUMN_MEMORY_INACTIVE, s.getMemoryInactive());
			initialValues.put(COLUMN_TRIGGERED_BY, s.getTriggeredBy());
			initialValues.put(COLUMN_BATTERY_STATE, s.getBatteryState());
			initialValues.put(COLUMN_BATTERY_LEVEL, s.getBatteryLevel());
			initialValues.put(COLUMN_NETWORKSTATUS, s.getNetworkStatus());
			// Add the piList as a blob
			Object o = s.getPiList();
			if (o != null) {
				try {
					ByteArrayOutputStream bo = new ByteArrayOutputStream();
					ObjectOutputStream oo = new ObjectOutputStream(bo);
					oo.writeObject(o);
					initialValues.put(COLUMN_PILIST, bo.toByteArray());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return mDatabase.insert(SAMPLES_VIRTUAL_TABLE, null, initialValues);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + SAMPLES_VIRTUAL_TABLE);
			onCreate(db);
		}
	}
}
