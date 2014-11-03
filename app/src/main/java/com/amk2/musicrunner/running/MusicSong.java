package com.amk2.musicrunner.running;

public class MusicSong {

	public long mId;
	public String mTitle;
	public String mArtist;
    public Integer mDuration;
    public Double mBpm;
	public boolean mIsPlaying;

	public MusicSong(long id, String title, String artist) {
		mId = id;
		mTitle = title;
		mArtist = artist;
        mDuration = 0;
		mIsPlaying = false;
        mBpm = -1.0;
	}

    public MusicSong(long id, String title, String artist, Integer duration) {
        mId = id;
        mTitle = title;
        mArtist = artist;
        mDuration = duration;
        mIsPlaying = false;
        mBpm = -1.0;
    }

    public MusicSong(long id, String title, String artist, Integer duration, Double bpm) {
        mId = id;
        mTitle = title;
        mArtist = artist;
        mDuration = duration;
        mIsPlaying = false;
        mBpm = bpm;
    }

}
