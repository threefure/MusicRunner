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

public class MusicListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    private static final String TAG = "MusicListFragment";
    private static final String TRACK_LIST = "trackList";
    private static final int MUSIC_LOADER_ID = 1;
    private static final HashMap<String, String> PlaylistTitle = new HashMap<String, String>();
    private static final HashMap<String, String> PlaylistType = new HashMap<String, String>();
    static {
        PlaylistTitle.put(MusicLib.FAST_PLAYLIST, "Fast Playlist");
        PlaylistTitle.put(MusicLib.MEDIUM_PLAYLIST, "Regular Playlist");
        PlaylistTitle.put(MusicLib.SLOW_PLAYLIST, "Slow Playlist");

        PlaylistType.put(MusicLib.FAST_PLAYLIST, "Ninja");
        PlaylistType.put(MusicLib.MEDIUM_PLAYLIST, "Human");
        PlaylistType.put(MusicLib.SLOW_PLAYLIST, "Turtle");
    }
    private static final String[] MUSIC_SELECT_PROJECTION = new String[] {
            android.provider.MediaStore.Audio.Media._ID,
            android.provider.MediaStore.Audio.Media.TITLE,
            android.provider.MediaStore.Audio.Media.ARTIST,
            android.provider.MediaStore.Audio.Media.DURATION
    };

    private ContentResolver mContentResolver;

    private LinearLayout playlistContainer;
    private ArrayList<MusicSong> mMusicSongList;
    private JSONObject mTrackListWrapper;
    SharedPreferences playlistPreferences;

    private HashMap<String, Integer> tracks;
    private HashMap<String, Integer> duration;
    private HashMap<String, Double> calories;

    private Long fastPlaylistId;
    private Long mediumPlaylistId;
    private Long slowPlaylistId;
    private Uri fastPlaylistUri;
    private Uri fastPlaylistMemberUri;
    private Uri mediumPlaylistUri;
    private Uri mediumPlaylistMemberUri;
    private Uri slowPlaylistUri;
    private Uri slowPlaylistMemberUri;

    private LayoutInflater inflater;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContentResolver = getActivity().getContentResolver();
        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        playlistPreferences = getActivity().getSharedPreferences("playlist", 0);

        initViews();
    }

    @Override
    public void onResume () {
        // put initLoader in onResume because the onLoadFinished would be called twice if initLoader is put in onActivityCreated
        // according to http://developer.android.com/guide/components/fragments.html#Creating
        // and http://stackoverflow.com/questions/11293441/android-loadercallbacks-onloadfinished-called-twice
        // but....not work!
        super.onResume();
        if (fastPlaylistUri == null || mediumPlaylistUri == null || slowPlaylistUri == null) {
            getLoaderManager().initLoader(MUSIC_LOADER_ID, null, this);
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), MusicLib.getMusicUri(), MUSIC_SELECT_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        mMusicSongList = convertCursorToMusicSongList(data);
        mTrackListWrapper = checkBpmForEachSong();
        if (mTrackListWrapper.has(TRACK_LIST)) {
            try {
                RestfulUtility.PostRequest postRequest = new RestfulUtility.PostRequest(mTrackListWrapper.toString());
                String trackListArrayString = postRequest.execute(Constant.TRACK_INFO_API_URL).get();
                JSONArray trackListArray = new JSONArray(trackListArrayString);
                saveBpmForEachSong(trackListArray);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        initPlaylists();
        setPlaylists();
        setViews();

        // temporarily setting the playlist to slow one
        playlistPreferences.edit().putLong("id", slowPlaylistId).commit();

        //need to destroy loader so that onLoadFinished won't be called twice
        getLoaderManager().destroyLoader(MUSIC_LOADER_ID);
    }

    private void setViews() {
        addPlaylistTemplate(MusicLib.FAST_PLAYLIST, fastPlaylistId);
        addPlaylistTemplate(MusicLib.MEDIUM_PLAYLIST, mediumPlaylistId);
        addPlaylistTemplate(MusicLib.SLOW_PLAYLIST, slowPlaylistId);
    }

    private void addPlaylistTemplate(String type, Long playlistMemberId) {
        View musicListTemplate = inflater.inflate(R.layout.music_list_template, null);
        TextView titleTextView    = (TextView) musicListTemplate.findViewById(R.id.playlist_title);
        TextView typeTextView     = (TextView) musicListTemplate.findViewById(R.id.playlist_type);
        TextView tracksTextView   = (TextView) musicListTemplate.findViewById(R.id.playlist_tracks);
        TextView durationTextView = (TextView) musicListTemplate.findViewById(R.id.playlist_duration);
        TextView caloriesTextView = (TextView) musicListTemplate.findViewById(R.id.playlist_calories);
        titleTextView.setText(PlaylistTitle.get(type));
        typeTextView.setText(PlaylistType.get(type));
        tracksTextView.setText(tracks.get(type).toString());
        durationTextView.setText(TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(duration.get(type)/1000)));
        caloriesTextView.setText(StringLib.truncateDoubleString(calories.get(type).toString(),2));

        musicListTemplate.setTag(playlistMemberId);
        musicListTemplate.setOnClickListener(this);
        playlistContainer.addView(musicListTemplate);
    }

    private void initPlaylists () {
        tracks = new HashMap<String, Integer>();
        tracks.put(MusicLib.FAST_PLAYLIST, 0);
        tracks.put(MusicLib.MEDIUM_PLAYLIST, 0);
        tracks.put(MusicLib.SLOW_PLAYLIST, 0);
        duration = new HashMap<String, Integer>();
        duration.put(MusicLib.FAST_PLAYLIST, 0);
        duration.put(MusicLib.MEDIUM_PLAYLIST, 0);
        duration.put(MusicLib.SLOW_PLAYLIST, 0);
        calories = new HashMap<String, Double>();
        calories.put(MusicLib.FAST_PLAYLIST, 0.0);
        calories.put(MusicLib.MEDIUM_PLAYLIST, 0.0);
        calories.put(MusicLib.SLOW_PLAYLIST, 0.0);

        fastPlaylistUri         = getPlaylistUri(MusicLib.FAST_PLAYLIST_NAME);
        fastPlaylistId          = getPlaylistId(fastPlaylistUri);
        fastPlaylistMemberUri   = getPlaylistMemberUri(fastPlaylistUri);

        mediumPlaylistUri       = getPlaylistUri(MusicLib.MEDIUM_PLAYLIST_NAME);
        mediumPlaylistId        = getPlaylistId(mediumPlaylistUri);
        mediumPlaylistMemberUri = getPlaylistMemberUri(mediumPlaylistUri);

        slowPlaylistUri         = getPlaylistUri(MusicLib.SLOW_PLAYLIST_NAME);
        slowPlaylistId          = getPlaylistId(slowPlaylistUri);
        slowPlaylistMemberUri   = getPlaylistMemberUri(slowPlaylistUri);
    }

    private void setPlaylists () {
        int length = mMusicSongList.size();
        Double bpm;
        MusicSong ms;
        HashMap<String, String> songInfo;

        for (int i = 0; i < length; i ++) {
            ms = mMusicSongList.get(i);
            songInfo = MusicLib.getSongInfo(getActivity(), ms.mTitle);
            bpm = Double.parseDouble(songInfo.get(MusicLib.BPM));
            if (bpm == null || bpm == -1.0) {
                //do nothing
            } else if (bpm < 110.0) {
                addToPlaylist(slowPlaylistMemberUri, Long.parseLong(songInfo.get(MusicLib.SONG_REAL_ID)), tracks.get(MusicLib.SLOW_PLAYLIST));
                updatePlaylistInfo(MusicLib.SLOW_PLAYLIST, ms.mDuration);
            } else if (bpm >= 110.0 && bpm < 130.0) {
                addToPlaylist(mediumPlaylistMemberUri, Long.parseLong(songInfo.get(MusicLib.SONG_REAL_ID)), tracks.get(MusicLib.MEDIUM_PLAYLIST));
                updatePlaylistInfo(MusicLib.MEDIUM_PLAYLIST, ms.mDuration);
            } else {
                addToPlaylist(fastPlaylistMemberUri, Long.parseLong(songInfo.get(MusicLib.SONG_REAL_ID)), tracks.get(MusicLib.FAST_PLAYLIST));
                updatePlaylistInfo(MusicLib.FAST_PLAYLIST, ms.mDuration);
            }
        }
        calories.put(MusicLib.FAST_PLAYLIST, calculateCalories(duration.get(MusicLib.FAST_PLAYLIST)/1000, 325.0*tracks.get(MusicLib.FAST_PLAYLIST)));
        calories.put(MusicLib.MEDIUM_PLAYLIST, calculateCalories(duration.get(MusicLib.MEDIUM_PLAYLIST)/1000, 325.0*tracks.get(MusicLib.MEDIUM_PLAYLIST)));
        calories.put(MusicLib.SLOW_PLAYLIST, calculateCalories(duration.get(MusicLib.SLOW_PLAYLIST)/1000, 325.0*tracks.get(MusicLib.SLOW_PLAYLIST)));
    }

    private void addToPlaylist(Uri playlistMemberUri, Long songRealId, Integer playOrder) {
        Uri uri = MusicLib.insertSongToPlaylist(getActivity(), playlistMemberUri, songRealId, playOrder);
        Log.d(TAG, "song is add to playlist, uri = " + uri.toString());
    }

    private void updatePlaylistInfo (String type, Integer dur) {
        tracks.put(type, tracks.get(type) + 1);
        duration.put(type, duration.get(type) + dur);
    }

    private Long getPlaylistId (Uri playlistUri) {
        return ContentUris.parseId(playlistUri);
    }

    private Uri getPlaylistUri (String playlistName) {
        Uri playlistUri = MusicLib.getPlaylistUri(getActivity(), playlistName);
        if (playlistUri == null) {
            playlistUri = MusicLib.insertPlaylistId(getActivity(), playlistName);
            Log.d(TAG, "playlist has been created, uri=" + playlistUri.toString());
        } else {
            Integer cnt = MusicLib.cleanPlaylist(getActivity(), playlistUri);
            Log.d(TAG, "playlist has been cleaned, cnt=" + cnt);
        }
        return playlistUri;
    }

    private Uri getPlaylistMemberUri (Uri playlistUri) {
        Long id = ContentUris.parseId(playlistUri);
        return MediaStore.Audio.Playlists.Members.getContentUri("external", id);
    }

    private ArrayList<MusicSong> convertCursorToMusicSongList(Cursor cursor) {
        ArrayList<MusicSong> songList = new ArrayList<MusicSong>();
        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Integer trackDuration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                Uri musicUri = ContentUris.withAppendedId(MusicLib.getMusicUri(), id);
                String musicFilePath = MusicLib.getMusicFilePath(getActivity(), musicUri);
                if(isMusicFile(musicFilePath)) {
                    songList.add(new MusicSong(id, title, artist, trackDuration));
                }
            }
        }
        return songList;
    }

    private JSONObject checkBpmForEachSong () {
        MusicSong mMusicSong;
        HashMap<String, String> songInfo;
        JSONObject trackListWrapper = new JSONObject();
        JSONArray  trackList        = new JSONArray();
        int length = mMusicSongList.size();
        try {
            for (int i = 0; i < length; i++) {
                mMusicSong = mMusicSongList.get(i);
                songInfo = MusicLib.getSongInfo(getActivity(), mMusicSong.mTitle);
                if (songInfo == null){
                    //get song bpm
                    JSONObject trackInfo = new JSONObject();
                    trackInfo.put(MusicLib.ARTIST, URLEncoder.encode(mMusicSong.mArtist, "UTF-8"));
                    trackInfo.put(MusicLib.TITLE, URLEncoder.encode(mMusicSong.mTitle, "UTF-8"));
                    trackInfo.put(MusicLib.SONG_REAL_ID, mMusicSong.mId);
                    trackList.put(trackInfo);
                } else if (songInfo.get(MusicLib.BPM) == null) {
                    JSONObject trackInfo = new JSONObject();
                    trackInfo.put(MusicLib.ARTIST, URLEncoder.encode(mMusicSong.mArtist, "UTF-8"));
                    trackInfo.put(MusicLib.ARTIST_ID, URLEncoder.encode(songInfo.get(MusicLib.ARTIST_ID), "UTF-8"));
                    trackInfo.put(MusicLib.TITLE, URLEncoder.encode(mMusicSong.mTitle, "UTF-8"));
                    trackInfo.put(MusicLib.SONG_REAL_ID, mMusicSong.mId);
                    trackInfo.put(MusicLib.ID, songInfo.get(MusicLib.ID));
                    trackList.put(trackInfo);
                }
            }
            if (trackList.length() > 0) {
                trackListWrapper.put(TRACK_LIST, trackList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return trackListWrapper;
    }

    private void saveBpmForEachSong (JSONArray trackListArray) {
        JSONObject trackInfo;
        int length = trackListArray.length();
        try {
            for (int i = 0; i < length; i ++) {
                trackInfo = trackListArray.getJSONObject(i);
                Log.d(TAG, trackInfo.getString(MusicLib.ARTIST));
                Log.d(TAG, trackInfo.getString(MusicLib.BPM));
                if (trackInfo.has(MusicLib.ID)) {
                    MusicLib.updateSongInfoBPM(getActivity(), trackInfo);
                } else {
                    MusicLib.insertSongInfo(getActivity(), trackInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isMusicFile(String filePath) {
        if (!TextUtils.isEmpty(filePath) && (filePath.endsWith("mp3") || filePath.endsWith("MP3"))) {
            return true;
        } else {
            return false;
        }
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

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playlist:
                Intent intent = new Intent(getActivity().getApplicationContext(), MusicListDetailActivity.class);
                intent.putExtra(MusicListDetailActivity.PLAYLIST_MEMBER_ID, (Long) view.getTag());
                startActivity(intent);
                break;
        }
    }
}
