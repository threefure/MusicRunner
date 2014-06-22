package com.amk2.musicrunner.sqliteDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by daz on 2014/5/29.
 */
public class MusicTrackDBHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_COMMON_DATA_TABLE =
            "CREATE TABLE " + MusicTrackMetaData.MusicTrackCommonDataDB.TABLE_NAME + " (" +
                    MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE + TEXT_TYPE + COMMA_SEP +
                    MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE + TEXT_TYPE + COMMA_SEP +
                    MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT + TEXT_TYPE +
                    " )";
    private static final String SQL_CREATE_RUNNING_EVENT_DATA_TABLE =
            "CREATE TABLE " + MusicTrackMetaData.MusicTrackRunningEventDataDB.TABLE_NAME + " (" +
                    MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES + TEXT_TYPE + COMMA_SEP +
                    MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE + TEXT_TYPE + COMMA_SEP +
                    MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED + TEXT_TYPE + COMMA_SEP +
                    MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_PICTURE_PATH + TEXT_TYPE +
                    " )";
    private static final String SQL_DELETE_COMMON_DATA_TABLE = "DROP TABLE IF EXISTS " + MusicTrackMetaData.MusicTrackCommonDataDB.TABLE_NAME;
    private static final String SQL_DELETE_RUNNING_EVENT_DATA_TABLE = "DROP TABLE IF EXISTS " + MusicTrackMetaData.MusicTrackRunningEventDataDB.TABLE_NAME;

    public static final int DATA_VERSION = 4;
    public static final String DATABASE_NAME = "MusicTrackRunner.db";

    public MusicTrackDBHelper(Context context) {
        super (context, DATABASE_NAME, null, DATA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_DELETE_COMMON_DATA_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_COMMON_DATA_TABLE);

        sqLiteDatabase.execSQL(SQL_DELETE_RUNNING_EVENT_DATA_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RUNNING_EVENT_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(SQL_DELETE_COMMON_DATA_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_COMMON_DATA_TABLE);

        sqLiteDatabase.execSQL(SQL_DELETE_RUNNING_EVENT_DATA_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RUNNING_EVENT_DATA_TABLE);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int newerVersion, int oldVersion){
        super.onDowngrade(sqLiteDatabase, newerVersion, oldVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase sqLiteDatabase) {
        super.onOpen(sqLiteDatabase);
    }
}
