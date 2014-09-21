package com.amk2.musicrunner.running;

import android.graphics.Bitmap;

/**
 * Record the music information during running.
 *
 * mMusicSong      : The previous song information(id, title, artist)
 * mPlayingDuration: The playing duration of the previous song in millisecond.
 * mDistance       : The running distance of the previous song.
 *
 * Created by DannyLin on 2014/6/29.
 */
public class MusicRecord {
    public MusicSong mMusicSong;
    public int mPlayingDuration;
    public int mDistance;

    public MusicRecord(MusicSong musicSong, int duration) {
        mMusicSong = musicSong;
        mPlayingDuration = duration;
        mDistance = 0;
    }
}
