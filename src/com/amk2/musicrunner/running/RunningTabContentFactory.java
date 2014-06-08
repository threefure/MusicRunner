package com.amk2.musicrunner.running;

import android.content.Context;
import android.view.View;
import android.widget.TabHost.TabContentFactory;

public class RunningTabContentFactory implements TabContentFactory {

    private Context mContext;

    public RunningTabContentFactory(Context context) {
        mContext = context;
    }

    @Override
    public View createTabContent(String tag) {
        View v = new View(mContext);
        v.setMinimumWidth(0);
        v.setMinimumHeight(0);
        v.setVisibility(View.GONE);
        return v;
    }

}
