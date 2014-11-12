package com.amk2.musicrunner.music;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.musiclist.MusicAddToPlaylistActivity;
import com.amk2.musicrunner.musiclist.MusicListDetailActivity;
import com.amk2.musicrunner.musiclist.MusicMetaData;
import com.amk2.musicrunner.musiclist.PlaylistMetaData;
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
public class MusicRankFragment extends Fragment implements OnSongRankPreparedListener, View.OnClickListener{
    public static final String UPDATE_MUSIC_RANK = "musiclist.update_music_rank";
    private static final String TAG = MusicRankFragment.class.getSimpleName();
    private double performanceRangeOffset = 0.0001;

    private LayoutInflater inflater;

    private RelativeLayout introduction;
    private LinearLayout musicRankInitialInformation;
    private ListView musicRankListView;
    private MusicRankListAdapter musicRankListAdapter;

    private SharedPreferences mSettingSharedPreferences;
    private SharedPreferences mUserInstructionSharedPreferences;

    private Integer unitDistance;
    private boolean hasIntroduced;

    private ArrayList<SongPerformance> mSongPerformanceList;

    private Fragment self;
    private Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.music_rank_fragment, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        self = this;
        mActivity = getActivity();
        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSettingSharedPreferences = mActivity.getSharedPreferences(SettingActivity.SETTING_SHARED_PREFERENCE, 0);
        mUserInstructionSharedPreferences = mActivity.getSharedPreferences(Constant.USER_INSTRUCTION, Context.MODE_PRIVATE);
        unitDistance = mSettingSharedPreferences.getInt(SettingActivity.DISTANCE_UNIT, SettingActivity.SETTING_DISTANCE_KM);
        hasIntroduced = mUserInstructionSharedPreferences.getBoolean(Constant.RANK_PAGE, false);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mUpdateRankReceiver, new IntentFilter(UPDATE_MUSIC_RANK));
        initViews();
        setViews();
    }

    public void onStart () {
        super.onStart();
        if (mSongPerformanceList.size() == 0) {
            SongRankLoaderRunnable songRankLoaderRunnable = new SongRankLoaderRunnable(this);
            Thread loaderThread = new Thread(songRankLoaderRunnable);
            loaderThread.start();
        }
    }

    @Override
    public void onStop () {
        super.onStop();
    }

    private void initViews() {
        View thisView = getView();
        musicRankInitialInformation = (LinearLayout) thisView.findViewById(R.id.initial_information);
        musicRankListView = (ListView) thisView.findViewById(R.id.music_rank_list_view);
        introduction = (RelativeLayout) thisView.findViewById(R.id.introduction);
    }

    private void setViews() {
        mSongPerformanceList = new ArrayList<SongPerformance>();
        musicRankListAdapter = new MusicRankListAdapter(mActivity, R.layout.music_rank_template, mSongPerformanceList);
        musicRankListView.setAdapter(musicRankListAdapter);

        //set up introduction click event
        if (!hasIntroduced) {
            introduction.setVisibility(View.VISIBLE);
            introduction.setOnClickListener(this);
            hasIntroduced = true;
            mUserInstructionSharedPreferences.edit().remove(Constant.RANK_PAGE).putBoolean(Constant.RANK_PAGE, true).commit();
        }
    }

    private BroadcastReceiver mUpdateRankReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SongRankLoaderRunnable songRankLoaderRunnable = new SongRankLoaderRunnable(self);
            Thread loaderThread = new Thread(songRankLoaderRunnable);
            loaderThread.start();
        }
    };

    private Handler mSongRankUIHandler = new Handler();

    @Override
    public void OnSongRankPrepared(ArrayList<SongPerformance> songPerformanceList, Double averagePerformance) {
        mSongPerformanceList = songPerformanceList;
        if (songPerformanceList.size() > 0) {
            musicRankInitialInformation.setVisibility(View.GONE);
            musicRankListView.setVisibility(View.VISIBLE);
            mSongRankUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    musicRankListAdapter.updateSongPerformanceArrayList(mSongPerformanceList);
                }
            });
        } else {
            musicRankInitialInformation.setVisibility(View.VISIBLE);
            musicRankListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.introduction:
                view.setVisibility(View.GONE);
                break;
        }
    }

    private class SongRankLoaderRunnable implements Runnable {
        Fragment mFragment;
        Context mContext;
        ContentResolver mContentResolver;
        OnSongRankPreparedListener listener;
        public SongRankLoaderRunnable (Fragment fragment) {
            mFragment = fragment;
            //mContext = fragment.getActivity();
            listener = (OnSongRankPreparedListener) fragment;
            mContentResolver = mActivity.getContentResolver();
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
                        songInfo = MusicLib.getSongInfo(mActivity, songId);
                        artist = MusicLib.getArtist(mActivity, Long.parseLong(songInfo.get(MusicLib.ARTIST_ID)));
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

    public class MusicRankListAdapter extends ArrayAdapter<SongPerformance> implements View.OnClickListener{
        ArrayList<SongPerformance> mSongPerformanceArrayList;

        public MusicRankListAdapter(Context context, int resource, ArrayList<SongPerformance> songPerformances) {
            super(context, resource);
            mSongPerformanceArrayList = songPerformances;
        }

        public void updateSongPerformanceArrayList (ArrayList<SongPerformance> songPerformances) {
            mSongPerformanceArrayList = songPerformances;
            notifyDataSetChanged();
        }

        @Override
        public int getCount () {
            return mSongPerformanceArrayList.size();
        }

        @Override
        public SongPerformance getItem (int i) {
            return mSongPerformanceArrayList.get(i);
        }

        @Override
        public long getItemId (int i) {
            return mSongPerformanceArrayList.get(i).songId;
        }

        @Override
        public View getView (int i, View view, ViewGroup viewGroup) {
            SongPerformance sp = mSongPerformanceArrayList.get(i);
            TextView songRankTextView;
            TextView titleTextView;
            TextView artistTextView;
            TextView caloriesTextView;
            ImageView albumPhotoImageView;
            if (view == null) {
                // inflate the view
                view = inflater.inflate(R.layout.music_rank_template, null);
                songRankTextView = (TextView) view.findViewById(R.id.song_rank);
                titleTextView    = (TextView) view.findViewById(R.id.title);
                artistTextView   = (TextView) view.findViewById(R.id.artist);
                caloriesTextView = (TextView) view.findViewById(R.id.calories);
                albumPhotoImageView = (ImageView) view.findViewById(R.id.album_photo);

                // setting ViewTag
                SongPerformanceViewTag songPerformanceViewTag = new SongPerformanceViewTag(
                        sp.songId, songRankTextView, titleTextView, artistTextView, caloriesTextView, albumPhotoImageView
                );
                view.setTag(songPerformanceViewTag);

                // setting click listener
                view.setOnClickListener(this);
            } else {
                // view is existed, reuse it
                SongPerformanceViewTag songPerformanceViewTag = (SongPerformanceViewTag)view.getTag();

                // need to reset song id in order to getting correct cong
                songPerformanceViewTag.songId = sp.songId;

                songRankTextView = songPerformanceViewTag.songRank;
                titleTextView    = songPerformanceViewTag.title;
                artistTextView   = songPerformanceViewTag.artist;
                caloriesTextView = songPerformanceViewTag.calories;
                albumPhotoImageView = songPerformanceViewTag.albumPhoto;
            }

            // setting rank number
            songRankTextView.setText(String.valueOf(i + 1));

            // setting title
            titleTextView.setText(sp.name);

            // setting artist
            artistTextView.setText(sp.artist);

            //setting calories
            caloriesTextView.setText(StringLib.truncateDoubleString(sp.calories.toString(), 2));

            // setting album photo
            Uri musicUri = ContentUris.withAppendedId(MusicLib.getMusicUri(), sp.realSongId);
            String filePath = MusicLib.getMusicFilePath(getContext(), musicUri);
            Bitmap albumPhoto = MusicLib.getMusicAlbumArt(filePath);
            if (albumPhoto != null) {
                albumPhotoImageView.setImageBitmap(albumPhoto);
            }

            return view;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.music_rank:
                    SongPerformanceViewTag songPerformanceViewTag = (SongPerformanceViewTag) view.getTag();
                    Integer songId = songPerformanceViewTag.songId;
                    Intent intent = new Intent(getContext(), MusicRankDetailActivity.class);
                    intent.putExtra(MusicRankDetailActivity.SONG_ID, songId);
                    startActivity(intent);
                    break;
            }
        }

        public class SongPerformanceViewTag {
            Integer songId;
            TextView songRank;
            TextView title;
            TextView artist;
            TextView calories;
            ImageView albumPhoto;

            public SongPerformanceViewTag(Integer songId, TextView songRank, TextView title, TextView artist, TextView calories, ImageView albumPhoto) {
                this.songId = songId.intValue();
                this.songRank = songRank;
                this.title = title;
                this.artist = artist;
                this.calories = calories;
                this.albumPhoto = albumPhoto;
            }
        }
    }
}
