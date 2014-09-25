package com.amk2.musicrunner.musiclist;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.MusicSong;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;

import java.util.ArrayList;

/**
 * Created by logicmelody on 2014/9/23.
 */
public class MusicListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int MUSIC_LOADER_ID = 1;
    private static final String[] MUSIC_SELECT_PROJECTION = new String[] {
            android.provider.MediaStore.Audio.Media._ID,
            android.provider.MediaStore.Audio.Media.TITLE,
            android.provider.MediaStore.Audio.Media.ARTIST,
            android.provider.MediaStore.Audio.Media.DURATION
    };
    private static final int MUSIC_ID = 0;
    private static final int MUSIC_TITLE = 1;
    private static final int MUSIC_ARTIST = 2;
    private static final int MUSIC_DURATION = 3;

    private TextView tracksTextView;
    private TextView durationTextView;
    private TextView caloriesTextView;
    private ArrayList<MusicSong> mMusicSongList;

    private Integer tracks;
    private Integer duration;
    private Double calories;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(MUSIC_LOADER_ID, null, this);
        initViews();
    }

    private void initViews() {
        View thisView = getView();
        tracksTextView   = (TextView) thisView.findViewById(R.id.playlist_tracks);
        durationTextView = (TextView) thisView.findViewById(R.id.playlist_duration);
        caloriesTextView = (TextView) thisView.findViewById(R.id.playlist_calories);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), MusicLib.getMusicUri(), MUSIC_SELECT_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        tracks = 0;
        duration = 0;
        mMusicSongList = convertCursorToMusicSongList(data);
        calories = calculateCalories(duration, 325.0*tracks);

        tracksTextView.setText(tracks.toString());
        durationTextView.setText(TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(duration)));
        caloriesTextView.setText(StringLib.truncateDoubleString(calories.toString(), 2));


        Log.d("danny", "Music list size = " + mMusicSongList.size());
    }

    private ArrayList<MusicSong> convertCursorToMusicSongList(Cursor cursor) {
        ArrayList<MusicSong> songList = new ArrayList<MusicSong>();
        if (cursor != null) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                //int isRingtone = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE));
                //int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Integer trackDuration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                Uri musicUri = ContentUris.withAppendedId(MusicLib.getMusicUri(), id);
                String musicFilePath = MusicLib.getMusicFilePath(getActivity(), musicUri);
                if(isMusicFile(musicFilePath)) {
                    //Log.d("danny","Add music file path = " + musicFilePath);
                    Log.d("MusicList", "Duration=" + trackDuration.toString() + " artist=" + artist);
                    tracks ++;
                    duration += trackDuration;
                    songList.add(new MusicSong(id, title, artist));
                }
            }
            duration = duration/1000;
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
}
