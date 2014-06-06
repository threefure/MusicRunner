package com.amk2.musicrunner.start;

import android.location.Location;

import java.util.HashMap;

/**
 * Created by daz on 2014/4/27.
 */
public class LocationHelper {
    private static HashMap<String, String> map;
    private static Location location = null;
    private static String currentAdminArea = null;

    static {
        map = new HashMap<String, String>();
        map.put("台北市", "0");
    }

    public static void setLocation (Location _location) {
        location = _location;
    }

    public static Location getLocation () {
        return location;
    }

    public static void setCurrentAdminArea (String adminArea) {
        currentAdminArea = adminArea;
    }
    public static String getCityCode() {
        if (currentAdminArea == null) {
            return map.get("台北市");
        } else {
            return map.get(currentAdminArea);
        }
    }
}
