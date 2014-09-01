package com.amk2.musicrunner.sqliteDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by daz on 2014/5/29.
 */
public class MusicRunnerDBHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    /*private static final String SQL_CREATE_COMMON_DATA_TABLE =
            "CREATE TABLE " + MusicRunnerDBMetaData.MusicTrackCommonDataDB.TABLE_NAME + " (" +
                    MusicRunnerDBMetaData.MusicTrackCommonDataDB.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    MusicRunnerDBMetaData.MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE + TEXT_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE + TEXT_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT + TEXT_TYPE +
                    " )";*/
    private static final String SQL_CREATE_RUNNING_EVENT_TABLE =
            "CREATE TABLE " + MusicRunnerDBMetaData.MusicRunnerRunningEventDB.TABLE_NAME + " (" +
                    MusicRunnerDBMetaData.MusicRunnerRunningEventDB.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    MusicRunnerDBMetaData.MusicRunnerRunningEventDB.COLUMN_NAME_DURATION            + INTEGER_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND + TEXT_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerRunningEventDB.COLUMN_NAME_CALORIES            + TEXT_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE            + TEXT_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerRunningEventDB.COLUMN_NAME_SPEED               + TEXT_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerRunningEventDB.COLUMN_NAME_ROUTE               + TEXT_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerRunningEventDB.COLUMN_NAME_PHOTO_PATH          + TEXT_TYPE +
                    " )";
    private static final String SQL_CREATE_SONG_PERFORMANCE_TABLE =
            "CREATE TABLE " + MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB.TABLE_NAME + " (" +
                    MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB.COLUMN_NAME_SONG_ID             + INTEGER_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB.COLUMN_NAME_EVENT_ID            + INTEGER_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB.COLUMN_NAME_DURATION            + INTEGER_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB.COLUMN_NAME_DATE_IN_MILLISECOND + TEXT_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB.COLUMN_NAME_CALORIES            + TEXT_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB.COLUMN_NAME_DISTANCE            + TEXT_TYPE + COMMA_SEP +
                    MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB.COLUMN_NAME_SPEED               + TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_SONG_NAME_TABLE =
            "CREATE TABLE " + MusicRunnerDBMetaData.MusicRunnerSongNameDB.TABLE_NAME + " (" +
                    MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_NAME                  + TEXT_TYPE +
                    " )";
    //private static final String SQL_DELETE_COMMON_DATA_TABLE = "DROP TABLE IF EXISTS " + MusicRunnerDBMetaData.MusicTrackCommonDataDB.TABLE_NAME;
    private static final String SQL_DELETE_RUNNING_EVENT_TABLE    = "DROP TABLE IF EXISTS " + MusicRunnerDBMetaData.MusicRunnerRunningEventDB.TABLE_NAME;
    private static final String SQL_DELETE_SONG_PERFORMANCE_TABLE = "DROP TABLE IF EXISTS " + MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB.TABLE_NAME;
    private static final String SQL_DELETE_SONG_NAME_TABLE        = "DROP TABLE IF EXISTS " + MusicRunnerDBMetaData.MusicRunnerSongNameDB.TABLE_NAME;

    public static final int DATA_VERSION = 2;
    public static final String DATABASE_NAME = "MusicRunner.db";

    public MusicRunnerDBHelper(Context context) {
        super (context, DATABASE_NAME, null, DATA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_DELETE_RUNNING_EVENT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RUNNING_EVENT_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_SONG_PERFORMANCE_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_SONG_PERFORMANCE_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_SONG_NAME_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_SONG_NAME_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(SQL_DELETE_RUNNING_EVENT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RUNNING_EVENT_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_SONG_PERFORMANCE_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_SONG_PERFORMANCE_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_SONG_NAME_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_SONG_NAME_TABLE);
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
