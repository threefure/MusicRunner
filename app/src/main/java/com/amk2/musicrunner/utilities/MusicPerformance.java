package com.amk2.musicrunner.utilities;

/**
 * Created by ktlee on 9/7/14.
 */
public class MusicPerformance {

    public Integer duration;
    public Double distance;
    public Double calories;
    public String name;
    public Double performance;

    public MusicPerformance (Integer dur, Double dis, Double cal, String n) {
        duration = dur;
        distance = dis;
        calories = cal;
        name = n;
        performance = calories*60/duration;
    }
}
