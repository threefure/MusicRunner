package com.amk2.musicrunner.start;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.MusicTrackMetaData;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.start.StartFragment.StartTabFragmentListener;
import com.amk2.musicrunner.start.WeatherModel.WeatherWeekEntry;
import com.amk2.musicrunner.start.WeatherModel.WeatherHourlyEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;


/**
 * Created by daz on 2014/4/29.
 */
public class WeatherFragment extends Fragment{
    private static final LinearLayout.LayoutParams weeklyParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);

    private LinearLayout hourlyWeatherForecast;
    private LinearLayout weeklyWeatherForecast;
    private GetAddressFromLocation getAddress;
    private LayoutInflater inflater;
    private Location location;
    private StartTabFragmentListener mStartTabFragmentListener;

    private ContentResolver mContentResolver;

    public void setStartTabFragmentListener(StartTabFragmentListener listener) {
    	mStartTabFragmentListener = listener;
    }

    public void backPressed() {
    	mStartTabFragmentListener.onSwitchBetweenStartAndWeatherFragment();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getAddress = new GetAddressFromLocation(this.getActivity().getApplicationContext());
        mContentResolver = this.getActivity().getContentResolver();
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.weather_fragment, container, false);
    }

    @Override
    public void onActivityCreated (Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        View thisView = getView();
        hourlyWeatherForecast = (LinearLayout) thisView.findViewById(R.id.hourly_weather_forecast);
        weeklyWeatherForecast = (LinearLayout) thisView.findViewById(R.id.weekly_weather_forecast);
        inflater = (LayoutInflater) thisView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        location = LocationMetaData.getLocation();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeeklyForecast();
        update24HoursForecast();
    }

    private void updateWeeklyForecast () {
        // TODO projection could be moved to a static place
        String[] projection = {
                MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT,
                MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE
        };
        String selection = MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE + " LIKE ?";
        String[] selectionArgs = { String.valueOf(Constant.DB_KEY_WEEKLY_WEATHER) };

        Cursor cursor = mContentResolver.query(MusicTrackMetaData.MusicTrackCommonDataDB.CONTENT_URI, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        String JSONContent = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT));
        updateWeeklyForecastUI(JSONContent);

        Log.d("daz", "ui got the weather json:" + JSONContent);
    }
    private void updateWeeklyForecastUI(String JSONContent) {
        try {
            JSONArray weeklyJSONArray = new JSONArray(JSONContent);
            int length = weeklyJSONArray.length();
            for (int i = 0; i < length; i++) {
                addWeeklyForecast(weeklyJSONArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void addWeeklyForecast (JSONObject weeklyJSONObject) throws JSONException {
        View weekly = inflater.inflate(R.layout.weekly_template, null);
        TextView day = (TextView) weekly.findViewById(R.id.day);
        TextView temperature = (TextView) weekly.findViewById(R.id.temperature);

        day.setText(DayMapping.getDay(weeklyJSONObject.getString("day")));
        temperature.setText(weeklyJSONObject.getString("maxT") + "/" + weeklyJSONObject.getString("minT") + ".C");
        weeklyWeatherForecast.addView(weekly, weeklyParams);
    }

    private void update24HoursForecast () {
        // TODO projection could be moved to a static place
        String[] projection = {
                MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT,
                MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE
        };
        String selection = MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE + " LIKE ?";
        String[] selectionArgs = { String.valueOf(Constant.DB_KEY_24HRS_WEATHER) };

        Cursor cursor = mContentResolver.query(MusicTrackMetaData.MusicTrackCommonDataDB.CONTENT_URI, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        String JSONContent = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT));
        update24HoursForecastUI(JSONContent);

        Log.d("daz", "ui got the weather json:" + JSONContent);
    }

    private void update24HoursForecastUI(String JSONContent) {
        try {
            JSONArray t24HoursJSONArray = new JSONArray(JSONContent);
            int length = t24HoursJSONArray.length();
            for (int i = 0; i < length; i++) {
                add24HoursForecast(t24HoursJSONArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void add24HoursForecast (JSONObject t24HoursJSONArray) throws JSONException {
        View hourly = inflater.inflate(R.layout.hourly_template, null);
        TextView time = (TextView) hourly.findViewById(R.id.time);
        time.setText(t24HoursJSONArray.getString("time") + ":00");
        TextView temperature = (TextView) hourly.findViewById(R.id.temperature);
        temperature.setText(t24HoursJSONArray.getString("maxT") + "/" + t24HoursJSONArray.getString("minT") + ".C");
        hourlyWeatherForecast.addView(hourly);
    }

    private void get24HoursForecast (String cityCode) {
        GetWeather24HoursData weatherHourData = new GetWeather24HoursData();
        weatherHourData.execute(cityCode);
        try {
            ArrayList<WeatherHourlyEntry> weatherHourlyEntryList = weatherHourData.get();
            for (int i = 0; i < weatherHourlyEntryList.size(); i++) {
                addHourForecast(weatherHourlyEntryList.get(i));
            }
        } catch (CancellationException e) {
            Log.d("Error", "This is canceled");
        } catch (InterruptedException e) {
            Log.d("Error", "This is interrupted");
        } catch (ExecutionException e) {
            Log.d("Error", "Execution error");
        }
    }
    private void addHourForecast (WeatherHourlyEntry weatherHourlyEntry) {
        View hourly = inflater.inflate(R.layout.hourly_template, null);
        TextView time = (TextView) hourly.findViewById(R.id.time);
        time.setText(weatherHourlyEntry.time + ":00");
        TextView temperature = (TextView) hourly.findViewById(R.id.temperature);
        temperature.setText(weatherHourlyEntry.max_t + "/" + weatherHourlyEntry.min_t + ".C");
        hourlyWeatherForecast.addView(hourly);
    }

    @Override
    public void onPause () {
        super.onPause();
    }

}
