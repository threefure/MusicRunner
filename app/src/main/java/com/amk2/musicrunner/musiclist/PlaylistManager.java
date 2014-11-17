package com.amk2.musicrunner.musiclist;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.amk2.musicrunner.BuildConfig;
import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.running.MusicSong;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.RestfulUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.concurrent.ExecutionException;

/**
 * Created by daz on 10/7/14.
 */
public class PlaylistManager{
    public static final String BPM_UPDATED = "musiclist.playlistmanager.bpm_updated";
    public static final String PLAYLIST_DELETED = "musiclist.playlistmanager.playlist_delete";
    public static final int MEDIUM_PACE_PLAYLIST = 0;
    public static final int SLOW_PACE_PLAYLIST   = 1;
    public static final int HALF_HOUR_PLAYLIST   = 2;
    public static final int ONE_HOUR_PLAYLIST    = 4;
    public static final String HALF_HOUR_MEDIUM_PACE_PLAYLIST_TITLE = "30 Minutes Medium Pace";
    public static final String HALF_HOUR_SLOW_PACE_PLAYLIST_TITLE = "30 Minutes Slow Pace";
    public static final String ONE_HOUR_MEDIUM_PACE_PLAYLIST_TITLE = "1 Hour Medium Pace";
    public static final String ONE_HOUR_SLOW_PACE_PLAYLIST_TITLE = "1 Hour Slow Pace";
    private static final String TAG = "PlaylistManager";
    private static final String TRACK_LIST = "trackList";
    private static final int HALF_HOUR_IN_MILLI = 1800000;
    private static final int ONE_HOUR_IN_MILLI  = 3600000;
    static PlaylistManager instance;
    static {
        instance = new PlaylistManager();
    }

    private Context mContext;
    private Cursor mCursor;
    private ArrayList<MusicSong> mMusicSongList;
    private ArrayList<MusicSong> mFastPaceMusicSongList;
    private ArrayList<MusicSong> mMediumPaceMusicSongList;
    private ArrayList<MusicSong> mSlowPaceMusicSongList;
    private JSONObject mTrackListWrapper;
    private PlaylistManager () {

    }

    public static PlaylistManager getInstance () {
        return instance;
    }

    public void setContext (Context context) {
        mContext = context;
    }

    public void setCursor (Cursor cursor) {
        mCursor = cursor;
    }

    public void init() {
        mMusicSongList           = new ArrayList<MusicSong>();
        mFastPaceMusicSongList   = new ArrayList<MusicSong>();
        mMediumPaceMusicSongList = new ArrayList<MusicSong>();
        mSlowPaceMusicSongList   = new ArrayList<MusicSong>();
    }

    public void scan () {
        mMusicSongList = convertCursorToMusicSongList(mCursor);
        mTrackListWrapper = checkBpmForEachSong();
        if (mTrackListWrapper.has(TRACK_LIST)) {
            Log.d(TAG, "call api, " + mTrackListWrapper.toString());
            HandlePostTrackInfoRunnable handlePostTrackInfoRunnable = new HandlePostTrackInfoRunnable(mTrackListWrapper);
            Thread handlePostTrackInfoThread = new Thread(handlePostTrackInfoRunnable);
            handlePostTrackInfoThread.start();
        }
        Log.d(TAG, "checking playlist");
        categorizePlaylists();
    }

