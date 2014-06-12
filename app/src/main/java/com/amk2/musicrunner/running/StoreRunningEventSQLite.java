package com.amk2.musicrunner.running;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ktlee on 5/18/14.
 */
public class StoreRunningEventSQLite extends SQLiteOpenHelper {
    private static final int VERSION = 1;

    public StoreRunningEventSQLite (Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public StoreRunningEventSQLite (Context context, String name) {
        super(context, name, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}
