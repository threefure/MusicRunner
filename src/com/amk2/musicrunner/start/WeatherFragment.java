package com.amk2.musicrunner.start;

import android.app.Fragment;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.start.WeatherModel.WeatherWeekEntry;
import com.amk2.musicrunner.start.WeatherModel.WeatherHourlyEntry;
import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;


/**
 * Created by daz on 2014/4/29.
 */
public class WeatherFragment extends Fragment{
    private static final LinearLayout.LayoutParams weeklyParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);

    private LinearLayout hourlyWeatherForecast;
    private LinearLayout weeklyWeatherForecast;
    private GetAddressFromLocation getAddress;
    private LayoutInflater inflater;
    private Location location;
    private Address currentAddress;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getAddress = new GetAddressFromLocation(this.getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.weather_fragment, container, false);
    }

    @Override
    public void onActivityCreated (Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        View thisView = getView();
        hourlyWeatherForecast = (LinearLayout) thisView.findViewById(R.id.hourly_weather_forecast);
        weeklyWeatherForecast = (LinearLayout) thisView.findViewById(R.id.weekly_weather_forecast);
        inflater = (LayoutInflater) thisView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        location = CityCodeMapping.getLocation();
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO should store currentAddress in shared area, shouldn't call getAddress again
        location = new Location("Taipei");
        location.setLatitude(25.0426572);
        location.setLongitude(121.5745622);
        if (location != null) {
            getAddress.execute(location);
            try {
                currentAddress = getAddress.get();
                String cityCode = CityCodeMapping.getCityCode(currentAddress.getAdminArea());
                get24HoursForecast(cityCode);
                getWeekForecast(cityCode);

            } catch (InterruptedException e) {
                Log.e("Error", "InterruptedException");
            } catch (ExecutionException e) {
                Log.e("Error", "ExecutionException");
            }
        }
        /*for (int i = 0 ; i < 50; i ++) {
            View hourly = inflater.inflate(R.layout.hourly_template, null);
            TextView time = (TextView) hourly.findViewById(R.id.time);
            time.setText("9:00");
            TextView temperature = (TextView) hourly.findViewById(R.id.temperature);
            temperature.setText("25.c");
            hourlyWeatherForecast.addView(hourly);
        }*/
    }

    private void getWeekForecast (String cityCode) {
        GetWeatherWeekData weatherWeekData = new GetWeatherWeekData();
        weatherWeekData.execute(cityCode);
        try {
            ArrayList<WeatherWeekEntry> weatherWeekEntryList = weatherWeekData.get();
            for (int i = 0; i < weatherWeekEntryList.size(); i++) {
                addWeekForecast(weatherWeekEntryList.get(i));
            }
        } catch (CancellationException e) {
            Log.d("Error", "This is canceled");
        } catch (InterruptedException e) {
            Log.d("Error", "This is interrupted");
        } catch (ExecutionException e) {
            Log.d("Error", "Execution error");
        }
    }

    private void addWeekForecast (WeatherWeekEntry weatherWeekEntry) {
        View weekly = inflater.inflate(R.layout.weekly_template, null);
        TextView day = (TextView) weekly.findViewById(R.id.day);
        day.setText(DayMapping.getDay(weatherWeekEntry.day));
        TextView temperature = (TextView) weekly.findViewById(R.id.temperature);
        temperature.setText(weatherWeekEntry.max_t + "/" + weatherWeekEntry.min_t + ".C");
        weeklyWeatherForecast.addView(weekly, weeklyParams);
    }

    private void get24HoursForecast (String cityCode) {
        GetWeather24HoursData weatherHourData = new GetWeather24HoursData();
        weatherHourData.execute(cityCode);
        try {
            ArrayList<WeatherHourlyEntry> weatherHourlyEntryList = weatherHourData.get();
            for (int i = 0; i < weatherHourlyEntryList.size(); i++) {
                addHourForecast(weatherHourlyEntryList.get(i));
            }
        } catch (CancellationException e) {
            Log.d("Error", "This is canceled");
        } catch (InterruptedException e) {
            Log.d("Error", "This is interrupted");
        } catch (ExecutionException e) {
            Log.d("Error", "Execution error");
        }
    }
    private void addHourForecast (WeatherHourlyEntry weatherHourlyEntry) {
        View hourly = inflater.inflate(R.layout.hourly_template, null);
        TextView time = (TextView) hourly.findViewById(R.id.time);
        time.setText(weatherHourlyEntry.time + ":00");
        TextView temperature = (TextView) hourly.findViewById(R.id.temperature);
        temperature.setText(weatherHourlyEntry.max_t + "/" + weatherHourlyEntry.min_t + ".C");
        hourlyWeatherForecast.addView(hourly);
    }

    @Override
    public void onPause () {
        super.onPause();
    }
}
