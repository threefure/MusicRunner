package com.amk2.musicrunner.finish;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.LocationUtils;
import com.amk2.musicrunner.running.MapFragmentRun;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerRunningEventDB;
import com.amk2.musicrunner.utilities.CameraLib;
import com.amk2.musicrunner.utilities.ColorGenerator;
import com.amk2.musicrunner.utilities.Comparators;
import com.amk2.musicrunner.utilities.PhotoLib;
import com.amk2.musicrunner.utilities.ShowImageActivity;
import com.amk2.musicrunner.utilities.SongPerformance;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import static android.widget.Toast.makeText;

/**
 * Created by daz on 2014/6/15.
 */
public class FinishRunningActivity extends Activity implements View.OnClickListener{
    public static String FINISH_RUNNING_DURATION = "com.amk2.duration";
    public static String FINISH_RUNNING_DISTANCE = "com.amk2.distance";
    public static String FINISH_RUNNING_CALORIES = "com.amk2.calories";
    public static String FINISH_RUNNING_SPEED    = "com.amk2.speed";
    public static String FINISH_RUNNING_PHOTO    = "com.amk2.photo";
    public static String FINISH_RUNNING_SONGS    = "com.amk2.songs";
    public static Integer REQUEST_IMAGE_CAPTURE  = 1;

    private LayoutInflater inflater;

    private TextView distanceTextView;
    private TextView caloriesTextView;
    private TextView speedTextView;
    private TextView finishTimeTextView;
    private TextView saveButton;
    private TextView discardButton;
    private ImageView picPreviewImageView;
    private ImageButton cameraImageButton;
    //private LinearLayout musicListLinearLayout;

    private GoogleMap mMap;

    private int totalSec = 0;
    private String distance  = null;
    private String calories  = null;
    private String speed     = null;
    private String photoPath = null;
    private String route     = null;
    private String songNames = null;


