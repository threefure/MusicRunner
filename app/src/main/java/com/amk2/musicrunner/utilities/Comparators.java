package com.amk2.musicrunner.utilities;

import java.util.Comparator;

/**
 * Created by ktlee on 7/8/14.
 */
public class Comparators {
    public static class SongPerformanceComparators implements Comparator <SongPerformance> {
        @Override
        public int compare(SongPerformance song1, SongPerformance song2) {
            return song2.calories.compareTo(song1.calories);
        }
    }
}
