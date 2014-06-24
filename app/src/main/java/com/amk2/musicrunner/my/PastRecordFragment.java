package com.amk2.musicrunner.my;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.my.MyFragment.MyTabFragmentListener;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.start.DayMapping;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by daz on 2014/6/22.
 */
public class PastRecordFragment extends Fragment {

    private ContentResolver mContentResolver;
    private MyTabFragmentListener mMyTabFragmentListener;

    private LayoutInflater inflater;
    private LinearLayout pastRecordRunningEventContainer;

    private TextView textViewTotalDistance;
    private TextView textViewTotalDuration;
    private TextView textViewTotalSessions;
    private TextView textViewPastRecordDate;

    public void setMyTabFragmentListener(MyTabFragmentListener listener) {
        mMyTabFragmentListener = listener;
    }

    public void onBackPressed() {
        mMyTabFragmentListener.onSwitchBetweenMyAndPastRecordFragment();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    @Override
    public void onStart() {
        super.onStart();
        getPastRecords();
    }

    @Override
    public void onPause () {
        super.onPause();
    }

    private void getPastRecords() {
        int durationInSec;
        int totalDurationInSec = 0;
        int totalSessions      = 0;
        double totalDistance   = 0;
        long timeInMillis;
        String timeInMillisString;
        String distance, calories, speed, photoPath;


        String[] projection = {
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DURATION,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DATE_IN_MILLISECOND,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_PHOTO_PATH
        };
        Cursor cursor = mContentResolver.query(MusicTrackMetaData.MusicTrackRunningEventDataDB.CONTENT_URI, projection, null, null, null);
        while(cursor.moveToNext()) {
            durationInSec      = cursor.getInt(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DURATION));
            timeInMillisString = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DATE_IN_MILLISECOND));
            distance           = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE));
            calories           = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES));
            speed              = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED));
            photoPath          = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_PHOTO_PATH));
            timeInMillis       = Long.parseLong(timeInMillisString);

            Log.d("past records", "duration:" + durationInSec + ", distance:" + distance + ", calories:" + calories + ", speed:" + speed + ", photoPath:" + photoPath);
            addPastRecord(durationInSec, timeInMillis, distance, calories, speed, photoPath);

            totalSessions++;
            totalDurationInSec += durationInSec;
            totalDistance += Double.parseDouble(distance);
        }
        Log.d("total statistic", "sessions: " +totalSessions + ", duration:" + totalDurationInSec + ", distance:" + Double.toString(totalDistance));
        HashMap<String, Integer> readableTime = TimeConverter.getReadableTimeFormatFromSeconds(totalDurationInSec);
        String durationString = TimeConverter.getDurationString(readableTime);
        textViewTotalDistance.setText(Double.toString(totalDistance));
        textViewTotalDuration.setText(durationString);
        textViewTotalSessions.setText(Integer.toString(totalSessions));
    }

    private void addPastRecord (int durationInSec, long timeInMillis, String distance, String calories, String speed, String photoPath) {
        View pastRecord = inflater.inflate(R.layout.past_record_template, null);
        TextView textViewDistance  = (TextView) pastRecord.findViewById(R.id.past_record_entry_distance);
        TextView textViewDate      = (TextView) pastRecord.findViewById(R.id.past_record_date);
        TextView textViewDuration  = (TextView) pastRecord.findViewById(R.id.past_record_entry_duration);
        TextView textViewElevation = (TextView) pastRecord.findViewById(R.id.past_record_entry_elevation);
        HashMap<String, Integer> readableTime = TimeConverter.getReadableTimeFormatFromSeconds(durationInSec);
        String durationString = TimeConverter.getDurationString(readableTime);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        int day = calendar.get(Calendar.DAY_OF_WEEK) -1;

        textViewDate.setText(DayMapping.getDay(Integer.toString(day)));
        textViewDistance.setText(distance);
        textViewDuration.setText(durationString);
        pastRecordRunningEventContainer.addView(pastRecord);
    }

}
