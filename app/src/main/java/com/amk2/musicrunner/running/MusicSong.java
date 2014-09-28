package com.amk2.musicrunner.running;

public class MusicSong {

	public long mId;
	public String mTitle;
	public String mArtist;
    public Integer mDuration;
	public boolean mIsPlaying;

	public MusicSong(long id, String title, String artist) {
		mId = id;
		mTitle = title;
		mArtist = artist;
        mDuration = 0;
		mIsPlaying = false;
	}

    public MusicSong(long id, String title, String artist, Integer duration) {
        mId = id;
        mTitle = title;
        mArtist = artist;
        mDuration = duration;
        mIsPlaying = false;
    }

}
