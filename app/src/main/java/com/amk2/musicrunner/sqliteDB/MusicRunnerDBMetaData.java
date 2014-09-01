package com.amk2.musicrunner.sqliteDB;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by daz on 2014/5/29.
 */
public class MusicRunnerDBMetaData {
    public static final String AUTHORITY = "com.amk2.musicrunner.provider";
    public static final String ACCOUNT = "dummyaccount";
    public static final String ACCOUNT_TYPE = "com.amk2";
    public static Account mAccount;

    public MusicRunnerDBMetaData() {}

    public static void InitialAccount (Context context) {
        mAccount = CreateSyncAccount(context);
    }

    private static Account CreateSyncAccount (Context context) {
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);

        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manife st,
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

    public static abstract class MusicTrackCommonDataDB implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/CommonData");
        public static final String TABLE_NAME                  = "CommonData";
        public static final String COLUMN_NAME_ID              = "id";
        public static final String COLUMN_NAME_DATA_TYPE       = "type";
        public static final String COLUMN_NAME_EXPIRATION_DATE = "expirationDate";
        public static final String COLUMN_NAME_JSON_CONTENT    = "jsonContent";
    }

    public static abstract class MusicRunnerRunningEventDB implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/RunningEvent");
        public static final String TABLE_NAME           = "RunningEvent";
        public static final String COLUMN_NAME_ID       = "id";
        public static final String COLUMN_NAME_DURATION = "duration";
        public static final String COLUMN_NAME_DATE_IN_MILLISECOND = "date_in_milli";
        public static final String COLUMN_NAME_CALORIES = "calories";
        public static final String COLUMN_NAME_DISTANCE = "distance";
        public static final String COLUMN_NAME_SPEED    = "speed";
        public static final String COLUMN_NAME_PHOTO_PATH  = "pic";
        public static final String COLUMN_NAME_ROUTE    = "route";
    }

    public static abstract class MusicRunnerSongPerformanceDB implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/SongPerformance");
        public static final String TABLE_NAME           = "SongPerformance";
        public static final String COLUMN_NAME_ID       = "id";
        public static final String COLUMN_NAME_SONG_ID  = "song_id";
        public static final String COLUMN_NAME_EVENT_ID = "event_id";
        public static final String COLUMN_NAME_DURATION = "duration";
        public static final String COLUMN_NAME_DATE_IN_MILLISECOND = "date_in_milli";
        public static final String COLUMN_NAME_CALORIES = "calories";
        public static final String COLUMN_NAME_DISTANCE = "distance";
        public static final String COLUMN_NAME_SPEED    = "speed";
    }

    public static abstract class MusicRunnerSongNameDB implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/SongName");
        public static final String TABLE_NAME           = "SongName";
        public static final String COLUMN_NAME_ID       = "id";
        public static final String COLUMN_NAME_SONG_NAME= "song_name";
    }
}
