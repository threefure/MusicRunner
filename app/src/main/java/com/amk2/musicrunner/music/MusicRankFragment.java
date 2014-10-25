package com.amk2.musicrunner.music;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerSongPerformanceDB;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.OnSongRankPreparedListener;
import com.amk2.musicrunner.utilities.SongPerformance;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.UnitConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by logicmelody on 2014/8/30.
 */
public class MusicRankFragment extends Fragment implements View.OnClickListener, OnSongRankPreparedListener{
    private static final String TAG = MusicRankFragment.class.getSimpleName();
    private double performanceRangeOffset = 0.0001;

    private Double totalCalories;
    private Double mAveragePerformance;
    private Integer totalDuration;

    private LinearLayout musicRankInitialInformation;
    private LinearLayout musicRankContainer;
    //private ContentResolver mContentResolver;
    private LayoutInflater inflater;
    private SharedPreferences mSettingSharedPreferences;
    private Integer unitDistance;

    private ArrayList<SongPerformance> mSongPerformanceList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.music_rank_fragment, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSettingSharedPreferences = getActivity().getSharedPreferences(SettingActivity.SETTING_SHARED_PREFERENCE, 0);
        unitDistance = mSettingSharedPreferences.getInt(SettingActivity.DISTANCE_UNIT, SettingActivity.SETTING_DISTANCE_KM);
        initViews();
    }

    public void onStart () {
        super.onStart();
        if (mSongPerformanceList == null) {
            //mSongPerformanceList.clear();
            SongRankLoaderRunnable songRankLoaderRunnable = new SongRankLoaderRunnable(this);
            Thread loaderThread = new Thread(songRankLoaderRunnable);
            loaderThread.start();
        } else {
            SongRankLoaderRunnable songRankLoaderRunnable = new SongRankLoaderRunnable(this);
            Thread loaderThread = new Thread(songRankLoaderRunnable);
            loaderThread.start();
        }
    }

    @Override
    public void onStop () {
        super.onStop();
        musicRankContainer.removeAllViewsInLayout();
    }

    private void initViews() {
        View thisView = getView();
        musicRankInitialInformation = (LinearLayout) thisView.findViewById(R.id.initial_information);
        musicRankContainer = (LinearLayout) thisView.findViewById(R.id.music_rank_container);
    }

    private void addSongRank(SongPerformance sp, boolean isBest) {
        View songRankTemplate;
        Double distance;
        if (isBest) {
            songRankTemplate = inflater.inflate(R.layout.music_rank_best_template, null);
        } else {
            songRankTemplate = inflater.inflate(R.layout.music_rank_template, null);
        }
        TextView songNameTextView     = (TextView) songRankTemplate.findViewById(R.id.song_name);
        TextView singerTextView       = (TextView) songRankTemplate.findViewById(R.id.singer);
        TextView caloriesTextView     = (TextView) songRankTemplate.findViewById(R.id.calories);
        TextView distanceTextView     = (TextView) songRankTemplate.findViewById(R.id.distance);
        TextView distanceUnitTextView = (TextView) songRankTemplate.findViewById(R.id.distance_unit);
        TextView timesTextView        = (TextView) songRankTemplate.findViewById(R.id.times);
        TextView rankTagTextView      = (TextView) songRankTemplate.findViewById(R.id.rank_tag);

        ImageView albumPhotoImageView = (ImageView) songRankTemplate.findViewById(R.id.album_photo);

        // getting distance information
        if (unitDistance == SettingActivity.SETTING_DISTANCE_MI) {
            distance = UnitConverter.getMIFromKM(sp.distance);
        } else {
            distance = sp.distance;
        }

        //setting distance information
        distanceTextView.setText(StringLib.truncateDoubleString(distance.toString(), 1));
        distanceUnitTextView.setText(Constant.DistanceMap.get(unitDistance));

        songNameTextView.setText(StringLib.truncate(sp.name, 20));
        singerTextView.setText(sp.artist);
        caloriesTextView.setText(StringLib.truncateDoubleString(sp.calories.toString(), 1));

        timesTextView.setText(sp.times.toString());
        if (isBest) {
            rankTagTextView.setText("Best");
        } else {
            if (sp.performance > mAveragePerformance + performanceRangeOffset) {
                rankTagTextView.setText("Up");
                rankTagTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.music_runner_up_song_tag));
            } else if (sp.performance <= mAveragePerformance + performanceRangeOffset && sp.performance > mAveragePerformance - performanceRangeOffset) {
                rankTagTextView.setText("Keep");
                rankTagTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.music_runner_keep_song_tag));
            } else {
                rankTagTextView.setText("Easy");
                rankTagTextView.setBackground(getActivity().getResources().getDrawable(R.drawable.music_runner_easy_song_tag));
            }
        }

        Uri songUri = MusicLib.getMusicUriWithId(sp.realSongId);
        String songPath = MusicLib.getMusicFilePath(getActivity(), songUri);
        Bitmap albumPhoto = MusicLib.getMusicAlbumArt(songPath);
        if (albumPhoto != null) {
            albumPhotoImageView.setImageBitmap(albumPhoto);
        }

        songRankTemplate.setTag(sp.songId);
        songRankTemplate.setOnClickListener(this);
        musicRankContainer.addView(songRankTemplate);
    }

    private Handler mSongRankUIHandler = new Handler();

    @Override
    public void OnSongRankPrepared(ArrayList<SongPerformance> songPerformanceList, Double averagePerformance) {
        mSongPerformanceList = songPerformanceList;
        mAveragePerformance = averagePerformance;
        if (songPerformanceList.size() > 0) {
            mSongRankUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    int length = mSongPerformanceList.size();
                    Log.d(TAG, Thread.currentThread().getName());
                    for (int i = 0; i < length; i++) {
                        addSongRank(mSongPerformanceList.get(i), (i == 0));
                    }
                }
            });
        } else {
            musicRankInitialInformation.setVisibility(View.VISIBLE);
        }
    }

    private class SongRankLoaderRunnable implements Runnable {
        Fragment mFragment;
        Context mContext;
        ContentResolver mContentResolver;
        OnSongRankPreparedListener listener;
        public SongRankLoaderRunnable (Fragment fragment) {
            mFragment = fragment;
            mContext = fragment.getActivity();
            listener = (OnSongRankPreparedListener) fragment;
            mContentResolver = mContext.getContentResolver();
        }
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            Looper.prepare();

            ArrayList<SongPerformance> songPerformanceList = new ArrayList<SongPerformance>();
            HashMap<String, String> songInfo;
            Integer durationTemp, duration = 0, songId, lastSongId = -1;
            Double caloriesTemp, calories = 0.0, averagePerformance;
            String caloriesString, distanceString, currentEpoch, speedString, songName, artist;
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
            while (cursor.moveToNext()) {
                songId         = cursor.getInt(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_SONG_ID));
                durationTemp   = cursor.getInt(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DURATION));
                caloriesString = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_CALORIES));
                distanceString = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DISTANCE));
                currentEpoch   = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_DATE_IN_MILLISECOND));
                speedString    = cursor.getString(cursor.getColumnIndex(MusicRunnerSongPerformanceDB.COLUMN_NAME_SPEED));

                caloriesTemp = Double.parseDouble(caloriesString);
                calories += caloriesTemp;
                duration += durationTemp;
            /*
                this exception should be removed since the divided by zero should be handled in FinishRunning page
             */
                try {
                    if (lastSongId != songId) {
                        songInfo = MusicLib.getSongInfo(mContext, songId);
                        artist = MusicLib.getArtist(mContext, Long.parseLong(songInfo.get(MusicLib.ARTIST_ID)));
                        SongPerformance sp = new SongPerformance(duration, Double.parseDouble(distanceString), caloriesTemp, Double.parseDouble(speedString), songInfo.get(MusicLib.SONG_NAME), artist);
                        sp.setSongId(songId);
                        sp.setRealSongId(Long.parseLong(songInfo.get(MusicLib.SONG_REAL_ID)));
                        songPerformanceList.add(sp);
                        lastSongId = songId;
                    } else {
                        SongPerformance sp = songPerformanceList.get(songPerformanceList.size() - 1);
                        sp.addSongRecord(duration, Double.parseDouble(distanceString), Double.parseDouble(caloriesString));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            Collections.sort(songPerformanceList);
            averagePerformance = calories*60000/duration.doubleValue();
            listener.OnSongRankPrepared(songPerformanceList, averagePerformance);
        }
    }

    private void getMusicRanks() {
        /*HashMap<String, String> songInfo;
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

            try {
                if (lastSongId != songId) {
                    songInfo = MusicLib.getSongInfo(getActivity(), songId);
                    artist = MusicLib.getArtist(getActivity(), Long.parseLong(songInfo.get(MusicLib.ARTIST_ID)));
                    SongPerformance sp = new SongPerformance(duration, Double.parseDouble(distance), caloriesTemp, Double.parseDouble(speed), songInfo.get(MusicLib.SONG_NAME), artist);
                    sp.setSongId(songId);
                    sp.setRealSongId(Long.parseLong(songInfo.get(MusicLib.SONG_REAL_ID)));
                    songPerformanceList.add(sp);
                    lastSongId = songId;
                } else {
                    SongPerformance sp = songPerformanceList.get(songPerformanceList.size() - 1);
                    sp.addSongRecord(duration, Double.parseDouble(distance), Double.parseDouble(calories));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        Collections.sort(songPerformanceList);
        averagePerformance = totalCalories*60/totalDuration.doubleValue();*/
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
