package com.amk2.musicrunner.musiclist;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.musiclist.MusicMetaData;
import com.amk2.musicrunner.musiclist.PlaylistManager;
import com.amk2.musicrunner.musiclist.PlaylistMetaData;
import com.amk2.musicrunner.running.MusicSong;
import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daz on 10/12/14.
 */
public class MusicRunnerSongSelectorActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener{
    public static final String PLAYLIST_ID = "playlist_id";
    private static final String[] MUSIC_SELECT_PROJECTION = new String[] {
            android.provider.MediaStore.Audio.Media._ID,
            android.provider.MediaStore.Audio.Media.TITLE,
            android.provider.MediaStore.Audio.Media.ARTIST,
            android.provider.MediaStore.Audio.Media.DURATION
    };
    private static final Integer MUSIC_LOADER_ID = 1;
    private LayoutInflater inflater;
    private ArrayList<MusicSong> allSongsArrayList;
    private SongSelectorAdapter mSongSelectorAdapter;
    private Long playlistId;

    private TextView cancelTextView;
    private TextView okTextView;
    private AlertDialog.Builder dialog;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_song);
        playlistId = getIntent().getExtras().getLong(PLAYLIST_ID);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        initViews();
        setViews();
        //getListView().setEmptyView(findViewById(R.id.empty));
        allSongsArrayList = new ArrayList<MusicSong>();
        mSongSelectorAdapter = new SongSelectorAdapter(this, R.layout.select_song_template, allSongsArrayList);
        setListAdapter(mSongSelectorAdapter);

        getLoaderManager().initLoader(MUSIC_LOADER_ID, null, this);

    }

    @Override
    protected void onStart () {
        super.onStart();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, MusicLib.getMusicUri(), MUSIC_SELECT_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        allSongsArrayList = convertCursorToMusicSongList(cursor);
        mSongSelectorAdapter.updateSongArrayList(allSongsArrayList);
        mSongSelectorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        // Do something when a list item is clicked
        SongSelectorAdapter.ViewTag viewTag = (SongSelectorAdapter.ViewTag) view.getTag();
        if (!mSongSelectorAdapter.isSelected(position)) {
            viewTag.selected.setBackground(getResources().getDrawable(R.drawable.music_runner_clickable_red_orund_border));
        } else {
            viewTag.selected.setBackground(getResources().getDrawable(R.drawable.music_runner_clickable_grass_round_border));
        }
        mSongSelectorAdapter.toggle(position);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                dialog.show();
                break;
            case R.id.ok:
                ArrayList<MusicSong> selectedSongsArrayList = mSongSelectorAdapter.getSelectedSongs();
                PlaylistMetaData playlistMetaData = MusicLib.getPlaylistMetadata(this, playlistId);
                Uri playlistMemberUri = MusicLib.getPlaylistMemberUriFromId(playlistId);
                int length = selectedSongsArrayList.size();
                int order = playlistMetaData.mTracks + 1;
                for (int i = 0; i < length; i++) {
                    MusicLib.insertSongToPlaylist(this, playlistMemberUri, selectedSongsArrayList.get(i).mId, order);
                    order ++;
                }
                if(length > 0) {
                    setResult(RESULT_OK);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
                break;
        }
    }

    private void initViews () {
        cancelTextView = (TextView) findViewById(R.id.cancel);
        okTextView     = (TextView) findViewById(R.id.ok);
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

    private void setViews () {
        cancelTextView.setOnClickListener(this);
        okTextView.setOnClickListener(this);
    }

    public ArrayList<MusicSong> convertCursorToMusicSongList(Cursor cursor) {
        ArrayList<MusicSong> songList = new ArrayList<MusicSong>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Integer trackDuration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                Uri musicUri = ContentUris.withAppendedId(MusicLib.getMusicUri(), id);
                String musicFilePath = MusicLib.getMusicFilePath(this, musicUri);
                if(PlaylistManager.isMusicFile(musicFilePath)) {
                    songList.add(new MusicSong(id, title, artist, trackDuration));
                }
            }
        }
        return songList;
    }

    private class SongSelectorAdapter extends ArrayAdapter<MusicSong>{
        private ArrayList<MusicSong> mSongArrayList;
        private ArrayList<Boolean> mSongIsSelectedArrayList;
        private int mResourceId;
        public SongSelectorAdapter(Context context, int resource, List<MusicSong> objects) {
            super(context, resource, objects);
            mResourceId = resource;
            mSongArrayList = (ArrayList<MusicSong>) objects;
            mSongIsSelectedArrayList = new ArrayList<Boolean>(mSongArrayList.size());
            initializeSelectedItems();
        }

        public void updateSongArrayList (ArrayList<MusicSong> songArrayList) {
            mSongArrayList = songArrayList;
            mSongIsSelectedArrayList = new ArrayList<Boolean>(mSongArrayList.size());
            initializeSelectedItems();
        }

        private void initializeSelectedItems() {
            int length = mSongArrayList.size();
            for (int i = 0; i < length; i ++) {
                mSongIsSelectedArrayList.add(false);
            }
        }

        public boolean isSelected (int i) {
            return mSongIsSelectedArrayList.get(i);
        }

        public void toggle (int i) {
            boolean isSelected = mSongIsSelectedArrayList.get(i);
            mSongIsSelectedArrayList.set(i, !isSelected);
        }

        public ArrayList<MusicSong> getSelectedSongs () {
            ArrayList<MusicSong> selectedSongsArrayList = new ArrayList<MusicSong>();
            int length = mSongIsSelectedArrayList.size();
            for (int i = 0; i < length; i ++) {
                if (mSongIsSelectedArrayList.get(i)) {
                    selectedSongsArrayList.add(mSongArrayList.get(i));
                }
            }
            return selectedSongsArrayList;
        }

        @Override
        public int getCount() {
            return mSongArrayList.size();
        }

        @Override
        public MusicSong getItem(int i) {
            return mSongArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return mSongArrayList.get(i).mId;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            MusicSong ms = mSongArrayList.get(i);
            ViewTag viewTag;
            String durationString = TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(ms.mDuration / 1000));
            if (view == null ){
                view = inflater.inflate(mResourceId, null);
                viewTag = new ViewTag(
                        (ImageView)view.findViewById(R.id.album_cover_photo),
                        (TextView)view.findViewById(R.id.title),
                        (TextView)view.findViewById(R.id.artist),
                        (TextView)view.findViewById(R.id.duration),
                        (TextView)view.findViewById(R.id.selected));
                view.setTag(viewTag);
            } else {
                viewTag = (ViewTag) view.getTag();
            }
            viewTag.title.setText(ms.mTitle);
            viewTag.artist.setText(ms.mArtist);
            viewTag.duration.setText(durationString);
            if (mSongIsSelectedArrayList.get(i)) {
                viewTag.selected.setBackground(getResources().getDrawable(R.drawable.music_runner_clickable_red_orund_border));
            } else {
                viewTag.selected.setBackground(getResources().getDrawable(R.drawable.music_runner_clickable_grass_round_border));
            }
            return view;
        }

        private class ViewTag {
            ImageView albumCoverPhoto;
            TextView title;
            TextView artist;
            TextView duration;
            TextView selected;
            public ViewTag (ImageView albumCoverPhoto, TextView title, TextView artist, TextView duration, TextView selected) {
                this.albumCoverPhoto = albumCoverPhoto;
                this.title = title;
                this.artist = artist;
                this.duration = duration;
                this.selected = selected;
            }
        }
    }

}