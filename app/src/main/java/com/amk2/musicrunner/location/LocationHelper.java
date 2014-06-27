package com.amk2.musicrunner.location;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by ktlee on 6/6/14.
 */
public class LocationHelper implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    private Context context;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private Address currentAddress;
    private ContentResolver mContentResolver;

    public LocationHelper(Context context) {
        this.context = context;
        mContentResolver = context.getContentResolver();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationClient = new LocationClient(context, this, this);
    }

    public void Connect () {
        mLocationClient.connect();
    }

    public void Disconnect() {
        mLocationClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("daz", "GooglePlayService conntected");
        Location location = mLocationClient.getLastLocation();
        LocationMetaData.setLocation(location);
        try {
            Geocoder geocoder = new Geocoder(context, Locale.TAIWAN);
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                location.getLongitude(), 1
            );
            if (addresses != null && addresses.size() > 0) {
                currentAddress = addresses.get(0);
                LocationMetaData.setCurrentAdminArea(currentAddress.getAdminArea());
                Log.d("daz in locationhelper", currentAddress.getAdminArea());
            } else {
                Log.d("daz in locationhelper", "cannot determine address");
            }
            registerPeriodicSyncs();
        } catch (IllegalStateException e) {
            Log.e("Error", "task has already been executed");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnected() {
        Log.d("daz", "location helper disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void registerPeriodicSyncs () {
        Log.d("daz", "set up daily weather updater");
        HashMap<String, String> dailyBundleSettings = new HashMap<String, String>();
        dailyBundleSettings.put(Constant.SYNC_UPDATE, Constant.UPDATE_WEATHER);
        dailyBundleSettings.put(Constant.SYNC_CITYCODE, LocationMetaData.getCityCode());
        addPeriodSync(dailyBundleSettings, Constant.ONE_MINUTE);

        Log.d("daz", "set up 24hrs weather updater");
        HashMap<String, String> t24HrsBundleSettings = new HashMap<String, String>();
        t24HrsBundleSettings.put(Constant.SYNC_UPDATE, Constant.UPDATE_24HRS_WEATHER);
        t24HrsBundleSettings.put(Constant.SYNC_CITYCODE, LocationMetaData.getCityCode());
        addPeriodSync(t24HrsBundleSettings, Constant.ONE_MINUTE);

        Log.d("daz", "set up weekly weather updater");
        HashMap<String, String> weeklyBundleSettings = new HashMap<String, String>();
        weeklyBundleSettings.put(Constant.SYNC_UPDATE, Constant.UPDATE_WEEKLY_WEATHER);
        weeklyBundleSettings.put(Constant.SYNC_CITYCODE, LocationMetaData.getCityCode());
        addPeriodSync(weeklyBundleSettings, Constant.ONE_MINUTE);

        Log.d("daz", "set up youbike updater");
        HashMap<String, String> youbikeBundleSettings = new HashMap<String, String>();
        youbikeBundleSettings.put(Constant.SYNC_UPDATE, Constant.UPDATE_UBIKE);
        addPeriodSync(youbikeBundleSettings, Constant.ONE_MINUTE);
    }

    public void unregisterPeriodicSyncs () {
        Log.d("daz", "remove daily weather updater");
        HashMap<String, String> dailyBundleSettings = new HashMap<String, String>();
        dailyBundleSettings.put(Constant.SYNC_UPDATE, Constant.UPDATE_WEATHER);
        dailyBundleSettings.put(Constant.SYNC_CITYCODE, LocationMetaData.getCityCode());
        removePeriodSync(dailyBundleSettings);

        Log.d("daz", "remove 24hrs weather updater");
        HashMap<String, String> t24HrsBundleSettings = new HashMap<String, String>();
        t24HrsBundleSettings.put(Constant.SYNC_UPDATE, Constant.UPDATE_24HRS_WEATHER);
        t24HrsBundleSettings.put(Constant.SYNC_CITYCODE, LocationMetaData.getCityCode());
        removePeriodSync(t24HrsBundleSettings);

        Log.d("daz", "remove weekly weather updater");
        HashMap<String, String> weeklyBundleSettings = new HashMap<String, String>();
        weeklyBundleSettings.put(Constant.SYNC_UPDATE, Constant.UPDATE_WEEKLY_WEATHER);
        weeklyBundleSettings.put(Constant.SYNC_CITYCODE, LocationMetaData.getCityCode());
        removePeriodSync(weeklyBundleSettings);

        Log.d("daz", "remove youbike updater");
        HashMap<String, String> youbikeBundleSettings = new HashMap<String, String>();
        youbikeBundleSettings.put(Constant.SYNC_UPDATE, Constant.UPDATE_UBIKE);
        removePeriodSync(youbikeBundleSettings);
    }

    private void addPeriodSync (HashMap<String, String> bundleMap, long period) {
        Bundle bundle = new Bundle();
        for (String key:bundleMap.keySet()) {
            bundle.putString(key, bundleMap.get(key));
        }
        try {
            Log.d("daz", "add period sync");
            mContentResolver.addPeriodicSync(MusicTrackMetaData.mAccount, MusicTrackMetaData.AUTHORITY, bundle, period);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void removePeriodSync (HashMap<String, String> bundleMap) {
        Bundle bundle = new Bundle();
        for (String key:bundleMap.keySet()) {
            bundle.putString(key, bundleMap.get(key));
        }
        try {
            Log.d("daz", "remove period sync");
            mContentResolver.removePeriodicSync(MusicTrackMetaData.mAccount, MusicTrackMetaData.AUTHORITY, bundle);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
