package com.amk2.musicrunner;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by ktlee on 5/24/14.
 */
public class SyncService extends Service {
    private SyncAdapter syncAdapter = null;
    private static final Object syncAdapterLock = new Object();

    @Override
    public void onCreate() {
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
