package com.amk2.musicrunner.utilities;

import java.util.ArrayList;

/**
 * Created by daz on 10/9/14.
 */
public interface OnSongRankPreparedListener {
    void OnSongRankPrepared(ArrayList<SongPerformance> songPerformanceList, Double averagePerformance);
}
