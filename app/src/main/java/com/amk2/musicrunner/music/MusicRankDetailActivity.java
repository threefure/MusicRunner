package com.amk2.musicrunner.music;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.main.MusicRunnerApplication;
import com.amk2.musicrunner.musiclist.MusicAddToPlaylistActivity;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.amk2.musicrunner.utilities.UnitConverter;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

/**
 * Created by ktlee on 9/18/14.
 */
public class MusicRankDetailActivity extends Activity implements View.OnClickListener {
    public static final String SONG_ID = "music.id";

    private ContentResolver mContentResolver;

    private ActionBar mActionBar;
    private TextView titleTextView;
    private TextView artistTextView;
    private TextView timesTextView;
    private TextView durationTextView;

    private TextView caloriesTextView;
    private TextView bestCaloriesTextView;
    private TextView bestCaloriesDateTextView;
    private TextView averageCaloriesTextView;

    private TextView distanceTextView;
    private TextView distanceUnitTextView;
    private TextView bestDistanceTextView;
    private TextView bestDistanceUnitTextView;
    private TextView bestDistanceDateTextView;
    private TextView averageDistanceTextView;
    private TextView averageDistanceUnitTextView;

    private ImageView albumPhotoImageView;
    private ImageView songTempoImageView;
    private ImageView addToPlaylistImageView;

