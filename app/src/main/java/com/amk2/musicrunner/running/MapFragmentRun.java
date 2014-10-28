package com.amk2.musicrunner.running;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.utilities.ColorGenerator;
import com.amk2.musicrunner.running.DistanceFragment.OnBackToDistanceListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import static android.widget.Toast.makeText;

public class MapFragmentRun extends Fragment implements
        View.OnClickListener {

    private GoogleMap mMap = null; // Might be null if Google Play services APK is not available.



    private View mFragmentView;
    private Button mBackToDistanceButton;

    private OnBackToDistanceListener mOnBackToDistanceListener;

    public void setOnBackToDistanceListener(OnBackToDistanceListener listener) {
        mOnBackToDistanceListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_fragment_run, container, false);
        setUpMapIfNeeded();

        Intent intent = new Intent(this.getActivity(), MapService.class);
        this.getActivity().startService(intent);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialize();
    }

    private void initialize() {
        mFragmentView = getView();
        findViews();
        initButtons();
    }

    private void initButtons() {
        mBackToDistanceButton.bringToFront();
        mBackToDistanceButton.setOnClickListener(this);
    }

    private void findViews() {
        mBackToDistanceButton = (Button)mFragmentView.findViewById(R.id.back_to_distance_button);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();

        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        // Try to obtain the map from the SupportMapFragment.
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.running_map)).getMap();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.back_to_distance_button:
                mOnBackToDistanceListener.onBackToDistance();
                break;
        }
    }
}
