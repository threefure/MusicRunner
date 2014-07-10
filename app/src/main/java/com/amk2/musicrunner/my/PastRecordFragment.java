package com.amk2.musicrunner.my;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.my.MyFragment.MyTabFragmentListener;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;

import java.util.HashMap;

/**
 * Created by daz on 2014/6/22.
 */
public class PastRecordFragment extends Fragment implements View.OnClickListener{

    private ContentResolver mContentResolver;
    private MyTabFragmentListener mMyTabFragmentListener;

    private LayoutInflater inflater;
    private LinearLayout pastRecordRunningEventContainer;

    private TextView textViewTotalDistance;
    private TextView textViewTotalDuration;
    private TextView textViewTotalSessions;
    private TextView textViewPastRecordDate;

    /*** facebook share component ***/
    private UiLifecycleHelper uiHelper;

    public void setMyTabFragmentListener(MyTabFragmentListener listener) {
        mMyTabFragmentListener = listener;
    }

    public void onBackPressed() {
        mMyTabFragmentListener.onSwitchBetweenMyAndPastRecordFragment();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //facebook share component
        uiHelper = new UiLifecycleHelper(getActivity(), null);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.past_record_fragment, container, false);
    }

    @Override
    public void onActivityCreated (Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        View thisView = getView();
        pastRecordRunningEventContainer = (LinearLayout) thisView.findViewById(R.id.past_record_running_event_container);
        textViewTotalDistance  = (TextView) thisView.findViewById(R.id.past_record_distance);
        textViewTotalDuration  = (TextView) thisView.findViewById(R.id.past_record_duration);
        textViewTotalSessions  = (TextView) thisView.findViewById(R.id.past_record_sessions);
        textViewPastRecordDate = (TextView) thisView.findViewById(R.id.past_record_date);

        inflater = (LayoutInflater) thisView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mContentResolver = getActivity().getContentResolver();
        getPastRecords();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

//    @Override
//    public void onPause () {
//        super.onPause();
//    }

    private void getPastRecords() {
        int id;
        int durationInSec;
        int totalDurationInSec = 0;
        int totalSessions      = 0;
        double totalDistance   = 0;
        long timeInMillis;
        String timeInMillisString;
        String distance, calories, speed, photoPath, songNames;


        String[] projection = {
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_ID,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DURATION,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DATE_IN_MILLISECOND,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_PHOTO_PATH,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SONGS
        };
        String sortOrder = MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_ID + " DESC";
        Cursor cursor = mContentResolver.query(MusicTrackMetaData.MusicTrackRunningEventDataDB.CONTENT_URI, projection, null, null, sortOrder);
        while(cursor.moveToNext()) {
            id                 = cursor.getInt(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_ID));
            durationInSec      = cursor.getInt(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DURATION));
            timeInMillisString = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DATE_IN_MILLISECOND));
            distance           = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE));
            calories           = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES));
            speed              = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED));
            photoPath          = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_PHOTO_PATH));
            songNames          = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SONGS));
            timeInMillis       = Long.parseLong(timeInMillisString);

            Log.d("past records", "duration:" + durationInSec + ", distance:" + distance + ", calories:" + calories + ", speed:" + speed + ", photoPath:" + photoPath);
            addPastRecord(id, durationInSec, timeInMillis, distance, calories, speed, photoPath, songNames);

            totalSessions++;
            totalDurationInSec += durationInSec;
            totalDistance += Double.parseDouble(distance);
        }
        Log.d("total statistic", "sessions: " +totalSessions + ", duration:" + totalDurationInSec + ", distance:" + Double.toString(totalDistance));
        HashMap<String, Integer> readableTime = TimeConverter.getReadableTimeFormatFromSeconds(totalDurationInSec);
        String durationString = TimeConverter.getDurationString(readableTime);
        String distanceString = StringLib.truncateDoubleString(Double.toString(totalDistance), 2);
        textViewTotalDistance.setText(distanceString);
        textViewTotalDuration.setText(durationString);
        textViewTotalSessions.setText(Integer.toString(totalSessions));
    }

    private void addPastRecord (int id, int durationInSec, long timeInMillis, String distance, String calories, String speed, String photoPath, String songNames) {
        View pastRecord = inflater.inflate(R.layout.past_record_template, null);
        TextView textViewDistance    = (TextView) pastRecord.findViewById(R.id.past_record_entry_distance);
        TextView textViewDate        = (TextView) pastRecord.findViewById(R.id.past_record_date);
        TextView textViewDuration    = (TextView) pastRecord.findViewById(R.id.past_record_entry_duration);
        TextView textViewElevation   = (TextView) pastRecord.findViewById(R.id.past_record_entry_elevation);
        TextView textViewSongName    = (TextView) pastRecord.findViewById(R.id.past_record_entry_song_name);
        ImageButton imageButtonShare = (ImageButton) pastRecord.findViewById(R.id.past_record_entry_share_button);
        HashMap<String, Integer> readableTime = TimeConverter.getReadableTimeFormatFromSeconds(durationInSec);
        String durationString = TimeConverter.getDurationString(readableTime);
        String dateString = TimeConverter.getDateString(timeInMillis);
        String mostEfficientSong = MyFragment.getMostEfficientSongs(songNames);

        textViewDate.setText(dateString);
        textViewDistance.setText(distance);
        textViewDuration.setText(durationString);
        textViewSongName.setText(mostEfficientSong);
        pastRecordRunningEventContainer.addView(pastRecord);

        imageButtonShare.setTag(id);
        imageButtonShare.setOnClickListener(this);
        pastRecord.setTag(id);
        pastRecord.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.past_record_entry_share_button:
                // sharing running event should be put here
                Integer shareButtonId = (Integer) v.getTag();
                String[] projection = {
                        //MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DURATION,
                        MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DATE_IN_MILLISECOND,
                        MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE,
                        //MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES,
                        MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED//,
                        //MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_PHOTO_PATH
                };

                String selection = MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_ID + " LIKE ?";
                String[] selectionArgs = { shareButtonId.toString() };
                Cursor cursor = mContentResolver.query(MusicTrackMetaData.MusicTrackRunningEventDataDB.CONTENT_URI, projection, selection, selectionArgs, null);
                cursor.moveToFirst();
                //durationInSec      = cursor.getInt(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DURATION));
                String timeInMillisString = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DATE_IN_MILLISECOND));
                String distance           = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE));
                //calories           = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES));
                String speed              = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED));
                //photoPath          = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_PHOTO_PATH));
                long timeInMillis         = Long.parseLong(timeInMillisString);
                String dateString         = TimeConverter.getDateString(timeInMillis);

                shareRecord(dateString, distance, speed);
                break;
            case R.id.past_record:
                Integer pastRecordId = (Integer) v.getTag();
                Intent pastRecordDetailsIntent = new Intent(getActivity(), PastRecordDetailsActivity.class);
                pastRecordDetailsIntent.putExtra(PastRecordDetailsActivity.PAST_RECORD_ID, pastRecordId.toString());
                startActivity(pastRecordDetailsIntent);
                break;
        }
    }

    /*** facebook share component ***/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    public void shareRecord(String dateString, String distance, String speed){
        FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(getActivity())
                .setLink("https://developers.facebook.com/android") //need to change address to our website or google play store
                .setCaption("Speed: " + speed + " Date: " + dateString)
                .setApplicationName("Distance: " + distance)
                .setName("Music Run+ Record")
                .build();
        uiHelper.trackPendingDialogCall(shareDialog.present());
    }
}
