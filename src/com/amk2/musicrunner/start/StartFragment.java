package com.amk2.musicrunner.start;

import android.app.Fragment;
import android.content.ContentResolver;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.MusicTrackMetaData.MusicTrackCommonDataDB;
import com.amk2.musicrunner.TableObserver;
import com.amk2.musicrunner.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by daz on 2014/4/22.
 */
public class StartFragment extends Fragment implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public interface StartTabFragmentListener {
    	void onSwitchBetweenStartAndWeatherFragment();
    }

    private TextView chanceOfRain;
    private TextView uvIndex;
    private TextView humidity;
    private TextView startTemperature;
    private TextView suggestionDialog;
    private LinearLayout startTemperatureContainer;

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private GetAddressFromLocation getAddress;
    private Address currentAddress;

    private StartTabFragmentListener mStartTabFragmentListener;

    private ContentResolver mContentResolver;

    public void setStartTabFragmentListener(StartTabFragmentListener listener) {
    	mStartTabFragmentListener = listener;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationClient = new LocationClient(this.getActivity().getApplicationContext(),
                this, this);
        getAddress = new GetAddressFromLocation(this.getActivity().getApplicationContext());

        //-------------Register table observer----------
        mContentResolver = getActivity().getContentResolver();
        TableObserver observer = new TableObserver(this.getActivity().getApplicationContext(), UIUpdater);
        mContentResolver.registerContentObserver(MusicTrackCommonDataDB.CONTENT_URI, true, observer);

        //LocationHelper.initialMap();
        DayMapping.initialMap();
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
        startTemperatureContainer = (LinearLayout) thisView.findViewById(R.id.start_temperature_container);
        startTemperatureContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Switch to WeatherFragment
            	mStartTabFragmentListener.onSwitchBetweenStartAndWeatherFragment();
            }
        });

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
        Location location = mLocationClient.getLastLocation();
        LocationHelper.setLocation(location);
        getAddress.execute(location);
        try {
            currentAddress = getAddress.get();
            LocationHelper.setCurrentAdminArea(currentAddress.getAdminArea());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    /*public void onConnected (Bundle dataBundle) {
        Log.d("daz", "GooglePlayService conntected");
        Location location = mLocationClient.getLastLocation();
        LocationHelper.setLocation(location);
        getAddress.execute(location);
        Log.v("lat", Double.toString(location.getLatitude()));
        Log.v("lng", Double.toString(location.getLongitude()));
        try {
            currentAddress = getAddress.get();
            String cityCode = LocationHelper.getCityCode(currentAddress.getAdminArea());
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
    }*/

    @Override
    public void onDisconnected () {
        Log.d("LocationUpdater", "GooglePlayService disconntected");
    }

    public void onConnectionFailed (ConnectionResult connectionResult) {
        Log.d("LocationUpdater", "GooglePlayService failed");
    }

    private Handler UIUpdater = new Handler() {
        public void handleMessage (Message msg) {
            switch (msg.what) {
                case Constant.UPDATE_START_FRAGMENT_UI:
                    Log.d("daz", "i am going to update UI");
                    Bundle bundle = msg.getData();
                    String JSONContent = bundle.getString(Constant.JSON_CONTENT);
                    Log.d("daz in start fragment", "should show json content:" + JSONContent);
                    updateWeatherUI(JSONContent);
                    Log.d("daz in start fragment", "completed updating");
                    break;
                default:

            }
        }
    };

    public void updateWeatherUI (String weatherJSONContent) {
        try {
            JSONObject weatherJSONObject = new JSONObject(weatherJSONContent);
            chanceOfRain.setText(weatherJSONObject.getString("chance-of-rain") + "%");
            uvIndex.setText(weatherJSONObject.getString("uv"));
            startTemperature.setText(weatherJSONObject.getString("maxT") + ".C");
            suggestionDialog.setText(weatherJSONObject.getString("feeling") + "," + weatherJSONObject.getString("condition"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
