package com.amk2.musicrunner.utilities;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by ktlee on 9/7/14.
 */
public class UnitConverter {
    public static double getDPFromPixels(Context context, double pixels) {
        double dp;
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        switch(metrics.densityDpi){
            case DisplayMetrics.DENSITY_LOW:
                dp = pixels * 0.75;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                dp = pixels * 1;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                dp = pixels * 1.5;
                break;
            default:
                dp = pixels;
        }
        return dp;
    }

    public static double getPixelsFromDP(Context context, double dp) {
        double pixels;
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        switch(metrics.densityDpi){
            case DisplayMetrics.DENSITY_LOW:
                pixels = dp / 0.75;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                pixels = dp;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                pixels = dp / 1.5;
                break;
            default:
                pixels = dp;
        }
        return pixels;
    }

}
