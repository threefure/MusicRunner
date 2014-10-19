package com.amk2.musicrunner.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.amk2.musicrunner.R;

public class SettingActivity extends Activity implements View.OnClickListener{

    private ActionBar mActionBar;
    private final String TAG = "SettingActivity";
    private final int SETTING_WEIGHT_KG = 0;
    private final int SETTING_WEIGHT_LB = 1;
    private final int SETTING_DISTANCE_KM = 0;
    private final int SETTING_DISTANCE_MI = 1;
    private final int SETTING_HEIGHT_CM = 0;
    private final int SETTING_HEIGHT_IN = 1;
    private final int SETTING_PACE  = 0;
    private final int SETTING_SPEED = 1;
    private final int SETTING_DEGREE_C = 0;
    private final int SETTING_DEGREE_F = 1;

    private TextView accountTextView;
    private TextView facebookTextView;
    private TextView weightTextView;
    private TextView heightTextView;
    private TextView birthDateTextView;
    private RadioGroup unitWeightRadioGroup;
    private RadioGroup unitDistanceRadioGroup;
    private RadioGroup unitHeightRadioGroup;
    private RadioGroup unitSpeedPaceRadioGroup;
    private RadioGroup unitDegreeRadioGroup;
    private ToggleButton autoCueToggleButton;
    private Spinner autoCueSpinner;
    private Spinner languageSpinner;
    private RelativeLayout weightRelativeLayout;
    private RelativeLayout heightRelativeLayout;
    private RelativeLayout birthDateRelativeLayout;
    private Button logoutButton;

    private SharedPreferences mSettingSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);
        initialize();
    }

    private void initialize() {
        mActionBar = getActionBar();
        initActionBar();
        initViews();
        setViews();
    }

    private void initActionBar() {
        mActionBar.setTitle(getString(R.string.setting_text));
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
    }

    private void initViews() {
        accountTextView   = (TextView) findViewById(R.id.account);
        facebookTextView  = (TextView) findViewById(R.id.facebook);
        weightTextView    = (TextView) findViewById(R.id.weight);
        heightTextView    = (TextView) findViewById(R.id.height);
        birthDateTextView = (TextView) findViewById(R.id.birth_date);

        unitWeightRadioGroup    = (RadioGroup) findViewById(R.id.unit_weight);
        unitDistanceRadioGroup  = (RadioGroup) findViewById(R.id.unit_distance);
        unitHeightRadioGroup    = (RadioGroup) findViewById(R.id.unit_height);
        unitSpeedPaceRadioGroup = (RadioGroup) findViewById(R.id.unit_pace_speed);
        unitDegreeRadioGroup    = (RadioGroup) findViewById(R.id.unit_degree);

        autoCueToggleButton = (ToggleButton) findViewById(R.id.auto_cue_toggle);

        autoCueSpinner  = (Spinner) findViewById(R.id.auto_cue_spinner);
        languageSpinner = (Spinner) findViewById(R.id.language_spinner);

        weightRelativeLayout    = (RelativeLayout) findViewById(R.id.setting_weight);
        heightRelativeLayout    = (RelativeLayout) findViewById(R.id.setting_height);
        birthDateRelativeLayout = (RelativeLayout) findViewById(R.id.setting_birth_date);

        logoutButton = (Button) findViewById(R.id.logout);
    }

    private void setViews () {
        mSettingSharedPreferences = getSharedPreferences("setting", 0);
        String account = mSettingSharedPreferences.getString("account", "no account");
        Integer unitWeight    = mSettingSharedPreferences.getInt("weight", SETTING_WEIGHT_KG);
        Integer unitDistance  = mSettingSharedPreferences.getInt("distance", SETTING_DISTANCE_KM);
        Integer unitHeight    = mSettingSharedPreferences.getInt("height", SETTING_HEIGHT_IN);
        Integer unitSpeedPace = mSettingSharedPreferences.getInt("speed_pace", SETTING_PACE);
        Integer unitDegree    = mSettingSharedPreferences.getInt("degree", SETTING_DEGREE_C);
        accountTextView.setText(account);

        // setting weight radio button
        switch (unitWeight) {
            case SETTING_WEIGHT_KG:
                unitWeightRadioGroup.check(R.id.setting_kg);
                break;
            case SETTING_WEIGHT_LB:
                unitWeightRadioGroup.check(R.id.setting_lb);
                break;
        }

        // setting height radio button
        switch (unitHeight) {
            case SETTING_HEIGHT_CM:
                unitHeightRadioGroup.check(R.id.setting_cm);
                break;
            case SETTING_HEIGHT_IN:
                unitHeightRadioGroup.check(R.id.setting_in);
                break;
        }

        // setting distance radio button
        switch (unitDistance) {
            case SETTING_DISTANCE_KM:
                unitDistanceRadioGroup.check(R.id.setting_km);
                break;
            case SETTING_DISTANCE_MI:
                unitDistanceRadioGroup.check(R.id.setting_mi);
                break;
        }

        // setting speed/pace radio button
        switch (unitSpeedPace) {
            case SETTING_PACE:
                unitSpeedPaceRadioGroup.check(R.id.setting_pace);
                break;
            case SETTING_SPEED:
                unitSpeedPaceRadioGroup.check(R.id.setting_speed);
                break;
        }

        // setting degree radio button
        switch (unitDegree) {
            case SETTING_DEGREE_C:
                unitDegreeRadioGroup.check(R.id.setting_celsius);
                break;
            case SETTING_DEGREE_F:
                unitDegreeRadioGroup.check(R.id.setting_fahrenheit);
                break;
        }

        weightRelativeLayout.setOnClickListener(this);
        heightRelativeLayout.setOnClickListener(this);
        birthDateRelativeLayout.setOnClickListener(this);
    }

    private void setRadioButton () {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setting, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setting_weight:
                Log.d(TAG, "press on weight");
                break;
            case R.id.setting_height:
                Log.d(TAG, "press on height");
                break;
            case R.id.setting_birth_date:
                Log.d(TAG, "press on birth date");
                break;
        }
    }
}
