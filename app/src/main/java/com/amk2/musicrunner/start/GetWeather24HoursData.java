package com.amk2.musicrunner.start;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import com.amk2.musicrunner.start.WeatherModel.WeatherHourlyEntry;

/**
 * Created by daz on 2014/5/4.
 */

class GetWeather24HoursData extends AsyncTask<String, Void, ArrayList<WeatherHourlyEntry>> {
    private NetworkAccess na;
    public GetWeather24HoursData () {
        super();
        na = new NetworkAccess();
    }

    @Override
    protected ArrayList<WeatherHourlyEntry> doInBackground (String... city) {
        ArrayList<WeatherHourlyEntry> weatherHourlyEntryList = null;
        try {
            Log.d("Daz", "in getweatherhourlydata");
            Calendar c = Calendar.getInstance();
            Integer hour = c.get(Calendar.HOUR_OF_DAY);
            InputStream weatherStream = na.downloadUrl(NetworkAccess.baseWeather24HoursUrlString + "?cityCode=" + city[0].toString() + "&currentHour=" + hour.toString());
            weatherHourlyEntryList = WeatherJSONParser.readHour(weatherStream);
        } catch (IOException e) {
            Log.d("Error", "error happens when getting weather data");
        }

        return weatherHourlyEntryList;
    }

    protected void onPostExecute(ArrayList<WeatherHourlyEntry> weatherEntry) {
        //return weatherEntry;
    }
}
