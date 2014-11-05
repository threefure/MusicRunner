package com.amk2.musicrunner.start;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.RunningActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by daz on 2014/4/22.
 */
public class StartFragment extends Fragment implements
        View.OnClickListener,
        GoogleMap.CancelableCallback,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap googleMap;

    private Activity mActivity;
    private View mFragmentView;
    private Button mGoRunningButton;
    private Marker marker = null;
    private MarkerOptions markerOptions;

    private LocationClient mLocationClient;
    private Location mCurrentLocation;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.start_fragment, container, false);
    }
    @Override
    public void onActivityCreated (Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        initialize();
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop () {
        mLocationClient.disconnect();
        super.onStop();
    }

    public void initialize() {
        mActivity = getActivity();
        mFragmentView = getView();

        mLocationClient = new LocationClient(mActivity, this, this);

        findViews();
        initButtons();
    }

    private void initButtons() {
        mGoRunningButton.setOnClickListener(this);
    }

    private void findViews() {
        mGoRunningButton = (Button)mFragmentView.findViewById(R.id.start_go_running);
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.start_map)).getMap();
    }

    private void updateMap() {
        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16), 2, this);
        markerOptions.position(currentLatLng);
        if (marker == null) {
            marker = googleMap.addMarker(markerOptions);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.start_go_running:
                startActivity(new Intent(mActivity, RunningActivity.class));
                break;
        }
    }

    @Override
    public void onFinish() {
        Log.d("DAZ", "map is there");
    }

    @Override
    public void onCancel() {
        Log.d("DAZ", "map is canceled");
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = mLocationClient.getLastLocation();
        if (mCurrentLocation != null) {
            googleMap.getUiSettings().setZoomControlsEnabled(false);

            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.dog_front);
            markerOptions = new MarkerOptions();
            markerOptions.icon(bitmapDescriptor);
            updateMap();
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(getActivity(), "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            //showErrorDialog(connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (mCurrentLocation != null) {
            updateMap();
        }
    }
}
