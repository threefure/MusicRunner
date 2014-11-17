package com.amk2.musicrunner.start;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.main.MusicRunnerApplication;
import com.amk2.musicrunner.musiclist.MusicListFragment;
import com.amk2.musicrunner.running.RunningActivity;
import com.amk2.musicrunner.utilities.MusicLib;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

/**
 * Created by daz on 2014/4/22.
 */
public class StartFragment extends Fragment implements
        View.OnClickListener,
        GoogleMap.CancelableCallback,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;



    private Long playlistId;

    private Activity mActivity;
    private View mFragmentView;

    private Button mGoRunningButton;
    private TextView mPlaylistTitleTextView;
    private LinearLayout chosenPlaylistContainerLinearLayout;

    private SharedPreferences mPlaylistSharedPreferences;
    private OnGoToPlaylistTabListener mOnGoToPlaylistTabListener;

    private GoogleMap googleMap;
    private Marker marker = null;
    private MarkerOptions markerOptions;
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private boolean isGpsOn = false;
    private AlertDialog.Builder dialog;

    public interface OnGoToPlaylistTabListener {
        public void OnGoToPlaylistTab();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.start_fragment, container, false);
    }
    @Override
    public void onActivityCreated (Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        mPlaylistSharedPreferences = getActivity().getSharedPreferences(Constant.PLAYLIST, Context.MODE_PRIVATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mPlaylistChangedReceiver, new IntentFilter(MusicListFragment.CHANGE_PLAYLIST));
        playlistId = mPlaylistSharedPreferences.getLong(Constant.PLAYLIST_ID, -1);
        initialize();

    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop () {
        mLocationClient.disconnect();
        super.onStop();
    }

    public void initialize() {
        mActivity = getActivity();
        mFragmentView = getView();

        mLocationClient = new LocationClient(mActivity, this, this);

        findViews();
        setViews();
        initButtons();

        // set up alert dialog for gps didn't open
        dialog = new AlertDialog.Builder(mActivity)
                .setMessage(R.string.gps_checking)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(mActivity, RunningActivity.class));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                });
    }

    private void initButtons() {
        mGoRunningButton.setOnClickListener(this);
    }

    private void findViews() {
        mGoRunningButton                    = (Button)mFragmentView.findViewById(R.id.start_go_running);
        chosenPlaylistContainerLinearLayout = (LinearLayout) mFragmentView.findViewById(R.id.chosen_playlist_container);
        mPlaylistTitleTextView              = (TextView) mFragmentView.findViewById(R.id.chosen_playlist);
        googleMap                           = ((MapFragment) getFragmentManager().findFragmentById(R.id.start_map)).getMap();
    }

    private void setViews() {
        if (playlistId != -1) {
            Uri playlistUri = MusicLib.getPlaylistUriFromId(playlistId);
            String playlistName = MusicLib.getPlaylistName(getActivity(), playlistUri);
            mPlaylistTitleTextView.setText(playlistName);
        } else {
            mPlaylistTitleTextView.setText(R.string.no_playlist_selected);
        }
        chosenPlaylistContainerLinearLayout.setOnClickListener(this);
    }

    private void updateMap() {
        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(currentLatLng, 17, 70, 0)));

        markerOptions.position(currentLatLng);
        if (marker == null) {
            marker = googleMap.addMarker(markerOptions);
        } else {
            marker.setPosition(currentLatLng);
        }
    }

    @Override
    public void onClick(View view) {
        Tracker t = ((MusicRunnerApplication) getActivity().getApplication()).getTracker(MusicRunnerApplication.TrackerName.APP_TRACKER);
        t.setScreenName("StartPage");
        switch(view.getId()) {
            case R.id.start_go_running:
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("Start")
                        .setAction("GoRunning")
                        .build());
                if (isGpsOn) {
                    startActivity(new Intent(mActivity, RunningActivity.class));
                } else {
                    dialog.show();
                }
                break;
            case R.id.chosen_playlist_container:
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("Start")
                        .setAction("ChoosePlaylist")
                        .build());
                mOnGoToPlaylistTabListener.OnGoToPlaylistTab();
                break;
        }
    }

    @Override
    public void onFinish() {
        Log.d("DAZ", "map is there");
    }

    @Override
    public void onCancel() {
        Log.d("DAZ", "map is canceled");
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = mLocationClient.getLastLocation();
        if (mCurrentLocation != null) {
            isGpsOn = true;
            googleMap.getUiSettings().setZoomControlsEnabled(false);

            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.dog_front);
            markerOptions = new MarkerOptions();
            markerOptions.icon(bitmapDescriptor);
            updateMap();
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(getActivity(), "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
                switch (resultCode) {
                    case Activity.RESULT_OK :
                        mLocationClient.connect();
                        break;
                }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            //showErrorDialog(connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (mCurrentLocation != null) {
            updateMap();
        }
    }

    private void updatePlaylistTextView() {
        Uri playlistUri = MusicLib.getPlaylistUriFromId(playlistId);
        String playlistName = MusicLib.getPlaylistName(mActivity, playlistUri);
        mPlaylistTitleTextView.setText(playlistName);
    }

    private BroadcastReceiver mPlaylistChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            playlistId = extras.getLong(MusicListFragment.PLAYLIST_ID);
            updatePlaylistTextView();
        }
    };

    public void setOnGoToPlaylistTabListener (OnGoToPlaylistTabListener listener) {
        mOnGoToPlaylistTabListener = listener;
    }
}
