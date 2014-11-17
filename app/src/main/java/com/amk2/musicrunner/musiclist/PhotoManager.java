package com.amk2.musicrunner.musiclist;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by daz on 11/16/14.
 */
public class PhotoManager {
    public static final int STATE_LOAD_IMAGE = 0;
    public static final int STATE_LOAD_COMPLETE = 1;
    public static final int STATE_LOAD_CANCEL = 2;
    static PhotoManager instance;
    static {
        instance = new PhotoManager();
    }

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final int NUMBER_OF_CORES = 2;//Runtime.getRuntime().availableProcessors();

    private final ThreadPoolExecutor mPhotoLoaderPool;
    private final BlockingQueue<Runnable> mPhotoLoaderQueue;

    public static PhotoManager getInstance () {
        return instance;
    }
    private PhotoManager () {
        mPhotoLoaderQueue = new LinkedBlockingDeque<Runnable>();
        Log.d("Photo manager", "mPhotoLoaderQueue: " + mPhotoLoaderQueue.toString());
        mPhotoLoaderQueue.add(new Runnable() {
            @Override
            public void run() {
                // do nothing
            }
        });
        Log.d("Photo manager", "mPhotoLoaderQueue: " + mPhotoLoaderQueue.toString());
        mPhotoLoaderPool = new ThreadPoolExecutor(
                0, // initial pool size
                2, // max pool size
                1,
                TimeUnit.SECONDS,
                mPhotoLoaderQueue);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            switch (message.what){
                case STATE_LOAD_COMPLETE:
                    PhotoLoadTask task = (PhotoLoadTask) message.obj;
                    ImageView view = (ImageView) task.getTargetView();
                    Bitmap bitmap = task.getBitmap();
                    view.setImageBitmap(bitmap);
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    };

    public void handleState (PhotoLoadTask task, int state) {
        switch (state) {
            case STATE_LOAD_IMAGE:
                mPhotoLoaderPool.execute(task.getPhotoLoadRunnable());
                break;
            case STATE_LOAD_COMPLETE:
                Message completeMessage = handler.obtainMessage(STATE_LOAD_COMPLETE, task);
                completeMessage.sendToTarget();
                break;
            case STATE_LOAD_CANCEL:
                mPhotoLoaderPool.remove(task.getPhotoLoadRunnable());
                break;
        }
    }

    public void cancelAll () {
        Runnable[] runnables = new Runnable[mPhotoLoaderQueue.size()];
        mPhotoLoaderQueue.toArray(runnables);
        int length = runnables.length;

        synchronized (instance) {
            for (int i = 0; i < length; i ++) {
            }
        }
    }
}
