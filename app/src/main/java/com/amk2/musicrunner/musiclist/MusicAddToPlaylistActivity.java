package com.amk2.musicrunner.musiclist;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.MusicSong;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.OnPlaylistPreparedListener;
import com.hb.views.PinnedSectionListView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by daz on 11/4/14.
 */
public class MusicAddToPlaylistActivity extends Activity implements OnPlaylistPreparedListener, AdapterView.OnItemClickListener, View.OnClickListener {
    public static final String SONG_REAL_ID = "song_real_id";
    public static final String UPDATE_ALL_PLAYLIST = "update_all_playlist";
    private LayoutInflater inflater;
    private static final int MUSIC_LOADER_ID = 1;
    private static final String[] MUSIC_SELECT_PROJECTION = new String[] {
            android.provider.MediaStore.Audio.Media._ID,
            android.provider.MediaStore.Audio.Media.TITLE,
            android.provider.MediaStore.Audio.Media.ARTIST,
            android.provider.MediaStore.Audio.Media.DURATION
    };

    ArrayList<Object> mPlaylistMetaData;

    private PinnedSectionListView playlistContainer;
    private PlaylistPinnedSectionListAdapter playlistPinnedSectionListAdapter;
    private ActionBar mActionBar;

    private TextView okTextView;
    private TextView cancelTextView;
    private AlertDialog.Builder dialog;

    private Long songRealId;

    @Override
    protected void onCreate (Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_add_to_playlist);

        Intent intent = getIntent();
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mPlaylistMetaData = new ArrayList<Object>();
        mActionBar = getActionBar();
        songRealId = intent.getExtras().getLong(SONG_REAL_ID);

        initActionBar();
        initViews();
        setViews();

