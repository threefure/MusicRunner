package com.amk2.musicrunner.finish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amk2.musicrunner.R;

/**
 * Created by daz on 2014/6/15.
 */
public class FinishRunningActivity extends Activity implements View.OnClickListener{
    public static String FINISH_RUNNING_DISTANCE = "com.amk2.distance";
    public static String FINISH_RUNNING_CALORIES = "com.amk2.calories";
    public static String FINISH_RUNNING_SPEED    = "com.amk2.speed";

    private TextView distanceTextView;
    private TextView caloriesTextView;
    private TextView speedTextView;

    private Button saveButton;
    private Button discardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_running);

        Intent intent = getIntent();

        saveButton    = (Button) findViewById(R.id.save_running_event);
        discardButton = (Button) findViewById(R.id.discard_running_event);

        saveButton.setOnClickListener(this);
        discardButton.setOnClickListener(this);

        distanceTextView = (TextView) findViewById(R.id.finish_running_distance);
        caloriesTextView = (TextView) findViewById(R.id.finish_running_calories);
        speedTextView    = (TextView) findViewById(R.id.finish_running_speed);

        if (intent.getStringExtra(FINISH_RUNNING_DISTANCE) != null) {
            distanceTextView.setText(intent.getStringExtra(FINISH_RUNNING_DISTANCE));
        }
        if (intent.getStringExtra(FINISH_RUNNING_CALORIES) != null) {
            caloriesTextView.setText(intent.getStringExtra(FINISH_RUNNING_CALORIES));
        }
        if (intent.getStringExtra(FINISH_RUNNING_SPEED) != null) {
            speedTextView.setText(intent.getStringExtra(FINISH_RUNNING_SPEED));
        }
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
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.save_running_event:
                Log.d("daz", "save running event");
                finish();
                break;
            case R.id.discard_running_event:
                Log.d("daz", "discard running event");
                finish();
                break;
        }
    }
}
