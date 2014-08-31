package com.amk2.musicrunner.running;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amk2.musicrunner.R;

/**
 * Created by logicmelody on 2014/8/31.
 */
public class DistanceFragment extends Fragment {

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
    }

}
