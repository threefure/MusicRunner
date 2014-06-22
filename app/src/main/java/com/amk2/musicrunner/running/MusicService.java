
package com.amk2.musicrunner.running;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.main.MusicRunnerActivity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {

    private static final int NOTIFICATION_ID = 1;

    // Interface to notify main activity that the current song has been end,
    // and list view needs to highlight next song.
    public interface OnPlayingSongCompletionListener {
        void onPlayingSongCompletion();
    }

    private MediaPlayer mMusicPlayer;
    private ArrayList<MusicSong> mMusicSongList;
    private int mCurrentSongIndex = -1;
    private OnPlayingSongCompletionListener mOnPlayingSongCompletionListener;

    private boolean mIsShuffle = false;
    private boolean mIsPlaying = false;
    private Random mRand;

    private final IBinder mMusicBind = new MusicBinder();

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    private void initialize() {
        mRand = new Random();
        mMusicSongList = new ArrayList<MusicSong>();
        initializeMediaPlayer();
    }

    private void initializeMediaPlayer() {
        Log.d("danny", "initializeMediaPlayer");
        mMusicPlayer = new MediaPlayer();
        mMusicPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMusicPlayer.setOnPreparedListener(this);
        mMusicPlayer.setOnCompletionListener(this);
        mMusicPlayer.setOnErrorListener(this);
    }

    /**
     * Set OnPlayingSongCompletionListener from the object which implements this
     * interface.
     * 
     * @param listener
     */
    public void setOnPlayingSongCompletionListener(OnPlayingSongCompletionListener listener) {
        mOnPlayingSongCompletionListener = listener;
    }

    public OnPlayingSongCompletionListener getOnPlayingSongCompletionListener() {
        return mOnPlayingSongCompletionListener;
    }

    public void setMusicList(ArrayList<MusicSong> musicSongList) {
        mMusicSongList = musicSongList;
    }

    public void setSong(int songIndex) {
        mCurrentSongIndex = songIndex;
    }

    public void playSong() {
        mIsPlaying = true;
        mMusicPlayer.reset();
        if (mCurrentSongIndex == -1) {
            mCurrentSongIndex = 0;
        }
        MusicSong playSong = mMusicSongList.get(mCurrentSongIndex);
        long currSongId = playSong.mId;
        Uri songUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSongId);
        try {
            mMusicPlayer.setDataSource(getApplicationContext(), songUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        // mMusicPlayer.prepareAsync();
        try {
            mMusicPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isMusicPlayerStartRunning() {
        if (mCurrentSongIndex == -1) {
            return false;
        } else {
            return true;
        }
    }

    public void playPreviousSong() {
        if (mIsShuffle) {
            mCurrentSongIndex = getShuffleSongPosition();
        } else {
            mCurrentSongIndex--;
            if (mCurrentSongIndex < 0) {
                mCurrentSongIndex = mMusicSongList.size() - 1;
            }
        }
        playSong();
    }

    public void playNextSong() {
        if (mIsShuffle) {
            mCurrentSongIndex = getShuffleSongPosition();
        } else {
            mCurrentSongIndex++;
            if (mCurrentSongIndex >= mMusicSongList.size()) {
                mCurrentSongIndex = 0;
            }
        }
        playSong();
    }

    private int getShuffleSongPosition() {
        int nextSong = mCurrentSongIndex;
        while (nextSong == mCurrentSongIndex) {
            nextSong = mRand.nextInt(mMusicSongList.size());
        }
        return nextSong;
    }

    public void setShuffle() {
        if (mIsShuffle) {
            mIsShuffle = false;
        } else {
            mIsShuffle = true;
        }
    }

    public MusicSong getPlayingSong() {
        return mMusicSongList.get(mCurrentSongIndex);
    }

    public int getCurrentSongIndex() {
        return mCurrentSongIndex;
    }

    /**
     * Get the current position of playing song
     * 
     * @return Time in milliseconds
     */
    public int getPlayingSongPosition() {
        return mMusicPlayer.getCurrentPosition();
    }

    /**
     * Get the duration of the song
     * 
     * @return
     */
    public int getPlayingSongDuration() {
        return mMusicPlayer.getDuration();
    }

    public boolean isPlaying() {
        //return mMusicPlayer.isPlaying();
        return mIsPlaying;
    }

    public boolean isShuffle() {
        return mIsShuffle;
    }

    public void pausePlayer() {
        mIsPlaying = false;
        mMusicPlayer.pause();
    }

    public void seek(int posn) {
        mMusicPlayer.seekTo(posn);
    }

    public void playPlayer() {
        mIsPlaying = true;
        mMusicPlayer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // mMusicPlayer.stop();
        // mMusicPlayer.release();
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMusicPlayer.stop();
        mMusicPlayer.release();
        mIsPlaying = false;
        stopForeground(true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNextSong();
        // mOnPlayingSongCompletionListener.onPlayingSongCompletion();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @SuppressLint("NewApi")
    private void setNotification() {
        Intent notIntent = new Intent(this, MusicRunnerActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt).setSmallIcon(R.drawable.play)
                // .setTicker(getPlayingSong().getMusicSongTitle())
                .setOngoing(true).setContentTitle(getPlayingSong().mTitle)
                .setContentText(getPlayingSong().mArtist);
        Notification not = builder.build();

        // If we don't run this service in the foreground, the service will be
        // killed by system once we
        // kill the app in "recent apps".
        startForeground(NOTIFICATION_ID, not);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        //setNotification();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

}
