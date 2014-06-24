package com.amk2.musicrunner.running;

import com.amk2.musicrunner.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MapFragmentRun extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_fragment_run, container, false);
        // Get a handle to the Map Fragment
        GoogleMap map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.running_map)).getMap();


        Resources res = getResources();

        SharedPreferences prefs = getActivity().getSharedPreferences(res.getString(R.string.cur_location), Context.MODE_WORLD_READABLE);
        double lat = (double)prefs.getFloat(res.getString(R.string.cur_location_lat), 0);
        double lng = (double)prefs.getFloat(res.getString(R.string.cur_location_lng), 0);

        LatLng sydney = new LatLng(lat, lng);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

        map.addMarker(new MarkerOptions()
                .title("Sydney")
                .snippet("The most populous city in Australia.")
                .position(sydney));
        return v;
    }

}
