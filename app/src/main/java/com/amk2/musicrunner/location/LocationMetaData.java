package com.amk2.musicrunner.location;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import java.util.HashMap;

/**
 * Created by daz on 2014/4/27.
 */
public class LocationMetaData{
    private static HashMap<String, String> map;
    private static Location location = null;
    private static String currentAdminArea = null;

    static {
        map = new HashMap<String, String>();
        map.put("台北市", "0");
        map.put("新北市", "1");
        map.put("台中市", "2");
        map.put("台南市", "3");
        map.put("高雄市", "4");
        map.put("基隆市", "5");
        map.put("桃園縣", "6");
        map.put("新竹縣", "7");
        map.put("新竹市", "8");
        map.put("苗栗縣", "9");
        map.put("彰化縣", "10");
        map.put("南投縣", "11");
        map.put("雲林縣", "12");
        map.put("嘉義縣", "13");
        map.put("嘉義市", "14");
        map.put("屏東縣", "15");
        map.put("宜蘭縣", "16");
        map.put("花蓮縣", "17");
        map.put("臺東縣", "18");
        map.put("澎湖縣", "19");
        map.put("金門縣", "20");
        map.put("連江縣", "21");
    }

    public static void setLocation (Location _location) {
        location = _location;
    }

    public static Location getLocation () {
        return location;
    }

    public static void setCurrentAdminArea (String adminArea) {
        Log.d("daz in locationmetadata", "setting admin area " + adminArea);
        currentAdminArea = adminArea;
        Log.d("daz in locationmetadata", "currentAdminArea = " + currentAdminArea);
    }
    public static String getCurrentAdminArea () {
        if (currentAdminArea == null) {
            return "臺北市";
        }
        return currentAdminArea;
    }
    public static String getCityCode() {
        Log.d("daz in locationmetadata", "getCityCode() currentAdminArea = " + currentAdminArea);
        if (currentAdminArea == null) {
            Log.d("daz in location meta data", "return default city code ");
            return map.get("臺北市");
        } else {
            Log.d("daz in location meta data", "current admin area is " + currentAdminArea);
            return map.get(currentAdminArea);
        }
    }
}
