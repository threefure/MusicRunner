package com.amk2.musicrunner.running;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.amk2.musicrunner.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by tengchou on 10/21/14.
 */
public class MapService extends Service implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
    private static final String TAG = "MapService";

    public static final String BROADCAST_ACTION = "com.my.package.LOCATION_UPDATE";

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 60;

    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 40;

    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private LocationClient locationClient;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Location service created…");

        locationClient = new LocationClient(this, this, this);
        locationClient.connect();
    }

    // Unregister location listeners
    private void clearLocationData() {
        locationClient.disconnect();

        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // When service destroyed we need to unbind location listeners
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Location service destroyed…");

        clearLocationData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Calling command…");

        return START_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Location Callback. onConnected");

        Location currentLocation = locationClient.getLastLocation();

        // Create the LocationRequest object
        LocationRequest locationRequest = LocationRequest.create();

        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the update interval to 5 seconds
        locationRequest.setInterval(UPDATE_INTERVAL);

        // Set the fastest update interval to 1 second
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        locationClient.requestLocationUpdates(locationRequest, this);

        onLocationChanged(currentLocation);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        Log.d(TAG, "LOCATION: " + location.getLatitude() + ":" + location.getLongitude());

        // Since location information updated, broadcast it
        Intent broadcast = new Intent();

        // Set action so other parts of application can distinguish and use this information if needed
        broadcast.setAction(BROADCAST_ACTION);
        broadcast.putExtra("latitude", location.getLatitude());
        broadcast.putExtra("longitude", location.getLongitude());

        sendBroadcast(broadcast);
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Location Callback. onDisconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Location Callback. onConnectionFailed");
    }
}