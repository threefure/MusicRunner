package com.amk2.musicrunner.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

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
    private float textSize = 50;
    private float radius = 10;
    private int startXInDP = 400;
    private int startYInDP = 200;
    private int offsetLeftInDP  = 210;
    private int offsetRightInDP = 50;
    private int offsetXTopInDP = 50;
    private int offsetYTopInDP = 30;
    private Paint mTextPaint;
    private Paint mLinePaint;
    private Paint mPointPaint;
    private ArrayList<SongPerformance> songPerformanceJoints;

    public MusicRunnerLineMapView(Context _context, AttributeSet attrs) {
        super(_context, attrs);
        context = _context;

        initialize();
    }

    private void initialize () {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(textSize);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStrokeWidth(3);

        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setColor(Color.BLACK);
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
        Double km = 0.0;
        for (int i = 0; i < length; i ++ ) {
            km += songPerformanceJoints.get(i).distance;
        }
        lineLength = km * 100;
        height = UnitConverter.getPixelsFromDP(context, lineLength.intValue() + startY.intValue()*2);

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
            Double kms = 0.0;
            Double temp;
            canvas.drawText("Start", toX - offsetTopX.intValue(), toY - offsetTopY.intValue(), mTextPaint);
            canvas.drawCircle(toX, toY, radius, mPointPaint);
            for (int i = 0; i < length; i++) {
                temp = UnitConverter.getPixelsFromDP(context, songPerformanceJoints.get(i).distance * 100);
                toY = toY + temp.intValue();
                kms += songPerformanceJoints.get(i).distance;
                canvas.drawCircle(toX, toY, radius, mPointPaint);
                canvas.drawText(StringLib.truncateDoubleString(kms.toString(), 2) + " km", toX - offsetLeft.intValue(), toY, mTextPaint);
                canvas.drawText(songPerformanceJoints.get(i).name + " " + StringLib.truncateDoubleString(songPerformanceJoints.get(i).performance.toString(), 2) + " kcal/min",
                        toX + offsetRight.intValue(), toY, mTextPaint);
            }
            canvas.drawLine(fromX, fromY, toX, toY, mLinePaint);
        }
    }

    public void setMusicJoints (ArrayList<SongPerformance> joints) {
        songPerformanceJoints = joints;
        calibrate();
    }
}
