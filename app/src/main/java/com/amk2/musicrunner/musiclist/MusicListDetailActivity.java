package com.amk2.musicrunner.musiclist;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.utilities.HealthLib;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.OnSongPreparedListener;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;

import java.util.HashMap;

/**
 * Created by ktlee on 9/29/14.
 */
public class MusicListDetailActivity extends Activity implements OnSongPreparedListener, View.OnClickListener{
    public static final int REQUEST_ADD_MUSIC_TO_PLAYLIST = 0;
    public static final String PLAYLIST_ID = "playlist_id";
    private static final String TAG = "MusicListDetailActivity";

    private static final int ADD_MUSIC = 1;
    private static final int UPDATE_INFO = 2;

    private boolean isPlaylistUpdated = false;
    private Integer tracks;
    private Integer duration;
    private Integer playlistPosition;
    private Long playlistId;
    private Uri playlistUri;
    private Uri playlistMemberUri;
    private ContentResolver mContentResolver;
    private MediaMetadataRetriever retriever;
    private LayoutInflater inflater;

    private SharedPreferences mSettingSharedPreferences;
    private Double weight;

    private ActionBar mActionBar;
    private TextView playlistTitleTextView;
    private TextView distanceTextView;
    private TextView tracksTextView;
    private TextView durationTextView;
    private TextView caloriesTextView;
    private ImageView addMusicImageView;

