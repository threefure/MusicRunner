package com.amk2.musicrunner.start;

/**
 * Created by daz on 2014/4/27.
 */
public class WeatherEntry {
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
