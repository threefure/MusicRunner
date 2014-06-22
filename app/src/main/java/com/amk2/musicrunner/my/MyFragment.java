package com.amk2.musicrunner.my;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.constants.SharedPreferenceConstants;
import com.amk2.musicrunner.utilities.RestfulUtility;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MyFragment extends Fragment implements TabHost.OnTabChangeListener, View.OnClickListener {
    private ProgressBar timesBar;
    private ProgressBar speedsBar;
    private ProgressBar caloriesBar;
    private ProgressBar distanceBar;
    private Integer timesBarStatus;
    private Integer speedsBarStatus;
    private Integer caloriesBarStatus;
    private Integer distanceBarStatus;
    private ImageButton pastRecordMutton;
    private Handler handler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.my_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// This function is like onCreate() in activity.
		// You can start from here.
        SharedPreferences preferences = this.getActivity().getSharedPreferences(SharedPreferenceConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String account = preferences.getString(SharedPreferenceConstants.ACCOUNT_PARAMS, null);
//        TextView textView = (TextView) getView().findViewById(R.id.my_view);
//        textView.setText(account,TextView.BufferType.EDITABLE);
        initializeTab();
	}

    private void initializeTab(){
        TabHost tabHost = (TabHost) getView().findViewById(R.id.my_tab_host);
        tabHost.setup();
        TabHost.TabSpec spec = tabHost.newTabSpec("Running");
        spec.setContent(R.id.my_running_tab);
        spec.setIndicator("Running", getResources().getDrawable(android.R.drawable.ic_lock_idle_alarm));
        tabHost.addTab(spec);
        TabHost.TabSpec spec2 = tabHost.newTabSpec("Music");
        spec2.setContent(R.id.my_music_tab);
        spec2.setIndicator("Music", getResources().getDrawable(android.R.drawable.ic_lock_idle_alarm));
        tabHost.addTab(spec2);

        tabHost.setCurrentTab(0);

        //set default tab background color
        tabHost.getTabWidget().getChildAt(0).setBackgroundColor(Color.BLACK);
        tabHost.getTabWidget().getChildAt(1).setBackgroundColor(Color.WHITE);
        //set tab text color by selector
        TabWidget tabWidget = (TabWidget) tabHost.findViewById(android.R.id.tabs);
        View tabView = tabWidget.getChildTabViewAt(0);
        TextView tab = (TextView) tabView.findViewById(android.R.id.title);
        tab.setTextColor(this.getResources().getColorStateList(R.drawable.my_tab_selector));
        View tabView2 = tabWidget.getChildTabViewAt(1);
        TextView tab2 = (TextView) tabView2.findViewById(android.R.id.title);
        tab2.setTextColor(this.getResources().getColorStateList(R.drawable.my_tab_selector));
        //set tab background color depending on selected/unselected
        tabHost.setOnTabChangedListener(this);

        timesBar = (ProgressBar) getView().findViewById(R.id.time_bar);
        timesBar.setMax(100);

        speedsBar = (ProgressBar) getView().findViewById(R.id.speeds_bar);
        speedsBar.setMax(100);

        caloriesBar = (ProgressBar) getView().findViewById(R.id.calories_bar);
        caloriesBar.setMax(100);

        distanceBar = (ProgressBar) getView().findViewById(R.id.distance_bar);
        distanceBar.setMax(100);

        pastRecordMutton = (ImageButton) getView().findViewById(R.id.my_past_record_button);
        pastRecordMutton.setOnClickListener(this);

        String jsonString = getMyStatus();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            timesBarStatus = Integer.valueOf(jsonObject.getString("times"));
            timesBar.setProgress(timesBarStatus);

            speedsBarStatus = Integer.valueOf(jsonObject.getString("speeds"));
            speedsBar.setProgress(speedsBarStatus);

            caloriesBarStatus = Integer.valueOf(jsonObject.getString("calories"));
            caloriesBar.setProgress(caloriesBarStatus);

            distanceBarStatus = Integer.valueOf(jsonObject.getString("distance"));
            distanceBar.setProgress(distanceBarStatus);

        } catch (JSONException jsonException) {

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private String getMyStatus(){
        SharedPreferences preferences = getActivity().getSharedPreferences(SharedPreferenceConstants.PREFERENCE_NAME, getActivity().MODE_PRIVATE);
//        String account =  preferences.getString(SharedPreferenceConstants.ACCOUNT_PARAMS, null);
        String account = "paulou";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://ec2-54-187-71-254.us-west-2.compute.amazonaws.com:8080/getMyStatus");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("userAccount", account));
        HttpResponse response = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(pairs));
            response = client.execute(post);
        } catch (UnsupportedEncodingException uee) {

        } catch (ClientProtocolException cpe) {
            //ignore this exception for now
        } catch (IOException ioe) {
            //ignore this exception for now
        }
        return RestfulUtility.getStatusCode(response);
    }
    @Override
    public void onTabChanged(String tabId) {
        TabHost tab = (TabHost) getView().findViewById(R.id.my_tab_host);
        for (int i = 0; i < tab.getTabWidget().getChildCount(); i++) {
            tab.getTabWidget().getChildAt(i)
                    .setBackgroundResource(R.color.my_background_tab_unselected); // unselected
        }
        tab.getTabWidget().getChildAt(tab.getCurrentTab())
                .setBackgroundResource(R.color.my_background_tab_selected); // selected

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_past_record_button:
                // go to past record fragment
                Log.d("Daz", "past record button is pressed");
                break;
        }
    }
}
