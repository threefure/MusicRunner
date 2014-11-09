package com.amk2.musicrunner.my;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.login.LoginActivity;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerRunningEventDB;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.amk2.musicrunner.utilities.UnitConverter;

import java.util.Calendar;

public class MyFragment extends Fragment implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    private final String TAG = "MyFragment";
    private Activity mActivity;
    private ContentResolver mContentResolver;
    private String LAPS;

    private boolean isThisWeekChecked = true;

    private RadioGroup thisWeekTotalRadioGroup;
    private RadioButton thisWeekRadioButton;
    private RadioButton totalRadioButton;

    private TextView lapsTextView;
    private TextView lapsUnitHintTextView;
    private TextView caloriesTextView;
    private TextView durationTextView;
    private TextView distanceTextView;
    private TextView distanceUnitTextView;
    private Button pastActivitiesButton;
    private Button loginButton;

    private LinearLayout userInfoContainer;
    private LinearLayout loginContainer;

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
    private SharedPreferences mLoginSharedPreferences;
    private Integer unitDistance;
    private Integer unitSpeedPace;
    private Integer loginStatus;

    private Handler handler = new Handler();

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
        mLoginSharedPreferences   = getActivity().getSharedPreferences(LoginActivity.LOGIN, Context.MODE_PRIVATE);
        LAPS = getString(R.string.laps);
        initViews();
        getSharedPreferences();
        getTotalDataFromDB();
        setViews();
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    public void initViews () {
        View thisView = getView();

        thisWeekTotalRadioGroup    = (RadioGroup) thisView.findViewById(R.id.this_week_total_radio_group);
        thisWeekRadioButton        = (RadioButton) thisView.findViewById(R.id.this_week_radio_button);
        totalRadioButton           = (RadioButton) thisView.findViewById(R.id.total_radio_button);

        lapsTextView               = (TextView) thisView.findViewById(R.id.laps);
        lapsUnitHintTextView       = (TextView) thisView.findViewById(R.id.laps_unit_hint);
        caloriesTextView           = (TextView) thisView.findViewById(R.id.calories);
        durationTextView           = (TextView) thisView.findViewById(R.id.duration);
        distanceTextView           = (TextView) thisView.findViewById(R.id.distance);
        distanceUnitTextView       = (TextView) thisView.findViewById(R.id.distance_unit);

        pastActivitiesButton       = (Button) thisView.findViewById(R.id.past_activities_button);
        loginButton                = (Button) thisView.findViewById(R.id.login_button);

        userInfoContainer          = (LinearLayout) thisView.findViewById(R.id.user_info_container);
        loginContainer             = (LinearLayout) thisView.findViewById(R.id.login_container);


        thisWeekTotalRadioGroup.setOnCheckedChangeListener(this);
        pastActivitiesButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
    }

    public void setViews () {
        String lapsUnitHintString = "1 " + LAPS + " = ";
        thisWeekTotalRadioGroup.check(R.id.this_week_radio_button);
        if (unitDistance == SettingActivity.SETTING_DISTANCE_KM) {
            distanceUnitTextView.setText(R.string.km);
            lapsUnitHintString += ("0.8 " + getString(R.string.km));
        } else {
            distanceUnitTextView.setText(R.string.mi);
            lapsUnitHintString += ("0.5 " + getString(R.string.mi));
        }

        updateSummary(weeklyCalories, weeklyDistance, weeklyDuration);
        thisWeekRadioButton.setTextColor(getResources().getColor(R.color.white));

        if (loginStatus == LoginActivity.STATUS_LOGIN) {
            loginContainer.setVisibility(View.GONE);
            userInfoContainer.setVisibility(View.VISIBLE);
        } else {
            loginContainer.setVisibility(View.VISIBLE);
            userInfoContainer.setVisibility(View.GONE);
        }

        lapsUnitHintTextView.setText(lapsUnitHintString);
    }

    private void getSharedPreferences () {
        unitDistance  = mSettingSharedPreferences.getInt(SettingActivity.DISTANCE_UNIT, SettingActivity.SETTING_DISTANCE_KM);
        unitSpeedPace = mSettingSharedPreferences.getInt(SettingActivity.SPEED_PACE_UNIT, SettingActivity.SETTING_PACE);
        loginStatus   = mLoginSharedPreferences.getInt(LoginActivity.STATUS, LoginActivity.STATUS_LOGOUT);

        Log.d(TAG, "login status " + loginStatus.toString());
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

        GetLapsRunnable getLapsRunnable = new GetLapsRunnable(distance);
        Thread getLapsThread = new Thread(getLapsRunnable);
        getLapsThread.start();
    }

    public void updateLaps (Double laps) {
        final String lapsString = StringLib.truncateDoubleString(laps.toString(), 1);
        handler.post(new Runnable() {
            @Override
            public void run() {
                lapsTextView.setText(lapsString + " " + LAPS);
            }
        });

    }

    public class GetLapsRunnable implements Runnable {
        Double shownDistance;
        Double distance;
        Double lapUnit;
        public GetLapsRunnable (Double distance) {
            if (unitDistance == SettingActivity.SETTING_DISTANCE_KM) {
                this.distance = distance;
                lapUnit = 0.8;
            } else {
                this.distance = UnitConverter.getMIFromKM(distance);
                lapUnit = 0.5;
            }
            shownDistance = 0.0;
        }
        @Override
        public void run() {
            try {
                updateLaps(0.0);
                while (distance > lapUnit) {
                    shownDistance ++;
                    updateLaps(shownDistance);
                    distance -= lapUnit;
                    Thread.sleep(20);
                }
                shownDistance += (distance/lapUnit);
                updateLaps(shownDistance);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.past_activities_button:
                startActivity(new Intent(mActivity, MyPastActivitiesActivity.class));
                break;
            case R.id.login_button:
                mLoginSharedPreferences.edit().remove(LoginActivity.STATUS).putInt(LoginActivity.STATUS, LoginActivity.STATUS_NONE).commit();
                startActivity(new Intent(mActivity, LoginActivity.class));
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
                        totalRadioButton.setTextColor(getResources().getColor(R.color.very_light_gray));
                    }
                } else if (id == R.id.total_radio_button) {
                    if (isThisWeekChecked) {
                        // only refreshing information when user selected this week before
                        isThisWeekChecked = false;

                        updateSummary(totalCalories, totalDistance, totalDuration);
                        thisWeekRadioButton.setTextColor(getResources().getColor(R.color.very_light_gray));
                        totalRadioButton.setTextColor(getResources().getColor(R.color.white));
                    }
                }
                break;
        }
    }
}
