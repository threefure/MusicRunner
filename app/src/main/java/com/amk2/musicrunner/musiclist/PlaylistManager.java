package com.amk2.musicrunner.musiclist;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

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
import java.util.concurrent.ExecutionException;

/**
 * Created by daz on 10/7/14.
 */
public class PlaylistManager{
    private static final String TAG = "PlaylistManager";
    private static final String TRACK_LIST = "trackList";
    static PlaylistManager instance;
    static {
        instance = new PlaylistManager();
    }

    private Context mContext;
    private Cursor mCursor;
    private ArrayList<MusicSong> mMusicSongList;
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
        mMusicSongList = new ArrayList<MusicSong>();
    }

    public void scan () {
        mMusicSongList = convertCursorToMusicSongList(mCursor);
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
    }

    public void generate30MinsPlaylist (String type) {

    }

    public void generate1HrPlaylist () {

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
                String musicFilePath = MusicLib.getMusicFilePath(mContext, musicUri);
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
        JSONArray trackList        = new JSONArray();
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

    private boolean isMusicFile(String filePath) {
        if (!TextUtils.isEmpty(filePath) && (filePath.endsWith("mp3") || filePath.endsWith("MP3"))) {
            return true;
        } else {
            return false;
        }
    }
}
