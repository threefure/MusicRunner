package com.amk2.musicrunner.utilities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by logicmelody on 2014/9/21.
 */
public class MusicLib {

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
}
