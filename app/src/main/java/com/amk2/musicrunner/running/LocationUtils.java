package com.amk2.musicrunner.running;
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.amk2.musicrunner.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines app-wide constants and utilities
 */
public final class LocationUtils {

    // Debugging tag for the application
    public static final String APPTAG = "RunningMapFragment";

    // Name of shared preferences repository that stores persistent state
    public static final String SHARED_PREFERENCES =
            "com.example.android.location.SHARED_PREFERENCES";

    // Key for storing the "updates requested" flag in shared preferences
    public static final String KEY_UPDATES_REQUESTED =
            "com.example.android.location.KEY_UPDATES_REQUESTED";

    public static final float EARTH_RADIOUS = 6371;

    public static final float CAMERA_PAD = 19;
    public static final float DISCOVERY_CAMERA_PAD = 15;

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 1;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 1;

    public static final int LINE_WIDTH = 20;
    public static final int TOLERANCE_TIMES = 5;
    public static final double MIN_DISTANCE = 1;
    public static final int METER_CONVERSTION = 1609;
    public static final double EARTH_RADIOUS_MILES = 3958.75;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 100;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    public static final String LONGITUTE_PATTERN = "@long(.*?)gnol@";
    public static final String LATITUTE_PATTERN = "@lat(.*?)tal@";
    public static final String COLOR_PATTERN = "#(.*?)#";

    // Create an empty string for initializing strings
    public static final String EMPTY_STRING = new String();

    /**
     * Get the latitude and longitude from the Location object returned by
     * Location Services.
     *
     * @param currentLocation A Location object containing the current location
     * @return The latitude and longitude of the current location, or null if no
     * location is available.
     */
    public static String getLatLng(LatLng currentLocation, int color) {
        // If the location is valid
        if (currentLocation != null) {
            return  "@long" +
                    String.valueOf(currentLocation.longitude) +
                    "gnol@" +
                    "@lat" +
                    String.valueOf(currentLocation.latitude) +
                    "tal@" +
                    "#" + color + "#";
        } else {
            // Otherwise, return the empty string
            return EMPTY_STRING;
        }
    }

    public static ArrayList<LatLng> parseRouteToLocation(String route) {
        if (route == null)
            return null;
        ArrayList<LatLng> locationList = new ArrayList<LatLng>();
        ArrayList<Double> mLongituteList = LocationUtils.findChunk(LocationUtils.LONGITUTE_PATTERN, route);
        ArrayList<Double> mLatituteList = LocationUtils.findChunk(LocationUtils.LATITUTE_PATTERN, route);

        if(mLongituteList.size() != mLatituteList.size()) {
            Log.e("Record error:", "Location Longitute and latitute doesn't match");
            return null;
        }

        for (int i = 0; i < mLongituteList.size(); i++) {
            locationList.add(new LatLng(mLatituteList.get(i),mLongituteList.get(i)));
        }

        return locationList;
    }

    public static ArrayList<Integer> parseRouteColor(String route) {
        if (route == null)
            return null;
        ArrayList<Integer> colorList = new ArrayList<Integer>();
        ArrayList<Double> mColorList = LocationUtils.findChunk(LocationUtils.COLOR_PATTERN, route);
        for (int i = 0; i < mColorList.size(); i++) {
            colorList.add(mColorList.get(i).intValue());
        }
        return colorList;
    }

    private static ArrayList<Double> findChunk(String strPattern, String str) {
        ArrayList<Double> dou = new ArrayList<Double>();
        Pattern pattern = Pattern.compile(strPattern);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            String tStr = matcher.group(1);
            Double rec = Double.parseDouble(tStr);
            dou.add(rec);
        }
        return dou;
    }


}