    private SharedPreferences mSettingSharedPreferences;
    private Integer unitDistance;
    private Integer unitSpeedPace;
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_rank_detail);
        mContentResolver = getContentResolver();
        mSettingSharedPreferences = getSharedPreferences(SettingActivity.SETTING_SHARED_PREFERENCE, 0);
        mActionBar = getActionBar();
        getSharedPreferences();
        initActionBar();
        initViews();
        setViews();
    }

    private void getSharedPreferences () {
        unitDistance  = mSettingSharedPreferences.getInt(SettingActivity.DISTANCE_UNIT, SettingActivity.SETTING_DISTANCE_KM);
        unitSpeedPace = mSettingSharedPreferences.getInt(SettingActivity.SPEED_PACE_UNIT, SettingActivity.SETTING_PACE);
        language      = mSettingSharedPreferences.getString(SettingActivity.LANGUAGE, SettingActivity.SETTING_LANGUAGE_ENGLISH);
    }

    private void initActionBar() {
        View actionBarView = View.inflate(mActionBar.getThemedContext(), R.layout.customized_action_bar, null);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(Gravity.CENTER));
    }

    private void initViews() {
        titleTextView               = (TextView) findViewById(R.id.title);
        artistTextView              = (TextView) findViewById(R.id.artist);
        timesTextView               = (TextView) findViewById(R.id.times);
        durationTextView            = (TextView) findViewById(R.id.duration);

        caloriesTextView            = (TextView) findViewById(R.id.calories);
        bestCaloriesTextView        = (TextView) findViewById(R.id.best_calories);
        bestCaloriesDateTextView    = (TextView) findViewById(R.id.best_calories_date);
        averageCaloriesTextView     = (TextView) findViewById(R.id.average_calories);

        distanceTextView            = (TextView) findViewById(R.id.distance);
        distanceUnitTextView        = (TextView) findViewById(R.id.distance_unit);
        bestDistanceTextView        = (TextView) findViewById(R.id.best_distance);
        bestDistanceUnitTextView    = (TextView) findViewById(R.id.best_distance_unit);
        bestDistanceDateTextView    = (TextView) findViewById(R.id.best_distance_date);
        averageDistanceTextView     = (TextView) findViewById(R.id.average_distance);
        averageDistanceUnitTextView = (TextView) findViewById(R.id.average_distance_unit);

        albumPhotoImageView         = (ImageView) findViewById(R.id.album_photo);
        songTempoImageView          = (ImageView) findViewById(R.id.song_tempo);
        addToPlaylistImageView      = (ImageView) findViewById(R.id.add_to_playlist);
    }

    private void setViews() {
        HashMap<String, String> songInfo;
        Calendar calendar = Calendar.getInstance();
        Integer songId = (Integer) getIntent().getExtras().get(SONG_ID);
        Integer duration, totalDuration, times, speedTitleId, bestDistanceDuration;
        Double tempCalories, totalCalories, totalDistance, bestCaloriesPerformance, bestDistance, bestSpeed, averageCaloriesPerformance, performance, speed, minutes, hours;
        String calories, distance, currentEpoch, speedString, durationString, artist, bestCaloriesEpoch = null, bestDistanceEpoch = null, speedUnitString = "my_running_";
        String[] projection = {
                MusicRunnerSongPerformanceDB.COLUMN_NAME_ID,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_CALORIES,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_DURATION,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_SPEED,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_DISTANCE,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_DATE_IN_MILLISECOND
        };
        String selection = MusicRunnerSongPerformanceDB.COLUMN_NAME_SONG_ID + " = ?";
        String[] selectionArgs = { songId.toString() };
        Cursor cursor = mContentResolver.query(MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB.CONTENT_URI, projection, selection, selectionArgs, null);
        totalCalories = 0.0;
        totalDistance = 0.0;
        totalDuration = 0;
        times = 0;
        bestCaloriesPerformance = 0.0;
        bestSpeed = 0.0;
        bestDistance = 0.0;
        bestDistanceDuration = 0;
        while (cursor.moveToNext()) {
            duration     = cursor.getInt(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DURATION));
            calories     = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_CALORIES));
            distance     = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DISTANCE));
            currentEpoch = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DATE_IN_MILLISECOND));
            speedString  = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_SPEED));

            tempCalories = Double.parseDouble(calories);

            // compute the best calories performance
            performance  = tempCalories*60000/duration.doubleValue();
            if (performance > bestCaloriesPerformance) {
                bestCaloriesPerformance = performance;
                bestCaloriesEpoch = currentEpoch;
            }

            // compute the best distance performance
            speed  = Double.parseDouble(speedString);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestDistance = Double.parseDouble(distance);
                bestDistanceDuration = duration;
                bestDistanceEpoch = currentEpoch;
            }

            totalCalories += tempCalories;
            totalDistance += Double.parseDouble(distance);
            totalDuration += duration;
            times ++;
        }

        String[] songProjection = {
                MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_BPM
        };
        String songSelection = MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ID + " = ?";
        String[] songSelectionArgs = {songId.toString()};
        String bpmString;
        Double bpm = 0.0;
        cursor = mContentResolver.query(MusicRunnerDBMetaData.MusicRunnerSongNameDB.CONTENT_URI, songProjection, songSelection, songSelectionArgs, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            bpmString = cursor.getString(cursor.getColumnIndex(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_BPM));
            if (bpmString != null) {
                bpm = Double.parseDouble(bpmString);
            }
        }
        cursor.close();

        if (bpm > 0 && bpm < 110) {
            songTempoImageView.setImageResource(R.drawable.slow);
        } else if (bpm < 130 && bpm >= 110) {
            songTempoImageView.setImageResource(R.drawable.medium);
        } else if (bpm >= 130){
            songTempoImageView.setImageResource(R.drawable.fast);
        }

        // convert to second
        totalDuration = totalDuration/1000;

        // setting song info
        songInfo = MusicLib.getSongInfo(getApplicationContext(), songId);
        artist   = MusicLib.getArtist(getApplicationContext(), Long.parseLong(songInfo.get(MusicLib.ARTIST_ID)));

        // getting average calories performance
        averageCaloriesPerformance = totalCalories*60/totalDuration.doubleValue();

        // getting total duration
        durationString = TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(totalDuration));

        // getting distance information
        minutes = totalDuration.doubleValue()/60;
        hours = totalDuration.doubleValue()/3600;
        if (unitDistance == SettingActivity.SETTING_DISTANCE_MI) {
            totalDistance = UnitConverter.getMIFromKM(totalDistance);
            bestDistance  = UnitConverter.getMIFromKM(bestDistance);
            speedUnitString += "mi_";
        } else {
            speedUnitString += "km_";
        }

        if (unitSpeedPace == SettingActivity.SETTING_PACE) {
            speed = minutes/totalDistance;
            bestSpeed = bestDistanceDuration.doubleValue()/(bestDistance*60000);
            speedUnitString += "pace";
            speedTitleId = R.string.pace;
        } else {
            speed = totalDistance/hours;
            bestSpeed = (bestDistance*3600000)/bestDistanceDuration.doubleValue();
            speedUnitString += "speed";
            speedTitleId = R.string.speed;
        }
        if (speed.isNaN() || speed.isInfinite()) {
            speed = 0.0;
        }

        // setting locale
        Locale locale = Locale.US;
        if (language != SettingActivity.SETTING_LANGUAGE_ENGLISH) {
            locale = Locale.TAIWAN;
        }
        // setting calories information
        caloriesTextView.setText(StringLib.truncateDoubleString(totalCalories.toString(), 2));
        bestCaloriesTextView.setText(StringLib.truncateDoubleString(bestCaloriesPerformance.toString(), 2));
        if (bestCaloriesEpoch != null) {
            calendar.setTimeInMillis(Long.parseLong(bestCaloriesEpoch));
            bestCaloriesDateTextView.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) + " " +
                    calendar.get(Calendar.DAY_OF_MONTH));
        }
        averageCaloriesTextView.setText(StringLib.truncateDoubleString(averageCaloriesPerformance.toString(), 2));


        // setting distance information
        distanceTextView.setText(StringLib.truncateDoubleString(totalDistance.toString(), 2));
        distanceUnitTextView.setText(Constant.DistanceMap.get(unitDistance));
        bestDistanceTextView.setText(StringLib.truncateDoubleString(bestSpeed.toString(), 2));
        bestDistanceUnitTextView.setText(Constant.PaceSpeedMap.get(speedUnitString));
        if (bestDistanceEpoch != null) {
            calendar.setTimeInMillis(Long.parseLong(bestDistanceEpoch));
            bestDistanceDateTextView.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) + " " +
                    calendar.get(Calendar.DAY_OF_MONTH));
        }
        averageDistanceTextView.setText(StringLib.truncateDoubleString(speed.toString(), 2));
        averageDistanceUnitTextView.setText(Constant.PaceSpeedMap.get(speedUnitString));

        // setting song information
        titleTextView.setText(StringLib.truncate(songInfo.get(MusicLib.SONG_NAME), 20));
        artistTextView.setText(artist);

        timesTextView.setText(times.toString());
        durationTextView.setText(durationString);

        Uri songUri = MusicLib.getMusicUriWithId(Long.parseLong(songInfo.get(MusicLib.SONG_REAL_ID)));
        String songPath = MusicLib.getMusicFilePath(getApplicationContext(), songUri);
        Bitmap albumPhoto = MusicLib.getMusicAlbumArt(songPath);
        if (albumPhoto != null) {
            albumPhotoImageView.setImageBitmap(albumPhoto);
        }

        // setting click event for add to playlist
        addToPlaylistImageView.setTag(Long.parseLong(songInfo.get(MusicLib.SONG_REAL_ID)));
        addToPlaylistImageView.setOnClickListener(this);
    }

    @Override
    protected void onStart () {
        super.onStart();
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        Tracker t = ((MusicRunnerApplication) getApplication()).getTracker(MusicRunnerApplication.TrackerName.APP_TRACKER);
        t.setScreenName("RankDetailPage");
        switch (view.getId()) {
            case R.id.add_to_playlist:
                //tracking user action
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("RankDetail")
                        .setAction("addToPlaylistButton")
                        .build());

                Long songRealId = (Long) view.getTag();
                Intent intent = new Intent(this, MusicAddToPlaylistActivity.class);
                intent.putExtra(MusicAddToPlaylistActivity.SONG_REAL_ID, songRealId);
                startActivity(intent);
                break;
        }
    }
}
