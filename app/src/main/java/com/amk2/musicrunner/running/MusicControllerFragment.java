package com.amk2.musicrunner.running;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.MusicService.MusicBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MusicControllerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

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

    public interface OnChangeSongListener {
        void onChangeMusicSong(MusicRecord previousRecord);
    }
    private OnChangeSongListener mOnChangeSongListener;

    private View mMusicController;
    private View mMusicInfoContainer;
    private View mMusicControlContainer;
    private TextView mEmptyMusicText;
    private TextView mMusicTitle;
    private TextView mMusicArtist;
    private ImageView mMusicAlbumArt;
    private ImageView mPreviousButton;
    private ImageView mNextButton;
    private ImageView mPlayPauseButton;
    private ImageView mShuffleButton;

    private Activity mActivity;
    private View mFragmentView;
    private MusicService mMusicService;
    private Intent mPlayIntent;
    private int mCurrentMusicIndex = 0;
    private ArrayList<MusicSong> mMusicSongList;

    // Record music information during running.
    private MusicSong mPreviousSong;

    public void setOnChangeSongListener(OnChangeSongListener listener) {
        mOnChangeSongListener = listener;
    }

    private ServiceConnection mMusicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(mMusicSongList.isEmpty()) {
                showEmptyMusicView();
            } else {
                startPlayMusic(service);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBindToService = false;
        }
    };

    private void showEmptyMusicView() {
        mMusicController.setVisibility(View.GONE);
        mEmptyMusicText.setVisibility(View.VISIBLE);
    }

    private void startPlayMusic(IBinder service) {
        mMusicController.setVisibility(View.VISIBLE);
        mEmptyMusicText.setVisibility(View.GONE);
        //Log.d("danny", "Service connection in MusicControllerFragment");
        mIsBindToService = true;
        MusicBinder binder = (MusicBinder) service;
        mMusicService = binder.getService();
        mMusicService.setOnPlayingSongCompletionListener(new MusicService.OnPlayingSongCompletionListener() {
            @Override
            public void onPlayingSongCompletion(int duration) {
                setMusicText();
                setMusicAlbumArt();
                mOnChangeSongListener.onChangeMusicSong(new MusicRecord(mPreviousSong,duration));
                //Toast.makeText(mActivity, "Music duration = " + mMusicService.getMusicPositionWhenChangeSong(), Toast.LENGTH_SHORT).show();
            }
        });
        if (!mMusicService.isMusicPlayerStartRunning()) {
            mMusicService.setMusicList(mMusicSongList);
            mMusicService.setSong(mCurrentMusicIndex);
            mMusicService.playSong();
            mPreviousSong = mMusicService.getPlayingSong();
        }
        mCurrentMusicIndex = mMusicService.getCurrentSongIndex();
        setMusicText();
        setMusicAlbumArt();
        setPlayPauseIcon();
        setAllViewsVisible();
    }

    private void setMusicText() {
        String artist = mMusicService.getPlayingSong().mArtist;
        String title = mMusicService.getPlayingSong().mTitle;
        if(TextUtils.isEmpty(artist)) {
            artist = getString(R.string.empty_music_artist);
        }
        mMusicArtist.setText(artist);
        mMusicTitle.setText(title);
    }

    private void setAllViewsVisible() {
        mMusicInfoContainer.setVisibility(View.VISIBLE);
        mMusicControlContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_controller_fragment, container, false);
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
        mMusicController = mFragmentView.findViewById(R.id.music_controller);
        mMusicInfoContainer = mFragmentView.findViewById(R.id.music_info_container);
        mMusicControlContainer = mFragmentView.findViewById(R.id.music_control_container);
        mEmptyMusicText = (TextView) mFragmentView.findViewById(R.id.empty_music_text);

        // Music information
        mMusicAlbumArt = (ImageView) mFragmentView.findViewById(R.id.music_album_art);
        mMusicArtist = (TextView) mFragmentView.findViewById(R.id.music_artist);
        mMusicTitle = (TextView) mFragmentView.findViewById(R.id.music_title);

        // Music control buttons
        mShuffleButton = (ImageView) mFragmentView.findViewById(R.id.music_shuffle);
        mPreviousButton = (ImageView) mFragmentView.findViewById(R.id.previous_button);
        mNextButton = (ImageView) mFragmentView.findViewById(R.id.next_button);
        mPlayPauseButton = (ImageView) mFragmentView.findViewById(R.id.play_pause_button);
        mShuffleButton.setOnClickListener(this);
        mPreviousButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mPlayPauseButton.setOnClickListener(this);
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
                long id = data.getLong(MUSIC_ID);
                String title = data.getString(MUSIC_TITLE);
                String artist = data.getString(MUSIC_ARTIST);
                Uri musicUri = ContentUris.withAppendedId(MUSIC_URI,id);
                String musicFilePath = getMusicFilePath(musicUri);
                if(isMusicFile(musicFilePath)) {
                    //Log.d("danny","Add music file path = " + musicFilePath);
                    songList.add(new MusicSong(id, title, artist));
                }
            }
        }
        return songList;
    }

    private boolean isMusicFile(String filePath) {
        if (!TextUtils.isEmpty(filePath) && (filePath.endsWith("mp3") || filePath.endsWith("MP3"))) {
            return true;
        } else {
            return false;
        }
    }

    private String getMusicFilePath(Uri uri) {
        String filePath = null;
        if(uri != null) {
            Cursor cursor = null;
            try {
                cursor = mActivity.getContentResolver().query(uri, new String[]{
                        MediaStore.Audio.AudioColumns.DATA
                }, null, null, null);
                cursor.moveToFirst();
                filePath = cursor.getString(0);

                if(filePath == null || filePath.trim().isEmpty()) {
                    filePath = uri.toString();
                }
            } catch(Exception e) {
                e.printStackTrace();
                filePath = uri.toString();
            } finally {
                if(cursor != null) {
                    cursor.close();
                }
            }
        } else {
            filePath = uri.toString();
        }

        return filePath;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.music_shuffle:
                setShuffleIcon();
                break;
            case R.id.previous_button:
                //Toast.makeText(mActivity, "Music duration = " + mMusicService.getMusicPositionWhenChangeSong(), Toast.LENGTH_SHORT).show();
                mOnChangeSongListener.onChangeMusicSong(new MusicRecord(mPreviousSong,mMusicService.getMusicPositionWhenChangeSong()));
                mMusicService.playPreviousSong();
                setMusicText();
                setMusicAlbumArt();
                setPlayPauseIcon();
                mPreviousSong = mMusicService.getPlayingSong();
                break;
            case R.id.next_button:
                //Toast.makeText(mActivity, "Music duration = " + mMusicService.getMusicPositionWhenChangeSong(), Toast.LENGTH_SHORT).show();
                mOnChangeSongListener.onChangeMusicSong(new MusicRecord(mPreviousSong,mMusicService.getMusicPositionWhenChangeSong()));
                mMusicService.playNextSong();
                setMusicText();
                setMusicAlbumArt();
                setPlayPauseIcon();
                mPreviousSong = mMusicService.getPlayingSong();
                break;
            case R.id.play_pause_button:
                if(mMusicService.isPlaying()) {
                    mMusicService.pausePlayer();
                } else {
                    mMusicService.playPlayer();
                }
                setPlayPauseIcon();
                break;
        }
    }

    private void setMusicAlbumArt() {
        long id = mMusicService.getPlayingSong().mId;
        Uri musicUri = ContentUris.withAppendedId(MUSIC_URI,id);
        String musicFilePath = getMusicFilePath(musicUri);
        mMusicAlbumArt.setImageBitmap(getMusicAlbumArt(musicFilePath));
    }

    private Bitmap getMusicAlbumArt(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            Log.d("danny","setDataSource before");
            retriever.setDataSource(filePath);
            Log.d("danny","setDataSource after");
            byte[] data = retriever.getEmbeddedPicture();
            Log.d("danny","Data size = " + data.length);
            if (data != null) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
            if (bitmap == null) {
                Log.d("danny","Bitmap is null");
                return null;
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } catch(NoSuchMethodError ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    private void setShuffleIcon() {
        mMusicService.setShuffle();
        if(mMusicService.isShuffle()) {
            mShuffleButton.setImageResource(R.drawable.shuffle);
        } else {
            mShuffleButton.setImageResource(R.drawable.unshuffle);
        }
    }

    private void setPlayPauseIcon() {
        if(mMusicService.isPlaying()) {
            mPlayPauseButton.setImageResource(R.drawable.pause);
        } else {
            mPlayPauseButton.setImageResource(R.drawable.play);
        }
    }

    public MusicSong getCurrentMusic() {
        return mMusicService.getPlayingSong();
    }

    /**
     * This function will send the record of the last song when user stops running.
     *
     * @return The record of the last playing song
     */
    public MusicRecord getLastMusicRecord() {
        return new MusicRecord(mPreviousSong,mMusicService.getMusicPositionWhenChangeSong());
    }
}
