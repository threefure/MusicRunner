package com.amk2.musicrunner.start;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by daz on 2014/4/27.
 */
public class GetAddressFromLocation extends AsyncTask<Location, Void, Address> {

    // Store the context passed to the AsyncTask when the system instantiates it.
    Context locationContext;

    public GetAddressFromLocation(Context context) {
        super();
        locationContext = context;
    }

    @Override
    protected Address doInBackground(Location... params) {

        Geocoder geocoder = new Geocoder(locationContext, Locale.TAIWAN);

        Location location = params[0];

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1
            );
        } catch (IOException exception1) {
            Log.e("Error", "IOException");
            return null;
        } catch (IllegalArgumentException exception2) {
            Log.e("Error", "IllegalArgumentException");
            return null;
        }

        if (addresses != null && addresses.size() > 0) {

            Address address = addresses.get(0);
            return address;
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Address address) {
        //should do some cache stuff

    }
}
