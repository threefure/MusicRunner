package com.amk2.musicrunner.discover;

import com.amk2.musicrunner.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class DiscoverFragment extends Fragment
        implements View.OnClickListener {

    private MapView mapView;
    private GoogleMap map;
    private ImageButton mBikeStoreButton;
    private ImageButton mFamousSpotsButton;
    private ImageButton mUBikeButton;

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
		// This function is like onCreate() in activity.
		// You can start from here.
        map = mapView.getMap();

        // Set up on click listener for all button
        mBikeStoreButton = (ImageButton) getView().findViewById(R.id.discover_bikestore);
        mFamousSpotsButton = (ImageButton) getView().findViewById(R.id.discover_famous_spots);
        mUBikeButton = (ImageButton) getView().findViewById(R.id.discover_ubike);

        mBikeStoreButton.setOnClickListener(this);
        mFamousSpotsButton.setOnClickListener(this);
        mUBikeButton.setOnClickListener(this);
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
}
