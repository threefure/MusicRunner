package com.amk2.musicrunner;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.amk2.musicrunner.Authenticator;

/**
 * Created by ktlee on 5/24/14.
 */
public class AuthenticatorService extends Service {
    private Authenticator authenticator;

    public void onCreate() {
        authenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
