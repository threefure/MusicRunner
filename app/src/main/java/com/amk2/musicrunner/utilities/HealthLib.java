package com.amk2.musicrunner.utilities;

/**
 * Created by daz on 10/7/14.
 */
public class HealthLib {
    public static Double calculateCalories (int timeInSec, Double distanceInMeter) {
        if (distanceInMeter == 0.0) {
            return 0.0;
        }
        double mins  = (double) timeInSec / 60;
        double hours = (double) timeInSec / 3600;
        double per400meters = distanceInMeter / 400;
        double speed = mins / per400meters;
        double K = 30 / speed;
        double calories = 70*hours*K;

        return calories;
    }
}
