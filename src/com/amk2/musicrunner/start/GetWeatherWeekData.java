package com.amk2.musicrunner.start;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.amk2.musicrunner.start.WeatherModel.WeatherWeekEntry;

/**
 * Created by daz on 2014/4/27.
 */
class GetWeatherWeekData extends AsyncTask<String, Void, ArrayList<WeatherWeekEntry>> {
    private NetworkAccess na;
    public GetWeatherWeekData () {
        super();
        na = new NetworkAccess();
    }

    @Override
    protected ArrayList<WeatherWeekEntry> doInBackground (String... city) {
        ArrayList<WeatherWeekEntry> weatherWeekEntryList = null;
        try {
            Log.d("Daz", "in getweatherweekdata");
            InputStream weatherStream = na.downloadUrl(NetworkAccess.baseWeatherWeekUrlString + "?cityCode=" + city[0].toString());
            weatherWeekEntryList = WeatherJSONParser.readWeek(weatherStream);
        } catch (IOException e) {
            Log.d("Error", "error happens when getting weather data");
        }

        return weatherWeekEntryList;
    }

    protected void onPostExecute(ArrayList<WeatherWeekEntry> weatherEntry) {
        //return weatherEntry;
    }
}
