package com.amk2.musicrunner.my;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.LocationUtils;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerRunningEventDB;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerSongNameDB;
import com.amk2.musicrunner.utilities.ColorGenerator;
import com.amk2.musicrunner.utilities.PhotoLib;
import com.amk2.musicrunner.utilities.ShowImageActivity;
import com.amk2.musicrunner.utilities.SongPerformance;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.amk2.musicrunner.utilities.UnitConverter;
import com.amk2.musicrunner.views.MusicRunnerLineMapView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by daz on 2014/6/28.
 */
public class MyPastActivityDetailsActivity extends Activity implements View.OnClickListener{

    public static String EVENT_ID = "com.amk2.id";
    public static final String TAG = "MyPastActivityDetailsActivity";

    private Integer eventId;
    private String photoPath;

    private ContentResolver mContentResolver;

    private ArrayList<LatLng> mLocationList = null;
    private ArrayList<Integer> mColorList = null;
    private ArrayList<SongPerformance> songPerformanceArrayList;

    private GoogleMap mMap;

    private ActionBar mActionBar;
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView distanceTextView;
    private TextView distanceUnitTextView;
    private TextView caloriesTextView;
    private TextView speedTitleTextView;
    private TextView speedTextView;
    private TextView speedUnitTextView;
    private TextView durationTextView;
    private ImageView picPreviewImageView;
    private ImageButton cameraImageButton;
    private MusicRunnerLineMapView musicRunnerLineMapView;

    private SharedPreferences mSettingSharedPreferences;
    private Integer unitDistance;
    private Integer unitSpeedPace;

