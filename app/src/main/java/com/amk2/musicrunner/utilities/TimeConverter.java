package com.amk2.musicrunner.utilities;

import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by daz on 2014/6/24.
 */
public class TimeConverter {
    public static String SECOND = "sec";
    public static String MINUTE = "min";
    public static String HOUR   = "hr";
    public static HashMap<String, Integer> getReadableTimeFormatFromSeconds(int seconds) {
        int actualSec  = 0;
        int actualMin  = 0;
        int actualHour = 0;
        if (seconds > 0) {
            actualSec  = seconds%60;
            actualMin  = (seconds/60) % 60;
            actualHour = (seconds/3600);
        }
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put(SECOND, actualSec);
        map.put(MINUTE, actualMin);
        map.put(HOUR, actualHour);
        return map;
    }

    public static String getDurationString (HashMap<String, Integer> map) {
        int hour = map.get(HOUR);
        int min  = map.get(MINUTE);
        int sec  = map.get(SECOND);
        String durationString = "";
        if(hour < 10) {
            durationString += "0";
        }
        durationString += (hour + ":");
        if(min < 10) {
            durationString += "0";
        }
        durationString += (min + ":");
        if(sec < 10) {
            durationString += "0";
        }
        durationString += sec;

        return durationString;
    }

    public static String getDateString (long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        String day  = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.TAIWAN);
        int month   = calendar.get(Calendar.MONTH) + 1;
        int date    = calendar.get(Calendar.DAY_OF_MONTH);
        int hour    = calendar.get(Calendar.HOUR_OF_DAY);
        int min     = calendar.get(Calendar.MINUTE);
        String dateString = month + "/" + date + " " + day + " " + hour + ":" + min;
        return dateString;
    }
}
