package com.amk2.musicrunner.musiclist;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.amk2.musicrunner.utilities.MusicLib;
import com.amk2.musicrunner.utilities.PhotoLib;
import com.amk2.musicrunner.utilities.StringLib;

/**
 * Created by daz on 11/16/14.
 */
public class PhotoLoadTask {
    public static final int TYPE_ALBUM_PHOTO = 0;
    public static final int RUNNABLE_LOAD_COMPLETE = 0;
    public static final int RUNNABLE_LOAD_NULL     = 2;
    public static final int RUNNABLE_READY_TO_LOAD = 1;
    PhotoManager photoManager;
    PhotoLoadRunnable photoLoadRunnable;
    String filePath;
    Bitmap bitmap;
    View targetView;
    float targetWidth, targetHeight;
    int type;

    public PhotoLoadTask(String filePath, View targetView, float targetWidth, float targetHeight, int type) {
        photoManager = PhotoManager.getInstance();
        photoLoadRunnable = new PhotoLoadRunnable(this);
        this.filePath = filePath;
        this.targetView = targetView;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.type = type;
    }

    public PhotoLoadRunnable getPhotoLoadRunnable () {
        return photoLoadRunnable;
    }

    public View getTargetView () {
        return targetView;
    }

    public Bitmap getBitmap () {
        return bitmap;
    }

    public void handleState (int state) {
        switch (state) {
            case RUNNABLE_LOAD_COMPLETE:
                photoManager.handleState(this, PhotoManager.STATE_LOAD_COMPLETE);
                break;
            case RUNNABLE_READY_TO_LOAD:
                photoManager.handleState(this, PhotoManager.STATE_LOAD_IMAGE);
                break;
            case RUNNABLE_LOAD_NULL:
                // do nothing
                break;
        }
    }

    public class PhotoLoadRunnable implements Runnable {
        PhotoLoadTask photoLoadTask;
        public PhotoLoadRunnable (PhotoLoadTask photoLoadTask) {
            this.photoLoadTask = photoLoadTask;
        }
        @Override
        public void run() {
            switch (type) {
                case TYPE_ALBUM_PHOTO:
                    bitmap = MusicLib.getMusicAlbumArt(filePath);
                    if (bitmap != null) {
                        bitmap = PhotoLib.resizeToFitTarget(bitmap, targetWidth, targetHeight);
                        photoLoadTask.handleState(RUNNABLE_LOAD_COMPLETE);
                    } else {
                        photoLoadTask.handleState(RUNNABLE_LOAD_NULL);
                    }
                    break;
            }
        }
    }
}
