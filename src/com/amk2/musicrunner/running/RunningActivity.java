package com.amk2.musicrunner.running;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.amk2.musicrunner.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ktlee on 5/10/14.
 */
public class RunningActivity extends Activity {
    protected static final int STATE_RUNNING = 1;

    private TextView runningDistance;
    private TextView runningCalorie;
    private TextView runningSpeedRatio;
    private TextView hour;
    private TextView min;
    private TextView sec;

    private Button pauseButton;

    private boolean isRunning = false;
    private int totalSec   = 0;
    private int actualSec  = 0;
    private int actualMin  = 0;
    private int actualHour = 0;

    private Double distance = 0.0;
    private Double calorie = 0.0;
    private Double running_ratio = 0.0;

    private String distanceString;
    private String calorieString;
    private String ratioString;

    private NotificationCenter notificationCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        hour = (TextView) findViewById(R.id.timer_hour);
        min  = (TextView) findViewById(R.id.timer_minute);
        sec  = (TextView) findViewById(R.id.timer_second);
        runningDistance    = (TextView) findViewById(R.id.running_distance);
        runningCalorie     = (TextView) findViewById(R.id.running_calorie);
        runningSpeedRatio  = (TextView) findViewById(R.id.running_speed_ratio);

        pauseButton = (Button) findViewById(R.id.pause_running);
        pauseButton.setOnClickListener(buttonClickListener);

        Timer timer = new Timer();
        timer.schedule(runningTask, 0, 1000);

        notificationCenter = new NotificationCenter(this);
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
                    actualSec  = totalSec%60;

                    if (actualSec < 10) {
                        sec.setText("0" + actualSec);
                    } else {
                        sec.setText("" + actualSec);
                    }

                    if (actualSec == 0) {
                        actualMin = (actualMin + 1) % 60;

                        if (actualMin < 10) {
                            min.setText("0" + actualMin);
                        } else {
                            min.setText("" + actualMin);
                        }

                        if (actualMin == 0) {
                            actualHour += 1;

                            if (actualHour < 10) {
                                hour.setText("0" + actualHour);
                            } else {
                                hour.setText("" + actualHour);
                            }
                        }
                    }

                    //update distance
                    distance += 0.01;
                    distanceString = distance.toString();
                    distanceString = truncateDoubleString(distanceString, 2);
                    runningDistance.setText(distanceString);

                    //update calorie
                    calorie += 0.1;
                    calorieString = calorie.toString();
                    calorieString = truncateDoubleString(calorieString, 1);
                    runningCalorie.setText(calorieString);

                    //update ratio
                    running_ratio += 0.01;
                    ratioString = running_ratio.toString();
                    ratioString = truncateDoubleString(ratioString, 2);
                    runningSpeedRatio.setText(ratioString);

                    if (actualSec % 20 == 0) {
                        Log.v("Timer notifies per 10 sec", "lool");
                        notificationCenter.notifyStatus(actualSec, actualSec, distance, running_ratio);
                    }
                    break;
            }
        }
    };

    private OnClickListener buttonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pause_running:
                    isRunning = !isRunning;
                    break;
            }
        }
    };

    /*
     * truncateDoubleString: truncate double number to 小數點後兩位
     * str: string of double number
     * allowedDigits: 小數點後幾位
     */
    public static String truncateDoubleString (String str, int allowedDigits) {
        int dot_position = str.indexOf(".");
        if (str.length() - dot_position > (allowedDigits + 1)) { //小數點後數字大於兩位
            Log.d("truncate", "truncate");
            str = str.substring(0, dot_position + allowedDigits + 1);
        }
        return str;
    };


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
        runningTask.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_runner, menu);
        return true;
    }
}
