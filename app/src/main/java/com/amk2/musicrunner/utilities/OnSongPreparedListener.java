package com.amk2.musicrunner.utilities;

import com.amk2.musicrunner.musiclist.MusicMetaData;

import java.util.ArrayList;

/**
 * Created by daz on 10/9/14.
 */
public interface OnSongPreparedListener {
    void OnSongPrepared(MusicMetaData musicMetaData);
    void OnSongLoadedFinished(ArrayList<MusicMetaData> musicMetaDataArrayList);
}
