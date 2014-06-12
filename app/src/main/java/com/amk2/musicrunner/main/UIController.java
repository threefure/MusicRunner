
package com.amk2.musicrunner.main;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.discover.DiscoverFragment;
import com.amk2.musicrunner.my.MyFragment;
import com.amk2.musicrunner.setting.SettingFragment;
import com.amk2.musicrunner.start.StartFragment;
import com.amk2.musicrunner.start.StartFragment.StartTabFragmentListener;
import com.amk2.musicrunner.start.WeatherFragment;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

/**
 * Manipulate most of UI controls in this app. Note: Operate the UI as far as
 * possible in this class, not in main activity.
 *
 * @author DannyLin
 */
public class UIController implements TabListener, ViewPager.OnPageChangeListener {

    private static final String TAG = "UIController";
    private static final int TAB_SIZE = 4;

    private final MusicRunnerActivity mMainActivity;

    private FragmentManager mFragmentManager;
    private ActionBar mActionBar;
    private SwipeControllableViewPager mViewPager;
    private MainTabViewPagerAdapter mMainPagerAdapter;

    // Fragments for each tab
    private MyFragment mMyFragment;
    private StartFragment mStartFragment;
    private WeatherFragment mWeatherFragment;
    private DiscoverFragment mDiscoverFragment;
    private SettingFragment mSettingFragment;

    public static class TabState {
        public static final int MY = 0;
        public static final int START = 1;
        public static final int DISCOVER = 2;
        public static final int SETTING = 3;
    }

    public static class FragmentTag {
        public static final String MY_FRAGMENT_TAG = "my_fragment";
        public static final String START_FRAGMENT_TAG = "start_fragment";
        public static final String WEATHER_FRAGMENT_TAG = "weather_fragment";
        public static final String DISCOVER_FRAGMENT_TAG = "discover_fragment";
        public static final String SETTING_FRAGMENT_TAG = "setting_fragment";
    }

    public UIController(MusicRunnerActivity activity) {
        mMainActivity = activity;
        mFragmentManager = activity.getFragmentManager();
        mActionBar = activity.getActionBar();
    }

    public void onActivityCreate(Bundle savedInstanceState) {
        initialize();
    }

    private void initialize() {
        initActionBar();
        initFragments();
        initViewPager();
    }

    /**
     * Create all fragments and add as children of the view pager. The pager
     * adapter will only change the visibility(show/hide). It'll never
     * create/destroy fragments. If it's after screen rotation, the fragment
     * have been recreated by the FragmentManager. So first see if there're
     * already the target fragment existing.
     */
    private void initFragments() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        // Init StartFragment
        mStartFragment = (StartFragment) mFragmentManager
                .findFragmentByTag(FragmentTag.START_FRAGMENT_TAG);
        if (mStartFragment == null) {
            mStartFragment = new StartFragment();
            transaction.add(R.id.tab_pager, mStartFragment, FragmentTag.START_FRAGMENT_TAG);
        }

        // Init MyFragment
        mMyFragment = (MyFragment) mFragmentManager.findFragmentByTag(FragmentTag.MY_FRAGMENT_TAG);
        if (mMyFragment == null) {
            mMyFragment = new MyFragment();
            transaction.add(R.id.tab_pager, mMyFragment, FragmentTag.MY_FRAGMENT_TAG);
        }

        // Init SettingFragment
        mSettingFragment = (SettingFragment) mFragmentManager
                .findFragmentByTag(FragmentTag.SETTING_FRAGMENT_TAG);
        if (mSettingFragment == null) {
            mSettingFragment = new SettingFragment();
            transaction.add(R.id.tab_pager, mSettingFragment, FragmentTag.SETTING_FRAGMENT_TAG);
        }

        // Init DiscoverFragment
        mDiscoverFragment = (DiscoverFragment) mFragmentManager
                .findFragmentByTag(FragmentTag.DISCOVER_FRAGMENT_TAG);
        if (mDiscoverFragment == null) {
            mDiscoverFragment = new DiscoverFragment();
            transaction.add(R.id.tab_pager, mDiscoverFragment, FragmentTag.DISCOVER_FRAGMENT_TAG);
        }

