package com.amk2.musicrunner.utilities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData;

import org.apache.http.client.utils.URIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by logicmelody on 2014/9/21.
 */
public class MusicLib {
    private static final Uri MUSIC_URI = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    public static final String ID           = "id";
    public static final String SONG_NAME    = "songName";
    public static final String TITLE        = "title";
    public static final String ARTIST       = "artist";
    public static final String ARTIST_ID    = "artistId";
    public static final String SONG_REAL_ID = "songRealId";
    public static final String BPM          = "bpm";
    public static final String TEMPO        = "tempo";
    public static final String FAST_PLAYLIST   = "fast";
    public static final String MEDIUM_PLAYLIST = "medium";
    public static final String SLOW_PLAYLIST   = "slow";
    public static final String FAST_PLAYLIST_NAME   = "Ninja";
    public static final String MEDIUM_PLAYLIST_NAME = "Human";
    public static final String SLOW_PLAYLIST_NAME   = "Turtle";

    public static Uri getMusicUri() {
        return MUSIC_URI;
    }

    public static Uri getMusicUriWithId(Long id) {
        Uri musicUri = ContentUris.withAppendedId(MUSIC_URI, id);
        return musicUri;
    }

    public static String getMusicFilePath(Context context, Uri uri) {
        String filePath = null;
        if(uri != null) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, new String[]{
                        MediaStore.Audio.AudioColumns.DATA
                }, null, null, null);
                cursor.moveToFirst();
                filePath = cursor.getString(0);

