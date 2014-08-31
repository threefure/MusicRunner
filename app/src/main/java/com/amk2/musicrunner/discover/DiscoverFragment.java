package com.amk2.musicrunner.discover;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.LocationUtils;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.location.LocationListener;
import android.app.Fragment;
import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DiscoverFragment extends Fragment
        implements View.OnClickListener {



    /** Map */
    private MapView mapView;
    private Marker mMarker;

    /** TRACK */
    private ArrayList<LatLng> mTrackList;
    private GoogleMap mMap;

    /** GPS */
    private LocationManager locationMgr;
    private String provider;

    /* UI */
    private ImageButton mBikeStoreButton;
    private ImageButton mFamousSpotsButton;
    private ImageButton mUBikeButton;

    /* Content Resolver */
    private ContentResolver mContentResolver;

    Context mContext;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.discover_fragment, container, false);
        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        mContext = this.getActivity().getApplicationContext();

        mMap = mapView.getMap();

        // Set up on click listener for all button
        mBikeStoreButton = (ImageButton) getView().findViewById(R.id.discover_bikestore);
        mFamousSpotsButton = (ImageButton) getView().findViewById(R.id.discover_famous_spots);
        mUBikeButton = (ImageButton) getView().findViewById(R.id.discover_ubike);

        mBikeStoreButton.setOnClickListener(this);
        mFamousSpotsButton.setOnClickListener(this);
        mUBikeButton.setOnClickListener(this);

        mContentResolver = getActivity().getContentResolver();

        MapsInitializer.initialize(mContext);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (initLocationProvider()) {
            currentLocation();
        }
    }

    private boolean initLocationProvider() {
        locationMgr = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            return true;
        }

        return false;
    }

    private void currentLocation() {
        Location location = locationMgr.getLastKnownLocation(provider);
        updateWithNewLocation(location);

        //GPS Listener
        locationMgr.addGpsStatusListener(gpsListener);

        //Location Listener
        long minTime = 5000;//ms
        float minDist = 5.0f;//meter
        locationMgr.requestLocationUpdates(provider, minTime, minDist, locationListener);
    }

    private void updateWithNewLocation(Location location) {
        if (location != null) {
            double lng = location.getLongitude();
            double lat = location.getLatitude();

            showMarkerMe(lat, lng);
        }
    }

    private void showMarkerMe(double lat, double lng){
        LatLng current_location = new LatLng(lat, lng);

        if (mMarker != null)
            mMarker.remove();
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current_location, LocationUtils.DISCOVERY_CAMERA_PAD));

        mMap.setMyLocationEnabled(true);
        mMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.fox))
                .position(current_location)
                .title("Yo"));
    }

    private String getTimeString(long timeInMilliseconds){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(timeInMilliseconds);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.discover_bikestore:
                // TODO::discover_bikestore button
                break;
            case R.id.discover_famous_spots:
                // TODO::discover_famous_spots button
                break;
            case R.id.discover_ubike:
                // TODO::discover_ubike button
                updateYoubike();
                break;

        }
    }



    GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    break;

                case GpsStatus.GPS_EVENT_STOPPED:
                    break;

                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    break;

                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    break;
            }
        }
    };


    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    break;
                case LocationProvider.AVAILABLE:
                    break;
            }
        }

    };

    private void updateYoubike () {
        String[] projection = {
                MusicRunnerDBMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT,
                MusicRunnerDBMetaData.MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE
        };
        String selection = MusicRunnerDBMetaData.MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE + " LIKE ?";
        String[] selectionArgs = { String.valueOf(Constant.DB_KEY_YOUBIKE) };

        Cursor cursor = mContentResolver.query(MusicRunnerDBMetaData.MusicTrackCommonDataDB.CONTENT_URI, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        try {
            String JSONContent = cursor.getString(cursor.getColumnIndex(MusicRunnerDBMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT));
            updateYoubike(JSONContent);
        } catch (CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
            Log.d("Daz", "there's no youbike data stored in the phone");
        }
    }

    private void updateYoubike (String JSONContent) {
        try {
            JSONArray youbikeJSONArray = new JSONArray(JSONContent);
            int length = youbikeJSONArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject entry = youbikeJSONArray.getJSONObject(i);

                //------ should merged with showMarkerMe() with the extended title feature -----------
                MarkerOptions mo = new MarkerOptions();
                mo.position(new LatLng(Double.parseDouble(entry.getString("lat")), Double.parseDouble(entry.getString("lng"))));
                mo.title(entry.getString("sna")).snippet(entry.getString("sbi") + "/" + entry.getString("tot"));
                mMap.addMarker(mo);
                //------ should merged with showMarkerMe() -----------
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
