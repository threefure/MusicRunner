package com.amk2.musicrunner.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.amk2.musicrunner.sqliteDB.MusicRunnerDBHelper;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicTrackCommonDataDB;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerRunningEventDB;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerSongNameDB;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerArtistDB;

/**
 * Created by ktlee on 5/24/14.
 */
public class MusicRunnerProvider extends ContentProvider {
    private static final String TAG = "MusicRunnerProvider";

    private static final int COMMON_DATA_DIR_INDICATOR  = 1;
    private static final int COMMON_DATA_ITEM_INDICATOR = 2;
    private static final int RUNNING_EVENT_DATA_DIR_INDICATOR  = 3;
    private static final int RUNNING_EVENT_DATA_ITEM_INDICATOR = 4;
    private static final int SONG_PERFORMANCE_DATA_DIR_INDICATOR = 5;
    private static final int SONG_PERFORMANCE_DATA_ITEM_INDICATOR = 6;
    private static final int SONG_NAME_DATA_DIR_INDICATOR = 7;
    private static final int SONG_NAME_DATA_ITEM_INDICATOR = 8;
    private static final int ARTIST_DATA_DIR_INDICATOR = 9;
    private static final int ARTIST_DATA_ITEM_INDICATOR = 10;

    private MusicRunnerDBHelper musicTrackDBHelper;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MusicRunnerDBMetaData.AUTHORITY, MusicTrackCommonDataDB.TABLE_NAME, COMMON_DATA_DIR_INDICATOR);
        uriMatcher.addURI(MusicRunnerDBMetaData.AUTHORITY, MusicTrackCommonDataDB.TABLE_NAME + "/#", COMMON_DATA_ITEM_INDICATOR);
        uriMatcher.addURI(MusicRunnerDBMetaData.AUTHORITY, MusicRunnerRunningEventDB.TABLE_NAME, RUNNING_EVENT_DATA_DIR_INDICATOR);
        uriMatcher.addURI(MusicRunnerDBMetaData.AUTHORITY, MusicRunnerRunningEventDB.TABLE_NAME + "/#", RUNNING_EVENT_DATA_ITEM_INDICATOR);
        uriMatcher.addURI(MusicRunnerDBMetaData.AUTHORITY, MusicRunnerSongPerformanceDB.TABLE_NAME, SONG_PERFORMANCE_DATA_DIR_INDICATOR);
        uriMatcher.addURI(MusicRunnerDBMetaData.AUTHORITY, MusicRunnerSongPerformanceDB.TABLE_NAME + "/#", SONG_PERFORMANCE_DATA_ITEM_INDICATOR);
        uriMatcher.addURI(MusicRunnerDBMetaData.AUTHORITY, MusicRunnerSongNameDB.TABLE_NAME, SONG_NAME_DATA_DIR_INDICATOR);
        uriMatcher.addURI(MusicRunnerDBMetaData.AUTHORITY, MusicRunnerSongNameDB.TABLE_NAME + "/#", SONG_NAME_DATA_ITEM_INDICATOR);
        uriMatcher.addURI(MusicRunnerDBMetaData.AUTHORITY, MusicRunnerArtistDB.TABLE_NAME, ARTIST_DATA_DIR_INDICATOR);
        uriMatcher.addURI(MusicRunnerDBMetaData.AUTHORITY, MusicRunnerArtistDB.TABLE_NAME + "/#", ARTIST_DATA_ITEM_INDICATOR);
    }

    @Override
    public boolean onCreate() {
        musicTrackDBHelper = new MusicRunnerDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case COMMON_DATA_DIR_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicTrackCommonDataDB.TABLE_NAME);
                break;
            case COMMON_DATA_ITEM_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicTrackCommonDataDB.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MusicTrackCommonDataDB.COLUMN_NAME_ID + " = " + uri.getPathSegments().get(1));
                break;
            case RUNNING_EVENT_DATA_DIR_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicRunnerRunningEventDB.TABLE_NAME);
                break;
            case RUNNING_EVENT_DATA_ITEM_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicRunnerRunningEventDB.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MusicRunnerRunningEventDB.COLUMN_NAME_ID + " = " + uri.getPathSegments().get(1));
                break;
            case SONG_PERFORMANCE_DATA_DIR_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicRunnerSongPerformanceDB.TABLE_NAME);
                break;
            case SONG_PERFORMANCE_DATA_ITEM_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicRunnerSongPerformanceDB.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MusicRunnerSongPerformanceDB.COLUMN_NAME_ID + " = " + uri.getPathSegments().get(1));
                break;
            case SONG_NAME_DATA_DIR_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicRunnerSongNameDB.TABLE_NAME);
                break;
            case SONG_NAME_DATA_ITEM_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicRunnerSongNameDB.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MusicRunnerSongNameDB.COLUMN_NAME_ID + " = " + uri.getPathSegments().get(1));
                break;
            case ARTIST_DATA_DIR_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicRunnerArtistDB.TABLE_NAME);
                break;
            case ARTIST_DATA_ITEM_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicRunnerArtistDB.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MusicRunnerArtistDB.COLUMN_NAME_ID + " = " + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI - " + uri.toString());
        }

        SQLiteDatabase readableDB = musicTrackDBHelper.getReadableDatabase();
        Cursor cursor = sqLiteQueryBuilder.query(readableDB, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        ContentValues cv;
        String TableName = null;
        Uri ContentUri = null;

        if (contentValues == null) {
            cv = new ContentValues();
        } else {
            cv = new ContentValues(contentValues);
        }

        switch (uriMatcher.match(uri)) {
            case COMMON_DATA_DIR_INDICATOR:
                /*if (contentValues == null) {
                    cv = new ContentValues();
                } else {
                    cv = new ContentValues(contentValues);
                }*/
                if (!cv.containsKey(MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE)) {
                    throw new IllegalArgumentException("ContentValue must contain data type");
                }
                TableName = MusicTrackCommonDataDB.TABLE_NAME;
                ContentUri = MusicTrackCommonDataDB.CONTENT_URI;
                break;
            case RUNNING_EVENT_DATA_DIR_INDICATOR:
                /*if (contentValues == null) {
                    cv = new ContentValues();
                } else {
                    cv = new ContentValues(contentValues);
                }*/
                TableName = MusicRunnerRunningEventDB.TABLE_NAME;
                ContentUri = MusicRunnerRunningEventDB.CONTENT_URI;
                break;
            case SONG_PERFORMANCE_DATA_DIR_INDICATOR:
                if (!cv.containsKey(MusicRunnerSongPerformanceDB.COLUMN_NAME_SONG_ID)) {
                    throw new IllegalArgumentException("ContentValue must contain song ID");
                }
                TableName = MusicRunnerSongPerformanceDB.TABLE_NAME;
                ContentUri = MusicRunnerSongPerformanceDB.CONTENT_URI;
                break;
            case SONG_NAME_DATA_DIR_INDICATOR:
                TableName = MusicRunnerSongNameDB.TABLE_NAME;
                ContentUri = MusicRunnerSongNameDB.CONTENT_URI;
                break;
            case ARTIST_DATA_DIR_INDICATOR:
                TableName = MusicRunnerArtistDB.TABLE_NAME;
                ContentUri = MusicRunnerArtistDB.CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI - " + uri);
        }
        SQLiteDatabase writableDB = musicTrackDBHelper.getWritableDatabase();
        long rowId = writableDB.insert(TableName, null, cv);
        if (rowId > 0) {
            Log.d(TAG, "data is inserted in table:" + TableName + " with id=" + rowId);
            Uri addedUri = ContentUris.withAppendedId(ContentUri, rowId);
            this.getContext().getContentResolver().notifyChange(addedUri, null);
            return addedUri;
        }
        throw new IllegalArgumentException("Failed to insert data to uri - " + uri);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase writableDB = musicTrackDBHelper.getWritableDatabase();
        int cnt = -1;
        Uri updatedUri = null;
        switch (uriMatcher.match(uri)) {
            case COMMON_DATA_DIR_INDICATOR:
                cnt = writableDB.update(MusicTrackCommonDataDB.TABLE_NAME, contentValues, selection, selectionArgs);
                updatedUri = ContentUris.withAppendedId(MusicTrackCommonDataDB.CONTENT_URI, Integer.parseInt(selectionArgs[0]));
                break;
            case RUNNING_EVENT_DATA_DIR_INDICATOR:
                cnt = writableDB.update(MusicRunnerRunningEventDB.TABLE_NAME, contentValues, selection, selectionArgs);
                updatedUri = ContentUris.withAppendedId(MusicRunnerRunningEventDB.CONTENT_URI, Integer.parseInt(selectionArgs[0]));
                break;
            case SONG_NAME_DATA_DIR_INDICATOR:
                cnt = writableDB.update(MusicRunnerSongNameDB.TABLE_NAME, contentValues, selection, selectionArgs);
                updatedUri = ContentUris.withAppendedId(MusicRunnerSongNameDB.CONTENT_URI, Integer.parseInt(selectionArgs[0]));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI - " + uri);
        }
        this.getContext().getContentResolver().notifyChange(updatedUri, null);
        return cnt;
    }
}
