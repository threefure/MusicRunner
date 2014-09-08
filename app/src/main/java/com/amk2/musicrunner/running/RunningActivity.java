package com.amk2.musicrunner.running;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.finish.FinishRunningActivity;
import com.amk2.musicrunner.main.AbstractTabViewPagerAdapter;
import com.amk2.musicrunner.utilities.SongPerformance;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.TimeConverter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;



/**
 * Created by ktlee on 5/10/14.
 */
public class RunningActivity extends Activity implements ViewPager.OnPageChangeListener, View.OnClickListener, MusicControllerFragment.OnChangeSongListener, DistanceFragment.OnBackToDistanceListener {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int STATE_RUNNING = 1;

    public static class RunningFragmentTag {
        public static final String MAP_FRAGMENT_TAG = "map_fragment_tag";
        public static final String DISTANCE_FRAGMENT_TAG = "distance_fragment_tag";
        public static final String MUSIC_FRAGMENT_TAG = "music_fragment_tag";
    }

    public static class RunningPageState {
        public static final int MAP = 0;
        public static final int DISTANCE = 1;
        public static final int MUSIC = 2;
    }

    private static final int RUNNING_PAGE_SIZE = 3;

    private FragmentManager mFragmentManager;

    private ViewPager mRunningViewPager;
    private RunningTabViewPagerAdapter mRunningTabViewPagerAdapter;
    private MapFragmentRun mMapFragment;
    private DistanceFragment mDistanceFragment;
    private MusicControllerFragment mMusicControllerFragment;

    private TextView distanceTextView;
    private TextView calorieTextView;
    private TextView speedTextView;
    private TextView durationTextView;

    private RelativeLayout pauseContainerRelativeLayout;
    private RelativeLayout doneResumeContainerRelativeLayout;
    private Button pauseButton;
    private Button doneButton;
    private Button resumeButton;

    private ImageView picPreviewImageView;
    private ImageButton cameraImageButton;

    private HashMap<String, Integer> readableTime;
    private String durationString;

    private boolean isRunning = true;
    private int totalSec   = 0;
    private int actualSec  = 0;
    private Integer previousSongStartTime    = 0;
    private Double previousSongStartCalories = 0.0;
    private Double previousSongEndDistance = 0.0;

    private Double distance = 0.0;
    private Double calorie = 0.0;
    private Double speed = 0.0;

    private String distanceString;
    private String calorieString;
    private String speedString = "0";
    private String songNames = "";

    private ArrayList<SongPerformance> songPerformanceArrayList;

    private String photoPath;

    private NotificationCenter notificationCenter;

    private File musicRunnerDir;

    public class RunningTabViewPagerAdapter extends AbstractTabViewPagerAdapter {

        public RunningTabViewPagerAdapter(FragmentManager fm, int size) {
            super(fm, size);
        }

        @Override
        protected Fragment getFragment(int position) {
            switch(position) {
            case RunningPageState.MAP:
                return mMapFragment;
            case RunningPageState.DISTANCE:
                    return mDistanceFragment;
            case RunningPageState.MUSIC:
                return mMusicControllerFragment;
            }
            return null;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.running_activity);
        initialize();
    }

    private void initialize() {
        initViews();
        initActionBar();
        initFragments();
        initViewPager();
        Timer timer = new Timer();
        timer.schedule(runningTask, 0, 1000);
        notificationCenter = new NotificationCenter(this);

        songPerformanceArrayList = new ArrayList<SongPerformance>();
    }

    private void initActionBar() {
        getActionBar().hide();
    }

    private void initViewPager() {
        mRunningTabViewPagerAdapter = new RunningTabViewPagerAdapter(mFragmentManager,RUNNING_PAGE_SIZE);
        mRunningViewPager.setAdapter(mRunningTabViewPagerAdapter);
        mRunningViewPager.setOnPageChangeListener(this);
        mRunningViewPager.setCurrentItem(RunningPageState.DISTANCE);
    }

