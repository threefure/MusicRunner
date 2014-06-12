package com.amk2.musicrunner;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amk2.musicrunner.MusicTrackMetaData.MusicTrackCommonDataDB;

/**
 * Created by ktlee on 5/25/14.
 */
public class TableObserver extends ContentObserver {
    private Handler mHandler;
    private ContentResolver contentResolver;
    public TableObserver(Context context, Handler handler) {
        super(handler);
        mHandler = handler;
        contentResolver = context.getContentResolver();
        Log.d("daz", "observer is created");
    }

    @Override
    public void onChange (boolean selfChange) {
        onChange(selfChange, null);
    }

    @Override
    public void onChange (boolean selfChange, Uri changeUri) {
        Log.d("daz", "detect changes! should start changing ui! with uri=" + changeUri.toString());
        if (changeUri.toString().indexOf(MusicTrackCommonDataDB.CONTENT_URI.toString()) > -1) {
            Message msg = new Message();
            String[] projection = {
                MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE,
                MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT,
                MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE
            };
            try {
                Cursor cursor = contentResolver.query(changeUri, projection, null, null, null);
                cursor.moveToFirst();
                String DataType = cursor.getString(cursor.getColumnIndex(MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE));

                if (DataType.equals(Constant.DB_KEY_DAILY_WEATHER)) {
                    String JSONContent = cursor.getString(cursor.getColumnIndex(MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT));
                    Bundle bundle = new Bundle();
                    bundle.putString(Constant.JSON_CONTENT, JSONContent);

                    msg.setData(bundle);
                    msg.what = Constant.UPDATE_START_FRAGMENT_UI;
                    mHandler.handleMessage(msg);

                    Log.d("daz", "notify start fragment to update ui");
                }
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            }

        }
    }
}
