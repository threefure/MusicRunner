package com.amk2.musicrunner;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.amk2.musicrunner.start.LocationHelper;
import com.amk2.musicrunner.start.WeatherJSONParser;
import com.amk2.musicrunner.start.WeatherModel.WeatherEntry;
import com.amk2.musicrunner.MusicTrackMetaData.MusicTrackCommonDataDB;

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
import java.util.Date;

/**
 * Created by ktlee on 5/24/14.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter{
    private ContentResolver contentResolver;

    private static long dailyWeatherID = -1;

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
        WeatherEntry weatherEntry = null;
        if (bundle.getString(Constant.SYNC_UPDATE).equals(Constant.UPDATE_WEATHER)) {
            Log.d("daz", "calling weather api");
            InputStream weatherStream = null;
            try {
                weatherStream = downloadUrl(Constant.baseWeatherUrlString + "?" + Constant.cityCodeQuery + LocationHelper.getCityCode());
                //weatherEntry = WeatherJSONParser.read(weatherStream);
                String weatherJSONString = getStringFromInputStream(weatherStream);
                Calendar expirationDate = Calendar.getInstance();
                expirationDate.add(Calendar.HOUR, 1);
                Log.d("new data content weatherJSONString", weatherJSONString);
                Log.d("expirationDate.getTime().toString()", expirationDate.getTime().toString());
                Log.d("daz", "remodel syncadapter updating db...with id=" + dailyWeatherID);
                dailyWeatherID = InsertCommonData(
                        dailyWeatherID,
                        Constant.DB_KEY_DAILY_WEATHER,
                        expirationDate.getTime().toString(),
                        weatherJSONString);
                Log.d("daz", "updated db, observer should know this change, new provider, with id=" + dailyWeatherID);
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
            } catch (IOException e) {
                e.printStackTrace();
            }

            //close stream
            try {
                weatherStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (bundle.getString(Constant.SYNC_UPDATE).equals(Constant.UPDATE_UBIKE)) {
            Log.d("daz", "calling ubike api");
        }
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