    private void initFragments() {
        mFragmentManager = getFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        // Init MapFragment
        mMapFragment = (MapFragmentRun)mFragmentManager.findFragmentByTag(RunningFragmentTag.MAP_FRAGMENT_TAG);
        if(mMapFragment == null) {
            mMapFragment = new MapFragmentRun();
            transaction.add(R.id.running_view_pager, mMapFragment, RunningFragmentTag.MAP_FRAGMENT_TAG);
        }
        mMapFragment.setOnBackToDistanceListener(this);

        // Init DistanceFragment
        mDistanceFragment = (DistanceFragment)mFragmentManager.findFragmentByTag(RunningFragmentTag.DISTANCE_FRAGMENT_TAG);
        if(mDistanceFragment == null) {
            mDistanceFragment = new DistanceFragment();
            transaction.add(R.id.running_view_pager, mDistanceFragment, RunningFragmentTag.DISTANCE_FRAGMENT_TAG);
        }

        // Init MusicControllerFragment
        mMusicControllerFragment = (MusicControllerFragment)mFragmentManager.findFragmentByTag(RunningFragmentTag.MUSIC_FRAGMENT_TAG);
        if(mMusicControllerFragment == null) {
            mMusicControllerFragment = new MusicControllerFragment();
            transaction.add(R.id.running_view_pager, mMusicControllerFragment, RunningFragmentTag.MUSIC_FRAGMENT_TAG);
        }
        mMusicControllerFragment.setOnChangeSongListener(this);
        mMusicControllerFragment.setOnBackToDistanceListener(this);

        transaction.hide(mMapFragment);
        transaction.hide(mDistanceFragment);
        transaction.hide(mMusicControllerFragment);

        transaction.commit();
    }


