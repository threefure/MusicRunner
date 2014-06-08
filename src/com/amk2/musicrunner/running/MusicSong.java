package com.amk2.musicrunner.running;

public class MusicSong {

	public long mId;
	public String mTitle;
	public String mArtist;
	public boolean mIsPlaying;

	public MusicSong(long id, String title, String artist) {
		mId = id;
		mTitle = title;
		mArtist = artist;
		mIsPlaying = false;
	}

}