                if(filePath == null || filePath.trim().isEmpty()) {
                    filePath = uri.toString();
                }
            } catch(Exception e) {
                e.printStackTrace();
                filePath = uri.toString();
            } finally {
                if(cursor != null) {
                    cursor.close();
                }
            }
        } else {
            filePath = uri.toString();
        }

        return filePath;
    }

    public static Bitmap getMusicAlbumArt(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            byte[] data = retriever.getEmbeddedPicture();
            if (data != null) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
            if (bitmap == null) {
                return null;
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        } catch(NoSuchMethodError ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    public static HashMap<String, String> getSongInfo (Context context, Integer songId) {
        HashMap<String, String> songInfo = new HashMap<String, String>();
        String[] projection = {
                MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_NAME,
                MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ARTIST_ID,
                MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_REAL_ID
        };
        String selection = MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = { songId.toString() };

        Cursor cursor = context.getContentResolver().query(MusicRunnerDBMetaData.MusicRunnerSongNameDB.CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Long artistId   = cursor.getLong(cursor.getColumnIndex(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ARTIST_ID));
            Long songRealId = cursor.getLong(cursor.getColumnIndex(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_REAL_ID));
            songInfo.put(SONG_NAME, cursor.getString(cursor.getColumnIndex(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_NAME)));
            songInfo.put(ARTIST_ID, artistId.toString());
            songInfo.put(SONG_REAL_ID, songRealId.toString());
        }
        cursor.close();
        return songInfo;
    }

    public static HashMap<String, String> getSongInfo (Context context, String name) {
        HashMap<String, String> songInfo = null;//new HashMap<String, String>();
        String[] projection = {
                MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ID,
                MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ARTIST_ID,
                MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_REAL_ID,
                MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_BPM
        };
        String selection = MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_NAME + " = ?";
        String[] selectionArgs = { name };

        Cursor cursor = context.getContentResolver().query(MusicRunnerDBMetaData.MusicRunnerSongNameDB.CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Long id         = cursor.getLong(cursor.getColumnIndex(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ID));
            Long artistId   = cursor.getLong(cursor.getColumnIndex(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ARTIST_ID));
            Long songRealId = cursor.getLong(cursor.getColumnIndex(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_REAL_ID));
            String bpm      = cursor.getString(cursor.getColumnIndex(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_BPM));
            songInfo = new HashMap<String, String>();
            songInfo.put(ID, id.toString());
            songInfo.put(ARTIST_ID, artistId.toString());
            songInfo.put(SONG_REAL_ID, songRealId.toString());
            songInfo.put(BPM, bpm);
        }
        cursor.close();
        return songInfo;
    }

    public static Uri insertSongInfo (Context context, HashMap<String, String> songInfo) {
        Long artistId = Long.parseLong(songInfo.get(ARTIST_ID));
        Double bpm = Double.parseDouble(songInfo.get(BPM));
        ContentValues values = new ContentValues();
        if (artistId == null) {
            artistId = getArtistId(context, songInfo.get(ARTIST));
        }
        if (artistId == null) {
            artistId = insertArtist(context, songInfo.get(ARTIST));
        }
        if (bpm == -2) {
            bpm = null;
        }

        values.put(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_REAL_ID, songInfo.get(SONG_REAL_ID));
        values.put(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_NAME, songInfo.get(TITLE));
        values.put(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ARTIST_ID, artistId);
        values.put(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_BPM, bpm.toString());

        Uri uri = context.getContentResolver().insert(MusicRunnerDBMetaData.MusicRunnerSongNameDB.CONTENT_URI, values);
        return uri;
    }

    public static Uri insertSongInfo (Context context, JSONObject songInfo) {
        Long artistId = null;
        Long songRealId = null;
        Double bpm = null;
        String title = null;
        String artist = null;

        try {
            //artistId = songInfo.getLong("artistId");
            songRealId = songInfo.getLong(SONG_REAL_ID);
            bpm = songInfo.getDouble(BPM);
            title = URLDecoder.decode(songInfo.getString(TITLE), "UTF-8");
            artist = URLDecoder.decode(songInfo.getString(ARTIST), "UTF-8");
            artistId = getArtistId(context, artist);
            if (artistId == null) {
                artistId = insertArtist(context, artist);
            }
            //if (bpm == -2) {
            //    bpm = null;
            //}
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_REAL_ID, songRealId);
        values.put(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_SONG_NAME, title);
        values.put(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ARTIST_ID, artistId);
        values.put(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_BPM, bpm.toString());

        Uri uri = context.getContentResolver().insert(MusicRunnerDBMetaData.MusicRunnerSongNameDB.CONTENT_URI,values);
        return uri;
    }

    public static Integer updateSongInfoBPM (Context context, JSONObject songInfo) {
        Long songId = null;
        Double bpm = null;

        try {
            songId = songInfo.getLong(ID);
            bpm = songInfo.getDouble(BPM);
            if (bpm == -2) {
                bpm = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        ContentValues values = new ContentValues();
        String selection = MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = { songId.toString() };
        values.put(MusicRunnerDBMetaData.MusicRunnerSongNameDB.COLUMN_NAME_BPM, bpm.toString());

        Integer cnt = context.getContentResolver().update(MusicRunnerDBMetaData.MusicRunnerSongNameDB.CONTENT_URI, values, selection, selectionArgs);
        return cnt;
    }

    public static String getArtist (Context context, Long artistId) {
        String[] projection = {
                MusicRunnerDBMetaData.MusicRunnerArtistDB.COLUMN_NAME_ARTIST
        };
        String artist = "";
        String selection = MusicRunnerDBMetaData.MusicRunnerArtistDB.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = { artistId.toString() };

        Cursor cursor = context.getContentResolver().query(MusicRunnerDBMetaData.MusicRunnerArtistDB.CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            artist = cursor.getString(cursor.getColumnIndex(MusicRunnerDBMetaData.MusicRunnerArtistDB.COLUMN_NAME_ARTIST));
        }
        cursor.close();
        return artist;
    }

    public static Long getArtistId (Context context, String artist) {
        String[] artistProjection = {
                MusicRunnerDBMetaData.MusicRunnerArtistDB.COLUMN_NAME_ID
        };
        String artistSelection = MusicRunnerDBMetaData.MusicRunnerArtistDB.COLUMN_NAME_ARTIST + " = ?";
        String[] artistSelectionArgs = {artist};
        Long artistId = null;
        Cursor cursor = context.getContentResolver().query(MusicRunnerDBMetaData.MusicRunnerArtistDB.CONTENT_URI,
                artistProjection, artistSelection, artistSelectionArgs, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            artistId = cursor.getLong(cursor.getColumnIndex(MusicRunnerDBMetaData.MusicRunnerArtistDB.COLUMN_NAME_ID));
        }
        cursor.close();
        return artistId;
    }

    public static Long insertArtist (Context context, String artist) {
        ContentValues values = new ContentValues();
        values.put(MusicRunnerDBMetaData.MusicRunnerArtistDB.COLUMN_NAME_ARTIST, artist);
        Uri uri = context.getContentResolver().insert(MusicRunnerDBMetaData.MusicRunnerArtistDB.CONTENT_URI, values);
        return ContentUris.parseId(uri);
    }

    public static Uri getPlaylistUri (Context context, String playlistTitle) {
        String[] projection = {
                MediaStore.Audio.Playlists._ID
        };
        String selection = MediaStore.Audio.Playlists.NAME + " = ?";
        String[] selectionArgs = {playlistTitle};
        Long playlistId;
        Uri playlistUri = null;
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            playlistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
            playlistUri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, playlistId);
        }
        return playlistUri;
    }

    public static Uri getPlaylistUriFromId (Long id) {
        return ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
    }

    public static Uri getPlaylistMemberUri (Uri playlistUri) {
        Long id = ContentUris.parseId(playlistUri);
        return MediaStore.Audio.Playlists.Members.getContentUri("external", id);
    }

    public static Uri getPlaylistMemberUriFromId (Long id) {
        return MediaStore.Audio.Playlists.Members.getContentUri("external", id);
    }

    public static Uri insertPlaylistId (Context context, String playlistTitle) {
        Calendar calendar = Calendar.getInstance();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME, playlistTitle);
        values.put(MediaStore.Audio.Playlists.DATE_ADDED, calendar.getTimeInMillis());
        values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, calendar.getTimeInMillis());
        Uri uri = context.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
        return uri;
    }

    public static Integer cleanPlaylist (Context context, Uri playlistUri) {
        Long id = ContentUris.parseId(playlistUri);
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        int cnt = context.getContentResolver().delete(uri, null, null);
        return cnt;
    }

    public static Uri insertSongToPlaylist (Context context, Uri playlistUri, Long songRealId, Integer playOrder) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, playOrder);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songRealId);
        Uri uri = context.getContentResolver().insert(playlistUri, values);
        return uri;
    }
}
