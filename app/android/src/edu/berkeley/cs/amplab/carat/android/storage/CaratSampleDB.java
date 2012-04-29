package edu.berkeley.cs.amplab.carat.android.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
 * Stores samples in a SQLite database until sent. If you add COLUMNS in the
 * database, check all the TODO:s for places you need to update.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class CaratSampleDB {

    private static final String TAG = "CaratSampleDB";

    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_SAMPLE = "sample";

    public static final String DATABASE_NAME = "caratdata";
    public static final String SAMPLES_VIRTUAL_TABLE = "sampleobjects";
    private static final int DATABASE_VERSION = 2;

    private static final HashMap<String, String> mColumnMap = buildColumnMap();

    private Sample lastSample = null;

    private SampleDbOpenHelper sampleOpenHelper = null;

    private static CaratSampleDB instance = null;

    public static CaratSampleDB getInstance(Context c) {
        if (instance == null)
            instance = new CaratSampleDB(c);
        return instance;
    }

    public CaratSampleDB(Context context) {
        if (sampleOpenHelper == null)
            sampleOpenHelper = new SampleDbOpenHelper(context);
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
        map.put(COLUMN_TIMESTAMP, COLUMN_TIMESTAMP);
        map.put(COLUMN_SAMPLE, COLUMN_SAMPLE);
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

        Cursor cursor = builder.query(sampleOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, groupBy, having, sortOrder);

        if (cursor == null) {
            sampleOpenHelper.close();
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            sampleOpenHelper.close();
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

        Cursor cursor = query(null, null, columns, null, null, null);

        if (cursor == null) {
            // There are no results
            sampleOpenHelper.close();
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
            sampleOpenHelper.close();
            return results;
        }
    }

    public SortedMap<Long, Sample> queryOldestSamples(int howmany) {
        String[] columns = mColumnMap.keySet().toArray(
                new String[mColumnMap.size()]);

        Cursor cursor = query(null, null, columns, null, null, COLUMN_TIMESTAMP
                + " ASC LIMIT " + howmany);

        SortedMap<Long, Sample> results = new TreeMap<Long, Sample>();

        if (cursor == null) {
            // There are no results
            sampleOpenHelper.close();
            return results;
        } else {
            // Display the number of results
            int count = cursor.getCount();

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Sample s = fillSample(cursor);
                results.put(
                        cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)),
                        s);
                cursor.moveToNext();
            }
            cursor.close();
            sampleOpenHelper.close();
            return results;
        }
    }

    public int delete(String whereClause, String[] whereArgs) {
        SQLiteDatabase db = sampleOpenHelper.getWritableDatabase();
        int deleted = db.delete(SAMPLES_VIRTUAL_TABLE, whereClause, whereArgs);
        db.close();
        return deleted;
    }

    public int deleteOldestSamples(double timestamp) {
        return delete(COLUMN_TIMESTAMP + " <= ?",
                new String[] { timestamp + "" });
    }

    public int deleteSamples(Set<Long> rowids) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        sb.append("(");
        for (Long rowid : rowids) {
            sb.append("" + rowid);
            i++;
            if (i != rowids.size()) {
                sb.append(", ");
            }
        }
        sb.append(")");
        Log.d("CaratSampleDB", "Deleting where rowid in " + sb.toString());
        return delete("rowid in " + sb.toString(), null);
    }

    public Sample queryLastSample() {
        String[] columns = mColumnMap.keySet().toArray(
                new String[mColumnMap.size()]);

        Cursor cursor = query(null, null, columns, null, null, COLUMN_TIMESTAMP
                + " DESC LIMIT 1");

        if (cursor == null) {
            // There are no results
            sampleOpenHelper.close();
            return null;
        } else {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                Sample s = fillSample(cursor);
                cursor.close();
                sampleOpenHelper.close();
                lastSample = s;
                return s;
            }
            cursor.close();
            sampleOpenHelper.close();
            return null;
        }
    }

    /*
     * Read a sample from the current position of the cursor. TODO: Needs to be
     * updated when fields update.
     */
    private Sample fillSample(Cursor cursor) {
        Sample s = null;
        byte[] sampleB = cursor.getBlob(cursor
                .getColumnIndex(CaratSampleDB.COLUMN_SAMPLE));
        if (sampleB != null) {
            ObjectInputStream oi;
            try {
                oi = new ObjectInputStream(new ByteArrayInputStream(sampleB));
                Object o = oi.readObject();
                if (o != null)
                    s = (Sample) o;
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

        return s;
    }

    public Sample getLastSample(Context c) {
        if (lastSample == null)
            queryLastSample();
        return lastSample;
    }

    public long putSample(Sample s) {
        // force init
        sampleOpenHelper.getReadableDatabase();
        long id = sampleOpenHelper.addSample(s);
        sampleOpenHelper.close();
        return id;
    }

    /**
     * This creates/opens the database.
     */
    private static class SampleDbOpenHelper extends SQLiteOpenHelper {
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

        SampleDbOpenHelper(Context context) {
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
         * Add a sample to the database.
         * 
         * @return rowId or -1 if failed
         */
        public long addSample(Sample s) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COLUMN_TIMESTAMP, s.getTimestamp());
            // Add the piList as a blob
            if (s != null) {
                try {
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    ObjectOutputStream oo = new ObjectOutputStream(bo);
                    oo.writeObject(s);
                    initialValues.put(COLUMN_SAMPLE, bo.toByteArray());
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
