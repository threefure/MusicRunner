package com.amk2.musicrunner.musiclist;

import android.app.Fragment;
import android.app.ListFragment;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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

import com.hb.views.PinnedSectionListView;
import com.hb.views.PinnedSectionListView.PinnedSectionListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private PinnedSectionListView playlistContainer;
    SharedPreferences playlistPreferences;

    ArrayList<Object> mPlaylistMetaData;

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
        mPlaylistMetaData = new ArrayList<Object>();

        initViews();
        setViews();
    }

    @Override
    public void onResume () {
        // put initLoader in onResume because the onLoadFinished would be called twice if initLoader is put in onActivityCreated
        // according to http://developer.android.com/guide/components/fragments.html#Creating
        // and http://stackoverflow.com/questions/11293441/android-loadercallbacks-onloadfinished-called-twice
        // but....not work!
        super.onResume();
        if (mPlaylistMetaData.size() == 0) {
            SongLoaderRunnable loader = new SongLoaderRunnable(this);
            Thread loaderThread = new Thread(loader);
            loaderThread.start();
        }
    }

    @Override
    public void onStop () {
        super.onStop();
    }

    private void initViews() {
        View thisView = getView();
        playlistContainer = (PinnedSectionListView) thisView.findViewById(R.id.playlist_container);
    }
    private void setViews() {
        playlistContainer.setAdapter(new PlaylistPinnedSectionListAdapter(getActivity(), R.layout.music_list_item_template, mPlaylistMetaData));
        PlaylistPinnedSectionListAdapter adapter = (PlaylistPinnedSectionListAdapter) playlistContainer.getAdapter();
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

    private Handler mPlaylistUIHandler = new Handler();

    @Override
    public void OnPlaylistPrepared(ArrayList<Object> playlistMetaDataHashMap) {
        mPlaylistMetaData = playlistMetaDataHashMap;
        mPlaylistUIHandler.post(new Runnable() {
            @Override
            public void run() {
                PlaylistPinnedSectionListAdapter adapter = (PlaylistPinnedSectionListAdapter) playlistContainer.getAdapter();
                adapter.updatePlaylistArrayList(mPlaylistMetaData);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private class PlaylistPinnedSectionListAdapter extends ArrayAdapter<Object> implements PinnedSectionListAdapter, View.OnClickListener {
        private final int TYPE_SECTION  = 0;
        private final int TYPE_PLAYLIST = 1;
        private boolean isPlaylistInitialized = false;
        private ArrayList<Object> mPlaylistArrayList;
        private View mSelectedPlaylist = null;
        private Context mContext;

        public PlaylistPinnedSectionListAdapter(Context context, int resource, ArrayList<Object> list) {
            super(context, resource, list);
            mContext = context;
            mPlaylistArrayList = list;
        }

        public void updatePlaylistArrayList (ArrayList<Object> playlistArrayList) {
            mPlaylistArrayList = playlistArrayList;
        }

        @Override
        public int getItemViewType (int position) {
            if (mPlaylistArrayList.get(position) instanceof PlaylistMetaData) {
                return TYPE_PLAYLIST;
            }
            return TYPE_SECTION;
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            if (viewType == TYPE_SECTION) {
                return true;
            }
            return false;
        }

        @Override
        public int getCount() {
            return mPlaylistArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return mPlaylistArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            if (mPlaylistArrayList.get(i) instanceof PlaylistMetaData) {
                return ((PlaylistMetaData) mPlaylistArrayList.get(i)).mId;
            }
            return -1;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View musicListTemplate;

            if (mPlaylistArrayList.get(i) instanceof PlaylistMetaData) {
                // playlist data
                PlaylistMetaData playlistMetaData = (PlaylistMetaData) mPlaylistArrayList.get(i);
                musicListTemplate = inflater.inflate(R.layout.music_list_template, null);
                TextView titleTextView    = (TextView) musicListTemplate.findViewById(R.id.playlist_title);
                TextView tracksTextView   = (TextView) musicListTemplate.findViewById(R.id.playlist_tracks);
                TextView choosePlaylistTextView = (TextView) musicListTemplate.findViewById(R.id.choose_playlist);
                titleTextView.setText(playlistMetaData.mTitle);
                tracksTextView.setText(playlistMetaData.mTracks.toString());

                // set song id to tag and onClick event
                musicListTemplate.setTag(playlistMetaData.mId);
                musicListTemplate.setOnClickListener(this);

                // set song id to tag and onClick event
                choosePlaylistTextView.setTag(playlistMetaData.mId);
                choosePlaylistTextView.setOnClickListener(this);
                if (!isPlaylistInitialized) {
                    isPlaylistInitialized = true;
                    choosePlaylistTextView.setBackground(mContext.getResources().getDrawable(R.drawable.music_runner_clickable_red_orund_border));
                    playlistPreferences.edit().remove("id").putLong("id", playlistMetaData.mId).commit();
                    mSelectedPlaylist = choosePlaylistTextView;
                }
            } else {
                // section title
                PlaylistSectionData playlistSectionData = (PlaylistSectionData) mPlaylistArrayList.get(i);
                musicListTemplate = inflater.inflate(R.layout.music_list_section_title_template, null);
                TextView titleTextView = (TextView) musicListTemplate.findViewById(R.id.playlist_section_title);
                titleTextView.setText(playlistSectionData.mSectionTitle);
            }
            return musicListTemplate;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.playlist:
                    Intent intent = new Intent(mContext, MusicListDetailActivity.class);
                    intent.putExtra(MusicListDetailActivity.PLAYLIST_MEMBER_ID, (Long) view.getTag());
                    startActivity(intent);
                    break;
                case R.id.choose_playlist:
                    Long newPlaylistId = (Long) view.getTag();
                    playlistPreferences.edit().remove("id").putLong("id", newPlaylistId).commit();

                    view.setBackground(mContext.getResources().getDrawable(R.drawable.music_runner_clickable_red_orund_border));
                    mSelectedPlaylist.setBackground(mContext.getResources().getDrawable(R.drawable.music_runner_clickable_grass_round_border));
                    mSelectedPlaylist = view;
                    break;
            }
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
            ArrayList<Object> playlistMetaDatas = new ArrayList<Object>();
            PlaylistMetaData halfHourSlowPlaylistMetaData = playlistManager.generate30MinsPlaylist(PlaylistManager.SLOW_PACE_PLAYLIST);
            PlaylistMetaData halfHourMediumPlaylistMetaData = playlistManager.generate30MinsPlaylist(PlaylistManager.MEDIUM_PACE_PLAYLIST);
            PlaylistMetaData oneHourSlowPlaylistMetaData = playlistManager.generate1HrPlaylist(PlaylistManager.SLOW_PACE_PLAYLIST);
            PlaylistMetaData oneHourMediumPlaylistMetaData = playlistManager.generate1HrPlaylist(PlaylistManager.MEDIUM_PACE_PLAYLIST);
            playlistMetaDatas.add(new PlaylistSectionData("30 Mins Playlist"));
            playlistMetaDatas.add(halfHourSlowPlaylistMetaData);
            playlistMetaDatas.add(halfHourMediumPlaylistMetaData);
            playlistMetaDatas.add(new PlaylistSectionData("1 Hr Playlist"));
            playlistMetaDatas.add(oneHourSlowPlaylistMetaData);
            playlistMetaDatas.add(oneHourMediumPlaylistMetaData);

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
