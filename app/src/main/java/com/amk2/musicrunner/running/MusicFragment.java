package com.amk2.musicrunner.running;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.MusicService.MusicBinder;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MusicFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final int MUSIC_LOADER_ID = 1;

    private static final Uri MUSIC_URI = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final String[] MUSIC_SELECT_PROJECTION = new String[] {
            android.provider.MediaStore.Audio.Media._ID,
            android.provider.MediaStore.Audio.Media.TITLE,
            android.provider.MediaStore.Audio.Media.ARTIST
    };
    private static final int MUSIC_ID = 0;
    private static final int MUSIC_TITLE = 1;
    private static final int MUSIC_ARTIST = 2;
    private static boolean mIsBindToService = false;

    private TextView mMusicTitle;
    private Button mPreviousButton;
    private Button mNextButton;
    private Button mPausePlayButton;

    private Activity mActivity;
    private View mFragmentView;
    private MusicService mMusicService;
    private Intent mPlayIntent;
    private int mCurrentMusicIndex = 0;
    private ArrayList<MusicSong> mMusicSongList;

    private ServiceConnection mMusicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("danny", "Service connection in MusicFragment");
            mIsBindToService = true;
            MusicBinder binder = (MusicBinder) service;
            mMusicService = binder.getService();
            if (!mMusicService.isMusicPlayerStartRunning()) {
                mMusicService.setMusicList(mMusicSongList);
                mMusicService.setSong(mCurrentMusicIndex);
                mMusicService.playSong();
            }
            mCurrentMusicIndex = mMusicService.getCurrentSongIndex();
            mMusicTitle.setText(mMusicService.getPlayingSong().mTitle);
            setPauseAndPlayContent();
            setAllViewsVisible();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBindToService = false;
        }
    };

    private void setAllViewsVisible() {
        mMusicTitle.setVisibility(View.VISIBLE);
        mPreviousButton.setVisibility(View.VISIBLE);
        mNextButton.setVisibility(View.VISIBLE);
        mPausePlayButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        mFragmentView = getView();
        initialize();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsBindToService = false;
    }

    private void initialize() {
        mMusicSongList = new ArrayList<MusicSong>();
        mPlayIntent = new Intent(mActivity, MusicService.class);
        setViews();
        getLoaderManager().initLoader(MUSIC_LOADER_ID, null, this);
    }

    private void setViews() {
        mMusicTitle = (TextView) mFragmentView.findViewById(R.id.music_title);
        mPreviousButton = (Button) mFragmentView.findViewById(R.id.previous_button);
        mNextButton = (Button) mFragmentView.findViewById(R.id.next_button);
        mPausePlayButton = (Button) mFragmentView.findViewById(R.id.pause_play_button);
        mPreviousButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mPausePlayButton.setOnClickListener(this);
    }

    private void bindToMusicService() {
        if(!mIsBindToService) {
            mActivity.bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mActivity, MUSIC_URI, MUSIC_SELECT_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMusicSongList = convertCursorToMusicSongList(data);
        Log.d("danny", "Music list size = " + mMusicSongList.size());
        // Sort music list
        Collections.sort(mMusicSongList, new Comparator<MusicSong>() {
            public int compare(MusicSong a, MusicSong b) {
                return a.mTitle.compareTo(b.mTitle);
            }
        });
        bindToMusicService();
    }

    private ArrayList<MusicSong> convertCursorToMusicSongList(Cursor data) {
        ArrayList<MusicSong> songList = new ArrayList<MusicSong>();
        if (data != null) {
            data.moveToPosition(-1);
            while (data.moveToNext()) {
                songList.add(new MusicSong(data.getLong(MUSIC_ID), data.getString(MUSIC_TITLE),
                        data.getString(MUSIC_ARTIST)));
            }
        }
        return songList;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.previous_button:
                mMusicService.playPreviousSong();
                mMusicTitle.setText(mMusicService.getPlayingSong().mTitle);
                break;
            case R.id.next_button:
                mMusicService.playNextSong();
                mMusicTitle.setText(mMusicService.getPlayingSong().mTitle);
                break;
            case R.id.pause_play_button:
                if(mMusicService.isPlaying()) {
                    mMusicService.pausePlayer();
                } else {
                    mMusicService.playPlayer();
                }
                setPauseAndPlayContent();
                break;
        }
    }

    private void setPauseAndPlayContent() {
        if(mMusicService.isPlaying()) {
            mPausePlayButton.setText(getString(R.string.pause_button_text));
        } else {
            mPausePlayButton.setText(getString(R.string.play_button_text));
        }
    }
}