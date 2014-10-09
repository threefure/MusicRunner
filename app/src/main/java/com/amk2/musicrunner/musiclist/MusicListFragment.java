package com.amk2.musicrunner.musiclist;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.MusicSong;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.OnPlaylistPreparedListener;
import com.amk2.musicrunner.utilities.RestfulUtility;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.zip.Inflater;

/**
 * Created by logicmelody on 2014/9/23.
 */

public class MusicListFragment extends Fragment implements /*LoaderManager.LoaderCallbacks<Cursor>,*/ View.OnClickListener, OnPlaylistPreparedListener {
    private static final String TAG = "MusicListFragment";
    private static final int MUSIC_LOADER_ID = 1;
    private static final int PLAYLIST_PREPARED = 0;
    private HashMap<Long, View> playlistViews;

    private static final String[] MUSIC_SELECT_PROJECTION = new String[] {
            android.provider.MediaStore.Audio.Media._ID,
            android.provider.MediaStore.Audio.Media.TITLE,
            android.provider.MediaStore.Audio.Media.ARTIST,
            android.provider.MediaStore.Audio.Media.DURATION
    };

    private LinearLayout playlistContainer;
    SharedPreferences playlistPreferences;

    HashMap<Integer, PlaylistMetaData> mPlaylistMetaData;

    private LayoutInflater inflater;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        playlistPreferences = getActivity().getSharedPreferences("playlist", 0);
        playlistViews = new HashMap<Long, View>();

