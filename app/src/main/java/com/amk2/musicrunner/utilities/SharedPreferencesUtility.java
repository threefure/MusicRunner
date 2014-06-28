package com.amk2.musicrunner.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.amk2.musicrunner.Constant;

/**
 * Created by paulou1009 on 6/27/14.
 */
public class SharedPreferencesUtility {

    public static SharedPreferences getSharedPreferences(Activity activity){
        return activity.getSharedPreferences(Constant.PREFERENCE_NAME, activity.MODE_PRIVATE);
    }

    public static void storeAccount(SharedPreferences preferences, String account){
        preferences.edit().putString(Constant.ACCOUNT_PARAMS, account).commit();

    }
    public static void storeAccount(Activity activity, String account){
        SharedPreferences preferences = activity.getSharedPreferences(Constant.PREFERENCE_NAME, activity.MODE_PRIVATE);
        preferences.edit().putString(Constant.ACCOUNT_PARAMS, account).commit();
    }

    public static String getAccount(Activity activity){
        SharedPreferences preferences = activity.getSharedPreferences(Constant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String account = preferences.getString(Constant.ACCOUNT_PARAMS, null);
        return account;
    }

    public static String getAccount(SharedPreferences preferences){
        return preferences.getString(Constant.ACCOUNT_PARAMS, null);
    }
}
