package com.amk2.musicrunner.start;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.RunningActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by daz on 2014/4/22.
 */
public class StartFragment extends Fragment implements View.OnClickListener, GoogleMap.CancelableCallback{

    private GoogleMap googleMap;

    private Activity mActivity;
    private View mFragmentView;
    private Button mGoRunningButton;

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
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void initialize() {
        mActivity = getActivity();
        mFragmentView = getView();

        findViews();
        initButtons();

        //initialize the map, should coordinate with location service
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.fox);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(-33.796923, 150.922433)).icon(bitmapDescriptor);

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.start_map)).getMap();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.796923, 150.922433), 16), 2, this);
        googleMap.addMarker(markerOptions);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
    }

    private void initButtons() {
        mGoRunningButton.setOnClickListener(this);
    }

    private void findViews() {
        mGoRunningButton = (Button)mFragmentView.findViewById(R.id.start_go_running);
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
}
