package com.amk2.musicrunner.running;

import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.views.MusicRunnerLineRunningView;

import java.util.AbstractMap;

/**
 * Created by logicmelody on 2014/8/31.
 */
public class DistanceFragment extends Fragment implements View.OnClickListener{
    private MusicControllerFragment.OnChangeToMusicControllerListener mOnChangeToMusicControllerListener;
    private MapFragmentRun.OnChangeToMapListener mOnChangeToMapListener;

    private MusicRunnerLineRunningView dottedLineView;
    private Button toMusicPlayerButton;
    private Button toMapButton;

    public int getP_speed() {
        return p_speed;
    }

    public void setP_speed(int p_speed) {
        this.p_speed = p_speed;
    }

    private int p_speed = 10;

    public interface OnBackToDistanceListener {
        void onBackToDistance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.distance_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dottedLineView = (MusicRunnerLineRunningView) getView().findViewById(R.id.music_runner_line_running_view);
        toMusicPlayerButton = (Button) getView().findViewById(R.id.to_music_player_button);
        toMapButton         = (Button) getView().findViewById(R.id.to_map_button);
        distanceFragmentThread.start();

        toMusicPlayerButton.setOnClickListener(this);
        toMapButton.setOnClickListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        dottedLineView.clearState();
    }

    Thread distanceFragmentThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    dottedLineView.postInvalidate();
                    Thread.sleep(p_speed);
                } catch (Exception e) {
                    e.getLocalizedMessage();
                }
            }
        }
    });


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.to_map_button:
                mOnChangeToMapListener.onChangeToMap();
                break;
            case R.id.to_music_player_button:
                mOnChangeToMusicControllerListener.onChangeToMusicController();
                break;
        }
    }

    public void setOnChangeToMusicControllerListener (MusicControllerFragment.OnChangeToMusicControllerListener listener) {
        mOnChangeToMusicControllerListener = listener;
    }

    public void setOnChangeToMapListener (MapFragmentRun.OnChangeToMapListener listener) {
        mOnChangeToMapListener = listener;
    }
}