        transaction.hide(mStartFragment);
        transaction.hide(mMyFragment);
        transaction.hide(mSettingFragment);
        transaction.hide(mDiscoverFragment);
        transaction.commit();
    }

    private void initViewPager() {
        mViewPager = (SwipeControllableViewPager) mMainActivity.findViewById(R.id.tab_pager);
        mMainPagerAdapter = new MainTabViewPagerAdapter(mFragmentManager,TAB_SIZE);
        mViewPager.setAdapter(mMainPagerAdapter);
        mViewPager.setSwipeable(true);
        mViewPager.setOnPageChangeListener(this);
    }

    private void initActionBar() {
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Add tabs
        mActionBar.addTab(mActionBar.newTab().setText(mMainActivity.getString(R.string.my_tab))
                .setTabListener(this));
        mActionBar.addTab(mActionBar.newTab().setText(mMainActivity.getString(R.string.start_tab))
                .setTabListener(this));
        mActionBar.addTab(mActionBar.newTab()
                .setText(mMainActivity.getString(R.string.discover_tab)).setTabListener(this));
        mActionBar.addTab(mActionBar.newTab().setText(mMainActivity.getString(R.string.setting_tab))
                .setTabListener(this));
    }

    public void onActivityRestoreInstanceState(Bundle savedInstanceState) {

    }

    public void onActivityResume() {

    }

    public void onActivitySaveInstanceState(Bundle outState) {

    }

    public void onActivityPause() {

    }

    public void onActivityDestroy() {

    }

    public void onActivityBackPressed() {
        if (mViewPager.getCurrentItem() == TabState.START
                && mMainPagerAdapter.getFragment(TabState.START) instanceof WeatherFragment) {
            ((WeatherFragment) mMainPagerAdapter.getFragment(TabState.START)).backPressed();
        } else {
            mMainActivity.finish();
        }
    }

    /**
     * Control which fragment needed to be displayed according to the tab
     *
     * @author DannyLin
     */
    public class MainTabViewPagerAdapter extends AbstractTabViewPagerAdapter implements StartTabFragmentListener {

        private Fragment mFragmentAtStartTab;

        public MainTabViewPagerAdapter(FragmentManager fm, int size) {
            super(fm, size);
            setSwitchFragmentListener();
        }

        private void setSwitchFragmentListener() {
            mStartFragment.setStartTabFragmentListener(this);
        }

        @Override
        protected Fragment getFragment(int position) {
            switch (position) {
                case TabState.MY:
                    return mMyFragment;
                case TabState.START:
                    if (mFragmentAtStartTab == null) {
                        mFragmentAtStartTab = mStartFragment;
                    }
                    return mFragmentAtStartTab;
                case TabState.DISCOVER:
                    return mDiscoverFragment;
                case TabState.SETTING:
                    return mSettingFragment;
            }
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof StartFragment && mFragmentAtStartTab instanceof WeatherFragment) {
                return POSITION_NONE;
            }
            if (object instanceof WeatherFragment && mFragmentAtStartTab instanceof StartFragment) {
                return POSITION_NONE;
            }
            return POSITION_UNCHANGED;
        }

        @Override
        public void onSwitchBetweenStartAndWeatherFragment() {
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            if (mFragmentAtStartTab instanceof StartFragment) {
                addWeatherFragment();
                mFragmentAtStartTab = mWeatherFragment;
                ((WeatherFragment) mFragmentAtStartTab).setStartTabFragmentListener(this);
            } else { // Instance of WeatherFragment
                mCurTransaction.remove(mFragmentAtStartTab);
                mFragmentAtStartTab = mStartFragment;
            }
            notifyDataSetChanged();
        }

        private void addWeatherFragment() {
            mWeatherFragment = (WeatherFragment) mFragmentManager
                    .findFragmentByTag(FragmentTag.WEATHER_FRAGMENT_TAG);
            if (mWeatherFragment == null) {
                mWeatherFragment = new WeatherFragment();
                mCurTransaction.add(R.id.tab_pager, mWeatherFragment,
                        FragmentTag.WEATHER_FRAGMENT_TAG);
            }
        }

    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageSelected(int position) {
        mActionBar.setSelectedNavigationItem(position);
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(tab.getPosition(), true);
            Log.d(TAG, "Set current position = " + tab.getPosition());
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

}
