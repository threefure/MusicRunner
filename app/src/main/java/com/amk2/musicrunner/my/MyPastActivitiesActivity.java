package com.amk2.musicrunner.my;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.sqliteDB.MusicRunnerDBMetaData.MusicRunnerRunningEventDB;
import com.amk2.musicrunner.utilities.PhotoLib;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.amk2.musicrunner.utilities.UnitConverter;

import org.w3c.dom.Text;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by ktlee on 9/10/14.
 */
public class MyPastActivitiesActivity extends Activity implements View.OnClickListener{

    public static final String TAG = "MyPastActivitiesActivity";

    private final int PastActivityIdTag = 1;
    private ActionBar mActionBar;
    private ContentResolver mContentResolver;

    private LinearLayout myPastActivityInitialInformation;
    //private LinearLayout myPastActivityContainer;
    private LayoutInflater inflater;
    private SharedPreferences mSettingSharedPreferences;
    private Integer distanceUnit;
    private String language;

    private ArrayList<PastActivityMetaData> mPastActivityMetaDataArrayList;
    private ListView pastActivitiesListView;
    private PastActivitiesAdapter pastActivitiesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_past_activities);
        mContentResolver = getContentResolver();

        initialize();
        setViews();
        getPastActivities();
    }

    private void initialize() {
        mActionBar = getActionBar();
        mSettingSharedPreferences = getSharedPreferences(SettingActivity.SETTING_SHARED_PREFERENCE, 0);
        distanceUnit = mSettingSharedPreferences.getInt(SettingActivity.DISTANCE_UNIT, SettingActivity.SETTING_DISTANCE_KM);
        language     = mSettingSharedPreferences.getString(SettingActivity.LANGUAGE, SettingActivity.SETTING_LANGUAGE_ENGLISH);
        initActionBar();
        initViews();
    }

    private void initViews() {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        myPastActivityInitialInformation = (LinearLayout) findViewById(R.id.initial_information);
        //myPastActivityContainer = (LinearLayout) findViewById(R.id.my_past_activity_container);

        pastActivitiesListView = (ListView) findViewById(R.id.past_activities_list_view);
    }

    private void setViews() {
        mPastActivityMetaDataArrayList = new ArrayList<PastActivityMetaData>();
        pastActivitiesAdapter = new PastActivitiesAdapter(this, R.layout.my_past_activity_template, mPastActivityMetaDataArrayList);
        pastActivitiesListView.setAdapter(pastActivitiesAdapter);
    }

    private void getPastActivities() {
        Integer eventId, duration;
        String distance, currentEpoch, photoPath;
        String[] projection = {
                MusicRunnerRunningEventDB.COLUMN_NAME_ID,
                MusicRunnerRunningEventDB.COLUMN_NAME_DURATION,
                MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE,
                MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND,
                MusicRunnerRunningEventDB.COLUMN_NAME_PHOTO_PATH
        };
        String orderBy = MusicRunnerRunningEventDB.COLUMN_NAME_ID + " DESC";
        Cursor cursor = mContentResolver.query(MusicRunnerRunningEventDB.CONTENT_URI, projection, null, null, orderBy);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                eventId      = cursor.getInt(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_ID));
                duration     = cursor.getInt(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DURATION));
                distance     = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DISTANCE));
                currentEpoch = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_DATE_IN_MILLISECOND));
                photoPath    = cursor.getString(cursor.getColumnIndex(MusicRunnerRunningEventDB.COLUMN_NAME_PHOTO_PATH));
                //addPastActivity(eventId, duration, distance, currentEpoch, photoPath);
                PastActivityMetaData pastActivityMetaData = new PastActivityMetaData(eventId, duration, Long.parseLong(currentEpoch), Double.parseDouble(distance), photoPath);
                mPastActivityMetaDataArrayList.add(pastActivityMetaData);
            }
            pastActivitiesAdapter.updatePastActivityMetaDataArrayList(mPastActivityMetaDataArrayList);
        } else {
            myPastActivityInitialInformation.setVisibility(View.VISIBLE);
        }
        cursor.close();
    }

    private void initActionBar() {
        View actionBarView = View.inflate(mActionBar.getThemedContext(), R.layout.customized_action_bar, null);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(Gravity.CENTER));
    }

    public class PastActivitiesAdapter extends ArrayAdapter<PastActivityMetaData> implements View.OnClickListener {
        ArrayList<PastActivityMetaData> pastActivityMetaDataArrayList;
        public PastActivitiesAdapter(Context context, int resource, ArrayList<PastActivityMetaData> pastActivityMetaDatas) {
            super(context, resource);
            pastActivityMetaDataArrayList = pastActivityMetaDatas;
        }

        public void updatePastActivityMetaDataArrayList (ArrayList<PastActivityMetaData> pastActivityMetaDatas) {
            pastActivityMetaDataArrayList = pastActivityMetaDatas;
            notifyDataSetChanged();
        }

        @Override
        public int getCount () {
            return pastActivityMetaDataArrayList.size();
        }

        @Override
        public PastActivityMetaData getItem (int i) {
            return pastActivityMetaDataArrayList.get(i);
        }

        @Override
        public long getItemId (int i) {
            return pastActivityMetaDataArrayList.get(i).eventId;
        }

        @Override
        public View getView (int i, View view, ViewGroup viewGroup) {
            PastActivityMetaData pastActivityMetaData = pastActivityMetaDataArrayList.get(i);
            TextView myPastActivityDateTextView;
            TextView myPastActivityDurationTextView;
            TextView myPastActivityDistanceTextView;
            TextView myPastActivityDistanceUnitTextView;
            ImageView myPastActivityPhotoImageView;
            if (view == null) {
                view = inflater.inflate(R.layout.my_past_activity_template, null);
                myPastActivityDateTextView         = (TextView) view.findViewById(R.id.my_past_activity_date);
                myPastActivityDurationTextView     = (TextView) view.findViewById(R.id.my_past_activity_duration);
                myPastActivityDistanceTextView     = (TextView) view.findViewById(R.id.my_past_activity_distance);
                myPastActivityDistanceUnitTextView = (TextView) view.findViewById(R.id.my_past_activity_distance_unit);
                myPastActivityPhotoImageView       = (ImageView) view.findViewById(R.id.my_past_activity_photo);

                PastActivityViewTag pastActivityViewTag = new PastActivityViewTag(
                        myPastActivityDateTextView, myPastActivityDurationTextView, myPastActivityDistanceTextView, myPastActivityDistanceUnitTextView, myPastActivityPhotoImageView, pastActivityMetaData.eventId
                );
                view.setTag(pastActivityViewTag);

                // setting click listener for past activity
                view.setOnClickListener(this);
            } else {
                PastActivityViewTag pastActivityViewTag = (PastActivityViewTag) view.getTag();
                // need to reset event id in order to getting correct event
                pastActivityViewTag.eventId = pastActivityMetaData.eventId;

                myPastActivityDateTextView         = pastActivityViewTag.myPastActivityDate;
                myPastActivityDurationTextView     = pastActivityViewTag.myPastActivityDuration;
                myPastActivityDistanceTextView     = pastActivityViewTag.myPastActivityDistance;
                myPastActivityDistanceUnitTextView = pastActivityViewTag.myPastActivityDistanceUnit;
                myPastActivityPhotoImageView       = pastActivityViewTag.myPastActivityPhoto;

            }

            // setting locale
            Locale locale = Locale.US;
            if (language != SettingActivity.SETTING_LANGUAGE_ENGLISH) {
                locale = Locale.TAIWAN;
            }
            // setting date
            myPastActivityDateTextView.setText(TimeConverter.getDateString(pastActivityMetaData.eventEpoch, locale));


            // setting duration
            myPastActivityDurationTextView.setText(TimeConverter.getDurationString(TimeConverter.getReadableTimeFormatFromSeconds(pastActivityMetaData.duration)));

            Double distance = pastActivityMetaData.distance;
            String distanceUnitString = getResources().getString(R.string.km);
            if (distanceUnit == SettingActivity.SETTING_DISTANCE_MI) {
                distance = UnitConverter.getMIFromKM(pastActivityMetaData.distance);
                distanceUnitString = getResources().getString(R.string.mi);
            }
            // setting distance
            myPastActivityDistanceTextView.setText(StringLib.truncateDoubleString(distance.toString(), 2));

            // setting distance unit
            myPastActivityDistanceUnitTextView.setText(distanceUnitString);

            // setting photo
            if (pastActivityMetaData.photoPath != null && pastActivityMetaData.photoPath.length() > 0) {
                Bitmap resizedPhoto = PhotoLib.resizeToFitTarget(pastActivityMetaData.photoPath, myPastActivityPhotoImageView.getLayoutParams().width, myPastActivityPhotoImageView.getLayoutParams().height);
                myPastActivityPhotoImageView.setImageBitmap(resizedPhoto);
            }

            return view;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.my_past_activity:
                    PastActivityViewTag pastActivityMetaData = (PastActivityViewTag) view.getTag();
                    Integer eventId = pastActivityMetaData.eventId;
                    Intent intent = new Intent(getApplicationContext(), MyPastActivityDetailsActivity.class);
                    intent.putExtra(MyPastActivityDetailsActivity.EVENT_ID, eventId);
                    startActivity(intent);
                    break;
            }
        }

        public class PastActivityViewTag {
            Integer eventId;
            TextView myPastActivityDate;
            TextView myPastActivityDuration;
            TextView myPastActivityDistance;
            TextView myPastActivityDistanceUnit;
            ImageView myPastActivityPhoto;
            public PastActivityViewTag(TextView myPastActivityDate, TextView myPastActivityDuration, TextView myPastActivityDistance, TextView myPastActivityDistanceUnit, ImageView myPastActivityPhoto, Integer eventId) {
                this.myPastActivityDate = myPastActivityDate;
                this.myPastActivityDuration = myPastActivityDuration;
                this.myPastActivityDistance = myPastActivityDistance;
                this.myPastActivityDistanceUnit = myPastActivityDistanceUnit;
                this.myPastActivityPhoto = myPastActivityPhoto;
                this.eventId = eventId;
            }
        }
    }

    public class PastActivityMetaData {
        Integer eventId;
        Integer duration;
        Long eventEpoch;
        Double distance;
        String photoPath;

        public PastActivityMetaData (Integer eventId, Integer duration, Long eventEpoch, Double distance, String photoPath) {
            this.eventId    = eventId;
            this.duration   = duration;
            this.eventEpoch = eventEpoch;
            this.distance   = distance;
            this.photoPath  = photoPath;
        }
    }

    @Override
    protected void onStart () {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_past_activity:
                Integer eventId = (Integer) v.getTag();
                Intent intent = new Intent(getApplicationContext(), MyPastActivityDetailsActivity.class);
                intent.putExtra(MyPastActivityDetailsActivity.EVENT_ID, eventId);
                startActivity(intent);
                break;
        }
    }
}
