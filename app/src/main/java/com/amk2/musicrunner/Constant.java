package com.amk2.musicrunner;

import com.amk2.musicrunner.setting.SettingActivity;

import java.util.HashMap;

/**
 * Created by ktlee on 5/25/14.
 */
public class Constant {
    public static final boolean isServerOn = false;

    //main activity
    public static final long MILLISECONDS_PER_SECOND = 1L;
    public static final long ONE_MINUTE = 60L;
    public static final String SYNC_UPDATE = "com.amk2.musicrunner.update";
    public static final String SYNC_CITYCODE = "com.amk2.musicrunner.citycode";

    public static final String UPDATE_WEATHER = "com.amk2.musicrunner.updateweather";
    public static final String UPDATE_24HRS_WEATHER = "com.amk2.musicrunner.update24hrsweather";
    public static final String UPDATE_WEEKLY_WEATHER = "com.amk2.musicrunner.updateweeklyweather";
    public static final String UPDATE_UBIKE = "com.amk2.musicrunner.updateubike";

    public static final int EXPIRATION_DATE_DURATION_DAILY  = 1;
    public static final int EXPIRATION_DATE_DURATION_24HRS  = 1;
    public static final int EXPIRATION_DATE_DURATION_WEEKLY = 12;
    public static final int EXPIRATION_DATE_DURATION_YOUBIKE = 5;

    public static final String JSON_CONTENT = "com.amk2.musicrunner.updateweather.jsoncontent";
    public static final String EXPIRATION_DATE = "com.amk2.musicrunner.updateweather.expirationdate";

    public static final String DB_KEY_DAILY_WEATHER = "com.amk2.musicrunner.daily.weather";
    public static final String DB_KEY_24HRS_WEATHER = "com.amk2.musicrunner.24hrs.weather";
    public static final String DB_KEY_WEEKLY_WEATHER = "com.amk2.musicrunner.weekly.weather";
    public static final String DB_KEY_YOUBIKE = "com.amk2.musicrunner.youbike";

    // Server API
    public static final String HOST = "http://ec2-54-187-100-217.us-west-2.compute.amazonaws.com";
    public static final String PORT = ":8080";
    public static final String WEATHER_CONDITION_API_URL = HOST + PORT + "/getWeatherConditions";
    public static final String WEATHER_HOURLY_API_URL    = HOST + PORT + "/getWeatherHourly";
    public static final String WEATHER_5DAYS_API_URL     = HOST + PORT + "/getWeatherForecast5Day";
    public static final String TRACK_INFO_API_URL        = HOST + PORT + "/getTrackInfo";

    //----------DEPRECATED-------------
    public static final String baseWeatherUrlString = HOST + PORT + "/weatherDailyJSON";//TODO need to append city query behind
    public static final String baseWeatherWeekUrlString = HOST + PORT + "/weatherWeeklyJSON";//TODO need to append city query behind
    public static final String baseWeather24HoursUrlString = HOST + PORT + "/weather24HoursJSON";//TODO need to append city query behind
    public static final String baseYoubikeUrlString = HOST + PORT + "/youBikeJSON";//TODO need to append city query behind
    public static final String storeRunningEventUrlString = HOST + PORT + "store?type=event";//TODO need to append city query behind
    public static final String cityCodeQuery = "cityCode=";
    //---------------------------------

    //AWS Setting
    //public static final String AWS_HOST = "http://ec2-54-187-71-254.us-west-2.compute.amazonaws.com:8080";
    public static final String AWS_HOST = "http://ec2-54-187-100-217.us-west-2.compute.amazonaws.com:8080";

    //SharedPreference Parameters
    public static final String PREFERENCE_NAME = "musicrun";
    public static final String ACCOUNT_PARAMS = "account";


    public static final int UPDATE_START_FRAGMENT_UI = 1;

    //Facebook Setting
    public static final String FACEBOOK_ACCOUNT_PREFIX = "facebook.";
    // Album name
    public static final String Album = "MusicRunner";

    // Running Activity
    public static final String PERF_SEPARATOR = ":>:";
    public static final String SONG_SEPARATOR = ":<:";

    // Shared Preference
    public static final HashMap<Integer, String> DistanceMap = new HashMap<Integer, String>();
    public static final HashMap<String, Integer> PaceSpeedMap = new HashMap<String, Integer>();
    public static final HashMap<String, Integer> AutoCueMap = new HashMap<String, Integer>();
    static {
        DistanceMap.put(SettingActivity.SETTING_DISTANCE_KM, "km");
        DistanceMap.put(SettingActivity.SETTING_DISTANCE_MI, "mi");

        PaceSpeedMap.put("my_running_km_pace", R.string.my_running_km_pace);
        PaceSpeedMap.put("my_running_mi_pace", R.string.my_running_mi_pace);
        PaceSpeedMap.put("my_running_km_speed", R.string.my_running_km_speed);
        PaceSpeedMap.put("my_running_mi_speed", R.string.my_running_mi_speed);

        AutoCueMap.put("5 Minutes", 5);
        AutoCueMap.put("10 Minutes", 10);
        AutoCueMap.put("15 Minutes", 15);
        AutoCueMap.put("20 Minutes", 20);
        AutoCueMap.put("25 Minutes", 25);
        AutoCueMap.put("30 Minutes", 30);
    }


}
