package com.amk2.musicrunner.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public abstract class AbstractTabViewPagerAdapter extends PagerAdapter {

    protected FragmentManager mFm;
    protected FragmentTransaction mCurTransaction = null;
    protected int mAdapterSize = 0;

    protected abstract Fragment getFragment(int position);

    public AbstractTabViewPagerAdapter(FragmentManager fm, int size) {
        mFm = fm;
        mAdapterSize = size;
    }

    /**
     * Show fragment Fragments will be instantiated near the current
     * fragment. Ex: Current = Fragment2 => Fragment1 and Fragment3 will be
     * instantiated.
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mCurTransaction == null) {
            mCurTransaction = mFm.beginTransaction();
        }

        Fragment f = getFragment(position);
        mCurTransaction.show(f);

        return f;
    }

    /**
     * We override this method to just hide the fragment instead of
     * destroying it.
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (mCurTransaction == null) {
            mCurTransaction = mFm.beginTransaction();
        }
        mCurTransaction.hide((Fragment) object);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commit();
            mCurTransaction = null;
        }
    }

    @Override
    public int getCount() {
        return mAdapterSize;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

}
