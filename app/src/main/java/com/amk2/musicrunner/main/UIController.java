
package com.amk2.musicrunner.main;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.RunningTabContentFactory;
import com.amk2.musicrunner.music.MusicRankFragment;
import com.amk2.musicrunner.musiclist.MusicListFragment;
import com.amk2.musicrunner.my.MyFragment;
//import com.amk2.musicrunner.my.PastRecordFragment;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.start.StartFragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;

/**
 * Manipulate most of UI controls in this app. Note: Operate the UI as far as
 * possible in this class, not in main activity.
 *
 * @author DannyLin
 */
public class UIController implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener, View.OnClickListener {

    public static final int REQUEST_SETTING = 1;

    private static final String TAG = "UIController";
    private static final int TAB_SIZE = 4;

    private final MusicRunnerActivity mMainActivity;

    private ActionBar mActionBar;
    private ImageView mSettingButton;

    private FragmentManager mFragmentManager;
    private SwipeControllableViewPager mViewPager;
    private MainTabViewPagerAdapter mMainPagerAdapter;

    // Fragments for each tab
    private StartFragment mStartFragment;
    private MyFragment mMyFragment;
    //private PastRecordFragment mPastRecordFragment;
    private MusicListFragment mMusicListFragment;
    private MusicRankFragment mMusicRankFragment;
    //private WeatherFragment mWeatherFragment;
    //private DiscoverFragment mDiscoverFragment;
    //private SettingFragment mSettingFragment;

    private TabHost mTabHost;

    public static class TabState {
        public static final int START = 0;
        public static final int MY = 1;
        public static final int MUSIC_LIST = 2;
        public static final int MUSIC_RANK = 3;
    }

    public static class TabTag {
        public static final String START_TAB_TAG = "start_tab_tag";
        public static final String MY_TAB_TAG = "my_tab_tag";
        public static final String MUSIC_LIST_TAB_TAG = "music_list_tab_tag";
        public static final String MUSIC_RANK_TAB_TAG = "setting_tab_tag";
    }

    public static class FragmentTag {
        public static final String START_FRAGMENT_TAG = "start_fragment";
        public static final String MY_FRAGMENT_TAG = "my_fragment";
        //public static final String PAST_RECORD_FRAGMENT_TAG = "past_record_fragment";
        public static final String MUSIC_LIST_FRAGMENT_TAG = "music_list_fragment";
        public static final String MUSIC_RANK_FRAGMENT_TAG = "music_rank_fragment";
        //public static final String WEATHER_FRAGMENT_TAG = "weather_fragment";
        //public static final String DISCOVER_FRAGMENT_TAG = "discover_fragment";
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
        initTabs();
        mViewPager.setCurrentItem(TabState.START);
    }

    private void initActionBar() {
        View actionBarView = View.inflate(mActionBar.getThemedContext(), R.layout.customized_action_bar, null);
        mSettingButton = (ImageView)actionBarView.findViewById(R.id.setting_button);
        mSettingButton.setOnClickListener(this);

        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(actionBarView, new ActionBar.LayoutParams(Gravity.CENTER));
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

        // Init MusicListFragment
        mMusicListFragment = (MusicListFragment) mFragmentManager
                .findFragmentByTag(FragmentTag.MUSIC_LIST_FRAGMENT_TAG);
        if (mMusicListFragment == null) {
            mMusicListFragment = new MusicListFragment();
            transaction.add(R.id.tab_pager, mMusicListFragment, FragmentTag.MUSIC_LIST_FRAGMENT_TAG);
        }

        // Init MusicRankFragment
        mMusicRankFragment = (MusicRankFragment) mFragmentManager
                .findFragmentByTag(FragmentTag.MUSIC_RANK_FRAGMENT_TAG);
        if (mMusicRankFragment == null) {
            mMusicRankFragment = new MusicRankFragment();
            transaction.add(R.id.tab_pager, mMusicRankFragment, FragmentTag.MUSIC_RANK_FRAGMENT_TAG);
        }

        transaction.hide(mStartFragment);
        transaction.hide(mMyFragment);
        transaction.hide(mMusicListFragment);
        transaction.hide(mMusicRankFragment);
        transaction.commit();
    }

    private void initViewPager() {
        mViewPager = (SwipeControllableViewPager) mMainActivity.findViewById(R.id.tab_pager);
        mMainPagerAdapter = new MainTabViewPagerAdapter(mFragmentManager,TAB_SIZE);
        mViewPager.setAdapter(mMainPagerAdapter);
        mViewPager.setSwipeable(true);
        mViewPager.setOnPageChangeListener(this);
    }

    private void initTabs() {
        mTabHost = (TabHost)mMainActivity.findViewById(android.R.id.tabhost);
        mTabHost.setup();
        addTab(TabTag.START_TAB_TAG);
        addTab(TabTag.MY_TAB_TAG);
        addTab(TabTag.MUSIC_LIST_TAB_TAG);
        addTab(TabTag.MUSIC_RANK_TAB_TAG);
        mTabHost.setOnTabChangedListener(this);
        setTabClickListener();
    }

