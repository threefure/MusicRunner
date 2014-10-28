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
import com.amk2.musicrunner.utilities.ColorGenerator;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by tengchou on 10/21/14.
 */
public class MapService extends Service implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
    private static final String TAG = "MapService";

    public static final String BROADCAST_ACTION = "com.my.package.LOCATION_UPDATE";

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 1;

    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 40;

    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private LocationClient locationClient;


    private static ArrayList<LatLng> mTrackList;
    private static ArrayList<Integer> mColorList;
    private Marker mMarker = null;
    private LatLng mlastLoc = null;
    private static double mSpeed;
    private static double mTotalDistance;
    private static int mColor = 0;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "Location service created…");

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
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

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
        LatLng curLoc = new LatLng(location.getLatitude(), location.getLongitude());
        drawLine(curLoc);
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


    private void drawLine(LatLng curr) {
        double distance;
        if (mTrackList == null) {
            mTrackList = new ArrayList<LatLng>();
        }
        if(mColorList == null) {
            mColorList = new ArrayList<Integer>();
        }
        if (mlastLoc == null) {
            mlastLoc = curr;
            mTrackList.add(new LatLng(curr.latitude, curr.longitude));
            mColorList.add(mColor);
        } else {
            distance = CalculationByDistance(mlastLoc.latitude, mlastLoc.longitude, curr.latitude, curr.longitude);

            // draw the line, and save it.
            if (distance > LocationUtils.MIN_DISTANCE) {
                mTotalDistance += distance;
                mSpeed = distance / LocationUtils.UPDATE_INTERVAL_IN_SECONDS;
                mTrackList.add(curr);
                mColorList.add(mColor);
                PolylineOptions polylineOpt = new PolylineOptions();
                polylineOpt.add(mlastLoc).add(curr);
                polylineOpt.color(ColorGenerator.generateColor(mColor));

                //Polyline line = mMap.addPolyline(polylineOpt);
                //line.setWidth(LocationUtils.LINE_WIDTH);

                mlastLoc = curr;
            } else {
                mSpeed = 0;
            }
        }
    }

    private double CalculationByDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
        double earthRadius = LocationUtils.EARTH_RADIOUS;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = LocationUtils.METER_CONVERSTION;

        return new Float(distance * meterConversion).floatValue();
    }
    public static double getmTotalDistance() {
        return mTotalDistance;
    }

    public static double getmSpeed() {
        return mSpeed;
    }

    public static ArrayList<LatLng> getmTrackList() { return mTrackList; }

    public static ArrayList<Integer> getmColorList() { return mColorList; }

    public static void resetAllParam() {
        MapService.mTotalDistance = 0;
        MapService.mSpeed = 0;
        MapService.mColor = 0;
        MapService.mTrackList.clear();
        MapService.mColorList.clear();
    }

    public static void musicChangeCallback(MusicRecord previousRecord) {
        mColor = mColor + 1;
        Log.d("Yo: ", previousRecord.mMusicSong.mTitle);
        Log.d("Yo: ", String.valueOf(previousRecord.mPlayingDuration));
    }
}