    private LayoutInflater inflater;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_past_activity_details);

        mContentResolver = getContentResolver();
        eventId = getIntent().getExtras().getInt(EVENT_ID);
        mSettingSharedPreferences = getSharedPreferences(SettingActivity.SETTING_SHARED_PREFERENCE, 0);
        getSharedPreferences();
        fetchSongPerformance();
        initActionBar();
        initViews();
        setViews();
    }

    private void initActionBar () {
        mActionBar = getActionBar();
        mActionBar.hide();
    }

    private void getSharedPreferences () {
        unitDistance  = mSettingSharedPreferences.getInt(SettingActivity.DISTANCE_UNIT, SettingActivity.SETTING_DISTANCE_KM);
        unitSpeedPace = mSettingSharedPreferences.getInt(SettingActivity.SPEED_PACE_UNIT, SettingActivity.SETTING_PACE);
    }

    private void initViews() {
        dateTextView         = (TextView) findViewById(R.id.my_past_activity_details_date);
        timeTextView         = (TextView) findViewById(R.id.my_past_activity_details_time);
        distanceUnitTextView = (TextView) findViewById(R.id.my_past_activity_details_distance_unit);
        distanceTextView     = (TextView) findViewById(R.id.my_past_activity_details_distance);
        caloriesTextView     = (TextView) findViewById(R.id.my_past_activity_details_calorie);
        speedTitleTextView   = (TextView) findViewById(R.id.my_past_activity_details_speed_title);
        speedTextView        = (TextView) findViewById(R.id.my_past_activity_details_speed);
        speedUnitTextView    = (TextView) findViewById(R.id.my_past_activity_details_speed_unit);
        durationTextView     = (TextView) findViewById(R.id.my_past_activity_details_duration);

        picPreviewImageView = (ImageView) findViewById(R.id.my_past_activity_details_photo);
        cameraImageButton   = (ImageButton) findViewById(R.id.my_past_activity_details_camera);
        musicRunnerLineMapView = (MusicRunnerLineMapView) findViewById(R.id.my_past_activity_details_line_map);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mMap = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.my_past_activity_details_map)).getMap();

        picPreviewImageView.setOnClickListener(this);
    }

    private void setViews () {
        long timeInMillis;
        String timeInMillisString, durationString;
        String distanceString, calories, speedString, route;
        String dayPeriodString, dateString, timeString, speedUnitString = "my_running_";
        Integer dayPeriod, duration, speedTitleId;
        Double distance, minutes, hours, speed;
        Calendar calendar = Calendar.getInstance();

        String[] projection = {
                MusicRunnerRunningEventDB.COLUMN_NAME_DURATION,
                MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND,
                MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE,
                MusicRunnerRunningEventDB.COLUMN_NAME_CALORIES,
                MusicRunnerRunningEventDB.COLUMN_NAME_SPEED,
                MusicRunnerRunningEventDB.COLUMN_NAME_PHOTO_PATH,
                MusicRunnerRunningEventDB.COLUMN_NAME_ROUTE
        };
        String selection = MusicRunnerRunningEventDB.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = { eventId.toString() };

        Cursor cursor = mContentResolver.query(MusicRunnerRunningEventDB.CONTENT_URI, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        duration           = cursor.getInt(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DURATION));
        timeInMillisString = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND));
        distanceString     = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE));
        calories           = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_CALORIES));
        speedString              = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_SPEED));
        photoPath          = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_PHOTO_PATH));
        route              = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_ROUTE));
        cursor.close();

        timeInMillis   = Long.parseLong(timeInMillisString);
        durationString = TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(duration));

        calendar.setTimeInMillis(timeInMillis);
        dayPeriod = TimeConverter.getDayPeriod(calendar.get(Calendar.HOUR_OF_DAY));
        switch (dayPeriod) {
            case TimeConverter.MIDNIGHT:
                dayPeriodString = "Midnight";
                break;
            case TimeConverter.NIGHT:
                dayPeriodString = "Night";
                break;
            case TimeConverter.MORNING:
                dayPeriodString = "Morning";
                break;
            case TimeConverter.AFTERNOON:
                dayPeriodString = "Afternoon";
                break;
            default:
                dayPeriodString = "";
        }
        dateString = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + calendar.get(Calendar.DAY_OF_MONTH);
        timeString = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + " " + dayPeriodString;
        dateTextView.setText(dateString);
        timeTextView.setText(timeString);

        // getting distance information
        distance = Double.parseDouble(distanceString);
        minutes = duration.doubleValue()/60;
        hours = duration.doubleValue()/3600;
        if (unitDistance == SettingActivity.SETTING_DISTANCE_MI) {
            distance = UnitConverter.getMIFromKM(distance);
            speedUnitString += "mi_";
        } else {
            speedUnitString += "km_";
        }

        if (unitSpeedPace == SettingActivity.SETTING_PACE) {
            speed = minutes/distance;
            speedUnitString += "pace";
            speedTitleId = R.string.pace;
        } else {
            speed = distance/hours;
            speedUnitString += "speed";
            speedTitleId = R.string.speed;
        }
        if (speed.isNaN() || speed.isInfinite()) {
            speed = 0.0;
        }
        distanceString = StringLib.truncateDoubleString(distance.toString(), 2);
        speedString = StringLib.truncateDoubleString(speed.toString(), 2);

        // setting distance information
        distanceTextView.setText(distanceString);
        distanceUnitTextView.setText(Constant.DistanceMap.get(unitDistance));

        // setting speed information
        speedTextView.setText(speedString);
        speedUnitTextView.setText(getResources().getString(Constant.PaceSpeedMap.get(speedUnitString)));
        speedTitleTextView.setText(getResources().getString(speedTitleId));

        // setting calories information
        caloriesTextView.setText(calories);

        // setting duration information
        durationTextView.setText(durationString);
        if (photoPath != null && photoPath.length() > 0) {
            setPicPreview();
        }

        if (songPerformanceArrayList != null) {
            musicRunnerLineMapView.setMusicJoints(songPerformanceArrayList);
        }

        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mLocationList = LocationUtils.parseRouteToLocation(route);
            mColorList = LocationUtils.parseRouteColor(route);
            mDrawRoute();
        }
    }

    private void fetchSongPerformance() {
        int songId, duration;
        String distance, calories, speed, songName;
        String[] projection = {
                MusicRunnerSongPerformanceDB.COLUMN_NAME_SONG_ID,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_DURATION,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_DATE_IN_MILLISECOND,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_DISTANCE,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_CALORIES,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_SPEED,
        };
        String selection = MusicRunnerSongPerformanceDB.COLUMN_NAME_EVENT_ID + " = ?";
        String[] selectionArgs = { eventId.toString() };

        songPerformanceArrayList = new ArrayList<SongPerformance>();

        Cursor cursor = mContentResolver.query(MusicRunnerSongPerformanceDB.CONTENT_URI, projection, selection, selectionArgs, null);
        while (cursor.moveToNext()) {
            songId      = cursor.getInt(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_SONG_ID));
            duration    = cursor.getInt(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DURATION));
            distance    = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DISTANCE));
            calories    = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_CALORIES));
            speed       = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_SPEED));

            songName = getSongName(songId);
            SongPerformance sp = new SongPerformance(duration, Double.parseDouble(distance), Double.parseDouble(calories), Double.parseDouble(speed), songName, null);
            songPerformanceArrayList.add(sp);
        }
        cursor.close();
    }

    private String getSongName (Integer songId) {
        String[] projection = {
                MusicRunnerSongNameDB.COLUMN_NAME_SONG_NAME
        };
        String songName = "";
        String selection = MusicRunnerSongNameDB.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = { songId.toString() };

        Cursor cursor = mContentResolver.query(MusicRunnerSongNameDB.CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            songName = cursor.getString(cursor.getColumnIndex(MusicRunnerSongNameDB.COLUMN_NAME_SONG_NAME));
        }
        cursor.close();
        return songName;
    }

    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_past_activity_details_photo:
                if (photoPath != null) {
                    Intent intent = new Intent(this, ShowImageActivity.class);
                    intent.putExtra(ShowImageActivity.PHOTO_PATH, photoPath);
                    startActivity(intent);
                }
                break;
        }
    }

    private void setPicPreview () {
        Bitmap resizedPhoto = PhotoLib.resizeToFitTarget(photoPath, picPreviewImageView.getLayoutParams().width, picPreviewImageView.getLayoutParams().height);
        picPreviewImageView.setImageBitmap(resizedPhoto);
        picPreviewImageView.setVisibility(View.VISIBLE);
        cameraImageButton.setVisibility(View.GONE);
    }

    private void mDrawRoute() {
        if(mLocationList == null)
            return;

        if(mLocationList.size() > 0) {
            LatLng lastPosition = null;

            for (int i = 0; i < mLocationList.size(); i++) {
                if(lastPosition != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, LocationUtils.CAMERA_PAD));
                    mMap.addPolyline(
                            new PolylineOptions()
                                    .geodesic(true)
                                    .color(ColorGenerator
                                            .generateColor(mColorList.get(i)))
                                    .add(lastPosition)
                                    .add(mLocationList.get(i))
                    );
                }
                lastPosition = mLocationList.get(i);
            }
        }
    }
}
