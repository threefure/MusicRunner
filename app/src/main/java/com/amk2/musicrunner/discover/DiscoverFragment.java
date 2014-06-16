package com.amk2.musicrunner.discover;

import com.amk2.musicrunner.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;
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
import android.widget.Toast;

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
	}

    @Override
    public void onStart() {
        super.onStart();
        if (initLocationProvider()) {
            currentLocation();
        } else {
            Log.d("onstart init: ", "please turn on the location service！");
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
        String where = "";
        if (location != null) {
            double lng = location.getLongitude();
            double lat = location.getLatitude();
            float speed = location.getSpeed();
            long time = location.getTime();
            String timeString = getTimeString(time);

            where = lng + lat + speed + timeString + provider;

            showMarkerMe(lat, lng);
            //cameraFocusOnMe(lat, lng);
            trackToMe(lat, lng);

        } else {
            where = "No location found.";
        }
    }

    private void trackToMe(double lat, double lng){
        if (mTrackList == null) {
            mTrackList = new ArrayList<LatLng>();
        }
        mTrackList.add(new LatLng(lat, lng));

        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : mTrackList) {
            polylineOpt.add(latlng);
        }

        polylineOpt.color(Color.RED);

        Polyline line = mMap.addPolyline(polylineOpt);
        line.setWidth(10);
    }

    private void cameraFocusOnMe(double lat, double lng){
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(16)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    }

    private void showMarkerMe(double lat, double lng){
        if (mMarker != null) {
            mMarker.remove();
        }

        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(lat, lng));
        markerOpt.title("Hello!");
        mMarker = mMap.addMarker(markerOpt);

        Toast.makeText(mContext, "lat:" + lat + ",lng:" + lng, Toast.LENGTH_SHORT).show();
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
                break;

        }
    }



    GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.d(String.valueOf(R.string.discover_fragment_tag), "GPS_EVENT_STARTED");
                    break;

                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.d(String.valueOf(R.string.discover_fragment_tag), "GPS_EVENT_STOPPED");
                    break;

                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.d(String.valueOf(R.string.discover_fragment_tag), "GPS_EVENT_FIRST_FIX");
                    break;

                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.d(String.valueOf(R.string.discover_fragment_tag), "GPS_EVENT_SATELLITE_STATUS");
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
                    Log.v(String.valueOf(R.string.discover_fragment_tag), "Status Changed: Out of Service");
                    Toast.makeText(mContext, "Status Changed: Out of Service",
                            Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.v(String.valueOf(R.string.discover_fragment_tag), "Status Changed: Temporarily Unavailable");
                    Toast.makeText(mContext, "Status Changed: Temporarily Unavailable",
                            Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.AVAILABLE:
                    Log.v(String.valueOf(R.string.discover_fragment_tag), "Status Changed: Available");
                    Toast.makeText(mContext, "Status Changed: Available",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };

}