    private LinearLayout songContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list_detail);
        mSettingSharedPreferences = getSharedPreferences(SettingActivity.SETTING_SHARED_PREFERENCE, 0);
        weight = Double.parseDouble(mSettingSharedPreferences.getString(SettingActivity.WEIGHT, "50"));
        HealthLib.setWeight(weight);

        init();
        initActionBar();
        initViews();
        setViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy () {
        if (isPlaylistUpdated) {
            Intent intent = new Intent(MusicListFragment.UPDATE_PLAYLIST);
            intent.putExtra(PLAYLIST_ID, playlistId);
            intent.putExtra(MusicListFragment.PLAYLIST_POSITION, playlistPosition);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        super.onDestroy();
    }

    private void init () {
        Intent intent = getIntent();
        playlistId        = intent.getExtras().getLong(PLAYLIST_ID);
        playlistPosition  = intent.getExtras().getInt(MusicListFragment.PLAYLIST_POSITION);
        playlistUri       = MusicLib.getPlaylistUriFromId(playlistId);
        playlistMemberUri = MusicLib.getPlaylistMemberUriFromId(playlistId);
        mContentResolver  = getContentResolver();
        retriever         = new MediaMetadataRetriever();
        inflater          = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBar        = getActionBar();
        tracks = 0;
        duration = 0;
    }

    private void initActionBar() {
        View actionBarView = View.inflate(mActionBar.getThemedContext(), R.layout.customized_action_bar, null);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(Gravity.CENTER));
    }

    private void initViews () {
        playlistTitleTextView = (TextView) findViewById(R.id.playlist_title);
        distanceTextView      = (TextView) findViewById(R.id.distance);
        tracksTextView        = (TextView) findViewById(R.id.tracks);
        durationTextView      = (TextView) findViewById(R.id.duration);
        caloriesTextView      = (TextView) findViewById(R.id.calories);
        addMusicImageView     = (ImageView) findViewById(R.id.add_music);
        songContainer         = (LinearLayout) findViewById(R.id.song_container);
    }

    private void setViews () {
        //create another thread to load music in the background
        PlaylistLoaderRunnable loader = new PlaylistLoaderRunnable(this);
        Thread loaderThread = new Thread(loader);
        loaderThread.start();

        // set playlist title
        String[] playlistProjection = {
                MediaStore.Audio.Playlists.NAME
        };
        String playlistTitle = "";
        Cursor cursor = mContentResolver.query(playlistUri, playlistProjection, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            playlistTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
        }
        cursor.close();
        playlistTitleTextView.setText(StringLib.truncate(playlistTitle, 20));
        addMusicImageView.setOnClickListener(this);
    }

    private void addSongToList (String filePath) {
        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String type;
        Integer typeContainerBackgroundId;
        Double bpm;
        HashMap<String, String> songInfo = MusicLib.getSongInfo(getApplicationContext(), title);
        Bitmap albumPhoto = MusicLib.getMusicAlbumArt(filePath);
        bpm = Double.parseDouble(songInfo.get(MusicLib.BPM));


        View musicListItemTemplate = inflater.inflate(R.layout.music_list_item_template, null);
        TextView titleTextView  = (TextView) musicListItemTemplate.findViewById(R.id.title);
        TextView artistTextView = (TextView) musicListItemTemplate.findViewById(R.id.artist);
        TextView typeTextView   = (TextView) musicListItemTemplate.findViewById(R.id.type);
        ImageView albumPhotoImageView = (ImageView) musicListItemTemplate.findViewById(R.id.album_photo);
        LinearLayout typeContainer = (LinearLayout) musicListItemTemplate.findViewById(R.id.type_container);

        titleTextView.setText(StringLib.truncate(title, 18));
        artistTextView.setText(StringLib.truncate(artist, 18));

        if (bpm < 110.0) {
            type = "Slow";
            typeContainerBackgroundId = R.drawable.music_runner_grass_round_border;
        } else if (bpm >= 110.0 && bpm < 130) {
            type = "Medium";
            typeContainerBackgroundId = R.drawable.music_runner_blue_round_border;
        } else {
            type = "Fast";
            typeContainerBackgroundId = R.drawable.music_runner_red_round_border;
        }
        typeTextView.setText(type);
        typeContainer.setBackground(getResources().getDrawable(typeContainerBackgroundId));
        if (albumPhoto != null) {
            albumPhotoImageView.setImageBitmap(albumPhoto);
        }

        songContainer.addView(musicListItemTemplate);
    }

    private void addSongToList (MusicMetaData musicMetaData) {
        View musicListItemTemplate = inflater.inflate(R.layout.music_list_item_template, null);
        TextView titleTextView  = (TextView) musicListItemTemplate.findViewById(R.id.title);
        TextView artistTextView = (TextView) musicListItemTemplate.findViewById(R.id.artist);
        TextView typeTextView   = (TextView) musicListItemTemplate.findViewById(R.id.type);
        ImageView albumPhotoImageView = (ImageView) musicListItemTemplate.findViewById(R.id.album_photo);
        LinearLayout typeContainer = (LinearLayout) musicListItemTemplate.findViewById(R.id.type_container);

        titleTextView.setText(StringLib.truncate(musicMetaData.mTitle, 18));
        artistTextView.setText(StringLib.truncate(musicMetaData.mArtist, 18));
        typeTextView.setText(musicMetaData.mType);
        typeContainer.setBackground(getResources().getDrawable(musicMetaData.typeContainerBackgroundId));
        if (musicMetaData.mAlbumPhoto != null) {
            albumPhotoImageView.setImageBitmap(musicMetaData.mAlbumPhoto);
        }
        songContainer.addView(musicListItemTemplate);
    }

    private void updateSummary () {
        String durationString;
        Double calories;
        tracksTextView.setText(tracks.toString());
        durationString = TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(duration / 1000));
        durationTextView.setText(durationString);
        calories = HealthLib.calculateCalories(duration / 1000, 325.0 * tracks, weight);
        caloriesTextView.setText(StringLib.truncateDoubleString(calories.toString(), 2));
    }

    private Handler mPlaylistUiHandler = new Handler();

    @Override
    public void OnSongPrepared(MusicMetaData musicMetaData) {
        mPlaylistUiHandler.post(new AddSongRunnable(musicMetaData));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_music:
                Intent intent = new Intent(this, MusicRunnerSongSelectorActivity.class);
                intent.putExtra(MusicRunnerSongSelectorActivity.PLAYLIST_ID, playlistId);
                startActivityForResult(intent, REQUEST_ADD_MUSIC_TO_PLAYLIST);
                break;
        }
    }

    @Override
    public void onActivityResult (int reqCode, int resCode, Intent data) {
        if (reqCode == REQUEST_ADD_MUSIC_TO_PLAYLIST && resCode == RESULT_OK) {
            isPlaylistUpdated = true;
            songContainer.removeAllViews();
            duration = 0;
            tracks = 0;
            PlaylistLoaderRunnable loader = new PlaylistLoaderRunnable(this);
            Thread loaderThread = new Thread(loader);
            loaderThread.start();
        }
    }

    static Object lock = new Object();

    private class AddSongRunnable implements Runnable {
        MusicMetaData mMusicMetaData;

        public AddSongRunnable (MusicMetaData musicMetaData) {
            mMusicMetaData = musicMetaData;
        }
        @Override
        public void run() {
            synchronized (lock) {
                addSongToList(mMusicMetaData);
                duration += mMusicMetaData.mDuration;
                tracks++;
                updateSummary();
            }
        }
    }

    public class PlaylistLoaderRunnable implements Runnable{
        Context context;
        ContentResolver contentResolver;
        OnSongPreparedListener listener;

        public PlaylistLoaderRunnable(Context c) {
            context = c;
            listener = (OnSongPreparedListener) c;
            contentResolver = context.getContentResolver();
        }
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Double bpm;
            Long audio_id;
            String title, artist, filePath;
            Integer duration;
            Bitmap albumPhoto;
            Uri musicUri;
            String[] projection = {
                    MediaStore.Audio.Playlists.Members.AUDIO_ID
            };
            Cursor cursor = contentResolver.query(playlistMemberUri, projection, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    audio_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));
                    musicUri = ContentUris.withAppendedId(MusicLib.getMusicUri(), audio_id);
                    filePath = MusicLib.getMusicFilePath(context, musicUri);

                    retriever.setDataSource(context, musicUri);
                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    HashMap<String, String> songInfo = MusicLib.getSongInfo(context, title);
                    bpm = Double.parseDouble(songInfo.get(MusicLib.BPM));
                    albumPhoto = MusicLib.getMusicAlbumArt(filePath);


                    MusicMetaData metaData = new MusicMetaData(title, artist, duration, bpm, albumPhoto);
                    listener.OnSongPrepared(metaData);
                }
            }
            cursor.close();
            Thread.interrupted();
        }
    }
}
