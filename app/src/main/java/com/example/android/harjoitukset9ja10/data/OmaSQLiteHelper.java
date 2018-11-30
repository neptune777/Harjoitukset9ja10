package com.example.android.harjoitukset9ja10.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by miksa on 2/6/18.
 */

/**
 * Modified by matti on 11/30/18.
 */

public class OmaSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lokaatiot.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + DatabaseContract.DatabaseEntry.TABLE_LOCATIONS + "( " + DatabaseContract.DatabaseEntry._ID
            + " integer primary key autoincrement, " +
            DatabaseContract.DatabaseEntry.COLUMN_LATITUDE
            + " text not null, "+ DatabaseContract.DatabaseEntry.COLUMN_LONGITUDE
            + " text not null,"+
            DatabaseContract.DatabaseEntry.COLUMN_ACCURACY
            + DatabaseContract.DatabaseEntry.COLUMN_PROVIDER
            + " text not null,"
            + " text not null, "+ DatabaseContract.DatabaseEntry.COLUMN_TIME
            + " text not null);";

    public OmaSQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(OmaSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.DatabaseEntry.TABLE_LOCATIONS);
        onCreate(db);
    }

}