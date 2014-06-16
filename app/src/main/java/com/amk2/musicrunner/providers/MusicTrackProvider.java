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

import com.amk2.musicrunner.sqliteDB.MusicTrackDBHelper;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData.MusicTrackCommonDataDB;

/**
 * Created by ktlee on 5/24/14.
 */
public class MusicTrackProvider extends ContentProvider {
    private static final int COMMON_DATA_DIR_INDICATOR = 1;
    private static final int COMMON_DATA_ITEM_INDICATOR = 2;

    private MusicTrackDBHelper musicTrackDBHelper;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MusicTrackMetaData.AUTHORITY, "CommonData", COMMON_DATA_DIR_INDICATOR);
        uriMatcher.addURI(MusicTrackMetaData.AUTHORITY, "CommonData/#", COMMON_DATA_ITEM_INDICATOR);
    }

    @Override
    public boolean onCreate() {
        musicTrackDBHelper = new MusicTrackDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(MusicTrackCommonDataDB.TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case COMMON_DATA_DIR_INDICATOR:

                break;
            case COMMON_DATA_ITEM_INDICATOR:
                sqLiteQueryBuilder.appendWhere(MusicTrackCommonDataDB.COLUMN_NAME_ID + " = " + uri.getPathSegments().get(1));
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
        if (uriMatcher.match(uri) != COMMON_DATA_DIR_INDICATOR) {
            throw new IllegalArgumentException("Unknown URI - " + uri);
        }
        ContentValues cv;
        if (contentValues == null) {
            cv = new ContentValues();
        } else {
            cv = new ContentValues(contentValues);
        }
        if (!cv.containsKey(MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE)) {
            throw new IllegalArgumentException("ContentValue must contain data type");
        }

        SQLiteDatabase writableDB = musicTrackDBHelper.getWritableDatabase();
        long rowId = writableDB.insert(MusicTrackMetaData.MusicTrackCommonDataDB.TABLE_NAME, null, cv);
        if (rowId > 0) {
            Log.d("daz", "data is inserted in provider! with id=" + rowId);
            Uri addedUri = ContentUris.withAppendedId(MusicTrackCommonDataDB.CONTENT_URI, rowId);
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
        int cnt;
        switch (uriMatcher.match(uri)) {
            case COMMON_DATA_DIR_INDICATOR:
                cnt = writableDB.update(MusicTrackMetaData.MusicTrackCommonDataDB.TABLE_NAME, contentValues, selection, selectionArgs);
                Uri updatedUri = ContentUris.withAppendedId(MusicTrackCommonDataDB.CONTENT_URI, Integer.parseInt(selectionArgs[0]));
                this.getContext().getContentResolver().notifyChange(updatedUri, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI - " + uri);
        }
        return cnt;
    }
}