        if (mPlaylistMetaData.size() == 0) {
            SongLoaderRunnable loader = new SongLoaderRunnable(this);
            Thread loaderThread = new Thread(loader);
            loaderThread.start();
        }
    }

    private void initActionBar() {
        mActionBar.hide();
    }

    private void initViews() {
        playlistContainer = (PinnedSectionListView) findViewById(R.id.playlist_container);
        playlistContainer.setOnItemClickListener(this);

        okTextView = (TextView) findViewById(R.id.ok);
        cancelTextView = (TextView) findViewById(R.id.cancel);
        dialog = new AlertDialog.Builder(this)
                .setMessage("Are you sure ending editing your playlist?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
    }

    private void setViews() {
        playlistContainer.setShadowVisible(false);
        playlistContainer.setAdapter(new PlaylistPinnedSectionListAdapter(this, R.layout.music_list_template, mPlaylistMetaData));
        playlistPinnedSectionListAdapter = (PlaylistPinnedSectionListAdapter) playlistContainer.getAdapter();

        okTextView.setOnClickListener(this);
        cancelTextView.setOnClickListener(this);
    }

    private Handler handler = new Handler();

    @Override
    public void OnPlaylistPrepared(ArrayList<Object> playlistMetaDataHashMap) {
        mPlaylistMetaData = playlistMetaDataHashMap;
        handler.post(new Runnable() {
            @Override
            public void run() {
                playlistPinnedSectionListAdapter.updatePlaylistArrayList(mPlaylistMetaData);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // on adapter item clicks
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok :
                PlaylistManager playlistManager = PlaylistManager.getInstance();
                ArrayList<Boolean> playlistSelection = playlistPinnedSectionListAdapter.getPlaylistSelection();
                PlaylistMetaData playlistMetaData;
                Uri playlistUri, playlistMemberUri;
                boolean isUpdatePlaylist = false;
                for (int i = 0; i < playlistSelection.size(); i++) {
                    if (playlistSelection.get(i)) {
                        playlistMetaData = (PlaylistMetaData) mPlaylistMetaData.get(i);
                        playlistUri = playlistMetaData.mUri;
                        playlistMemberUri = playlistManager.getPlaylistMemberUri(playlistUri);
                        playlistManager.addToPlaylist(playlistMemberUri, songRealId, playlistMetaData.mTracks);
                        isUpdatePlaylist = true;
                    }
                }
                if (isUpdatePlaylist) {
                    Intent intent = new Intent(MusicListFragment.UPDATE_PLAYLIST);
                    intent.putExtra(UPDATE_ALL_PLAYLIST, true);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
                finish();
                break;
            case R.id.cancel:
                dialog.show();
                break;
        }
    }


    private class PlaylistPinnedSectionListAdapter extends ArrayAdapter<Object> implements PinnedSectionListView.PinnedSectionListAdapter, View.OnClickListener {
        private final int TYPE_SECTION  = 0;
        private final int TYPE_PLAYLIST = 1;
        SharedPreferences mPlaylistPreferences;
        private ArrayList<Object> mPlaylistArrayList;
        private ArrayList<Boolean> mPlaylistSelection;
        private Context mContext;

        public PlaylistPinnedSectionListAdapter(Context context, int resource, ArrayList<Object> list) {
            super(context, resource, list);
            mContext = context;
            mPlaylistPreferences = mContext.getSharedPreferences("playlist", 0);
            mPlaylistArrayList = list;
        }

        public void updatePlaylistArrayList (ArrayList<Object> playlistArrayList) {
            mPlaylistArrayList = playlistArrayList;
            mPlaylistSelection = new ArrayList<Boolean>();
            for (int i = 0; i < mPlaylistArrayList.size(); i++) {
                mPlaylistSelection.add(false);
            }
            notifyDataSetChanged();
        }

        public ArrayList<Boolean> getPlaylistSelection () {
            return mPlaylistSelection;
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
            //Long selectedPlaylist = mPlaylistPreferences.getLong("id", -1);
            if (view == null) {
                if (getItemViewType(i) == TYPE_PLAYLIST) {
                    view = inflater.inflate(R.layout.music_list_template, null);

                    PlaylistMetaData playlistMetaData = (PlaylistMetaData) mPlaylistArrayList.get(i);
                    TextView titleTextView          = (TextView) view.findViewById(R.id.playlist_title);
                    TextView tracksTextView         = (TextView) view.findViewById(R.id.playlist_tracks);
                    TextView choosePlaylistTextView = (TextView) view.findViewById(R.id.choose_playlist);
                    PlaylistViewTag playlistViewTag = new PlaylistViewTag(i, titleTextView, tracksTextView, choosePlaylistTextView);

                    titleTextView.setText(playlistMetaData.mTitle);
                    tracksTextView.setText(playlistMetaData.mTracks.toString());

                    // set song id to tag and onClick event
                    view.setTag(playlistViewTag);

                    // set song id to tag and onClick event
                    choosePlaylistTextView.setTag(i);
                    choosePlaylistTextView.setOnClickListener(this);

                    if (mPlaylistSelection.get(i) == false) {
                        // isn't selected
                        choosePlaylistTextView.setBackground(mContext.getResources().getDrawable(R.drawable.playlist_selection_radio_button));
                    } else {
                        // selected
                        choosePlaylistTextView.setBackground(mContext.getResources().getDrawable(R.drawable.playlist_selection_radio_button_selected));
                    }
                } else {
                    PlaylistSectionData playlistSectionData = (PlaylistSectionData) mPlaylistArrayList.get(i);
                    view = inflater.inflate(R.layout.music_list_section_title_template, null);
                    TextView titleTextView = (TextView) view.findViewById(R.id.playlist_section_title);
                    SectionTitleViewTag sectionTitleViewTag = new SectionTitleViewTag(titleTextView);

                    titleTextView.setText(playlistSectionData.mSectionTitle);
                    view.setTag(sectionTitleViewTag);
                }
            } else {
                if (getItemViewType(i) == TYPE_PLAYLIST) {
                    PlaylistViewTag playlistViewTag;
                    PlaylistMetaData playlistMetaData = (PlaylistMetaData) mPlaylistArrayList.get(i);
                    if (view.getTag() instanceof PlaylistViewTag) {
                        playlistViewTag =  (PlaylistViewTag) view.getTag();
                        playlistViewTag.index = i;
                    } else {
                        view = inflater.inflate(R.layout.music_list_template, null);

                        TextView titleTextView          = (TextView) view.findViewById(R.id.playlist_title);
                        TextView tracksTextView         = (TextView) view.findViewById(R.id.playlist_tracks);
                        TextView choosePlaylistTextView = (TextView) view.findViewById(R.id.choose_playlist);
                        playlistViewTag = new PlaylistViewTag(i, titleTextView, tracksTextView, choosePlaylistTextView);

                        // set song id to tag and onClick event
                        view.setTag(playlistViewTag);
                    }
                    playlistViewTag.mTitleTextView.setText(playlistMetaData.mTitle);
                    playlistViewTag.mTracksTextView.setText(playlistMetaData.mTracks.toString());

                    // set song id to tag and onClick event
                    playlistViewTag.mChoosePlaylistTextView.setTag(i);
                    playlistViewTag.mChoosePlaylistTextView.setOnClickListener(this);

                    if (mPlaylistSelection.get(i) == false) {
                        // isn't selected
                        playlistViewTag.mChoosePlaylistTextView.setBackground(mContext.getResources().getDrawable(R.drawable.playlist_selection_radio_button));
                    } else {
                        // selected
                        playlistViewTag.mChoosePlaylistTextView.setBackground(mContext.getResources().getDrawable(R.drawable.playlist_selection_radio_button_selected));
                    }
                } else {
                    // setting section title
                    SectionTitleViewTag sectionTitleViewTag;
                    PlaylistSectionData playlistSectionData = (PlaylistSectionData) mPlaylistArrayList.get(i);
                    if (view.getTag() instanceof SectionTitleViewTag) {
                        sectionTitleViewTag = (SectionTitleViewTag) view.getTag();
                    } else {
                        view = inflater.inflate(R.layout.music_list_section_title_template, null);
                        TextView titleTextView = (TextView) view.findViewById(R.id.playlist_section_title);
                        sectionTitleViewTag = new SectionTitleViewTag(titleTextView);
                        view.setTag(sectionTitleViewTag);
                    }
                    sectionTitleViewTag.mTitleTextView.setText(playlistSectionData.mSectionTitle);

                }
            }
            return view;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.choose_playlist:
                    Integer index = (Integer) view.getTag();
                    if (mPlaylistSelection.get(index) == false) {
                        // isn't selected
                        mPlaylistSelection.set(index, true);
                        view.setBackground(mContext.getResources().getDrawable(R.drawable.playlist_selection_radio_button_selected));
                    } else {
                        // selected
                        mPlaylistSelection.set(index, false);
                        view.setBackground(mContext.getResources().getDrawable(R.drawable.playlist_selection_radio_button));
                    }
                    break;
            }
        }

        public class PlaylistViewTag {
            Integer index;
            TextView mTitleTextView;
            TextView mTracksTextView;
            TextView mChoosePlaylistTextView;
            public PlaylistViewTag (Integer index, TextView title, TextView tracks, TextView choosePlaylist) {
                this.index = index;
                mTitleTextView = title;
                mTracksTextView = tracks;
                mChoosePlaylistTextView = choosePlaylist;
            }
        }
        public class SectionTitleViewTag {
            TextView mTitleTextView;
            public SectionTitleViewTag (TextView title) {
                mTitleTextView = title;
            }
        }
    }

    public class SongLoaderRunnable implements Runnable, LoaderManager.LoaderCallbacks<Cursor> {
        Context mContext;
        OnPlaylistPreparedListener mPlaylistPreparedListener;

        public SongLoaderRunnable (Context context) {
            mContext = context;
            mPlaylistPreparedListener = (OnPlaylistPreparedListener)context;
        }

        @Override
        public void run() {
            Looper.prepare();
            getLoaderManager().initLoader(MUSIC_LOADER_ID, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(mContext, MusicLib.getMusicUri(), MUSIC_SELECT_PROJECTION, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            PlaylistManager playlistManager = PlaylistManager.getInstance();
            playlistManager.init();
            playlistManager.setContext(mContext);
            playlistManager.setCursor(cursor);
            playlistManager.scan();
            ArrayList<Object> playlistMetaDatas = new ArrayList<Object>();
            playlistMetaDatas.add(new PlaylistSectionData(getResources().getString(R.string.your_playlist)));
            ArrayList<PlaylistMetaData> UGPlaylistMetaDatas = playlistManager.getUserGeneratedPlaylist();
            playlistMetaDatas.addAll(UGPlaylistMetaDatas);

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
