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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import org.w3c.dom.Text;

import java.util.Calendar;

public class MyFragment extends Fragment implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    private final String TAG = "MyFragment";
    private Activity mActivity;
    private ContentResolver mContentResolver;
    private LayoutInflater inflater;
    private LinearLayout myMusicContainer;

    private boolean isThisWeekChecked = true;

    private RadioGroup thisWeekTotalRadioGroup;
    private RadioButton thisWeekRadioButton;
    private RadioButton totalRadioButton;

    private TextView caloriesTextView;
    private TextView durationTextView;
    private TextView distanceTextView;
    private TextView distanceUnitTextView;
    private Button pastActivitiesButton;

    private Integer totalTimes;
    private Integer totalDuration;
    private Double totalCalories;
    private Double totalDistance;

    private Integer weeklyTimes;
    private Integer weeklyDuration;
    private Double weeklyCalories;
    private Double weeklyDistance;

    //private Double weeklySpeed;

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
        getSharedPreferences();
        getTotalDataFromDB();

        setViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        //getSharedPreferences();
        //getTotalDataFromDB();
        //setWeeklySummary();
    }


    public void initViews () {
        View thisView = getView();

        thisWeekTotalRadioGroup    = (RadioGroup) thisView.findViewById(R.id.this_week_total_radio_group);
        thisWeekRadioButton        = (RadioButton) thisView.findViewById(R.id.this_week_radio_button);
        totalRadioButton           = (RadioButton) thisView.findViewById(R.id.total_radio_button);

        caloriesTextView           = (TextView) thisView.findViewById(R.id.calories);
        durationTextView           = (TextView) thisView.findViewById(R.id.duration);
        distanceTextView           = (TextView) thisView.findViewById(R.id.distance);
        distanceUnitTextView       = (TextView) thisView.findViewById(R.id.distance_unit);

        pastActivitiesButton       = (Button) thisView.findViewById(R.id.past_activities_button);

        thisWeekTotalRadioGroup.setOnCheckedChangeListener(this);
        pastActivitiesButton.setOnClickListener(this);
    }

    public void setViews () {
        thisWeekTotalRadioGroup.check(R.id.this_week_radio_button);
        if (unitDistance == SettingActivity.SETTING_DISTANCE_KM) {
            distanceUnitTextView.setText(R.string.km);
        } else {
            distanceUnitTextView.setText(R.string.mi);
        }

        updateSummary(weeklyCalories, weeklyDistance, weeklyDuration);
        thisWeekRadioButton.setTextColor(getResources().getColor(R.color.white));
    }

    private void getSharedPreferences () {
        unitDistance  = mSettingSharedPreferences.getInt(SettingActivity.DISTANCE_UNIT, SettingActivity.SETTING_DISTANCE_KM);
        unitSpeedPace = mSettingSharedPreferences.getInt(SettingActivity.SPEED_PACE_UNIT, SettingActivity.SETTING_PACE);
    }

    public void getTotalDataFromDB () {
        Calendar event_date = Calendar.getInstance();
        Calendar today      = Calendar.getInstance();
        String distance, calories, speed, currentEpoch;
        Integer duration;
        totalTimes = 0;
        totalDuration = 0;
        totalCalories = 0.0;
        totalDistance = 0.0;

        weeklyTimes = 0;
        weeklyDuration = 0;
        weeklyDistance = 0.0;
        weeklyCalories = 0.0;
        //weeklySpeed = 100.0;

        String[] projection = {
                MusicRunnerRunningEventDB.COLUMN_NAME_DURATION,
                MusicRunnerRunningEventDB.COLUMN_NAME_CALORIES,
                MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE,
                MusicRunnerRunningEventDB.COLUMN_NAME_SPEED,
                MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND
        };
        Cursor cursor = mContentResolver.query(MusicRunnerRunningEventDB.CONTENT_URI, projection, null, null, null);
        while(cursor.moveToNext()) {
            duration           = cursor.getInt(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DURATION));
            distance           = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE));
            calories           = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_CALORIES));
            currentEpoch       = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND));
            speed              = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_SPEED));
            event_date.setTimeInMillis(Long.parseLong(currentEpoch));

            totalTimes++;
            totalDuration += duration;
            totalCalories += Double.parseDouble(calories);
            totalDistance += Double.parseDouble(distance);

            // set up this week data
            if (event_date.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)) {
                weeklyTimes ++;
                weeklyDuration += duration;//cursor.getInt(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DURATION));
                weeklyDistance += Double.parseDouble(distance);
                weeklyCalories += Double.parseDouble(calories);
                //weeklySpeed     = (weeklySpeed > Double.parseDouble(speed))? Double.parseDouble(speed) : weeklySpeed;
            }
        }
        cursor.close();
    }

    public void updateSummary (Double calories, Double distance, Integer duration) {
        caloriesTextView.setText(StringLib.truncateDoubleString(calories.toString(), 2));
        String durationString = TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(duration));
        durationTextView.setText(durationString);
        distanceTextView.setText(StringLib.truncateDoubleString(distance.toString(), 2));
    }

    public void setWeeklySummary () {
        /*String duration = TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(weeklyDuration));
        String distanceString, speedString, speedUnitString = "my_running_";
        Double minutes = weeklyDuration.doubleValue()/60;
        Double hours   = weeklyDuration.doubleValue()/3600;
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
            speed = distance/hours;
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
*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.past_activities_button:
                startActivity(new Intent(mActivity, MyPastActivitiesActivity.class));
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        int id = radioGroup.getCheckedRadioButtonId();
        switch (radioGroup.getId()) {
            case R.id.this_week_total_radio_group:
                if (id == R.id.this_week_radio_button) {
                    if (!isThisWeekChecked) {
                        // only refreshing information when user selected total before
                        isThisWeekChecked = true;

                        updateSummary(weeklyCalories, weeklyDistance, weeklyDuration);
                        thisWeekRadioButton.setTextColor(getResources().getColor(R.color.white));
                        totalRadioButton.setTextColor(getResources().getColor(R.color.radio_button));
                    }
                } else if (id == R.id.total_radio_button) {
                    if (isThisWeekChecked) {
                        // only refreshing information when user selected this week before
                        isThisWeekChecked = false;

                        updateSummary(totalCalories + 500, totalDistance, totalDuration);
                        thisWeekRadioButton.setTextColor(getResources().getColor(R.color.radio_button));
                        totalRadioButton.setTextColor(getResources().getColor(R.color.white));
                    }
                }
                break;
        }
    }
}
