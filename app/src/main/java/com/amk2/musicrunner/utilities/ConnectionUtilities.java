package com.amk2.musicrunner.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by paulou on 11/16/14.
 */
public class ConnectionUtilities {
    private Context context;
    public ConnectionUtilities(Context c){
        this.context = c;
    }
    public boolean hasInternetConnecting(){
        ConnectivityManager connect = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connect != null)
        {
            NetworkInfo[] information = connect.getAllNetworkInfo();
            if (information != null)
                for (int x = 0; x < information.length; x++)
                    if (information[x].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }
}
