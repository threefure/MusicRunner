package com.amk2.musicrunner.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.login.LoginActivity;
import com.amk2.musicrunner.main.MusicRunnerApplication;
import com.amk2.musicrunner.utilities.RestfulUtility;
import com.amk2.musicrunner.utilities.SharedPreferencesUtility;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.UnitConverter;
import com.amk2.musicrunner.views.DatePickerFragment;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SettingActivity extends Activity implements
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener,
        DatePickerDialog.OnDateSetListener,
        RadioGroup.OnCheckedChangeListener{

    public static final String SETTING_SHARED_PREFERENCE = "setting";
    public static final String ACCOUNT         = "account";
    public static final String WEIGHT_UNIT     = "weight_unit";
    public static final String HEIGHT_UNIT     = "height_unit";
    public static final String DISTANCE_UNIT   = "distance_unit";
    public static final String SPEED_PACE_UNIT = "speed_pace_unit";
    public static final String DEGREE_UNIT     = "degree_unit";
    public static final String BIRTH_DATE      = "birth_date";
    public static final String WEIGHT          = "weight";
    public static final String HEIGHT          = "height";
    public static final String AUTO_CUE        = "auto_cue";
    public static final String LANGUAGE        = "language";
    public static final String AUTO_CUE_TOGGLE = "auto_cue_toggle";

    public static final int SETTING_WEIGHT_KG = 0;
    public static final int SETTING_WEIGHT_LB = 1;
    public static final int SETTING_DISTANCE_KM = 0;
    public static final int SETTING_DISTANCE_MI = 1;
    public static final int SETTING_HEIGHT_CM = 0;
    public static final int SETTING_HEIGHT_IN = 1;
    public static final int SETTING_PACE  = 0;
    public static final int SETTING_SPEED = 1;
    public static final int SETTING_DEGREE_C = 0;
    public static final int SETTING_DEGREE_F = 1;
    public static final String SETTING_AUTO_CUE_5_MINUTES = "5 Minutes";
    public static final String SETTING_LANGUAGE_ENGLISH  = "English";
    public static final String SETTING_LANGUAGE_CHINESE  = "中文";
    public static final String SETTING_LANGUAGE_JAPANESE = "日本語";

    private final String TAG = "SettingActivity";

    private ActionBar mActionBar;

    private String weightUnit = "";
    private String heightUnit = "";
    private boolean isChanged = false;

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
    private SharedPreferences mAccountSharedPreferences;
    private SharedPreferences mLoginSharedPreferences;
    private ArrayAdapter<CharSequence> autoCueArrayAdapter;
    private ArrayAdapter<CharSequence> languageArrayAdapter;
    private Calendar calendar;
    private Configuration configuration;
    private String usedLanguage;
    private ProgressDialog progressDialog;

    private Activity self;

    //Settings Parameters
    String account        = null;
    Integer userFrom      = null;
    Integer unitWeight    = null;
    Integer unitDistance  = null;
    Integer unitHeight    = null;
    Integer unitSpeedPace = null;
    Integer unitDegree    = null;
    Long birthDateEpoch   = null;
    String weight         = null;
    String height         = null;
    CharSequence autoCue  =null;
    CharSequence language = null;
    Boolean autoCueToggle = null;

    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);
        self = this;
        initialize();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("config", configuration);
        setResult(RESULT_OK, intent);

        if (isChanged) {
            storeSettings();
            progressDialog = ProgressDialog.show(self, getString(R.string.apply_setting), getString(R.string.please_wait));
            Thread runnable = new Thread() {
                @Override
                public void run() {
                    try {

                        Thread.sleep(1000);
                        restartApp();
                        progressDialog.dismiss();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            runnable.start();

        } else {
            super.onBackPressed();
        }
    }

    private void initialize() {
        mActionBar = getActionBar();
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        calendar = Calendar.getInstance();
        configuration = getResources().getConfiguration();
        mAccountSharedPreferences = getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE);
        mLoginSharedPreferences   = getSharedPreferences(LoginActivity.LOGIN, MODE_PRIVATE);
        mSettingSharedPreferences = getSharedPreferences(SETTING_SHARED_PREFERENCE, 0);
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
        logoutButton.setOnClickListener(this);
    }

    private void setViews () {


        account        = mLoginSharedPreferences.getString(LoginActivity.USER_NAME, "no account");//mSettingSharedPreferences.getString(ACCOUNT, "no account");
        userFrom      = mLoginSharedPreferences.getInt(LoginActivity.USER_FROM, -1);
        unitWeight    = mSettingSharedPreferences.getInt(WEIGHT_UNIT, SETTING_WEIGHT_KG);
        unitDistance  = mSettingSharedPreferences.getInt(DISTANCE_UNIT, SETTING_DISTANCE_KM);
        unitHeight    = mSettingSharedPreferences.getInt(HEIGHT_UNIT, SETTING_HEIGHT_IN);
        unitSpeedPace = mSettingSharedPreferences.getInt(SPEED_PACE_UNIT, SETTING_PACE);
        unitDegree    = mSettingSharedPreferences.getInt(DEGREE_UNIT, SETTING_DEGREE_C);
        birthDateEpoch   = mSettingSharedPreferences.getLong(BIRTH_DATE, 0);
        weight         = mSettingSharedPreferences.getString(WEIGHT, "--");
        height         = mSettingSharedPreferences.getString(HEIGHT, "--");
        autoCue  = mSettingSharedPreferences.getString(AUTO_CUE, SETTING_AUTO_CUE_5_MINUTES);
        language = mSettingSharedPreferences.getString(LANGUAGE, SETTING_LANGUAGE_ENGLISH);
        autoCueToggle = mSettingSharedPreferences.getBoolean(AUTO_CUE_TOGGLE, true);
        usedLanguage = language.toString();
        accountTextView.setText(account);

        if (userFrom == LoginActivity.FROM_FB) {
            facebookTextView.setText(R.string.connected);
        } else {
            facebookTextView.setText(R.string.disconnected);
        }

        // setting weight radio button
        switch (unitWeight) {
            case SETTING_WEIGHT_KG:
                unitWeightRadioGroup.check(R.id.setting_kg);
                weightUnit = "kg";
                break;
            case SETTING_WEIGHT_LB:
                unitWeightRadioGroup.check(R.id.setting_lb);
                weightUnit = "lb";
                break;
        }

        // setting height radio button
        switch (unitHeight) {
            case SETTING_HEIGHT_CM:
                unitHeightRadioGroup.check(R.id.setting_cm);
                heightUnit = "cm";
                break;
            case SETTING_HEIGHT_IN:
                unitHeightRadioGroup.check(R.id.setting_in);
                heightUnit = "in";
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

        // setting radio group checked change listener
        unitWeightRadioGroup.setOnCheckedChangeListener(this);
        unitHeightRadioGroup.setOnCheckedChangeListener(this);
        unitDistanceRadioGroup.setOnCheckedChangeListener(this);
        unitSpeedPaceRadioGroup.setOnCheckedChangeListener(this);
        unitDegreeRadioGroup.setOnCheckedChangeListener(this);

        // should get the weight data from db
        weightTextView.setText(weight + " " + weightUnit);
        heightTextView.setText(height + " " + heightUnit);

        weightRelativeLayout.setOnClickListener(this);
        heightRelativeLayout.setOnClickListener(this);
        birthDateRelativeLayout.setOnClickListener(this);

        autoCueArrayAdapter = ArrayAdapter.createFromResource(this, R.array.auto_cue, android.R.layout.simple_spinner_item);
        autoCueArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoCueSpinner.setAdapter(autoCueArrayAdapter);
        autoCueSpinner.setSelection(autoCueArrayAdapter.getPosition(autoCue));
        autoCueSpinner.setOnItemSelectedListener(this);

        languageArrayAdapter = ArrayAdapter.createFromResource(this, R.array.language, android.R.layout.simple_spinner_item);
        languageArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageArrayAdapter);
        languageSpinner.setSelection(languageArrayAdapter.getPosition(language));
        languageSpinner.setOnItemSelectedListener(this);

        autoCueToggleButton.setChecked(autoCueToggle);
        autoCueToggleButton.setOnCheckedChangeListener(this);

        if (birthDateEpoch != 0) {
            calendar.setTimeInMillis(birthDateEpoch);
            int year  = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day   = calendar.get(Calendar.DAY_OF_MONTH);
            birthDateTextView.setText(year + "/" + month + "/" + day);
        }
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
        Log.d(TAG, "change onclick");
        isChanged = true;
        Tracker t = ((MusicRunnerApplication) getApplication()).getTracker(MusicRunnerApplication.TrackerName.APP_TRACKER);
        t.setScreenName("SettingPage");
        switch (view.getId()) {
            case R.id.setting_weight:
                final AlertDialog.Builder setWeightDialog = new AlertDialog.Builder(this);
                setWeightDialog.setTitle("Weight");

                View setWeightEditTextLayout = inflater.inflate(R.layout.music_runner_edit_text, null);
                final EditText setWeightEditText = (EditText) setWeightEditTextLayout.findViewById(R.id.edit_text);
                setWeightEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                setWeightEditText.setHint(weightUnit);
                setWeightDialog.setView(setWeightEditTextLayout);

                setWeightDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String weight = setWeightEditText.getText().toString();
                        if (weight.length() > 0) {
                            mSettingSharedPreferences.edit().remove(WEIGHT).putString(WEIGHT, weight).apply();
                            weightTextView.setText(weight + " " + weightUnit);
                        }
                    }
                });
                setWeightDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                setWeightDialog.show();
                break;
            case R.id.setting_height:
                AlertDialog.Builder setHeightDialog = new AlertDialog.Builder(this);
                setHeightDialog.setTitle("Weight");

                View setHeightEditTextLayout = inflater.inflate(R.layout.music_runner_edit_text, null);
                final EditText setHeightEditText = (EditText) setHeightEditTextLayout.findViewById(R.id.edit_text);
                setHeightEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                setHeightEditText.setHint(heightUnit);
                setHeightDialog.setView(setHeightEditTextLayout);

                setHeightDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String height = setHeightEditText.getText().toString();
                        if (height.length() > 0) {
                            mSettingSharedPreferences.edit().remove(HEIGHT).putString(HEIGHT, height).apply();
                            heightTextView.setText(height + " " + heightUnit);
                        }
                    }
                });
                setHeightDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                setHeightDialog.show();
                break;
            case R.id.setting_birth_date:
                Long birthDateEpoch = mSettingSharedPreferences.getLong(BIRTH_DATE, 0);
                int year = 2000;
                int month = 0;
                int day = 1;
                if (birthDateEpoch > 0) {
                    calendar.setTimeInMillis(birthDateEpoch);
                    year  = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                    day   = calendar.get(Calendar.DAY_OF_MONTH);
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, year, month, day);
                datePickerDialog.show();
                break;
            case R.id.logout:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle(R.string.logout_statement)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    })
                    .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferencesUtility.clearSharedPreference(self);
                            mAccountSharedPreferences.edit().remove(Constant.ACCOUNT_PARAMS).commit();
                            mLoginSharedPreferences.edit().remove(LoginActivity.STATUS).putInt(LoginActivity.STATUS, LoginActivity.STATUS_NONE).commit();
                            mLoginSharedPreferences.edit().remove(LoginActivity.USER_NAME).remove(LoginActivity.USER_ID).remove(LoginActivity.USER_FROM).commit();
                            restartApp();
                        }
                    }).show();

                t.send(new HitBuilders.EventBuilder()
                        .setCategory("Logout")
                        .setAction("LogoutButton")
                        .build());
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
        Log.d(TAG, "id:" + id + " adapter id:" + adapterView.getId() + " view id:" + view.getId() + " resource id:" + R.string._5minutes);
        switch (adapterView.getId()){
            case R.id.auto_cue_spinner:
                mSettingSharedPreferences.edit().remove(AUTO_CUE).putString(AUTO_CUE, (adapterView.getItemAtPosition(pos)).toString()).apply();
                break;
            case R.id.language_spinner:
                mSettingSharedPreferences.edit().remove(LANGUAGE).putString(LANGUAGE, (adapterView.getItemAtPosition(pos)).toString()).apply();
                if ((adapterView.getItemAtPosition(pos)).toString().equals("中文")) {
                    configuration.setLocale(Locale.TRADITIONAL_CHINESE);
                    getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
                } else if ((adapterView.getItemAtPosition(pos)).toString().equals("English")) {
                    configuration.setLocale(Locale.ENGLISH);
                } else if ((adapterView.getItemAtPosition(pos)).toString().equals("日本語")) {
                    configuration.setLocale(Locale.JAPANESE);
                }

                if (!isChanged && !usedLanguage.equals(adapterView.getItemAtPosition(pos).toString())){
                    isChanged = true;
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Log.d(TAG, "change onCheckedChanged");
        isChanged = true;
        mSettingSharedPreferences.edit().remove(AUTO_CUE_TOGGLE).putBoolean(AUTO_CUE_TOGGLE, compoundButton.isChecked()).apply();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if (datePicker.getSpinnersShown()) {
            calendar.set(year, month, day);
            mSettingSharedPreferences.edit().remove(BIRTH_DATE).putLong(BIRTH_DATE, calendar.getTimeInMillis()).apply();
            month++;
            birthDateTextView.setText(year + "/" + month + "/" + day);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        int id = radioGroup.getCheckedRadioButtonId();
        Log.d(TAG, "change onCheckedChanged radio");
        isChanged = true;
        switch (radioGroup.getId()) {
            case R.id.unit_weight:
                String weightString = mSettingSharedPreferences.getString(WEIGHT, "0");
                Double weight = Double.parseDouble(weightString);
                if (id == R.id.setting_kg) {
                    mSettingSharedPreferences.edit().remove(WEIGHT_UNIT).putInt(WEIGHT_UNIT, SETTING_WEIGHT_KG).apply();
                    weightUnit = "kg";
                    if (weight > 0) {
                        weight = UnitConverter.getKGFromLB(weight);
                        weightString = StringLib.truncateDoubleString(weight.toString(), 2);
                        weightTextView.setText(weightString + " " + weightUnit);
                        mSettingSharedPreferences.edit().remove(WEIGHT).putString(WEIGHT, weightString).apply();
                    } else {
                        weightTextView.setText("-- " + weightUnit);
                    }
                } else {
                    weightUnit = "lb";
                    mSettingSharedPreferences.edit().remove(WEIGHT_UNIT).putInt(WEIGHT_UNIT, SETTING_WEIGHT_LB).apply();
                    if (weight > 0) {
                        weight = UnitConverter.getLBFromKG(weight);
                        weightString = StringLib.truncateDoubleString(weight.toString(), 2);
                        weightTextView.setText(weightString + " " + weightUnit);
                        mSettingSharedPreferences.edit().remove(WEIGHT).putString(WEIGHT, weightString).apply();
                    } else {
                        weightTextView.setText("-- " + weightUnit);
                    }
                }
                break;
            case R.id.unit_height:
                String heightString = mSettingSharedPreferences.getString(HEIGHT, "0");
                Double height = Double.parseDouble(heightString);
                if (id == R.id.setting_cm) {
                    mSettingSharedPreferences.edit().remove(HEIGHT_UNIT).putInt(HEIGHT_UNIT, SETTING_HEIGHT_CM).apply();
                    heightUnit = "cm";
                    if (height > 0) {
                        height = UnitConverter.getCMFromIN(height);
                        heightString = StringLib.truncateDoubleString(height.toString(), 2);
                        heightTextView.setText(heightString + " " + heightUnit);
                        mSettingSharedPreferences.edit().remove(HEIGHT).putString(HEIGHT, heightString).apply();
                    } else {
                        heightTextView.setText("-- " + heightUnit);
                    }
                } else {
                    mSettingSharedPreferences.edit().remove(HEIGHT_UNIT).putInt(HEIGHT_UNIT, SETTING_HEIGHT_IN).apply();
                    heightUnit = "in";
                    if (height > 0) {
                        height = UnitConverter.getINFromCM(height);
                        heightString = StringLib.truncateDoubleString(height.toString(), 2);
                        heightTextView.setText(heightString + " " + heightUnit);
                        mSettingSharedPreferences.edit().remove(HEIGHT).putString(HEIGHT, heightString).apply();
                    } else {
                        heightTextView.setText("-- " + heightUnit);
                    }
                }
                break;
            case R.id.unit_distance:
                if (id == R.id.setting_km) {
                    mSettingSharedPreferences.edit().remove(DISTANCE_UNIT).putInt(DISTANCE_UNIT, SETTING_DISTANCE_KM).apply();
                } else {
                    mSettingSharedPreferences.edit().remove(DISTANCE_UNIT).putInt(DISTANCE_UNIT, SETTING_DISTANCE_MI).apply();
                }
                break;
            case R.id.unit_pace_speed:
                if (id == R.id.setting_pace) {
                    mSettingSharedPreferences.edit().remove(SPEED_PACE_UNIT).putInt(SPEED_PACE_UNIT, SETTING_PACE).apply();
                } else {
                    mSettingSharedPreferences.edit().remove(SPEED_PACE_UNIT).putInt(SPEED_PACE_UNIT, SETTING_SPEED).apply();
                }
                break;
            case R.id.unit_degree:
                if (id == R.id.setting_celsius) {
                    mSettingSharedPreferences.edit().remove(DEGREE_UNIT).putInt(DEGREE_UNIT, SETTING_DEGREE_C).apply();
                } else {
                    mSettingSharedPreferences.edit().remove(DEGREE_UNIT).putInt(DEGREE_UNIT, SETTING_DEGREE_F).apply();
                }
                break;
        }
    }

    private void restartApp () {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(
                getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void storeSettings(){
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        String userAccount = mAccountSharedPreferences.getString(Constant.ACCOUNT_PARAMS,"no account");
        pairs.add(new BasicNameValuePair("userAccount",userAccount));
        pairs.add(new BasicNameValuePair("unitWeight",unitWeight.toString()));
        pairs.add(new BasicNameValuePair("unitDistance",unitDistance.toString()));
        pairs.add(new BasicNameValuePair("unitHeight",unitHeight.toString()));
        pairs.add(new BasicNameValuePair("unitSpeedPace",unitSpeedPace.toString()));
        pairs.add(new BasicNameValuePair("unitDegree",unitDegree.toString()));
        pairs.add(new BasicNameValuePair("birthday",birthDateEpoch.toString()));
        pairs.add(new BasicNameValuePair("weightValue",weight.equals("--") ? "0":weight));
        pairs.add(new BasicNameValuePair("heightValue",height.equals("--") ? "0" : height));
        pairs.add(new BasicNameValuePair("autoCue",autoCue.toString()));
        pairs.add(new BasicNameValuePair("autoCueToggle",autoCueToggle == true ? "1":"0"));
        try {
            pairs.add(new BasicNameValuePair("language", URLEncoder.encode(language.toString(), "utf8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpResponse response = RestfulUtility.restfulPostRequest(RestfulUtility.UPDATE_SETTINGS, pairs);
    }
}
