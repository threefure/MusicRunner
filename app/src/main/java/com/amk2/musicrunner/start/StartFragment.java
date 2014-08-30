package com.amk2.musicrunner.start;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.amk2.musicrunner.R;
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
    public interface StartTabFragmentListener {
    	void onSwitchBetweenStartAndWeatherFragment();
    }

    private GoogleMap googleMap;

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
        //initialize the map, should coordinate with location service
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.fox);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(-33.796923, 150.922433)).icon(bitmapDescriptor);

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.start_map)).getMap();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.796923, 150.922433), 16), 2, this);
        googleMap.addMarker(markerOptions);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
    }

    @Override
    public void onClick(View view) {

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
