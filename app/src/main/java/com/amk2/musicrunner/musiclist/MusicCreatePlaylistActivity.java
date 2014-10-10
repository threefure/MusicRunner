package com.amk2.musicrunner.musiclist;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.utilities.MusicLib;

/**
 * Created by daz on 10/10/14.
 */
public class MusicCreatePlaylistActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "MusicCreatePlaylistActivity";
    private EditText mPlaylistNameEditText;
    private Button mCreatePlaylistButton;
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);
        init();
    }

    private void init () {
        initViews();
        setViews();
    }

    private void initViews() {
        mPlaylistNameEditText = (EditText) findViewById(R.id.edit_playlist_name);
        mCreatePlaylistButton = (Button) findViewById(R.id.create_playlist);
    }

    private void setViews() {
        mCreatePlaylistButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_playlist:
                String playlistName = mPlaylistNameEditText.getText().toString();
                boolean isPlaylistExisted = MusicLib.isPlaylistExisted(this, playlistName);
                if (isPlaylistExisted) {
                    Log.d(TAG, "playlist exists!");
                    Toast.makeText(this, "Playlist Exist! Please pick another name!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "playlist doesn't exists!");
                    Toast.makeText(this, "Playlist doesn't Exist!", Toast.LENGTH_SHORT).show();
                    Uri uri = MusicLib.createPlaylist(this, playlistName);
                    Intent intent = new Intent(MusicListFragment.CREATE_PLAYLIST);
                    intent.putExtra(MusicListFragment.PLAYLIST_URI, ContentUris.parseId(uri));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    finish();
                }
                break;
        }
    }
}
