package com.amk2.musicrunner.music;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.SongPerformance;
import com.amk2.musicrunner.utilities.StringLib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by logicmelody on 2014/8/30.
 */
public class MusicFragment extends Fragment implements View.OnClickListener{
    private double performanceRangeOffset = 0.0001;

    private Double totalCalories;
    private Double averagePerformance;
    private Integer totalDuration;

    private LinearLayout musicRankContainer;
    private ContentResolver mContentResolver;
    private LayoutInflater inflater;

    private ArrayList<SongPerformance> songPerformanceList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.music_fragment, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContentResolver = getActivity().getContentResolver();
        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        songPerformanceList = new ArrayList<SongPerformance>();
        initViews();
    }

    public void onStart () {
        super.onStart();
        if (songPerformanceList != null) {
            songPerformanceList.clear();
        }
        getMusicRanks();
        setViews();
    }

    @Override
    public void onStop () {
        super.onStop();
        musicRankContainer.removeAllViewsInLayout();
    }

    private void initViews() {
        View thisView = getView();
        musicRankContainer = (LinearLayout) thisView.findViewById(R.id.music_rank_container);
    }

    private void setViews() {
        int length = songPerformanceList.size();
        for (int i = 0; i < length; i++) {
            addSongRank(songPerformanceList.get(i), (i == 0));
        }
    }

    private void addSongRank(SongPerformance sp, boolean isBest) {
        View songRankTemplate;
        if (isBest) {
            songRankTemplate = inflater.inflate(R.layout.music_rank_best_template, null);
        } else {
            songRankTemplate = inflater.inflate(R.layout.music_rank_template, null);
        }
        TextView songNameTextView     = (TextView) songRankTemplate.findViewById(R.id.song_name);
        TextView singerTextView       = (TextView) songRankTemplate.findViewById(R.id.singer);
        TextView caloriesTextView     = (TextView) songRankTemplate.findViewById(R.id.calories);
        TextView distanceTextView     = (TextView) songRankTemplate.findViewById(R.id.distance);
        TextView timesTextView        = (TextView) songRankTemplate.findViewById(R.id.times);
        TextView rankTagTextView      = (TextView) songRankTemplate.findViewById(R.id.rank_tag);
        ImageView albumPhotoImageView = (ImageView) songRankTemplate.findViewById(R.id.album_photo);

        songNameTextView.setText(StringLib.truncate(sp.name, 20));
        singerTextView.setText(sp.artist);
        caloriesTextView.setText(StringLib.truncateDoubleString(sp.calories.toString(), 1));
        distanceTextView.setText(StringLib.truncateDoubleString(sp.distance.toString(), 1));
        timesTextView.setText(sp.times.toString());
        if (isBest) {
            rankTagTextView.setText("Best");
        } else {
            if (sp.performance > averagePerformance + performanceRangeOffset) {
                rankTagTextView.setText("Up");
                rankTagTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.music_runner_up_song_tag));
            } else if (sp.performance <= averagePerformance + performanceRangeOffset && sp.performance > averagePerformance - performanceRangeOffset) {
                rankTagTextView.setText("Keep");
                rankTagTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.music_runner_keep_song_tag));
            } else {
                rankTagTextView.setText("Easy");
                rankTagTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.music_runner_easy_song_tag));
            }
        }

        Uri songUri = MusicLib.getMusicUri(sp.realSongId);
        String songPath = MusicLib.getMusicFilePath(getActivity(), songUri);
        Bitmap albumPhoto = MusicLib.getMusicAlbumArt(songPath);
        if (albumPhoto != null) {
            albumPhotoImageView.setImageBitmap(albumPhoto);
        }

        songRankTemplate.setTag(sp.songId);
        songRankTemplate.setOnClickListener(this);
        musicRankContainer.addView(songRankTemplate);
    }

    private void getMusicRanks() {
        HashMap<String, String> songInfo;
        Integer duration, songId, lastSongId = -1;
        Double caloriesTemp;
        String calories, distance, currentEpoch, speed, songName, artist;
        String[] projection = {
                MusicRunnerSongPerformanceDB.COLUMN_NAME_ID,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_SONG_ID,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_CALORIES,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_DURATION,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_SPEED,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_DISTANCE,
                MusicRunnerSongPerformanceDB.COLUMN_NAME_DATE_IN_MILLISECOND
        };
        String orderBy = MusicRunnerSongPerformanceDB.COLUMN_NAME_SONG_ID + " DESC";
        Cursor cursor = mContentResolver.query(MusicRunnerSongPerformanceDB.CONTENT_URI, projection, null, null, orderBy);
        totalCalories = 0.0;
        totalDuration = 0;
        while (cursor.moveToNext()) {
            songId       = cursor.getInt(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_SONG_ID));
            duration     = cursor.getInt(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DURATION));
            calories     = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_CALORIES));
            distance     = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DISTANCE));
            currentEpoch = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DATE_IN_MILLISECOND));
            speed        = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_SPEED));

            caloriesTemp = Double.parseDouble(calories);
            totalCalories += caloriesTemp;
            totalDuration += duration;

            if (lastSongId != songId) {
                songInfo = MusicLib.getSongInfo(getActivity(), songId);
                artist   = MusicLib.getArtist(getActivity(), Long.parseLong(songInfo.get(MusicLib.ARTIST_ID)));
                SongPerformance sp = new SongPerformance(duration, Double.parseDouble(distance), caloriesTemp, Double.parseDouble(speed), songInfo.get(MusicLib.SONG_NAME), artist);
                sp.setSongId(songId);
                sp.setRealSongId(Long.parseLong(songInfo.get(MusicLib.SONG_REAL_ID)));
                songPerformanceList.add(sp);
                lastSongId = songId;
            } else {
                SongPerformance sp = songPerformanceList.get(songPerformanceList.size() - 1);
                sp.addSongRecord(duration, Double.parseDouble(distance), Double.parseDouble(calories));
            }
        }
        Collections.sort(songPerformanceList);
        averagePerformance = totalCalories*60/totalDuration.doubleValue();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_rank:
                Log.d("MusicFragment", "click!");
                Integer songId = (Integer) v.getTag();
                Intent intent = new Intent(getActivity().getApplicationContext(), MusicRankDetailActivity.class);
                intent.putExtra(MusicRankDetailActivity.SONG_ID, songId);
                startActivity(intent);
                break;
        }
    }
}
