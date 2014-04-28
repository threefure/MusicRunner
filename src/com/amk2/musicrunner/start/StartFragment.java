package com.amk2.musicrunner.start;

import android.app.Fragment;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import android.app.Fragment;
import android.content.IntentSender;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * Created by daz on 2014/4/22.
 */
public class StartFragment extends Fragment implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private TextView chanceOfRain;
    private TextView uvIndex;
    private TextView humidity;
    private TextView startTemperature;
    private TextView suggestionDialog;

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    //private InformationUpdater informationUpdater;
    private GetAddressFromLocation getAddress;
    private Address currentAddress;


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationClient = new LocationClient(this.getActivity().getApplicationContext(),
                this, this);
        getAddress = new GetAddressFromLocation(this.getActivity().getApplicationContext());
        CityCodeMapping.initialMap();
    }
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.start_fragment, container, false);
    }
    @Override //setting needed static information here
    public void onActivityCreated (Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        View thisView = getView();
        chanceOfRain = (TextView) thisView.findViewById(R.id.chance_of_rain_container);
        uvIndex = (TextView) thisView.findViewById(R.id.uv_index_container);
        humidity = (TextView) thisView.findViewById(R.id.humidity_container);
        startTemperature = (TextView) thisView.findViewById(R.id.start_temperature);
        suggestionDialog = (TextView) thisView.findViewById(R.id.suggestion_dialog);

        mLocationClient.connect();
    }
    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onPause () {
        super.onPause();
    }

    @Override
    public void onConnected (Bundle dataBundle) {
        Log.d("daz", "GooglePlayService conntected");
        getAddress.execute(mLocationClient.getLastLocation());

        try {
            currentAddress = getAddress.get();
            String cityCode = CityCodeMapping.getCityCode(currentAddress.getAdminArea());
            GetWeatherData weatherData = new GetWeatherData();
            weatherData.execute(cityCode);
            try {
                WeatherEntry weatherEntry = weatherData.get();
                chanceOfRain.setText(weatherEntry.chanceOfRain + "%");
                uvIndex.setText(weatherEntry.uv);
                startTemperature.setText(weatherEntry.temperature + ".C");
                suggestionDialog.setText(weatherEntry.feeling + "," + weatherEntry.condition);
            } catch (CancellationException e) {
                Log.d("Error", "This is canceled");
            } catch (InterruptedException e) {
                Log.d("Error", "This is interrupted");
            } catch (ExecutionException e) {
                Log.d("Error", "Execution error");
            }
        } catch (InterruptedException e) {
            Log.e("Error", "InterruptedException");
        } catch (ExecutionException e) {
            Log.e("Error", "ExecutionException");
        }
    }

    @Override
    public void onDisconnected () {
        Log.d("LocationUpdater", "GooglePlayService disconntected");
    }

    public void onConnectionFailed (ConnectionResult connectionResult) {
        Log.d("LocationUpdater", "GooglePlayService failed");
    }
/*
    // should have a data updating center?
    class InformationUpdater  extends AsyncTask<Void, Void, WeatherEntry> {
        @Override
        protected WeatherEntry doInBackground(Void... voids) {
            Log.d("Daz", "start to update information");
            WeatherEntry weatherEntry = null;
            GetWeatherData weatherData = new GetWeatherData();
            Integer cityCode = 0;
            Log.d("Daz", "execute getting weather data");
            weatherData.execute(cityCode);//execute getting weather data
            try {
                Log.d("Daz", "try to get weather");
                weatherEntry = weatherData.get();
                Log.d("Daz's weather string", weatherEntry.chanceOfRain);
            } catch (CancellationException e) {
                Log.d("Error", "This is canceled");
            } catch (InterruptedException e) {
                Log.d("Error", "This is interrupted");
            } catch (ExecutionException e) {
                Log.d("Error", "Execution error");
            }
            return weatherEntry;
        }

        @Override
        protected void onPostExecute(WeatherEntry weatherEntry) {
            Log.d("weather in post", weatherEntry.chanceOfRain);
        }
    }*/
}
