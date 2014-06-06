package com.amk2.musicrunner.main;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.MusicTrackMetaData;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.MusicTrackMetaData.MusicTrackCommonDataDB;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;

/**
 * Main activity of MusicRunner+
 *
 * @author DannyLin
 */
public class MusicRunnerActivity extends Activity {

	private UIController mUIController;
    public static final String ACCOUNT_TYPE = "com.amk2";
    public static final String ACCOUNT = "dummyaccount";
    public static Account mAccount;

    private ContentResolver mContentResolver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_runner);
		initialize();
        initializeSyncJobs();

		mUIController.onActivityCreate(savedInstanceState);
	}

	private void initialize() {
		mUIController = new UIController(this);
	}

    private void initializeSyncJobs() {
        mAccount = CreateSyncAccount(this);
        mContentResolver = getContentResolver();
        mContentResolver.setSyncAutomatically(mAccount, MusicTrackMetaData.AUTHORITY, true);

        Bundle bundle1 = new Bundle();
        bundle1.putString(Constant.SYNC_UPDATE, Constant.UPDATE_WEATHER);
        ContentResolver.addPeriodicSync(mAccount, MusicTrackMetaData.AUTHORITY, bundle1, Constant.ONE_MINUTE);
        Bundle bundle2 = new Bundle();
        bundle2.putString(Constant.SYNC_UPDATE, Constant.UPDATE_UBIKE);
        ContentResolver.addPeriodicSync(mAccount, MusicTrackMetaData.AUTHORITY, bundle2, Constant.ONE_MINUTE);
    }

    private static Account CreateSyncAccount(Context context) {
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            return newAccount;
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            return newAccount;
        }
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
	protected void onDestroy() {
		super.onDestroy();
		mUIController.onActivityDestroy();
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

}
