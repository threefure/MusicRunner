package com.amk2.musicrunner.utilities;

/**
 * Created by daz on 10/7/14.
 */
public class HealthLib {
    private static double weight = 50;
    public static void setWeight (double _weight) {
        weight = _weight;
    }
    public static Double calculateCalories (int timeInSec, Double distanceInMeter, Double weight) {
        if (distanceInMeter == 0.0) {
            return 0.0;
        }
        double mins  = (double) timeInSec / 60;
        double hours = (double) timeInSec / 3600;
        double per400meters = distanceInMeter / 400;
        double speed = mins / per400meters;
        double K = 30 / speed;
        double calories = weight*hours*K;

        return calories;
    }

    // calculate calories without weight, must call setWeight before using this
    public static Double calculateCalories (int timeInSec, Double distanceInMeter) {
        if (distanceInMeter == 0.0) {
            return 0.0;
        }
        double mins  = (double) timeInSec / 60;
        double hours = (double) timeInSec / 3600;
        double per400meters = distanceInMeter / 400;
        double speed = mins / per400meters;
        double K = 30 / speed;
        double calories = weight*hours*K;

        return calories;
    }
}
