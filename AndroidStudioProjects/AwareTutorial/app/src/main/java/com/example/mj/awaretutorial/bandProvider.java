package com.example.mj.awaretutorial;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

/**
 * Created by MJ on 6/30/15.
 */
public class bandProvider extends ContentProvider {

    public static final int DATABASE_VERSION = 1;


    /**
     * Authority for this content provider
     */
    public static String AUTHORITY = "com.example.mj.awaretutorial.provider.band";

    private static final int BAND = 1;
    private static final int BAND_ID = 2;



    public static final class Band_Data implements BaseColumns {
        private Band_Data() {
        }

        ;

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/band");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.example.mj.awaretutorial.band";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.example.mj.awaretutorial.band";

        //columns for the table
        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String HeartRate = "Heart_Rate";

    }
        /**
         * Database stored in external folder: /AWARE/band.db
          */
        public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() +
                "/AWARE/band.db";


        /**
         * database tables
         */

        public static final String[] DATABASE_TABLES = {"band"};

        /**
         * Database table fields
         */

        public static final String[] TABLES_FIELDS = {
         Band_Data._ID + " integer primary key autoincrement," +
                 Band_Data.TIMESTAMP + " real default 0," +
                 Band_Data.DEVICE_ID + " text default ''," +
                 "UNIQUE(" + Band_Data.TIMESTAMP + "," + Band_Data.DEVICE_ID + ")," +
                 Band_Data.HeartRate + " int"
        };

        private static UriMatcher sUriMatcher = null;
        private static HashMap<String,String> tableMap = null;
        private static DatabaseHelper databaseHelper = null;
        private static SQLiteDatabase database = null;

        private boolean initializeDB(){
            if (databaseHelper == null){
                databaseHelper = new DatabaseHelper ( getContext(), DATABASE_NAME, null, DATABASE_VERSION
                , DATABASE_TABLES, TABLES_FIELDS);
            }
            if(databaseHelper != null && (database == null || !database.isOpen())){
                database = databaseHelper.getWritableDatabase();
            }
            return( database != null && databaseHelper != null);
        }


        /**
         * delete entry form the database
         */

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs){
            if(!initializeDB()){
                Log.w(AUTHORITY, "Database unavailable...");
                return 0;
            }
            int count = 0;
            switch(sUriMatcher.match(uri)){
                case BAND:
                    count = database.delete(DATABASE_TABLES[0], selection,selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }

        @Override
        public String getType(Uri uri){
            switch (sUriMatcher.match(uri)){
                case BAND:
                    return Band_Data.CONTENT_TYPE;
                case BAND_ID:
                    return Band_Data.CONTENT_ITEM_TYPE;
                default:
                    throw new IllegalArgumentException("unknown URI " + uri);
            }
        }

        /**
         * insert entry to the database
         */

        @Override
        public Uri insert(Uri uri, ContentValues initialValues){
            if(!initializeDB()){
                Log.w(AUTHORITY, "Database unavailable...");
                return null;
            }

            ContentValues values = (initialValues !=null) ? new ContentValues(initialValues) : new ContentValues();

            switch (sUriMatcher.match(uri)){
                case BAND:
                    database.beginTransaction();
                    long band_id = database.insertWithOnConflict(DATABASE_TABLES[0],
                            Band_Data.DEVICE_ID, values, SQLiteDatabase.CONFLICT_IGNORE);
                    database.setTransactionSuccessful();
                    database.endTransaction();;
                    if(band_id >0){
                        Uri bandUri = ContentUris.withAppendedId(Band_Data.CONTENT_URI, band_id);
                        getContext().getContentResolver().notifyChange(bandUri, null);
                        return bandUri;
                    }
                    throw new SQLException("Failed to insert row into" + uri);
                default:
                    throw new IllegalArgumentException("Unknown URI" + uri);
            }
        }



    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".provider.band";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(bandProvider.AUTHORITY, DATABASE_TABLES[0],
                BAND);
        sUriMatcher.addURI(bandProvider.AUTHORITY, DATABASE_TABLES[0] +
        "/#", BAND_ID);

        tableMap = new HashMap<String, String>();
        tableMap.put(Band_Data.TIMESTAMP, Band_Data.TIMESTAMP);
        tableMap.put(Band_Data.DEVICE_ID, Band_Data.DEVICE_ID);
        tableMap.put(Band_Data.HeartRate, Band_Data.HeartRate);

        return true;


    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case BAND:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(tableMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());

            return null;
        }
    }




    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if(!initializeDB()){
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }
        int count = 0;
        switch(sUriMatcher.match(uri)){
            case BAND:
                database.beginTransaction();
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                database .setTransactionSuccessful();
                database.endTransaction();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }
}
