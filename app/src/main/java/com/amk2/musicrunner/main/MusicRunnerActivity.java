
package com.amk2.musicrunner.main;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.services.SyncService;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.MusicService;
import com.amk2.musicrunner.running.RunningActivity;
import com.amk2.musicrunner.location.LocationHelper;
import com.google.android.gms.maps.MapsInitializer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Locale;

/**
 * Main activity of MusicRunner+
 * 
 * @author DannyLin
 */
public class MusicRunnerActivity extends Activity implements View.OnClickListener{

    private UIController mUIController;
    private ContentResolver mContentResolver;
    private SharedPreferences mSharedPreferences;
    private RelativeLayout introduction;
    private SharedPreferences userInstructionSharedPreferences;
    private boolean hasIntroduced;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_runner);
        userInstructionSharedPreferences = getSharedPreferences(Constant.USER_INSTRUCTION, MODE_PRIVATE);
        hasIntroduced = userInstructionSharedPreferences.getBoolean(Constant.START_PAGE, false);
        initialize();
        mUIController.onActivityCreate(savedInstanceState);
        goToRunningPage();
    }

    private void goToRunningPage() {
        if (isMyServiceRunning(MusicService.class)) {
            startActivity(new Intent(this,RunningActivity.class));
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initialize() {
        initViews();

        Configuration configuration = getResources().getConfiguration();
        mSharedPreferences = getSharedPreferences(SettingActivity.SETTING_SHARED_PREFERENCE, 0);
        CharSequence language = mSharedPreferences.getString(SettingActivity.LANGUAGE, SettingActivity.SETTING_LANGUAGE_ENGLISH);
        if (language.equals("中文")) {
            configuration.setLocale(Locale.TAIWAN);
        } else {
            configuration.setLocale(Locale.ENGLISH);
        }
        getResources().updateConfiguration(getResources().getConfiguration(), getResources().getDisplayMetrics());
        mUIController = new UIController(this);
    }

    private void initViews () {
        introduction = (RelativeLayout) findViewById(R.id.introduction);
        if (!hasIntroduced) {
            introduction.setVisibility(View.VISIBLE);
            introduction.setOnClickListener(this);
            hasIntroduced = true;
            userInstructionSharedPreferences.edit().remove(Constant.START_PAGE).putBoolean(Constant.START_PAGE, true).commit();
        }
    }

    private void initializeSyncJobs() {
        MusicRunnerDBMetaData.InitialAccount(this);
        mContentResolver = getContentResolver();
        mContentResolver.setSyncAutomatically(MusicRunnerDBMetaData.mAccount,
                MusicRunnerDBMetaData.AUTHORITY, true);
    }

    @Override
    public void onConfigurationChanged (Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUIController.onActivityRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUIController.onActivityResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUIController.onActivitySaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUIController.onActivityPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        goToRunningPage();
//    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, SyncService.class);
        stopService(intent);
        mUIController.onActivityDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_runner, menu);
        return false;
    }

    @Override
    public void onBackPressed() {
        mUIController.onActivityBackPressed();
    }


    @Override
    public void onActivityResult (int reqCode, int resCode, Intent data) {
        if (reqCode == UIController.REQUEST_SETTING && resCode == RESULT_OK) {
            getResources().updateConfiguration(getResources().getConfiguration(), getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.introduction:
                view.setVisibility(View.GONE);
                break;
        }
    }
}
