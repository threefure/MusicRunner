package com.amk2.musicrunner.music;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by ktlee on 9/18/14.
 */
public class MusicRankDetailActivity extends Activity {
    public static final String SONG_ID = "music.id";

    private ContentResolver mContentResolver;

    private ActionBar mActionBar;
    private TextView songNameTextView;
    private TextView singerTextView;
    private TextView bestPerformanceTextView;
    private TextView averagePerformanceTextView;
    private TextView bestPerformanceDateTextView;
    private TextView paceTextView;
    private TextView distanceTextView;
    private TextView timesTextView;
    private TextView durationTextView;
    private TextView caloriesTextView;

    private ImageView albumPhotoImageView;
    private ImageView addToPlaylistImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_rank_detail);
        mContentResolver = getContentResolver();
        mActionBar = getActionBar();
        initActionBar();
        initViews();
        setViews();
    }

    private void initActionBar() {
        View actionBarView = View.inflate(mActionBar.getThemedContext(), R.layout.customized_action_bar, null);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(Gravity.CENTER));
    }

    private void initViews() {
        songNameTextView            = (TextView) findViewById(R.id.song_name);
        singerTextView              = (TextView) findViewById(R.id.singer);
        bestPerformanceTextView     = (TextView) findViewById(R.id.best_performance);
        averagePerformanceTextView  = (TextView) findViewById(R.id.average_performance);
        bestPerformanceDateTextView = (TextView) findViewById(R.id.best_performance_date);
        paceTextView                = (TextView) findViewById(R.id.pace);
        distanceTextView            = (TextView) findViewById(R.id.distance);
        timesTextView               = (TextView) findViewById(R.id.times);
        durationTextView            = (TextView) findViewById(R.id.duration);
        caloriesTextView            = (TextView) findViewById(R.id.calories);
        albumPhotoImageView         = (ImageView) findViewById(R.id.album_photo);
    }

    private void setViews() {
        HashMap<String, String> songInfo;
        Calendar calendar = Calendar.getInstance();
        Integer songId = (Integer) getIntent().getExtras().get(SONG_ID);
        Integer duration, totalDuration, times;
        Double tempCalories, totalCalories, totalDistance, bestPerformance, averagePerformance, performance, pace;
        String calories, distance, currentEpoch, speed, songName, durationString, artist, bestEpoch = null;
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
        bestPerformance = 0.0;
        while (cursor.moveToNext()) {
            duration     = cursor.getInt(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DURATION));
            calories     = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_CALORIES));
            distance     = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DISTANCE));
            currentEpoch = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DATE_IN_MILLISECOND));
            speed        = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_SPEED));

            tempCalories = Double.parseDouble(calories);
            performance  = tempCalories*60/duration.doubleValue();
            if (performance > bestPerformance) {
                bestPerformance = performance;
                bestEpoch = currentEpoch;
            }

            totalCalories += tempCalories;
            totalDistance += Double.parseDouble(distance);
            totalDuration += duration;
            times ++;
        }

        songInfo = MusicLib.getSongInfo(getApplicationContext(), songId);
        artist   = MusicLib.getArtist(getApplicationContext(), Long.parseLong(songInfo.get(MusicLib.ARTIST_ID)));
        averagePerformance = totalCalories*60/totalDuration.doubleValue();
        durationString = TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(totalDuration));
        pace = totalDuration/(totalDistance*60);
        if (bestEpoch != null) {
            calendar.setTimeInMillis(Long.parseLong(bestEpoch));
        }

        songNameTextView.setText(StringLib.truncate(songInfo.get(MusicLib.SONG_NAME), 20));
        singerTextView.setText(artist);
        bestPerformanceTextView.setText(StringLib.truncateDoubleString(bestPerformance.toString(), 2));
        bestPerformanceDateTextView.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " +
                calendar.get(Calendar.DAY_OF_MONTH));
        averagePerformanceTextView.setText(StringLib.truncateDoubleString(averagePerformance.toString(), 2));
        distanceTextView.setText(StringLib.truncateDoubleString(totalDistance.toString(), 2));
        timesTextView.setText(times.toString());
        paceTextView.setText(StringLib.truncateDoubleString(pace.toString(), 2));
        durationTextView.setText(durationString);
        caloriesTextView.setText(StringLib.truncateDoubleString(totalCalories.toString(), 2));

        Uri songUri = MusicLib.getMusicUri(Long.parseLong(songInfo.get(MusicLib.SONG_REAL_ID)));
        String songPath = MusicLib.getMusicFilePath(getApplicationContext(), songUri);
        Bitmap albumPhoto = MusicLib.getMusicAlbumArt(songPath);
        albumPhotoImageView.setImageBitmap(albumPhoto);
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
}
