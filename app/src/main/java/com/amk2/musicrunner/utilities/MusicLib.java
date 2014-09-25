package com.amk2.musicrunner.utilities;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData;

import java.util.HashMap;

/**
 * Created by logicmelody on 2014/9/21.
 */
public class MusicLib {
    private static final Uri MUSIC_URI = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    public static final String SONG_NAME = "songName";
    public static final String ARTIST_ID = "artistId";
    public static final String SONG_REAL_ID = "songRealId";

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
        return songInfo;
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
        return artist;
    }
}
