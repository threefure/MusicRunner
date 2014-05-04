package com.amk2.musicrunner.start;

import android.location.Location;

import java.util.HashMap;

/**
 * Created by daz on 2014/4/27.
 */
public class CityCodeMapping {
    private static HashMap<String, String> map;
    private static Location location = null;

    public static void initialMap () {
        map = new HashMap<String, String>();
        map.put("台北市", "0");
    }

    public static void setLocation (Location _location) {
        location = _location;
    }

    public static Location getLocation () {
        return location;
    }

    public static String getCityCode(String city) {
        return map.get(city);
    }
}
