package com.amk2.musicrunner.my;

import android.app.Fragment;
import android.content.Context;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.my.MyFragment.MyTabFragmentListener;

import com.amk2.musicrunner.R;

/**
 * Created by daz on 2014/6/22.
 */
public class PastRecordFragment extends Fragment {

    private MyTabFragmentListener mMyTabFragmentListener;
    private TextView textViewTotalDistance;

    public void setMyTabFragmentListener(MyTabFragmentListener listener) {
        mMyTabFragmentListener = listener;
    }

    public void onBackPressed() {
        mMyTabFragmentListener.onSwitchBetweenMyAndPastRecordFragment();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.past_record_fragment, container, false);
    }

    @Override
    public void onActivityCreated (Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause () {
        super.onPause();
    }
}
