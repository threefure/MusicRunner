package com.amk2.musicrunner.my;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerRunningEventDB;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.amk2.musicrunner.utilities.UnitConverter;

import java.util.Calendar;

public class MyFragment extends Fragment implements View.OnClickListener {

    private final String TAG = "MyFragment";
    private Activity mActivity;
    private ContentResolver mContentResolver;
    private LayoutInflater inflater;
    private LinearLayout myMusicContainer;

    private ProgressBar timesProgressBar;
    private ProgressBar calorieProgressBar;
    private ProgressBar distanceProgressBar;

    private TextView totalTimesTextView;
    private TextView totalCalorieTextView;
    private TextView totalDistanceTextView;
    private TextView weeklyDurationTextView;
    private TextView weeklyTimesTextView;
    private TextView weeklyDistanceTextView;
    private TextView weeklyDistanceUnitTextView;
    private TextView weeklyCalorieTextView;
    private TextView weeklySpeedTitleTextView;
    private TextView weeklySpeedTextView;
    private TextView weeklySpeedUnitTextView;

    private RelativeLayout pastActivitiesRelativeLayout;

    private Integer totalTimes;
    private Integer weeklyDuration;
    private Integer weeklyTimes;
    private Double totalCalories;
    private Double totalDistance;
    private Double weeklyCalories;
    private Double weeklyDistance;
    private Double weeklySpeed;

    private SharedPreferences mSettingSharedPreferences;
    private Integer unitDistance;
    private Integer unitSpeedPace;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.my_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        initialize();
	}

    private void initialize() {
        mContentResolver = getActivity().getContentResolver();
        mSettingSharedPreferences = getActivity().getSharedPreferences(SettingActivity.SETTING_SHARED_PREFERENCE, 0);
        initViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        getSharedPreferences();
        getTotalDataFromDB();
        setWeeklySummary();
    }


    public void initViews () {
        View thisView = getView();

        weeklyDurationTextView     = (TextView) thisView.findViewById(R.id.my_weekly_duration);
        weeklyDistanceTextView     = (TextView) thisView.findViewById(R.id.my_weekly_distance);
        weeklyDistanceUnitTextView = (TextView) thisView.findViewById(R.id.my_weekly_distance_unit);
        weeklyCalorieTextView      = (TextView) thisView.findViewById(R.id.my_weekly_calories);
        weeklySpeedTitleTextView   = (TextView) thisView.findViewById(R.id.my_weekly_speed_title);
        weeklySpeedTextView        = (TextView) thisView.findViewById(R.id.my_weekly_speed);
        weeklySpeedUnitTextView    = (TextView) thisView.findViewById(R.id.my_weekly_speed_unit);

        pastActivitiesRelativeLayout = (RelativeLayout) thisView.findViewById(R.id.my_past_activities);

        pastActivitiesRelativeLayout.setOnClickListener(this);
    }

    private void getSharedPreferences () {
        unitDistance  = mSettingSharedPreferences.getInt(SettingActivity.DISTANCE_UNIT, SettingActivity.SETTING_DISTANCE_KM);
        unitSpeedPace = mSettingSharedPreferences.getInt(SettingActivity.SPEED_PACE_UNIT, SettingActivity.SETTING_PACE);
    }

    public void getTotalDataFromDB () {
        Calendar event_date = Calendar.getInstance();
        Calendar today      = Calendar.getInstance();
        String distance, calories, speed, currentEpoch;
        totalTimes = 0;
        totalCalories = 0.0;
        totalDistance = 0.0;
        weeklyDuration = 0;
        weeklyTimes = 0;
        weeklyDistance = 0.0;
        weeklyCalories = 0.0;
        weeklySpeed = 100.0;

        String[] projection = {
                MusicRunnerRunningEventDB.COLUMN_NAME_DURATION,
                MusicRunnerRunningEventDB.COLUMN_NAME_CALORIES,
                MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE,
                MusicRunnerRunningEventDB.COLUMN_NAME_SPEED,
                MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND
        };
        Cursor cursor = mContentResolver.query(MusicRunnerRunningEventDB.CONTENT_URI, projection, null, null, null);
        while(cursor.moveToNext()) {
            distance           = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE));
            calories           = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_CALORIES));
            currentEpoch       = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND));
            speed              = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_SPEED));
            event_date.setTimeInMillis(Long.parseLong(currentEpoch));

            totalTimes++;
            totalCalories += Double.parseDouble(calories);
            totalDistance += Double.parseDouble(distance);

            Log.d(TAG, "week of year = " + event_date.get(Calendar.WEEK_OF_YEAR) + " epoch=" + Long.parseLong(currentEpoch));
            // set up this week data
            if (event_date.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)) {
                weeklyTimes ++;
                weeklyDuration += cursor.getInt(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DURATION));
                weeklyDistance += Double.parseDouble(distance);
                weeklyCalories += Double.parseDouble(calories);
                weeklySpeed     = (weeklySpeed > Double.parseDouble(speed))? Double.parseDouble(speed) : weeklySpeed;
            }
        }
        cursor.close();
    }

    public void setProgressBar () {
        timesProgressBar.setMax(30);
        timesProgressBar.setProgress(totalTimes.intValue());
        totalTimesTextView.setText(totalTimes.intValue() + "/30 times");

        calorieProgressBar.setMax(100);
        calorieProgressBar.setProgress(totalCalories.intValue());
        totalCalorieTextView.setText(totalCalories.intValue() + "/100 kcal");

        distanceProgressBar.setMax(50);
        distanceProgressBar.setProgress(totalDistance.intValue());
        totalDistanceTextView.setText(totalDistance.intValue() + "/50 km");
    }

    public void setWeeklySummary () {
        String duration = TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(weeklyDuration));
        String distanceString, speedString, speedUnitString = "my_running_";
        Double minutes = weeklyDuration.doubleValue()/60;
        Double distance = weeklyDistance;
        Double speed;
        Integer speedTitleId;
        weeklyDurationTextView.setText(duration);

        if (unitDistance == SettingActivity.SETTING_DISTANCE_MI) {
            distance = UnitConverter.getMIFromKM(distance);
            speedUnitString += "mi_";
        } else {
            speedUnitString += "km_";
        }

        if (unitSpeedPace == SettingActivity.SETTING_PACE) {
            speed = minutes/distance;
            if (speed.isNaN() || speed.isInfinite()) {
                speed = 0.0;
            }
            speedUnitString += "pace";
            speedTitleId = R.string.pace;
        } else {
            speed = distance/minutes;
            speedUnitString += "speed";
            speedTitleId = R.string.speed;
        }

        distanceString = StringLib.truncateDoubleString(distance.toString(), 2);
        speedString = StringLib.truncateDoubleString(speed.toString(), 2);

        // set up distance information
        weeklyDistanceTextView.setText(distanceString);
        weeklyDistanceUnitTextView.setText(Constant.DistanceMap.get(unitDistance));

        // set up speed information
        weeklySpeedTextView.setText(speedString);
        weeklySpeedUnitTextView.setText(getResources().getString(Constant.PaceSpeedMap.get(speedUnitString)));
        weeklySpeedTitleTextView.setText(getResources().getString(speedTitleId));

        // set up calorie information
        weeklyCalorieTextView.setText(StringLib.truncateDoubleString(weeklyCalories.toString(), 2));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_past_activities:
                startActivity(new Intent(mActivity, MyPastActivitiesActivity.class));
                break;
        }
    }
}
