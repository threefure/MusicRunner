package com.amk2.musicrunner;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by daz on 2014/5/29.
 */
public class MusicTrackMetaData {
    public static final String AUTHORITY = "com.amk2.musicrunner.provider";

    public MusicTrackMetaData(){}

    public static abstract class MusicTrackCommonDataDB implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/CommonData");
        public static final String TABLE_NAME = "CommonData";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_DATA_TYPE = "type";
        public static final String COLUMN_NAME_EXPIRATION_DATE = "expirationDate";
        public static final String COLUMN_NAME_JSON_CONTENT = "jsonContent";
    }
}
