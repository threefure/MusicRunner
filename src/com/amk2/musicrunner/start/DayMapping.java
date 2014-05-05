package com.amk2.musicrunner.start;

import java.util.HashMap;

/**
 * Created by daz on 2014/5/3.
 */
public class DayMapping {
    private static HashMap<String, String> map;

    public static void initialMap () {
        map = new HashMap<String, String>();
        map.put("0", "Sun");
        map.put("1", "Mon");
        map.put("2", "Tue");
        map.put("3", "Wed");
        map.put("4", "Thu");
        map.put("5", "Fri");
        map.put("6", "Sat");
    }
    public static String getDay(String day) {
        return map.get(day);
    }
}
