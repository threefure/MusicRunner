package com.amk2.musicrunner.running;

import android.os.AsyncTask;

import com.amk2.musicrunner.start.NetworkAccess;

import java.io.IOException;

/**
 * Created by ktlee on 5/12/14.
 */

// store the running event to both server and phone
public class StoreRunningEvent extends AsyncTask<String, Void, Void> {
    NetworkAccess na;

    public StoreRunningEvent () {
        na = new NetworkAccess();
    }
    @Override
    protected Void doInBackground(String... voids) {
        String mockJson = "{hello:'world'}";
        try {
            na.HttpPost(NetworkAccess.storeRunningEventUrlString, mockJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
