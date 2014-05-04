package com.amk2.musicrunner.start;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import com.amk2.musicrunner.start.WeatherModel.WeatherEntry;

/**
 * Created by daz on 2014/4/27.
 */
class GetWeatherData extends AsyncTask<String, Void, WeatherEntry> {
    private NetworkAccess na;
    public GetWeatherData () {
        super();
        na = new NetworkAccess();
    }

    @Override
    protected WeatherEntry doInBackground (String... city) {
        WeatherEntry weatherEntry = null;
        try {
            Log.d("Daz", "in getweatherdata");
            InputStream weatherStream = na.downloadUrl(NetworkAccess.baseWeatherUrlString + "?cityCode=" + city[0].toString());
            weatherEntry = WeatherJSONParser.read(weatherStream);
        } catch (IOException e) {
            Log.d("Error", "error happens when getting weather data");
        }

        return weatherEntry;
    }

    protected void onPostExecute(WeatherEntry weatherEntry) {
        //return weatherEntry;
    }
}
