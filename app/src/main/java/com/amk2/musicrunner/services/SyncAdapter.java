package com.amk2.musicrunner.services;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData.MusicTrackCommonDataDB;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

/**
 * Created by ktlee on 5/24/14.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter{
    private ContentResolver contentResolver;

    private static long dailyWeatherID = -1;
    private static long t24HrsWeatherID = -1;
    private static long weeklyWeatherID = -1;
    private static long youbikeID = -1;

    public SyncAdapter (Context context, boolean autoInintialize) {
        super(context, autoInintialize);
        Log.d("daz", "syncadapter");
        contentResolver = context.getContentResolver();
    }

    public SyncAdapter (Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        contentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String AUTHORITY, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d("Daz", "in syncAdapter, request!!!!!");
        Log.d("daz", bundle.getString(Constant.SYNC_UPDATE));
        if (bundle.getString(Constant.SYNC_UPDATE).equals(Constant.UPDATE_WEATHER)) {
            // update daily weather
            String cityCode = bundle.getString(Constant.SYNC_CITYCODE);
            String urlString = Constant.baseWeatherUrlString + "?" + Constant.cityCodeQuery + cityCode;
            dailyWeatherID = GetDataFromServerAndSetExpirationDate(
                    urlString,
                    Constant.DB_KEY_DAILY_WEATHER,
                    Calendar.HOUR,
                    Constant.EXPIRATION_DATE_DURATION_DAILY,
                    dailyWeatherID);
            Log.d("daz", "updated db for daily, observer should know this change, new provider, with id=" + dailyWeatherID);

        } else if (bundle.getString(Constant.SYNC_UPDATE).equals(Constant.UPDATE_24HRS_WEATHER)) {
            // update 24 hours weather
            String cityCode = bundle.getString(Constant.SYNC_CITYCODE);
            String urlString = Constant.baseWeather24HoursUrlString + "?" + Constant.cityCodeQuery + cityCode + "&currentHour=" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            t24HrsWeatherID = GetDataFromServerAndSetExpirationDate(
                    urlString,
                    Constant.DB_KEY_24HRS_WEATHER,
                    Calendar.HOUR,
                    Constant.EXPIRATION_DATE_DURATION_24HRS,
                    t24HrsWeatherID);
            Log.d("daz", "updated db for 24hours, observer should know this change, new provider, with id=" + t24HrsWeatherID);

        } else if (bundle.getString(Constant.SYNC_UPDATE).equals(Constant.UPDATE_WEEKLY_WEATHER)) {
            // update weekly weather
            String cityCode = bundle.getString(Constant.SYNC_CITYCODE);
            String urlString = Constant.baseWeatherWeekUrlString + "?" + Constant.cityCodeQuery + cityCode;
            weeklyWeatherID = GetDataFromServerAndSetExpirationDate(
                    urlString,
                    Constant.DB_KEY_WEEKLY_WEATHER,
                    Calendar.HOUR,
                    Constant.EXPIRATION_DATE_DURATION_WEEKLY,
                    weeklyWeatherID);
            Log.d("daz", "updated db for weekly, observer should know this change, new provider, with id=" + weeklyWeatherID);

        } else if (bundle.getString(Constant.SYNC_UPDATE).equals(Constant.UPDATE_UBIKE)) {
            Log.d("daz", "calling ubike api");
            String urlString = Constant.baseYoubikeUrlString;
            youbikeID = GetDataFromServerAndSetExpirationDate(
                    urlString,
                    Constant.DB_KEY_YOUBIKE,
                    Calendar.MINUTE,
                    Constant.EXPIRATION_DATE_DURATION_YOUBIKE,
                    youbikeID);
            Log.d("daz", "updated db for weekly, observer should know this change, new provider, with id=" + weeklyWeatherID);

        }
    }

    private long GetDataFromServerAndSetExpirationDate (String urlString,
                                                        String DBKeyType,
                                                        int ExpirationDateDelayType,
                                                        int ExpirationDateDelayAmount,
                                                        long _ID) {
        InputStream stream = null;
        long returnID = -1;
        try {
            stream = downloadUrl(urlString);
            String JSONString = getStringFromInputStream(stream);
            Calendar expirationDate = Calendar.getInstance();
            expirationDate.add(ExpirationDateDelayType, ExpirationDateDelayAmount);
            //Log.d("new data content weatherJSONString", weatherJSONString);
            //Log.d("expirationDate.getTime().toString()", expirationDate.getTime().toString());
            Log.d("daz", "updating db id=" + _ID + " type=" + DBKeyType + " content: " + JSONString);
            returnID = InsertCommonData(
                    _ID,
                    DBKeyType,
                    expirationDate.getTime().toString(),
                    JSONString);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return returnID;
    }

    private void readDB() {
        //-------read db-----------
        String[] projection = {
                MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT,
                MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE
        };
        String selection = MusicTrackCommonDataDB.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(dailyWeatherID) };
        Cursor cursor = contentResolver.query(
                MusicTrackCommonDataDB.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        cursor.moveToFirst();
        String cor = cursor.getString(cursor.getColumnIndex(MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT));
        Log.d("daz", "weatherJSONString COLUMN_NAME_JSON_CONTENT in db: " + cor);
        String date = cursor.getString(cursor.getColumnIndex(MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE));
        Log.d("daz", "weatherJSONString COLUMN_NAME_EXPIRATION_DATE in db: " + date);
        //----------------------
    }

    private long InsertCommonData (long index, String type, String expirationDate, String jsonContent) {
        if (index >= 0) {
            Log.d("daz", "update!");
            ContentValues values = new ContentValues();
            values.put(MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE, expirationDate);
            values.put(MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT, jsonContent);

            String selection = MusicTrackCommonDataDB.COLUMN_NAME_ID + " LIKE ?";
            String[] selectionArgs = { String.valueOf(index) };
            contentResolver.update(MusicTrackCommonDataDB.CONTENT_URI, values, selection, selectionArgs);
            return index;

        } else {
            Log.d("daz", "insert!!!!");
            ContentValues values = new ContentValues();
            values.put(MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE, type);
            values.put(MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE, expirationDate);
            values.put(MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT, jsonContent);
            Uri uri = contentResolver.insert(MusicTrackCommonDataDB.CONTENT_URI, values);
            Log.d("daz insert uri:", uri.toString());
            return ContentUris.parseId(uri);
        }
    }

    private InputStream downloadUrl (String urlString) throws IOException{
        InputStream in = HttpGetMethod(urlString);
        if (in != null) {
            return in;
        } else {
            throw new IOException("Cannot download data from url = " + urlString);
        }
    }

    public InputStream HttpGetMethod (String urlString) {
        InputStream inputStream;

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(urlString);
        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(httpGet);
            inputStream = httpResponse.getEntity().getContent();
            return inputStream;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public String getStringFromInputStream(InputStream in) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            String s;
            br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            while ( (s = br.readLine()) != null) {
                sb.append(s);
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("Error","Unsupport UTF-8 data type");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