    private void setTabClickListener() {
        if (mViewPager != null) {
            // Start
            mTabHost.getTabWidget().getChildTabViewAt(TabState.START).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(TabState.START);
                    Log.d(TAG, "Set current position = " + mTabHost.getCurrentTab());
                }
            });

            // My
            mTabHost.getTabWidget().getChildTabViewAt(TabState.MY).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(TabState.MY);
                    //if(mMainPagerAdapter.getFragment(TabState.MY) instanceof PastRecordFragment) {
                    //    ((PastRecordFragment) mMainPagerAdapter.getFragment(TabState.MY)).onBackPressed();
                    //}
                    Log.d(TAG, "Set current position = " + mTabHost.getCurrentTab());
                }
            });

            // Music list
            mTabHost.getTabWidget().getChildTabViewAt(TabState.MUSIC_LIST).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(TabState.MUSIC_LIST);
                    Log.d(TAG, "Set current position = " + mTabHost.getCurrentTab());
                }
            });

            // Music rank
            mTabHost.getTabWidget().getChildTabViewAt(TabState.MUSIC_RANK).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(TabState.MUSIC_RANK);
                    Log.d(TAG, "Set current position = " + mTabHost.getCurrentTab());
                }
            });
        }
    }

    private void addTab(String tag) {
        View tabView = getTabView(tag);
        mTabHost.addTab(mTabHost.newTabSpec(tag).setIndicator(tabView)
                .setContent(new RunningTabContentFactory(mMainActivity)));
    }

    private View getTabView(String tag) {
        LayoutInflater layoutInflater = LayoutInflater.from(mMainActivity);
        View tabView = new View(mMainActivity);
        if(TabTag.START_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.start_tab, null);
        } else if(TabTag.MY_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.my_tab, null);
        } else if(TabTag.MUSIC_LIST_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.music_list_tab, null);
        } else if(TabTag.MUSIC_RANK_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.music_rank_tab, null);
        }
        return tabView;
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
        switch(mViewPager.getCurrentItem()) {
            case TabState.START:
                mMainActivity.setResult(Activity.RESULT_OK);
                mMainActivity.finish();
                break;
            case TabState.MY:
                //if(mMainPagerAdapter.getFragment(TabState.MY) instanceof PastRecordFragment) {
                //    ((PastRecordFragment) mMainPagerAdapter.getFragment(TabState.MY)).onBackPressed();
                //} else {
                    mViewPager.setCurrentItem(TabState.START);
                //}
                break;
            case TabState.MUSIC_LIST:
                mViewPager.setCurrentItem(TabState.START);
                break;
            case TabState.MUSIC_RANK:
                mViewPager.setCurrentItem(TabState.START);
                break;
        }
    }

    /**
     * Control which fragment needed to be displayed according to the tab
     *
     * @author DannyLin
     */
    public class MainTabViewPagerAdapter extends AbstractTabViewPagerAdapter {

        private Fragment mFragmentAtMyTab;

        public MainTabViewPagerAdapter(FragmentManager fm, int size) {
            super(fm, size);
            //setSwitchFragmentListener();
        }

        //private void setSwitchFragmentListener() {
            //mMyFragment.setMyTabFragmentListener(this);
            //mStartFragment.setStartTabFragmentListener(this);
        //}

        @Override
        protected Fragment getFragment(int position) {
            switch (position) {
                case TabState.START:
                    return mStartFragment;
                case TabState.MY:
                    if (mFragmentAtMyTab == null) {
                        mFragmentAtMyTab = mMyFragment;
                    }
                    return mFragmentAtMyTab;
                case TabState.MUSIC_LIST:
                    return mMusicListFragment;
                case TabState.MUSIC_RANK:
                    return mMusicRankFragment;
            }
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            /*if (object instanceof MyFragment && mFragmentAtMyTab instanceof PastRecordFragment) {
                return POSITION_NONE;
            }
            if (object instanceof PastRecordFragment && mFragmentAtMyTab instanceof MyFragment) {
                return POSITION_NONE;
            }*/
            return POSITION_UNCHANGED;
        }

        /*@Override
        public void onSwitchBetweenMyAndPastRecordFragment() {
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            if (mFragmentAtMyTab instanceof MyFragment) {
                ///addPastRecordFragment();
                //mFragmentAtMyTab = mPastRecordFragment;
                //((PastRecordFragment) mFragmentAtMyTab).setMyTabFragmentListener(this);
            } else { // Instance of PastRecordFragment
                mCurTransaction.remove(mFragmentAtMyTab);
                mFragmentAtMyTab = mMyFragment;
            }
            notifyDataSetChanged();
        }*/
/*
        private void addPastRecordFragment() {
            mPastRecordFragment = (PastRecordFragment) mFragmentManager
                    .findFragmentByTag(FragmentTag.PAST_RECORD_FRAGMENT_TAG);
            if (mPastRecordFragment == null) {
                mPastRecordFragment = new PastRecordFragment();
                mCurTransaction.add(R.id.tab_pager, mPastRecordFragment,
                        FragmentTag.PAST_RECORD_FRAGMENT_TAG);
            }
        }*/
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
        mTabHost.setCurrentTab(position);
    }

    @Override
    public void onTabChanged(String tabId) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.setting_button:
                mMainActivity.startActivityForResult(new Intent(mMainActivity, SettingActivity.class), REQUEST_SETTING);
                break;
        }
    }
}
