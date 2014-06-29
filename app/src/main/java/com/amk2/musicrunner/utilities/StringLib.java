package com.amk2.musicrunner.utilities;

/**
 * Created by daz on 2014/6/29.
 */
public class StringLib {
    /*
     * truncateDoubleString: truncate double number to 小數點後兩位
     * str: string of double number
     * allowedDigits: 小數點後幾位
     */
    public static String truncateDoubleString (String str, int allowedDigits) {
        int dot_position = str.indexOf(".");
        if (str.length() - dot_position > (allowedDigits + 1)) { //小數點後數字大於兩位
            str = str.substring(0, dot_position + allowedDigits + 1);
        }
        return str;
    };
}