    private ContentResolver mContentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_running);
        initialize();
    }

    private void initialize () {
        initActionBar();
        initView();
    }

    private void initActionBar() {
        getActionBar().hide();
    }

    private void initView() {
        mContentResolver = getContentResolver();

        Intent intent = getIntent();

        saveButton    = (TextView) findViewById(R.id.finish_save);
        discardButton = (TextView) findViewById(R.id.finish_discard);


        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        distanceTextView    = (TextView) findViewById(R.id.finish_distance);
        caloriesTextView    = (TextView) findViewById(R.id.finish_calorie);
        speedTextView       = (TextView) findViewById(R.id.finish_speed);
        finishTimeTextView  = (TextView) findViewById(R.id.finish_duration);
        picPreviewImageView = (ImageView) findViewById(R.id.finish_photo);
        cameraImageButton   = (ImageButton) findViewById(R.id.finish_camera);


//        musicListLinearLayout = (LinearLayout) findViewById(R.id.music_result_holder);

        saveButton.setOnClickListener(this);
        discardButton.setOnClickListener(this);
        picPreviewImageView.setOnClickListener(this);
        cameraImageButton.setOnClickListener(this);

        totalSec  = intent.getIntExtra(FINISH_RUNNING_DURATION, 0);
        distance  = intent.getStringExtra(FINISH_RUNNING_DISTANCE);
        calories  = intent.getStringExtra(FINISH_RUNNING_CALORIES);
        speed     = intent.getStringExtra(FINISH_RUNNING_SPEED);
        photoPath = intent.getStringExtra(FINISH_RUNNING_PHOTO);
        songNames = intent.getStringExtra(FINISH_RUNNING_SONGS);

        if (totalSec > 0) {
            HashMap<String, Integer> time =  TimeConverter.getReadableTimeFormatFromSeconds(totalSec);
            String duration = TimeConverter.getDurationString(time);
            finishTimeTextView.setText(duration);
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
            setPicPreview();
            //Bitmap resizedPhoto = PhotoLib.resizeToFitTarget(photoPath, picPreviewImageView.getLayoutParams().width, picPreviewImageView.getLayoutParams().height);
            //picPreviewImageView.setImageBitmap(resizedPhoto);
        }
        if (songNames != null) {
            //addSongNames();
        }

        // Get a handle to the Map Fragment
        mMap = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.finish_map)).getMap();
        mMap.getUiSettings().setZoomControlsEnabled(false);
        // Draw the map base on last run
        mDrawRoute();
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
    protected void onActivityResult (int reqCode, int resCode, Intent intent) {
        if (reqCode == REQUEST_IMAGE_CAPTURE && resCode == RESULT_OK) {
            CameraLib.galleryAddPic(this, photoPath);
            setPicPreview();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.finish_save:
                Calendar calendar = Calendar.getInstance();
                long timeInMillis = calendar.getTimeInMillis();
                ContentValues values = new ContentValues();
                values.put(MusicRunnerRunningEventDB.COLUMN_NAME_DURATION, totalSec);
                values.put(MusicRunnerDBMetaData.MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND, Long.toString(timeInMillis));
                values.put(MusicRunnerRunningEventDB.COLUMN_NAME_CALORIES, calories);
                values.put(MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE, distance);
                values.put(MusicRunnerRunningEventDB.COLUMN_NAME_SPEED, speed);
                values.put(MusicRunnerRunningEventDB.COLUMN_NAME_PHOTO_PATH, photoPath);
                values.put(MusicRunnerRunningEventDB.COLUMN_NAME_ROUTE, route);
                //values.put(MusicRunnerRunningEventDB.COLUMN_NAME_SONGS, songNames);
                Uri uri = mContentResolver.insert(MusicRunnerDBMetaData.MusicRunnerRunningEventDB.CONTENT_URI, values);

                Log.d("Save running event, uri=", uri.toString());

                finish();
                break;
            case R.id.finish_discard:
                Log.d("daz", "discard running event");
                finish();
                break;
            case R.id.finish_photo:
                if (photoPath != null) {
                    Intent intent = new Intent(this, ShowImageActivity.class);
                    intent.putExtra(ShowImageActivity.PHOTO_PATH, photoPath);
                    startActivity(intent);
                }
                break;
            case R.id.finish_camera:
                dispatchTakePictureIntent();
                break;
        }
        MapFragmentRun.resetAllParam();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File dir = CameraLib.getAlbumStorageDir();
            File photoFile = null;
            try {
                photoFile = CameraLib.createImageFile(dir);
                photoPath = photoFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    private void setPicPreview () {
        Bitmap resizedPhoto = PhotoLib.resizeToFitTarget(photoPath, picPreviewImageView.getLayoutParams().width, picPreviewImageView.getLayoutParams().height);
        picPreviewImageView.setImageBitmap(resizedPhoto);
        picPreviewImageView.setVisibility(View.VISIBLE);
        cameraImageButton.setVisibility(View.GONE);
    }

    private void mDrawRoute() {
        ArrayList<LatLng> polylines = MapFragmentRun.getmTrackList();
        ArrayList<Integer> mColorList = MapFragmentRun.getmColorList();
        if(polylines.size() > 0) {
            LatLng lastPosition = null;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polylines.get(0), LocationUtils.CAMERA_PAD));

            for (int i = 0; i < polylines.size(); i++) {
                if(lastPosition != null) {
                    mMap.addPolyline(
                            new PolylineOptions()
                                    .geodesic(true)
                                    .color(ColorGenerator
                                    .generateColor(mColorList.get(i)))
                                    .add(lastPosition)
                                    .add(polylines.get(i))
                    );
                    route = (route == null) ?
                            (LocationUtils.getLatLng(lastPosition, mColorList.get(i))) :
                            (route + LocationUtils.getLatLng(lastPosition, mColorList.get(i)));
                }
                lastPosition = polylines.get(i);
            }
        }
    }

    private void addSongNames() {
        ArrayList<SongPerformance> songPerformanceArrayList = new ArrayList<SongPerformance>();
        for (String song : songNames.split(Constant.SONG_SEPARATOR)) {
            if (song.length() > 0) {
                String[] songXperf = song.split(Constant.PERF_SEPARATOR);
                Double perf = Double.parseDouble(songXperf[1]);
                songPerformanceArrayList.add(new SongPerformance(songXperf[0], perf));
            }
        }
        songNames = "";
        if (songPerformanceArrayList.size() > 0) {
            Collections.sort(songPerformanceArrayList, new Comparators.SongPerformanceComparators());
            int max = 3;
            for (int i = 0; i < songPerformanceArrayList.size() && i < max; i++) {
                String songName = songPerformanceArrayList.get(i).getSong();
                String performanceString = songPerformanceArrayList.get(i).getPerformance().toString();
                Log.d("sorted song", performanceString + "," + songName);

                View finishMusic = inflater.inflate(R.layout.finish_music_template, null);
                TextView songNameTextView = (TextView) finishMusic.findViewById(R.id.song_name);
                songNameTextView.setText((i+1) + ". " + songPerformanceArrayList.get(i).getSong() + ", " + songPerformanceArrayList.get(i).getPerformance().toString() + " kcal/min");
                //musicListLinearLayout.addView(finishMusic);

                songNames += (songName + Constant.PERF_SEPARATOR + performanceString + Constant.SONG_SEPARATOR );
            }
        }
    }
}
