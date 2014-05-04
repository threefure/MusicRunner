package com.amk2.musicrunner.start;

/**
 * Created by daz on 2014/5/3.
 */
public class WeatherModel {
     public static class WeatherEntry {
        public final String uv;
        public final String condition;
        public final String temperature;
        public final String feeling;
        public final String chanceOfRain;

        public WeatherEntry(String uv, String condition, String temperature, String feeling, String chanceOfRain) {
            this.uv = uv;
            this.condition = condition;
            this.temperature = temperature;
            this.feeling = feeling;
            this.chanceOfRain = chanceOfRain;
        }
    }
    public static class WeatherWeekEntry {
        public final String month;
        public final String date;
        public final String day;
        public final String condition;
        public final String max_t;
        public final String min_t;

        public WeatherWeekEntry(String month, String date, String day, String condition, String max_t, String min_t) {
            this.month = month;
            this.date = date;
            this.day = day;
            this.condition = condition;
            this.max_t = max_t;
            this.min_t = min_t;
        }
    }
}
