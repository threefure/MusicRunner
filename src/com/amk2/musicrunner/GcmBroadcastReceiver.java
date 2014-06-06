package com.amk2.musicrunner;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.amk2.musicrunner.main.MusicRunnerActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by ktlee on 5/24/14.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {

    public static final String AUTHORITY = "com.amk2.musicrunner.provider";
    public static final String ACCOUNT_TYPE = "com.amk2";
    public static final String ACCOUNT = "dummyaccount";
    public static final String KEY_SYNC_REQUEST = "com.amk2.musicrunner.KEY_SYNC_REQUEST";
    private Account mAccount = MusicRunnerActivity.mAccount;

    @Override
    public void onReceive(Context context, Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);

        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)
                &&
                intent.getBooleanExtra(KEY_SYNC_REQUEST,true)) {
            /*
             * Signal the framework to run your sync adapter. Assume that
             * app initialization has already created the account.
             */
            ContentResolver.requestSync(mAccount, AUTHORITY, null);
        } else {

        }
    }
}
