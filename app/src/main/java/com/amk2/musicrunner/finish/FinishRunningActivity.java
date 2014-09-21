package com.amk2.musicrunner.finish;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.LocationUtils;
import com.amk2.musicrunner.running.MapFragmentRun;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerRunningEventDB;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerSongNameDB;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerArtistDB;
import com.amk2.musicrunner.utilities.CameraLib;
import com.amk2.musicrunner.utilities.ColorGenerator;
import com.amk2.musicrunner.utilities.SongPerformance;
import com.amk2.musicrunner.utilities.PhotoLib;
import com.amk2.musicrunner.utilities.ShowImageActivity;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.amk2.musicrunner.views.MusicRunnerLineMapView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static android.widget.Toast.makeText;

/**
 * Created by daz on 2014/6/15.
 */
public class FinishRunningActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "FinishRunningActivity";
    public static String FINISH_RUNNING_DURATION = "com.amk2.duration";
    public static String FINISH_RUNNING_DISTANCE = "com.amk2.distance";
    public static String FINISH_RUNNING_CALORIES = "com.amk2.calories";
    public static String FINISH_RUNNING_SPEED    = "com.amk2.speed";
    public static String FINISH_RUNNING_PHOTO    = "com.amk2.photo";
    public static String FINISH_RUNNING_SONGS    = "com.amk2.songs";
    public static Integer REQUEST_IMAGE_CAPTURE  = 1;

    private LayoutInflater inflater;

    private TextView finishDateTextView;
    private TextView finishTimeTextView;
    private TextView distanceTextView;
    private TextView caloriesTextView;
    private TextView speedTextView;
    private TextView finishDurationTextView;
    private TextView saveButton;
    private TextView discardButton;
    private ImageView picPreviewImageView;
    private ImageButton cameraImageButton;
    private MusicRunnerLineMapView musicRunnerLineMapView;

    private GoogleMap mMap;

    private int totalSec = 0;
    private String distance  = null;
    private String calories  = null;
    private String speed     = null;
    private String photoPath = null;
    private String route     = null;

    private ArrayList<SongPerformance> songPerformanceArrayList;
    private ContentResolver mContentResolver;

    private Calendar calendar;
    private long timeInMillis;

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
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        finishDateTextView     = (TextView) findViewById(R.id.finish_date);
        finishTimeTextView     = (TextView) findViewById(R.id.finish_time);
        saveButton             = (TextView) findViewById(R.id.finish_save);
        discardButton          = (TextView) findViewById(R.id.finish_discard);
        distanceTextView       = (TextView) findViewById(R.id.finish_distance);
        caloriesTextView       = (TextView) findViewById(R.id.finish_calorie);
        speedTextView          = (TextView) findViewById(R.id.finish_speed);
        finishDurationTextView = (TextView) findViewById(R.id.finish_duration);
        picPreviewImageView    = (ImageView) findViewById(R.id.finish_photo);
        cameraImageButton      = (ImageButton) findViewById(R.id.finish_camera);
        musicRunnerLineMapView = (MusicRunnerLineMapView) findViewById(R.id.finish_line_map);

        saveButton.setOnClickListener(this);
        discardButton.setOnClickListener(this);
        picPreviewImageView.setOnClickListener(this);
        cameraImageButton.setOnClickListener(this);

        totalSec  = intent.getIntExtra(FINISH_RUNNING_DURATION, 0);
        distance  = intent.getStringExtra(FINISH_RUNNING_DISTANCE);
        calories  = intent.getStringExtra(FINISH_RUNNING_CALORIES);
        speed     = intent.getStringExtra(FINISH_RUNNING_SPEED);
        photoPath = intent.getStringExtra(FINISH_RUNNING_PHOTO);
        songPerformanceArrayList = intent.getParcelableArrayListExtra(FINISH_RUNNING_SONGS);

        if (totalSec > 0) {
            HashMap<String, Integer> time =  TimeConverter.getReadableTimeFormatFromSeconds(totalSec);
            String duration = TimeConverter.getDurationString(time);
            finishDurationTextView.setText(duration);
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
        if (photoPath != null && photoPath.length() > 0) {
            setPicPreview();
        }
        if (songPerformanceArrayList != null) {
            musicRunnerLineMapView.setMusicJoints(songPerformanceArrayList);
        }

        calendar = Calendar.getInstance();
        timeInMillis = calendar.getTimeInMillis();

        String dayPeriod, dateString, timeString;
        dayPeriod = TimeConverter.getDayPeriod(calendar.get(Calendar.HOUR_OF_DAY));
        timeString = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + " " + dayPeriod;
        dateString = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + calendar.get(Calendar.DAY_OF_MONTH);
        finishDateTextView.setText(dateString);
        finishTimeTextView.setText(timeString);

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
                saveToSQLiteDB();
                finish();
                break;
            case R.id.finish_discard:
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

    private void saveToSQLiteDB() {
        Uri eventUri = saveEvent(timeInMillis);
        long id = ContentUris.parseId(eventUri);
        saveSongPerformance(id, timeInMillis);
    }

    private Uri saveEvent (long timeInMillis) {
        ContentValues values = new ContentValues();
        values.put(MusicRunnerRunningEventDB.COLUMN_NAME_DURATION, totalSec);
        values.put(MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND, Long.toString(timeInMillis));
        values.put(MusicRunnerRunningEventDB.COLUMN_NAME_CALORIES, calories);
        values.put(MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE, distance);
        values.put(MusicRunnerRunningEventDB.COLUMN_NAME_SPEED, speed);
        values.put(MusicRunnerRunningEventDB.COLUMN_NAME_PHOTO_PATH, photoPath);
        values.put(MusicRunnerRunningEventDB.COLUMN_NAME_ROUTE, route);
        Uri uri = mContentResolver.insert(MusicRunnerDBMetaData.MusicRunnerRunningEventDB.CONTENT_URI, values);
        return uri;
    }

    private void saveSongPerformance (long eventId, long timeInMillis) {
        int length = songPerformanceArrayList.size();
        long songId;
        SongPerformance currentSP;
        String[] songNameProjection = {
                MusicRunnerSongNameDB.COLUMN_NAME_ID
        };
        String songNameSelection = MusicRunnerSongNameDB.COLUMN_NAME_SONG_NAME + " = ?";
        String[] songNameSelectionArgs = new String[1];
        Cursor cursor;
        for (int i = 0; i < length; i ++) {
            currentSP = songPerformanceArrayList.get(i);
            songNameSelectionArgs[0] = currentSP.name;
            cursor = mContentResolver.query(MusicRunnerSongNameDB.CONTENT_URI,
                    songNameProjection, songNameSelection, songNameSelectionArgs, null);
            if (cursor == null) {
                // no such song in DB;
                songId = saveSongName(currentSP.name, currentSP.artist, currentSP.realSongId);
            } else if (cursor.getCount() < 1) {
                songId = saveSongName(currentSP.name, currentSP.artist, currentSP.realSongId);
            } else {
                cursor.moveToFirst();
                songId = cursor.getLong(cursor.getColumnIndex(MusicRunnerSongNameDB.COLUMN_NAME_ID));
            }
            ContentValues values = new ContentValues();
            values.put(MusicRunnerSongPerformanceDB.COLUMN_NAME_SONG_ID, songId);
            values.put(MusicRunnerSongPerformanceDB.COLUMN_NAME_EVENT_ID, eventId);
            values.put(MusicRunnerSongPerformanceDB.COLUMN_NAME_DURATION, currentSP.duration);
            values.put(MusicRunnerSongPerformanceDB.COLUMN_NAME_DATE_IN_MILLISECOND, timeInMillis);
            values.put(MusicRunnerSongPerformanceDB.COLUMN_NAME_CALORIES, currentSP.calories);
            values.put(MusicRunnerSongPerformanceDB.COLUMN_NAME_DISTANCE, currentSP.distance);
            values.put(MusicRunnerSongPerformanceDB.COLUMN_NAME_SPEED, currentSP.speed);
            Uri uri = mContentResolver.insert(MusicRunnerSongPerformanceDB.CONTENT_URI, values);
            Log.d(TAG, "Saved song performance: " + currentSP.name + " into DB, id=" + ContentUris.parseId(uri));
        }
    }

    private long saveSongName (String name, String artist, long realSongId) {
        long artistId;
        ContentValues values = new ContentValues();
        String[] artistProjection = {
                MusicRunnerArtistDB.COLUMN_NAME_ID
        };
        String artistSelection = MusicRunnerArtistDB.COLUMN_NAME_ARTIST + " = ?";
        String[] artistSelectionArgs = {artist};
        Cursor cursor = mContentResolver.query(MusicRunnerArtistDB.CONTENT_URI,
                artistProjection, artistSelection, artistSelectionArgs, null);
        if (cursor == null) {
            artistId = saveArtist(artist);
        } else if (cursor.getCount() < 1) {
            artistId = saveArtist(artist);
        } else {
            cursor.moveToFirst();
            artistId = cursor.getLong(cursor.getColumnIndex(MusicRunnerArtistDB.COLUMN_NAME_ID));
        }

        values.put(MusicRunnerSongNameDB.COLUMN_NAME_SONG_NAME, name);
        values.put(MusicRunnerSongNameDB.COLUMN_NAME_ARTIST_ID, artistId);
        values.put(MusicRunnerSongNameDB.COLUMN_NAME_SONG_REAL_ID, realSongId);
        Uri uri = mContentResolver.insert(MusicRunnerSongNameDB.CONTENT_URI, values);
        Log.d(TAG, "Saved song name: " + name + " into DB, id=" + ContentUris.parseId(uri));
        return ContentUris.parseId(uri);
    }

    private long saveArtist (String artist) {
        ContentValues values = new ContentValues();
        values.put(MusicRunnerArtistDB.COLUMN_NAME_ARTIST, artist);
        Uri uri = mContentResolver.insert(MusicRunnerArtistDB.CONTENT_URI, values);
        Log.d(TAG, "Saved artist: " + artist + " into artist DB, id=" + ContentUris.parseId(uri));
        return ContentUris.parseId(uri);
    }
}
