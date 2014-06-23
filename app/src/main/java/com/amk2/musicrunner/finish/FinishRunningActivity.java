package com.amk2.musicrunner.finish;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData.MusicTrackRunningEventDataDB;
import com.amk2.musicrunner.utilities.PhotoLib;

/**
 * Created by daz on 2014/6/15.
 */
public class FinishRunningActivity extends Activity implements View.OnClickListener{
    public static String FINISH_RUNNING_DURATION = "com.amk2.duration";
    public static String FINISH_RUNNING_DISTANCE = "com.amk2.distance";
    public static String FINISH_RUNNING_CALORIES = "com.amk2.calories";
    public static String FINISH_RUNNING_SPEED    = "com.amk2.speed";
    public static String FINISH_RUNNING_PHOTO    = "com.amk2.photo";

    private TextView distanceTextView;
    private TextView caloriesTextView;
    private TextView speedTextView;
    private TextView secTextView;
    private TextView minTextView;
    private TextView hourTextView;
    private ImageView photoImageView;

    private int totalSec = 0;
    private String distance  = null;
    private String calories  = null;
    private String speed     = null;
    private String photoPath = null;

    private Button saveButton;
    private Button discardButton;

    private ContentResolver mContentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_running);

        mContentResolver = getContentResolver();

        Intent intent = getIntent();

        saveButton    = (Button) findViewById(R.id.save_running_event);
        discardButton = (Button) findViewById(R.id.discard_running_event);

        saveButton.setOnClickListener(this);
        discardButton.setOnClickListener(this);

        distanceTextView = (TextView) findViewById(R.id.finish_running_distance);
        caloriesTextView = (TextView) findViewById(R.id.finish_running_calories);
        speedTextView    = (TextView) findViewById(R.id.finish_running_speed);
        photoImageView   = (ImageView) findViewById(R.id.finish_running_photo);
        secTextView      = (TextView) findViewById(R.id.timer_second);
        minTextView      = (TextView) findViewById(R.id.timer_minute);
        hourTextView     = (TextView) findViewById(R.id.timer_hour);

        totalSec  = intent.getIntExtra(FINISH_RUNNING_DURATION, 0);
        distance  = intent.getStringExtra(FINISH_RUNNING_DISTANCE);
        calories  = intent.getStringExtra(FINISH_RUNNING_CALORIES);
        speed     = intent.getStringExtra(FINISH_RUNNING_SPEED);
        photoPath = intent.getStringExtra(FINISH_RUNNING_PHOTO);

        if (totalSec > 0) {
            int actualSec  = totalSec%60;
            int actualMin  = 0;
            int actualHour = 0;

            if (actualSec < 10) {
                secTextView.setText("0" + actualSec);
            } else {
                secTextView.setText("" + actualSec);
            }

            if (actualSec == 0) {
                actualMin = (actualMin + 1) % 60;

                if (actualMin < 10) {
                    minTextView.setText("0" + actualMin);
                } else {
                    minTextView.setText("" + actualMin);
                }

                if (actualMin == 0) {
                    actualHour += 1;

                    if (actualHour < 10) {
                        hourTextView.setText("0" + actualHour);
                    } else {
                        hourTextView.setText("" + actualHour);
                    }
                }
            }
        }
        if (distance != null) {
            distanceTextView.setText(distance);
        }
        if (calories != null) {
            caloriesTextView.setText(calories);
        }
        if (speed != null) {
            speedTextView.setText(speed);
        }
        if (photoPath != null) {
            Bitmap resizedPhoto = PhotoLib.resizeToFitTarget(photoPath, photoImageView.getLayoutParams().width, photoImageView.getLayoutParams().height);
            photoImageView.setImageBitmap(resizedPhoto);
        }
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
        switch(view.getId()) {
            case R.id.save_running_event:
                ContentValues values = new ContentValues();
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_DURATION, totalSec);
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES, calories);
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE, distance);
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED, speed);
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_PICTURE_PATH, photoPath);
                Uri uri = mContentResolver.insert(MusicTrackRunningEventDataDB.CONTENT_URI, values);
                Log.d("Save running event, uri=", uri.toString());
                finish();
                break;
            case R.id.discard_running_event:
                Log.d("daz", "discard running event");
                finish();
                break;
        }
    }
}