        initViews();
    }

    @Override
    public void onResume () {
        // put initLoader in onResume because the onLoadFinished would be called twice if initLoader is put in onActivityCreated
        // according to http://developer.android.com/guide/components/fragments.html#Creating
        // and http://stackoverflow.com/questions/11293441/android-loadercallbacks-onloadfinished-called-twice
        // but....not work!
        super.onResume();
        if (mPlaylistMetaData == null) {
            SongLoaderRunnable loader = new SongLoaderRunnable(this);
            Thread loaderThread = new Thread(loader);
            loaderThread.start();
        }
    }

    @Override
    public void onStop () {
        super.onStop();
        //playlistContainer.removeAllViewsInLayout();
    }

    private void initViews() {
        View thisView = getView();
        playlistContainer = (LinearLayout) thisView.findViewById(R.id.playlist_container);
    }

    private void addPlaylistTemplate(PlaylistMetaData playlistMetaData) {
        View musicListTemplate = inflater.inflate(R.layout.music_list_template, null);
        TextView titleTextView    = (TextView) musicListTemplate.findViewById(R.id.playlist_title);
        TextView typeTextView     = (TextView) musicListTemplate.findViewById(R.id.playlist_type);
        TextView tracksTextView   = (TextView) musicListTemplate.findViewById(R.id.playlist_tracks);
        TextView durationTextView = (TextView) musicListTemplate.findViewById(R.id.playlist_duration);
        TextView caloriesTextView = (TextView) musicListTemplate.findViewById(R.id.playlist_calories);
        TextView choosePlaylistTextView = (TextView) musicListTemplate.findViewById(R.id.choose_playlist);
        titleTextView.setText(playlistMetaData.mTitle);
        //typeTextView.setText(PlaylistType.get(type));
        tracksTextView.setText(playlistMetaData.mTracks.toString());
        durationTextView.setText(TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(playlistMetaData.mDuration/1000)));
        caloriesTextView.setText(StringLib.truncateDoubleString(playlistMetaData.mCalories.toString(),2));

        musicListTemplate.setTag(playlistMetaData.mId);
        musicListTemplate.setOnClickListener(this);
        choosePlaylistTextView.setTag(playlistMetaData.mId);
        choosePlaylistTextView.setOnClickListener(this);
        playlistContainer.addView(musicListTemplate);
        playlistViews.put(playlistMetaData.mId, musicListTemplate);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playlist:
                Intent intent = new Intent(getActivity().getApplicationContext(), MusicListDetailActivity.class);
                intent.putExtra(MusicListDetailActivity.PLAYLIST_MEMBER_ID, (Long) view.getTag());
                startActivity(intent);
                break;
            case R.id.choose_playlist:
                Long oldPlaylistId = playlistPreferences.getLong("id", 0);
                Long newPlaylistId = (Long) view.getTag();
                playlistPreferences.edit().remove("id").putLong("id", newPlaylistId).commit();
                playlistViews.get(oldPlaylistId).findViewById(R.id.choose_playlist).setBackground(getActivity().getResources().getDrawable(R.drawable.music_runner_clickable_grass_round_border));
                playlistViews.get(newPlaylistId).findViewById(R.id.choose_playlist).setBackground(getActivity().getResources().getDrawable(R.drawable.music_runner_clickable_red_orund_border));
                break;
        }
    }

    private Handler mPlaylistUIHandler = new Handler() {
        @Override
        public void handleMessage (Message message) {
            switch (message.what) {
                /*case PLAYLIST_PREPARED:
                    mPlaylistMetaData = (HashMap<Integer, PlaylistMetaData>) message.obj;
                    updatePlaylistUI();
                    Long id = playlistPreferences.getLong("id", 0);
                    if (id != null) {
                        Long initId = mPlaylistMetaData.get(PlaylistManager.HALF_HOUR_PLAYLIST + PlaylistManager.SLOW_PACE_PLAYLIST).mId;
                        playlistPreferences.edit().putLong("id", initId).commit();
                        playlistViews.get(initId).findViewById(R.id.choose_playlist).setBackground(getActivity().getResources().getDrawable(R.drawable.music_runner_clickable_red_orund_border));
                    }
                    break;*/
            }
        }
    };

    private void updatePlaylistUI () {
        addPlaylistTemplate(mPlaylistMetaData.get(PlaylistManager.HALF_HOUR_PLAYLIST + PlaylistManager.SLOW_PACE_PLAYLIST));
        addPlaylistTemplate(mPlaylistMetaData.get(PlaylistManager.HALF_HOUR_PLAYLIST + PlaylistManager.MEDIUM_PACE_PLAYLIST));
        addPlaylistTemplate(mPlaylistMetaData.get(PlaylistManager.ONE_HOUR_PLAYLIST + PlaylistManager.SLOW_PACE_PLAYLIST));
        addPlaylistTemplate(mPlaylistMetaData.get(PlaylistManager.ONE_HOUR_PLAYLIST + PlaylistManager.MEDIUM_PACE_PLAYLIST));
    }

    @Override
    public void OnPlaylistPrepared(HashMap<Integer, PlaylistMetaData> playlistMetaDataHashMap) {
        mPlaylistMetaData = playlistMetaDataHashMap;
        updatePlaylistUI();
        Long id = playlistPreferences.getLong("id", 0);
        if (id != null) {
            Long initId = mPlaylistMetaData.get(PlaylistManager.HALF_HOUR_PLAYLIST + PlaylistManager.SLOW_PACE_PLAYLIST).mId;
            playlistPreferences.edit().putLong("id", initId).commit();
            playlistViews.get(initId).findViewById(R.id.choose_playlist).setBackground(getActivity().getResources().getDrawable(R.drawable.music_runner_clickable_red_orund_border));
        }
    }

    public class SongLoaderRunnable implements Runnable, LoaderManager.LoaderCallbacks<Cursor> {
        Fragment fragment;
        OnPlaylistPreparedListener mPlaylistPreparedListener;

        public SongLoaderRunnable (Fragment f) {
            fragment = f;
            mPlaylistPreparedListener = (OnPlaylistPreparedListener)f;
        }

        @Override
        public void run() {
            Looper.prepare();
            getLoaderManager().initLoader(MUSIC_LOADER_ID, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(fragment.getActivity(), MusicLib.getMusicUri(), MUSIC_SELECT_PROJECTION, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            PlaylistManager playlistManager = PlaylistManager.getInstance();
            playlistManager.init();
            playlistManager.setContext(fragment.getActivity());
            playlistManager.setCursor(cursor);
            playlistManager.scan();
            HashMap<Integer, PlaylistMetaData> playlistMetaDatas = new HashMap<Integer, PlaylistMetaData>();
            PlaylistMetaData halfHourSlowPlaylistMetaData = playlistManager.generate30MinsPlaylist(PlaylistManager.SLOW_PACE_PLAYLIST);
            PlaylistMetaData halfHourMediumPlaylistMetaData = playlistManager.generate30MinsPlaylist(PlaylistManager.MEDIUM_PACE_PLAYLIST);
            PlaylistMetaData oneHourSlowPlaylistMetaData = playlistManager.generate1HrPlaylist(PlaylistManager.SLOW_PACE_PLAYLIST);
            PlaylistMetaData oneHourMediumPlaylistMetaData = playlistManager.generate1HrPlaylist(PlaylistManager.MEDIUM_PACE_PLAYLIST);
            playlistMetaDatas.put(PlaylistManager.HALF_HOUR_PLAYLIST + PlaylistManager.SLOW_PACE_PLAYLIST, halfHourSlowPlaylistMetaData);
            playlistMetaDatas.put(PlaylistManager.HALF_HOUR_PLAYLIST + PlaylistManager.MEDIUM_PACE_PLAYLIST, halfHourMediumPlaylistMetaData);
            playlistMetaDatas.put(PlaylistManager.ONE_HOUR_PLAYLIST + PlaylistManager.SLOW_PACE_PLAYLIST, oneHourSlowPlaylistMetaData);
            playlistMetaDatas.put(PlaylistManager.ONE_HOUR_PLAYLIST + PlaylistManager.MEDIUM_PACE_PLAYLIST, oneHourMediumPlaylistMetaData);

            mPlaylistPreparedListener.OnPlaylistPrepared(playlistMetaDatas);
            //need to destroy loader so that onLoadFinished won't be called twice
            getLoaderManager().destroyLoader(MUSIC_LOADER_ID);
            Thread.interrupted();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    }
}
