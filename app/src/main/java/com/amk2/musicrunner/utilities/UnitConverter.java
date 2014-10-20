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
        /*double pixels;
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
        }*/
        final float scale = context.getResources().getDisplayMetrics().density;
        float pixels = (int) (dp * scale + 0.5f);
        return pixels;
    }

    public static double getKGFromLB (double lb) {
        return lb*0.4536;
    }

    public static double getLBFromKG (double kg) {
        return kg*2.2046;
    }

    public static double getCMFromIN (double in) {
        return in*2.54;
    }

    public static double getINFromCM (double cm) {
        return cm*0.3937;
    }

    public static double getKMFromMI (double mi) {
        return mi*1.6;
    }

    public static double getMIFromKM (double km) {
        return km*0.6213;
    }
}
