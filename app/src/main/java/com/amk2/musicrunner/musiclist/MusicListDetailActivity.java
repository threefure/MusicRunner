package com.amk2.musicrunner.musiclist;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ktlee on 9/29/14.
 */
public class MusicListDetailActivity extends Activity {
    private static final String TAG = "MusicListDetailActivity";
    public static final String PLAYLIST_MEMBER_ID = "playlist_member_id";

    private Long playlistId;
    private Uri playlistUri;
    private Uri playlistMemberUri;
    private ContentResolver mContentResolver;
    private MediaMetadataRetriever retriever;
    private LayoutInflater inflater;

    private ActionBar mActionBar;
    private TextView playlistTitleTextView;
    private TextView distanceTextView;
    private TextView tracksTextView;
    private TextView durationTextView;
    private TextView caloriesTextView;

    private LinearLayout songContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list_detail);
        Intent intent = getIntent();
        init();
        initActionBar();
        initViews();
        setViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void init () {
        Intent intent = getIntent();
        playlistId = intent.getExtras().getLong(PLAYLIST_MEMBER_ID);
        playlistUri = MusicLib.getPlaylistUriFromId(playlistId);
        playlistMemberUri = MusicLib.getPlaylistMemberUriFromId(playlistId);
        mContentResolver = getContentResolver();
        retriever = new MediaMetadataRetriever();
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBar = getActionBar();
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
        songContainer         = (LinearLayout) findViewById(R.id.song_container);
    }

    private void setViews () {
        Handler handler = new Handler(Looper.getMainLooper());
        /*Thread loader = new Thread() {
            public void run () {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new SongLoader(getApplicationContext()));
            }
        };
        loader.start();*/
        //handler.post(new SongLoader(getApplicationContext()));
        SongLoader loader = new SongLoader(getApplicationContext());
        Thread loaderThread = new Thread(loader);
        loaderThread.start();
        /*Integer tracks = 0, duration = 0;
        Double calories = 0.0;
        Long audio_id;
        Uri musicUri;
        String filePath, durationString, playlistTitle = "Init title";
        String[] projection = {
                MediaStore.Audio.Playlists.Members.AUDIO_ID
        };
        Cursor cursor = mContentResolver.query(playlistMemberUri, projection, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                audio_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));
                musicUri = ContentUris.withAppendedId(MusicLib.getMusicUri(), audio_id);
                filePath = MusicLib.getMusicFilePath(getApplicationContext(), musicUri);
                retriever.setDataSource(getApplicationContext(), musicUri);
                tracks ++;
                duration += Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                addSongToList(filePath);
            }
        }
        cursor.close();
        String[] playlistProjection = {
                MediaStore.Audio.Playlists.NAME
        };
        cursor = mContentResolver.query(playlistUri, playlistProjection, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            playlistTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
        }
        cursor.close();
        playlistTitleTextView.setText(StringLib.truncate(playlistTitle, 20));
        tracksTextView.setText(tracks.toString());
        durationString = TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(duration/1000));
        durationTextView.setText(durationString);
        calories = calculateCalories(duration/1000, 325.0*tracks);
        caloriesTextView.setText(StringLib.truncateDoubleString(calories.toString(), 2));*/
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

    private Double calculateCalories (int timeInSec, Double distanceInMeter) {
        if (distanceInMeter == 0.0) {
            return 0.0;
        }
        double mins  = (double) timeInSec / 60;
        double hours = (double) timeInSec / 3600;
        double per400meters = distanceInMeter / 400;
        double speed = mins / per400meters;
        double K = 30 / speed;
        double calories = 70*hours*K;

        return calories;
    }

    private static final int ADD_MUSIC = 1;
    private static final int UPDATE_INFO = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage (Message message) {
            switch (message.what) {
                case ADD_MUSIC:
                    MusicMetaData musicMetaData = (MusicMetaData) message.obj;
                    addSongToList(musicMetaData);
                    break;
            }
        }
    };

    public class SongLoader implements Runnable{
        Context context;
        ContentResolver contentResolver;

        public SongLoader (Context c) {
            context = c;
            contentResolver = context.getContentResolver();
        }
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Integer tracks = 0, duration = 0;
            Double calories = 0.0, bpm;
            Long audio_id;
            Uri musicUri;
            String title, artist, filePath, durationString, playlistTitle = "Init title";
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
                    tracks ++;
                    duration += Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    HashMap<String, String> songInfo = MusicLib.getSongInfo(getApplicationContext(), title);
                    Bitmap albumPhoto = MusicLib.getMusicAlbumArt(filePath);
                    bpm = Double.parseDouble(songInfo.get(MusicLib.BPM));
                    MusicMetaData metaData = new MusicMetaData(title, artist, bpm, albumPhoto);
                    Message completeMessage = handler.obtainMessage(ADD_MUSIC, metaData);
                    completeMessage.sendToTarget();
                }
            }
            cursor.close();
            String[] playlistProjection = {
                    MediaStore.Audio.Playlists.NAME
            };
            cursor = contentResolver.query(playlistUri, playlistProjection, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                playlistTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
            }
            cursor.close();

            /*
            playlistTitleTextView.setText(StringLib.truncate(playlistTitle, 20));
            tracksTextView.setText(tracks.toString());
            durationString = TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(duration/1000));
            durationTextView.setText(durationString);
            calories = calculateCalories(duration/1000, 325.0*tracks);
            caloriesTextView.setText(StringLib.truncateDoubleString(calories.toString(), 2));*/
        }
    }
}
