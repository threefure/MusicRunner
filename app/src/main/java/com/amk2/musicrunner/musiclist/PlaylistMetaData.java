package com.amk2.musicrunner.musiclist;

import android.content.ContentUris;
import android.net.Uri;

import com.amk2.musicrunner.utilities.HealthLib;
import com.amk2.musicrunner.utilities.MusicLib;

/**
 * Created by ktlee on 10/4/14.
 */
public class PlaylistMetaData {
    public Uri mUri;
    public String mTitle;
    public Integer mDuration;
    public Integer mTracks;
    public Integer mType;
    public Double mCalories;
    public Long mId;

    public PlaylistMetaData (Uri uri, String title, Integer duration, Integer tracks) {
        mUri = uri;
        mTitle = title;
        mDuration = duration;
        mTracks = tracks;
        mId = ContentUris.parseId(uri);
        mCalories = HealthLib.calculateCalories(duration/1000, tracks.doubleValue()*500);
    }

    public void setType (int type) {
        mType = type;
    }
}
