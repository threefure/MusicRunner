package com.amk2.musicrunner.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.amk2.musicrunner.running.RandomUtils;

import java.util.Random;

import static java.lang.Thread.sleep;

/**
 * Created by tengchou on 9/20/14.
 */
public class MusicRunnerLineRunningView extends View {


    private int p_rectColor         = Color.BLACK;
    private int p_backGroundColor   = Color.GRAY;
    private int p_viewWidth         = 0;
    private int p_viewHeight        = 0;
    private int p_rectWidth         = 20;
    private int p_rectHeight        = 100;
    private int p_space             = 100;
    private static int p_initial    = 0;
    private Canvas p_canvas;


    public MusicRunnerLineRunningView(Context context) {
        super(context);
    }

    public MusicRunnerLineRunningView(Context context, AttributeSet attrs){
        super(context, attrs);

    }

    private Paint makePaint(int color) {
        Paint p = new Paint();
        p.setColor(color);
        return(p);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        p_viewWidth = getWidth();
        p_viewHeight = getHeight();
        p_canvas = canvas;
        p_canvas.drawColor(p_backGroundColor);
        if(p_initial < (p_rectHeight + p_space))
            p_initial += 5;
        else
            p_initial = 0;
        drawVerticalDottedLine(p_initial - (p_rectHeight + p_space));
        Log.e("test", String.valueOf(p_initial));
    }

    private void drawRect(int altitude) {
        float left = ((p_viewWidth / 2) - (p_rectWidth / 2));
        float top = altitude;
        float right = left + p_rectWidth;
        float bottom = top + p_rectHeight;
        Paint squareColor = makePaint(p_rectColor);
        p_canvas.drawRect(left, top, right, bottom, squareColor);
    }

    private void drawVerticalDottedLine(int initial) {
        int rectCount = p_viewHeight / (p_rectHeight + p_space);
        for(int i = 0; i < rectCount; i++) {
            drawRect((initial + i * (p_rectHeight + p_space)));
        }
    }

    public int getP_rectColor() {
        return p_rectColor;
    }

    public void setP_rectColor(int p_rectColor) {
        this.p_rectColor = p_rectColor;
    }

    public int getP_backGroundColor() {
        return p_backGroundColor;
    }

    public void setP_backGroundColor(int p_backGroundColor) {
        this.p_backGroundColor = p_backGroundColor;
    }

    public int getP_viewWidth() {
        return p_viewWidth;
    }

    public void setP_viewWidth(int p_viewWidth) {
        this.p_viewWidth = p_viewWidth;
    }

    public int getP_viewHeight() {
        return p_viewHeight;
    }

    public void setP_viewHeight(int p_viewHeight) {
        this.p_viewHeight = p_viewHeight;
    }

    public int getP_rectWidth() {
        return p_rectWidth;
    }

    public void setP_rectWidth(int p_rectWidth) {
        this.p_rectWidth = p_rectWidth;
    }

    public int getP_rectHeight() {
        return p_rectHeight;
    }

    public void setP_rectHeight(int p_rectHeight) {
        this.p_rectHeight = p_rectHeight;
    }

    public int getP_space() {
        return p_space;
    }

    public void setP_space(int p_space) {
        this.p_space = p_space;
    }

}
