package com.amk2.musicrunner.utilities;

import android.graphics.Color;

/**
 * Created by tengchou on 6/29/14.
 */
public class ColorGenerator {

    public static final int TOTAL_COLOR = 8;

    public static int generateColor(int num) {

        switch (num % TOTAL_COLOR) {
            case 0:
                return Color.RED;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.BLACK;
            case 4:
                return Color.MAGENTA;
            case 5:
                return Color.DKGRAY;
            case 6:
                return Color.YELLOW;
            case 7:
                return Color.CYAN;
            default:
                return Color.TRANSPARENT;
        }
    }
}
