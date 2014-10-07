package com.amk2.musicrunner.musiclist;

import android.graphics.Bitmap;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.views.MusicRunnerLineMapView;

/**
 * Created by ktlee on 10/4/14.
 */
public class MusicMetaData {

    public String mTitle, mArtist, mType;
    Integer typeContainerBackgroundId, mDuration;
    public Double mBpm;
    public Bitmap mAlbumPhoto;
    public MusicMetaData (String title, String artist, Integer duration, Double bpm, Bitmap albumPhoto) {
        mTitle = title;
        mArtist = artist;
        mDuration = duration;
        mBpm = bpm;
        if (bpm < 110.0) {
            mType = "Slow";
            typeContainerBackgroundId = R.drawable.music_runner_grass_round_border;
        } else if (bpm >= 110.0 && bpm < 130) {
            mType = "Medium";
            typeContainerBackgroundId = R.drawable.music_runner_blue_round_border;
        } else {
            mType = "Fast";
            typeContainerBackgroundId = R.drawable.music_runner_red_round_border;
        }
        mAlbumPhoto = albumPhoto;
    }
}