    private class HandlePostTrackInfoRunnable implements Runnable {
        JSONObject mTrackListWrapper;
        public HandlePostTrackInfoRunnable (JSONObject mTrackListWrapper) {
            this.mTrackListWrapper = mTrackListWrapper;
        }
        @Override
        public void run() {
            JSONArray trackList = null;
            try {
                trackList = mTrackListWrapper.getJSONArray(TRACK_LIST);
                Integer NumberOfTracksInAChunk = 20;

                int base = 0;
                while (trackList.length() > base) {
                    JSONArray trackListChunk = new JSONArray();
                    JSONObject wrapperChunk = new JSONObject();
                    for (int i = base; i < (base + NumberOfTracksInAChunk) && i < trackList.length(); i++) {
                        trackListChunk.put(trackList.getJSONObject(i));
                    }
                    base += 10;
                    wrapperChunk.put(TRACK_LIST, trackListChunk);
                    PostTrackInfoRunnable postTrackInfoRunnable = new PostTrackInfoRunnable(wrapperChunk.toString());
                    Thread postTrackInfoThread = new Thread(postTrackInfoRunnable);
                    postTrackInfoThread.start();
                    Thread.sleep(15000);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private class PostTrackInfoRunnable implements Runnable {
        String postString;
        public PostTrackInfoRunnable (String postString) {
            this.postString = postString;
        }
        @Override
        public void run() {
            try {
                RestfulUtility.PostRequest postRequest = new RestfulUtility.PostRequest(postString);
                String trackInfoApiUrl;
                if (BuildConfig.DEBUG) {
                    trackInfoApiUrl = Constant.TRACK_INFO_DEBUG_API_URL;
                } else {
                    trackInfoApiUrl = Constant.TRACK_INFO_API_URL;
                }
                String trackListArrayString = postRequest.execute(trackInfoApiUrl).get();
                JSONArray trackListArray = new JSONArray(trackListArrayString);
                saveBpmForEachSong(trackListArray);
                categorizePlaylists();

                Intent intent = new Intent(MusicListFragment.UPDATE_PLAYLIST);
                intent.putExtra(BPM_UPDATED, true);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    public PlaylistMetaData generate30MinsPlaylist (Integer type) {
        PlaylistMetaData playlistMetaData = null;
        switch (type) {
            case MEDIUM_PACE_PLAYLIST:
                playlistMetaData = generatePlaylist(mMediumPaceMusicSongList, HALF_HOUR_IN_MILLI, HALF_HOUR_MEDIUM_PACE_PLAYLIST_TITLE);
                break;
            case SLOW_PACE_PLAYLIST:
                playlistMetaData = generatePlaylist(mSlowPaceMusicSongList, HALF_HOUR_IN_MILLI, HALF_HOUR_SLOW_PACE_PLAYLIST_TITLE);
                break;
        }
        playlistMetaData.setType(type + HALF_HOUR_PLAYLIST);
        return playlistMetaData;
    }

    public PlaylistMetaData generate1HrPlaylist (Integer type) {
        PlaylistMetaData playlistMetaData = null;
        switch (type) {
            case MEDIUM_PACE_PLAYLIST:
                playlistMetaData = generatePlaylist(mMediumPaceMusicSongList, ONE_HOUR_IN_MILLI, ONE_HOUR_MEDIUM_PACE_PLAYLIST_TITLE);
                break;
            case SLOW_PACE_PLAYLIST:
                playlistMetaData = generatePlaylist(mSlowPaceMusicSongList, ONE_HOUR_IN_MILLI, ONE_HOUR_SLOW_PACE_PLAYLIST_TITLE);
                break;
        }
        playlistMetaData.setType(type + ONE_HOUR_PLAYLIST);
        return playlistMetaData;
    }

    private PlaylistMetaData generatePlaylist(ArrayList<MusicSong> musicSongList, Integer targetDuration, String playlistName) {
        int length = musicSongList.size(), duration = 0, tracks = 0;
        Uri playlistUri = getPlaylistUri(playlistName);
        Uri playlistMemberUri = getPlaylistMemberUri(playlistUri);
        MusicSong ms;
        PlaylistMetaData playlistMetaData;
        try {
            for (int i = 0; i < length; i++) {
                ms = musicSongList.get(i);
                addToPlaylist(playlistMemberUri, ms.mId, i);
                duration += ms.mDuration;
                tracks++;
                if (duration > targetDuration) {
                    break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            Log.d(TAG, "index out of bound");
        }
        playlistMetaData = new PlaylistMetaData(playlistUri, playlistName, duration, tracks);
        return playlistMetaData;
    }

    public ArrayList<PlaylistMetaData> getUserGeneratedPlaylist () {
        ArrayList<PlaylistMetaData> playlistMetaDatas = new ArrayList<PlaylistMetaData>();
        Uri allPlaylistUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = mContext.getContentResolver();
        Long playlistId;
        Uri playlistUri;
        String playlistName;
        String[] projection = {
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.DATE_ADDED
        };
        String orderBy = MediaStore.Audio.Playlists.DATE_ADDED + " DESC";
        Cursor cursor = contentResolver.query(allPlaylistUri, projection, null, null, orderBy);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                playlistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                playlistUri = MusicLib.getPlaylistUriFromId(playlistId);
                playlistName = MusicLib.getPlaylistName(mContext, playlistUri);
                if (!playlistName.equals(HALF_HOUR_SLOW_PACE_PLAYLIST_TITLE) &&
                    !playlistName.equals(HALF_HOUR_MEDIUM_PACE_PLAYLIST_TITLE) &&
                    !playlistName.equals(ONE_HOUR_SLOW_PACE_PLAYLIST_TITLE) &&
                    !playlistName.equals(ONE_HOUR_MEDIUM_PACE_PLAYLIST_TITLE)) {
                    //user generated playlist
                    PlaylistMetaData playlistMetaData = MusicLib.getPlaylistMetadata(mContext, playlistId);
                    playlistMetaDatas.add(playlistMetaData);
                }
            }
        }
        return playlistMetaDatas;
    }

    public Uri getPlaylistUri (String playlistName) {
        Uri playlistUri = MusicLib.getPlaylistUri(mContext, playlistName);
        if (playlistUri == null) {
            playlistUri = MusicLib.createPlaylist(mContext, playlistName);
            Log.d(TAG, "playlist has been created, uri=" + playlistUri.toString());
        } else {
            Integer cnt = MusicLib.cleanPlaylist(mContext, playlistUri);
            Log.d(TAG, "playlist has been cleaned, cnt=" + cnt);
        }
        return playlistUri;
    }

    public Uri getPlaylistMemberUri (Uri playlistUri) {
        Long id = ContentUris.parseId(playlistUri);
        return MediaStore.Audio.Playlists.Members.getContentUri("external", id);
    }

    public void addToPlaylist(Uri playlistMemberUri, Long songRealId, Integer playOrder) {
        Uri uri = MusicLib.insertSongToPlaylist(mContext, playlistMemberUri, songRealId, playOrder);
    }

    public ArrayList<MusicSong> convertCursorToMusicSongList(Cursor cursor) {
        ArrayList<MusicSong> songList = new ArrayList<MusicSong>();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Integer trackDuration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                Uri musicUri = ContentUris.withAppendedId(MusicLib.getMusicUri(), id);
                Cursor musicCursor = mContext.getContentResolver().query(musicUri, null, null, null, null);
                // check if it's a valid uri
                if (musicCursor != null && musicCursor.getCount() > 0) {
                    retriever.setDataSource(mContext, musicUri);
                    String genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                    if (genre == null) {
                        genre = "";
                    }

                    String musicFilePath = MusicLib.getMusicFilePath(mContext, musicUri);
                    if (isMusicFile(musicFilePath)) {
                        songList.add(new MusicSong(id, title, artist, genre, trackDuration));
                    }
                }
                musicCursor.close();
            }
        }
        return songList;
    }

    private JSONObject checkBpmForEachSong () {
        MusicSong mMusicSong;
        HashMap<String, String> songInfo;
        JSONObject trackListWrapper   = new JSONObject();
        JSONArray trackListWithoutBPM = new JSONArray();
        int length = mMusicSongList.size();
        try {
            for (int i = 0; i < length; i++) {
                mMusicSong = mMusicSongList.get(i);
                songInfo = MusicLib.getSongInfo(mContext, mMusicSong.mTitle);
                if (songInfo == null){
                    //get song bpm
                    JSONObject trackInfo = new JSONObject();
                    trackInfo.put(MusicLib.ARTIST, URLEncoder.encode(mMusicSong.mArtist, "UTF-8"));
                    trackInfo.put(MusicLib.TITLE, URLEncoder.encode(mMusicSong.mTitle, "UTF-8"));
                    trackInfo.put(MusicLib.GENRE, URLEncoder.encode(mMusicSong.mGenre, "UTF-8"));
                    trackInfo.put(MusicLib.SONG_REAL_ID, mMusicSong.mId);
                    trackListWithoutBPM.put(trackInfo);
                } else if (songInfo.get(MusicLib.BPM) == null || Double.parseDouble(songInfo.get(MusicLib.BPM)) == -2) {
                    JSONObject trackInfo = new JSONObject();
                    trackInfo.put(MusicLib.ARTIST, URLEncoder.encode(mMusicSong.mArtist, "UTF-8"));
                    trackInfo.put(MusicLib.ARTIST_ID, URLEncoder.encode(songInfo.get(MusicLib.ARTIST_ID), "UTF-8"));
                    trackInfo.put(MusicLib.TITLE, URLEncoder.encode(mMusicSong.mTitle, "UTF-8"));
                    trackInfo.put(MusicLib.GENRE, URLEncoder.encode(mMusicSong.mGenre, "UTF-8"));
                    trackInfo.put(MusicLib.SONG_REAL_ID, mMusicSong.mId);
                    trackInfo.put(MusicLib.ID, songInfo.get(MusicLib.ID));
                    trackListWithoutBPM.put(trackInfo);
                }
            }
            if (trackListWithoutBPM.length() > 0) {
                trackListWrapper.put(TRACK_LIST, trackListWithoutBPM);
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
                if (trackInfo.has(MusicLib.ID)) {
                    MusicLib.updateSongInfoBPM(mContext, trackInfo);
                } else {
                    MusicLib.insertSongInfo(mContext, trackInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void categorizePlaylists () {
        int length = mMusicSongList.size();
        Double bpm;
        MusicSong ms;
        HashMap<String, String> songInfo;
        mSlowPaceMusicSongList.clear();
        mMediumPaceMusicSongList.clear();
        mFastPaceMusicSongList.clear();

        for (int i = 0; i < length; i ++) {
            ms = mMusicSongList.get(i);
            songInfo = MusicLib.getSongInfo(mContext, ms.mTitle);
            if (songInfo != null && songInfo.get(MusicLib.BPM) != null) {
                bpm = Double.parseDouble(songInfo.get(MusicLib.BPM));
                if (bpm == null || bpm < 0) {
                    //do nothing
                } else if (bpm < 110.0) {
                    mSlowPaceMusicSongList.add(ms);
                } else if (bpm >= 110.0 && bpm < 130.0) {
                    mMediumPaceMusicSongList.add(ms);
                } else {
                    mFastPaceMusicSongList.add(ms);
                }
            }
        }
    }

    public static boolean isMusicFile(String filePath) {
        if (!TextUtils.isEmpty(filePath) && (filePath.endsWith("mp3") || filePath.endsWith("MP3"))) {
            return true;
        } else {
            return false;
        }
    }
}
