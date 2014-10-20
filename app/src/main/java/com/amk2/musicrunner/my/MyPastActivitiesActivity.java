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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerRunningEventDB;
import com.amk2.musicrunner.utilities.PhotoLib;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.amk2.musicrunner.utilities.UnitConverter;

import java.util.Calendar;
import java.util.Locale;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by ktlee on 9/10/14.
 */
public class MyPastActivitiesActivity extends Activity implements View.OnClickListener{

    public static final String TAG = "MyPastActivitiesActivity";

    private final int PastActivityIdTag = 1;
    private ActionBar mActionBar;
    private ContentResolver mContentResolver;

    private LinearLayout myPastActivityContainer;
    private LayoutInflater inflater;
    private SharedPreferences mSettingSharedPreferences;
    private Integer distanceUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_past_activities);
        mContentResolver = getContentResolver();

        initialize();
        getPastActivities();
    }

    private void initialize() {
        mActionBar = getActionBar();
        mSettingSharedPreferences = getSharedPreferences(SettingActivity.SETTING_SHARED_PREFERENCE, 0);
        distanceUnit = mSettingSharedPreferences.getInt(SettingActivity.DISTANCE_UNIT, SettingActivity.SETTING_DISTANCE_KM);
        initActionBar();
        initViews();
    }

    private void initViews() {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        myPastActivityContainer = (LinearLayout) findViewById(R.id.my_past_activity_container);
    }

    private void getPastActivities() {
        Integer eventId, duration;
        String distance, currentEpoch, photoPath;
        String[] projection = {
                MusicRunnerRunningEventDB.COLUMN_NAME_ID,
                MusicRunnerRunningEventDB.COLUMN_NAME_DURATION,
                MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE,
                MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND,
                MusicRunnerRunningEventDB.COLUMN_NAME_PHOTO_PATH
        };
        String orderBy = MusicRunnerRunningEventDB.COLUMN_NAME_ID + " DESC";
        Cursor cursor = mContentResolver.query(MusicRunnerRunningEventDB.CONTENT_URI, projection, null, null, orderBy);
        while (cursor.moveToNext()) {
            eventId            = cursor.getInt(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_ID));
            duration           = cursor.getInt(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DURATION));
            distance           = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE));
            currentEpoch       = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND));
            photoPath          = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_PHOTO_PATH));

            addPastActivity(eventId, duration, distance, currentEpoch, photoPath);
        }
        cursor.close();
    }

    private void addPastActivity(Integer eventId, Integer duration, String distanceString, String currentEpoch, String photoPath) {
        View pastActivity = inflater.inflate(R.layout.my_past_activity_template, null);
        ImageView photoImageView        = (ImageView) pastActivity.findViewById(R.id.my_past_activity_photo);
        ImageView dayConditionImageView = (ImageView) pastActivity.findViewById(R.id.my_past_activity_day_condition);
        TextView dateTextView           = (TextView) pastActivity.findViewById(R.id.my_past_activity_date);
        TextView durationTextView       = (TextView) pastActivity.findViewById(R.id.my_past_activity_duration);
        TextView distanceTextView       = (TextView) pastActivity.findViewById(R.id.my_past_activity_distance);
        TextView distanceUnitTextView   = (TextView) pastActivity.findViewById(R.id.my_past_activity_distance_unit);
        String dateString, distanceUnitString;
        Integer dayPeriod, dayConditionId;
        Double distance;

        Calendar date = Calendar.getInstance();

        if (photoPath != null && photoPath.length() > 0) {
            Bitmap resizedPhoto = PhotoLib.resizeToFitTarget(photoPath, photoImageView.getLayoutParams().width, photoImageView.getLayoutParams().height);
            photoImageView.setImageBitmap(resizedPhoto);
        }

        durationTextView.setText(TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(duration)));

        // setting distance information
        if (distanceUnit == SettingActivity.SETTING_DISTANCE_MI) {
            distance = Double.parseDouble(distanceString);
            distance = UnitConverter.getMIFromKM(distance);
            distanceString = StringLib.truncateDoubleString(distance.toString(), 2);
        }
        distanceUnitString = Constant.DistanceMap.get(distanceUnit);
        distanceTextView.setText(distanceString);
        distanceUnitTextView.setText(distanceUnitString);


        date.setTimeInMillis(Long.parseLong(currentEpoch));
        dateString = (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.DAY_OF_MONTH) + " " + date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
        dateTextView.setText(dateString);

        dayPeriod = TimeConverter.getDayPeriod(date.get(Calendar.HOUR_OF_DAY));
        switch (dayPeriod) {
            case TimeConverter.MIDNIGHT:
            case TimeConverter.NIGHT:
                dayConditionId = R.drawable.night;
                break;
            case TimeConverter.MORNING:
                dayConditionId = R.drawable.morning;
                break;
            case TimeConverter.AFTERNOON:
                dayConditionId = R.drawable.afternoon;
                break;
            default:
                dayConditionId = 0;
        }
        Bitmap resizedPhoto2 = PhotoLib.resizeToFitTarget(getResources(), dayConditionId, dayConditionImageView.getLayoutParams().width, dayConditionImageView.getLayoutParams().height);
        dayConditionImageView.setImageBitmap(resizedPhoto2);

        pastActivity.setTag(eventId);
        pastActivity.setOnClickListener(this);
        myPastActivityContainer.addView(pastActivity);
    }

    private void initActionBar() {
        View actionBarView = View.inflate(mActionBar.getThemedContext(), R.layout.customized_action_bar, null);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(Gravity.CENTER));
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_past_activity:
                Integer eventId = (Integer) v.getTag();
                Intent intent = new Intent(getApplicationContext(), MyPastActivityDetailsActivity.class);
                intent.putExtra(MyPastActivityDetailsActivity.EVENT_ID, eventId);
                startActivity(intent);
                break;
        }
    }
}
