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
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.utilities.HealthLib;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.OnSongPreparedListener;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;

import java.util.ArrayList;
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
    private static final int LEFT  = 0;
    private static final int RIGHT = 1;

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

    private ArrayList<MusicMetaData> musicMetaDataArrayList;
    private PlaylistDetailAdapter mPlaylistDetailAdapter;

    private SharedPreferences mSettingSharedPreferences;
    private Double weight;

    private ActionBar mActionBar;
    private ListView mPlaylistDetailListView;
    private TextView playlistTitleTextView;
    private TextView distanceTextView;
    private TextView tracksTextView;
    private TextView durationTextView;
    private TextView caloriesTextView;
    private ImageView addMusicImageView;

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
        mPlaylistDetailListView = (ListView) findViewById(R.id.playlist_detail_list_view);
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

        // initialize list view
        musicMetaDataArrayList = new ArrayList<MusicMetaData>();
        mPlaylistDetailAdapter = new PlaylistDetailAdapter(this, R.layout.music_list_item_template, musicMetaDataArrayList);
        mPlaylistDetailListView.setAdapter(mPlaylistDetailAdapter);
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
        //mPlaylistUiHandler.post(new AddSongRunnable(musicMetaData));
    }

    @Override
    public void OnSongLoadedFinished(final ArrayList<MusicMetaData> musicMetaDatas) {
        this.musicMetaDataArrayList = musicMetaDatas;
        mPlaylistUiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < musicMetaDataArrayList.size(); i ++ ) {
                    duration += musicMetaDataArrayList.get(i).mDuration;
                }
                tracks = musicMetaDataArrayList.size();
                updateSummary();
                mPlaylistDetailAdapter.updateMusicMetaDataArrayList(musicMetaDataArrayList);
            }
        });

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
            updatePlaylist();
        }
    }

    private void updatePlaylist () {
        isPlaylistUpdated = true;
        duration = 0;
        tracks = 0;
        PlaylistLoaderRunnable loader = new PlaylistLoaderRunnable(this);
        Thread loaderThread = new Thread(loader);
        loaderThread.start();
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
            Double bpm = -3.0;
            Long audio_id;
            String title, artist, filePath;
            Integer duration;
            Bitmap albumPhoto;
            Uri musicUri;
            ArrayList<MusicMetaData> musicMetaDataArrayList = new ArrayList<MusicMetaData>();
            String[] projection = {
                    MediaStore.Audio.Playlists.Members.AUDIO_ID,
                    MediaStore.Audio.Playlists.Members.TITLE,
                    MediaStore.Audio.Playlists.Members.ARTIST,
                    MediaStore.Audio.Playlists.Members.DURATION
            };
            Cursor cursor = contentResolver.query(playlistMemberUri, projection, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    audio_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));
                    musicUri = ContentUris.withAppendedId(MusicLib.getMusicUri(), audio_id);
                    filePath = MusicLib.getMusicFilePath(context, musicUri);

                    retriever.setDataSource(context, musicUri);
                    title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE));//retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST));//retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DURATION));//Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    if (title != null) {
                        HashMap<String, String> songInfo = MusicLib.getSongInfo(context, title);
                        if (songInfo != null && songInfo.get(MusicLib.BPM) != null) {
                            bpm = Double.parseDouble(songInfo.get(MusicLib.BPM));
                        }
                        albumPhoto = MusicLib.getMusicAlbumArt(filePath);

                        MusicMetaData metaData = new MusicMetaData(title, artist, duration, bpm, albumPhoto, audio_id);
                        musicMetaDataArrayList.add(metaData);
                    }
                }
                listener.OnSongLoadedFinished(musicMetaDataArrayList);
            } else {
                listener.OnSongLoadedFinished(new ArrayList<MusicMetaData>());
            }
            cursor.close();
            Thread.interrupted();
        }
    }

    private class PlaylistDetailAdapter extends ArrayAdapter<MusicMetaData> implements View.OnTouchListener, View.OnClickListener {
        private View mSwipeView;
        private ArrayList<MusicMetaData> mMusicMetaDataArrayList;
        private boolean isMove = false, isOpened = false;
        private float originX, offset;
        private int maxOffset = -1, direction;

        public PlaylistDetailAdapter(Context context, int resource, ArrayList<MusicMetaData> musicMeatDataArrayList) {
            super(context, resource);
            mMusicMetaDataArrayList = musicMeatDataArrayList;
        }

        public void updateMusicMetaDataArrayList (ArrayList<MusicMetaData> musicMeatDataArrayList) {
            mMusicMetaDataArrayList = musicMeatDataArrayList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount () {
            return mMusicMetaDataArrayList.size();
        }

        @Override
        public MusicMetaData getItem (int i) {
            return mMusicMetaDataArrayList.get(i);
        }

        @Override
        public long getItemId (int i) {
            return -1;
        }

        @Override
        public View getView (int i, View view, ViewGroup viewGroup) {
            MusicMetaData musicMetaData = mMusicMetaDataArrayList.get(i);
            TextView songIndexNumber;
            TextView title;
            TextView artist;
            ImageView albumPhoto;
            ImageView songTempo;
            RelativeLayout songContainer;
            Button deleteButton;
            if (view == null) {
                // inflate the view
                view = inflater.inflate(R.layout.music_list_item_template, null);
                songIndexNumber = (TextView) view.findViewById(R.id.song_index_number);
                title           = (TextView) view.findViewById(R.id.title);
                artist          = (TextView) view.findViewById(R.id.artist);
                albumPhoto     = (ImageView) view.findViewById(R.id.album_photo);
                songTempo      = (ImageView) view.findViewById(R.id.song_tempo);
                songContainer  = (RelativeLayout) view.findViewById(R.id.song_container);
                deleteButton   = (Button) view.findViewById(R.id.delete);

                // setting tag for song view
                SongViewTag songViewTag = new SongViewTag(songIndexNumber, title, artist, albumPhoto, songTempo, songContainer, deleteButton);
                view.setTag(songViewTag);

                PositionAndIdViewTag positionAndIdViewTag = new PositionAndIdViewTag(i, musicMetaData.mAudioId);
                deleteButton.setTag(positionAndIdViewTag);

            } else {
                //reuse the view
                SongViewTag songViewTag = (SongViewTag) view.getTag();
                songIndexNumber = songViewTag.songIndexNumber;
                title           = songViewTag.title;
                artist          = songViewTag.artist;
                albumPhoto      = songViewTag.albumPhoto;
                songTempo       = songViewTag.songTempo;
                songContainer   = songViewTag.songContainer;
                deleteButton    = songViewTag.deleteButton;

                PositionAndIdViewTag positionAndIdViewTag = (PositionAndIdViewTag) deleteButton.getTag();
                positionAndIdViewTag.audioId = musicMetaData.mAudioId;
                positionAndIdViewTag.position = i;
                deleteButton.setTag(positionAndIdViewTag);
            }

            // setting song index
            songIndexNumber.setText(String.valueOf(i + 1));

            // setting title
            title.setText(StringLib.truncate(musicMetaData.mTitle, 18));

            // setting artist
            artist.setText(StringLib.truncate(musicMetaData.mArtist, 18));

            // setting album photo
            if (musicMetaData.mAlbumPhoto != null)
                albumPhoto.setImageBitmap(musicMetaData.mAlbumPhoto);

            // setting tempo icon
            if (musicMetaData.mBpm != null) {
                if (musicMetaData.mBpm > 0 && musicMetaData.mBpm < 110) {
                    songTempo.setImageResource(R.drawable.slow);
                } else if (musicMetaData.mBpm < 130 && musicMetaData.mBpm >= 110) {
                    songTempo.setImageResource(R.drawable.medium);
                } else if (musicMetaData.mBpm >= 130){
                    songTempo.setImageResource(R.drawable.fast);
                } else {
                    songTempo.setImageResource(R.drawable.lay);
                }
            } else {
                songTempo.setImageResource(R.drawable.lay);
            }

            // setting swipe feature for songContainer
            songContainer.setOnTouchListener(this);

            if (maxOffset == -1) {
                maxOffset = deleteButton.getLayoutParams().width;
            }

            deleteButton.setOnClickListener(this);

            return view;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = MotionEventCompat.getActionMasked(motionEvent);
            switch(action) {
                case (MotionEvent.ACTION_DOWN) :
                    if (isOpened && view != mSwipeView) {
                        isOpened = false;
                        mSwipeView.setX(mSwipeView.getX() + maxOffset);
                    }
                    offset = 0;
                    originX = motionEvent.getX();
                    return true;
                case (MotionEvent.ACTION_MOVE) :
                    motionEvent.getAction();
                    offset = motionEvent.getX() - originX;
                    if (isOpened) {
                        direction = RIGHT;
                    } else {
                        direction = LEFT;
                    }
                    if (Math.abs(offset) > 3) {
                        isMove = true;
                    }
                    return true;

                case (MotionEvent.ACTION_UP) :
                    if (isMove) {
                        TranslateAnimation translateAnimation ;
                        if (direction == LEFT) {
                            isOpened = true;
                            translateAnimation = new TranslateAnimation(
                                    Animation.ABSOLUTE, maxOffset,
                                    Animation.RELATIVE_TO_SELF, 0,
                                    Animation.RELATIVE_TO_SELF, 0,
                                    Animation.RELATIVE_TO_SELF, 0);
                            view.setX(view.getX() - maxOffset);
                            mSwipeView = view;
                        } else {
                            isOpened = false;
                            translateAnimation = new TranslateAnimation(
                                    Animation.ABSOLUTE, -maxOffset,
                                    Animation.RELATIVE_TO_SELF, 0,
                                    Animation.RELATIVE_TO_SELF, 0,
                                    Animation.RELATIVE_TO_SELF, 0);
                            view.setX(view.getX() + maxOffset);
                        }

                        translateAnimation.setDuration(100);
                        AnimationSet animationSet = new AnimationSet(true);
                        animationSet.addAnimation(translateAnimation);
                        animationSet.setFillBefore(false);
                        animationSet.setFillAfter(true);
                        view.startAnimation(animationSet);

                        isMove = false;
                    }
                    return true;
                case (MotionEvent.ACTION_CANCEL) :
                    isMove = false;
                    return true;
                case (MotionEvent.ACTION_OUTSIDE) :
                    isMove = false;
                    return true;
            }
            return false;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.delete:
                    PositionAndIdViewTag positionAndIdViewTag = (PositionAndIdViewTag) view.getTag();
                    Long audioId = positionAndIdViewTag.audioId;
                    int numberOfDelete = MusicLib.deleteSongFromPlaylist(getContext(), playlistMemberUri, audioId);
                    Log.d(TAG, "number of delete" + numberOfDelete);
                    if (numberOfDelete > 0 && isOpened) {
                        mSwipeView.setX(mSwipeView.getX() + maxOffset);
                        isOpened = false;
                    }
                    updatePlaylist();
                    break;
            }
        }

        public class SongViewTag {
            TextView songIndexNumber;
            TextView title;
            TextView artist;
            ImageView albumPhoto;
            ImageView songTempo;
            RelativeLayout songContainer;
            Button deleteButton;
            public SongViewTag(TextView songIndexNumber, TextView title, TextView artist, ImageView albumPhoto, ImageView songTempo, RelativeLayout songContainer, Button deleteButton) {
                this.songIndexNumber = songIndexNumber;
                this.title = title;
                this.artist = artist;
                this.albumPhoto = albumPhoto;
                this.songTempo = songTempo;
                this.songContainer = songContainer;
                this.deleteButton = deleteButton;
            }
        }

        public class PositionAndIdViewTag {
            Integer position;
            Long audioId;
            public PositionAndIdViewTag (Integer position, Long audioId) {
                this.position = position;
                this.audioId = audioId;
            }
        }
    }
}
