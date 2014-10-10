package com.amk2.musicrunner.utilities;

import com.amk2.musicrunner.musiclist.PlaylistMetaData;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ktlee on 10/9/14.
 */
public interface OnPlaylistPreparedListener {
    void OnPlaylistPrepared (ArrayList<Object> playlistMetaDataHashMap);
}