    private void initViews() {
        durationTextView = (TextView) findViewById(R.id.running_duration);

        distanceTextView = (TextView) findViewById(R.id.running_distance);
        calorieTextView  = (TextView) findViewById(R.id.running_calorie);
        speedTextView    = (TextView) findViewById(R.id.running_speed);

        pauseButton   = (Button) findViewById(R.id.running_pause);
        doneButton    = (Button) findViewById(R.id.running_done);
        resumeButton  = (Button) findViewById(R.id.running_resume);

        pauseContainerRelativeLayout      = (RelativeLayout) findViewById(R.id.pause_container);
        doneResumeContainerRelativeLayout = (RelativeLayout) findViewById(R.id.done_resume_container);

        //picPreviewImageView = (ImageView) findViewById(R.id.pic_preview);
        cameraImageButton   = (ImageButton) findViewById(R.id.running_camera);
        mRunningViewPager   = (ViewPager)findViewById(R.id.running_view_pager);

        musicRunnerDir      = getAlbumStorageDir(Constant.Album);

        pauseButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);
        resumeButton.setOnClickListener(this);
        //picPreviewImageView.setOnClickListener(this);
        cameraImageButton.setOnClickListener(this);

    }

    private TimerTask runningTask = new TimerTask() {
        @Override
        public void run() {
            if (isRunning) {
                totalSec ++;
                Message msg = new Message();
                msg.what = STATE_RUNNING;
                handler.sendMessage(msg);
            }
        }
    };

    private Handler handler = new Handler(){
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STATE_RUNNING:

                    // update time
                    readableTime = TimeConverter.getReadableTimeFormatFromSeconds(totalSec);
                    durationString = TimeConverter.getDurationString(readableTime);
                    durationTextView.setText(durationString);

                    //update distance
                    distance += 0.1;
                    //distance = MapFragmentRun.getmTotalDistance() * 0.001;
                    distanceString = distance.toString();
                    distanceString = StringLib.truncateDoubleString(distanceString, 2);
                    distanceTextView.setText(distanceString);

                    //update calorie
                    //calorie += 0.1;
                    //calorie = calculateCalories(totalSec, MapFragmentRun.getmTotalDistance());
                    calorie = calculateCalories(totalSec, distance);
                    calorieString = calorie.toString();
                    calorieString = StringLib.truncateDoubleString(calorieString, 2);
                    calorieTextView.setText(calorieString);

                    //update ratio
                    //running_speed += 0.01;
                    if (distance > 0) {
                        speed = ((double) totalSec / 60)/distance;//MapFragmentRun.getmSpeed();
                    }
                    speedString = speed.toString();
                    speedString = StringLib.truncateDoubleString(speedString, 2);
                    speedTextView.setText(speedString);

                    actualSec = totalSec % 60;
                    if (actualSec % Constant.ONE_MINUTE == 0) {
                        //notificationCenter.notifyStatus(actualMin, actualSec, distance, speed, calorie);
                    }
                    break;
            }
        }
    };

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    private Double calculateCalories (int timeInSec, Double distanceInMeter) {
        if (distanceInMeter == 0.0) {
            return 0.0;
        }
        double mins  = (double) timeInSec / 60;
        double hours = (double) timeInSec / 3600;
        double per400meters = distanceInMeter / 400;
        double speed = mins / per400meters;
        double K = 30 / speed;
        double calories = 70*hours*K;

        return calories;
    }

    private void galleryAddPic() {
        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + photoPath)));
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs() || file.isDirectory()) {
            Log.e("Album", "Directory not created");
        }
        return file;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(imageFileName, ".jpg", musicRunnerDir);
        photoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == REQUEST_IMAGE_CAPTURE && resCode == RESULT_OK) {
            galleryAddPic();
            //Bitmap resizedPhoto = PhotoLib.resizeToFitTarget(photoPath, picPreviewImageView.getLayoutParams().width, picPreviewImageView.getLayoutParams().height);
            //picPreviewImageView.setImageBitmap(resizedPhoto);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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
        stopService(new Intent(this,MusicService.class));
        runningTask.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_runner, menu);
        return false;
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

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.running_done:
                onChangeMusicSong(mMusicControllerFragment.getLastMusicRecord());  // Handle the last song
                stopService(new Intent(this,MusicService.class));
                finish();

                Intent finishRunningIntent = new Intent(getApplication(), FinishRunningActivity.class);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_DURATION, totalSec);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_DISTANCE, distanceString);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_CALORIES, calorieString);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_SPEED, speedString);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_PHOTO, photoPath);
                finishRunningIntent.putParcelableArrayListExtra(FinishRunningActivity.FINISH_RUNNING_SONGS, songPerformanceArrayList);
                startActivity(finishRunningIntent);
                break;
            case R.id.running_pause:
                pauseContainerRelativeLayout.setVisibility(View.GONE);
                doneResumeContainerRelativeLayout.setVisibility(View.VISIBLE);
                isRunning = false;
                break;
            case R.id.running_resume:
                pauseContainerRelativeLayout.setVisibility(View.VISIBLE);
                doneResumeContainerRelativeLayout.setVisibility(View.GONE);
                isRunning = true;
                break;
            case R.id.running_camera:
                dispatchTakePictureIntent();
                break;
            /*case R.id.pic_preview:
                if (photoPath != null) {
                    Intent intent = new Intent(this, ShowImageActivity.class);
                    intent.putExtra(ShowImageActivity.PHOTO_PATH, photoPath);
                    startActivity(intent);
                }
                break;*/
        }
    }

    @Override
    public void onChangeMusicSong(MusicRecord previousRecord) {

        String songName = previousRecord.mMusicSong.mTitle;
        String performanceString;
        Double timeDiff = ((double)totalSec - previousSongStartTime.doubleValue()) / 60;
        Double caloriesDiff = calorie - previousSongStartCalories;
        Double performance = 0.0;
        if (timeDiff != 0) {
            performance = caloriesDiff/timeDiff;
        }
        performanceString = StringLib.truncateDoubleString(performance.toString(), 2);
        songNames += (songName + Constant.PERF_SEPARATOR + performanceString + Constant.SONG_SEPARATOR );


        mMapFragment.musicChangeCallback(previousRecord);

        SongPerformance mp = new SongPerformance(previousRecord.mPlayingDuration, distance - previousSongEndDistance, calorie - previousSongStartCalories, previousRecord.mMusicSong.mTitle);
        songPerformanceArrayList.add(mp);

        previousSongEndDistance = distance;
        previousSongStartCalories = calorie;
        previousSongStartTime     = totalSec;
    }

    @Override
    public void onBackToDistance() {
        mRunningViewPager.setCurrentItem(RunningPageState.DISTANCE);
    }

}
