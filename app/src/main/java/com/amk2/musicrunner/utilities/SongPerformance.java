package com.amk2.musicrunner.utilities;

/**
 * Created by ktlee on 7/8/14.
 */
public class SongPerformance {
    private Double performance;
    private String song;
    public Double getPerformance () {
        return performance;
    }
    public String getSong () {
        return song;
    }
    public SongPerformance (String song, Double performance) {
        this.song = song;
        this.performance = performance;
    }
}
