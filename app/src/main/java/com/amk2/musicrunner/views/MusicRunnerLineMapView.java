package com.amk2.musicrunner.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.main.UIController;
import com.amk2.musicrunner.setting.SettingActivity;
import com.amk2.musicrunner.utilities.SongPerformance;
import com.amk2.musicrunner.utilities.StringLib;
import com.amk2.musicrunner.utilities.UnitConverter;

import java.util.ArrayList;

/**
 * Created by ktlee on 9/7/14.
 */
public class MusicRunnerLineMapView extends View{
    private Context context;

    private boolean isVertical = true;
    private Double height;
    private Double lineLength;
    private Double startX;
    private Double startY;
    private Double offsetLeft;
    private Double offsetRight;
    private Double offsetTopX;
    private Double offsetTopY;
    private Double minimalOffsetInPX;
    private Double textOffsetYInPX;
    private float textSizeInDP = 16;
    private float radius = 10;
    private int startXInDP = 130;
    private int startYInDP = 60;
    private int offsetLeftInDP  = 70;
    private int offsetRightInDP = 20;
    private int offsetXTopInDP = 10;
    private int offsetYTopInDP = 10;
    private int textOffsetYInDP = 40;
    private Paint mTextPaint;
    private Paint mLinePaint;
    private Paint mPointPaint;
    private ArrayList<SongPerformance> songPerformanceJoints;
    private String performanceUnit;


    private SharedPreferences mSettingSharedPreferences;
    private Integer unitDistance;

    public MusicRunnerLineMapView(Context _context, AttributeSet attrs) {
        super(_context, attrs);
        context = _context;

        initialize();
    }

    private void initialize () {
        mSettingSharedPreferences = context.getSharedPreferences(SettingActivity.SETTING_SHARED_PREFERENCE, 0);
        unitDistance = mSettingSharedPreferences.getInt(SettingActivity.DISTANCE_UNIT, SettingActivity.SETTING_DISTANCE_KM);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize((float)UnitConverter.getPixelsFromDP(context, textSizeInDP));

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStrokeWidth(3);

        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setColor(Color.BLACK);

        minimalOffsetInPX = UnitConverter.getPixelsFromDP(context, 45);
        textOffsetYInPX   = UnitConverter.getDPFromPixels(context, textOffsetYInDP);

        performanceUnit = context.getResources().getString(R.string.kcal_per_min);
    }

    private void calibrate () {
        //start rendering point
        startX = UnitConverter.getPixelsFromDP(context, startXInDP);
        startY = UnitConverter.getPixelsFromDP(context, startYInDP);

        //rendering offset
        offsetLeft  = UnitConverter.getPixelsFromDP(context, offsetLeftInDP);
        offsetRight = UnitConverter.getPixelsFromDP(context, offsetRightInDP);
        offsetTopX  = UnitConverter.getPixelsFromDP(context, offsetXTopInDP);
        offsetTopY  = UnitConverter.getPixelsFromDP(context, offsetYTopInDP);

        //compute view height
        int length = songPerformanceJoints.size();
        Double temp;
        lineLength = 0.0;
        for (int i = 0; i < length; i ++ ) {
            temp = UnitConverter.getPixelsFromDP(context, songPerformanceJoints.get(i).distance.floatValue()*100);
            if (temp < minimalOffsetInPX) {
                temp = minimalOffsetInPX;
            }
            lineLength += temp;
        }
        height = lineLength + startY*2;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isInEditMode()) {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(widthMeasureSpec, height.intValue());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInEditMode()) {
            int length = songPerformanceJoints.size();
            int fromX = startX.intValue(), fromY = startY.intValue(), toX = startX.intValue(), toY = startY.intValue();
            Double totalDistance = 0.0, distance;
            Double temp;
            String unit = Constant.DistanceMap.get(unitDistance);
            canvas.drawText("Start", toX - offsetTopX.intValue(), toY - offsetTopY.intValue(), mTextPaint);
            canvas.drawCircle(toX, toY, radius, mPointPaint);
            for (int i = 0; i < length; i++) {
                temp = UnitConverter.getPixelsFromDP(context, (songPerformanceJoints.get(i).distance.floatValue())*100);
                if (temp < minimalOffsetInPX) {
                    temp = minimalOffsetInPX;//UnitConverter.getPixelsFromDP(context, minimalOffsetInPX.intValue());
                }
                toY = toY + temp.intValue();

                if (unitDistance == SettingActivity.SETTING_DISTANCE_KM) {
                    distance = songPerformanceJoints.get(i).distance;
                } else {
                    distance = UnitConverter.getMIFromKM(songPerformanceJoints.get(i).distance);
                }
                totalDistance += distance;
                canvas.drawCircle(toX, toY, radius, mPointPaint);
                // distance
                canvas.drawText(StringLib.truncateDoubleString(totalDistance.toString(), 2) + " " + unit, toX - offsetLeft.intValue(), toY, mTextPaint);
                // song title
                canvas.drawText(StringLib.truncate(songPerformanceJoints.get(i).name, 20), toX + offsetRight.intValue(), toY, mTextPaint);
                //song performance
                canvas.drawText(StringLib.truncateDoubleString(songPerformanceJoints.get(i).performance.toString(), 2) + " " + performanceUnit,
                        toX + offsetRight.intValue(), toY + textOffsetYInPX.intValue(), mTextPaint);
            }
            canvas.drawLine(fromX, fromY, toX, toY, mLinePaint);
        }
    }

    public void setMusicJoints (ArrayList<SongPerformance> joints) {
        songPerformanceJoints = joints;
        calibrate();
    }
}
