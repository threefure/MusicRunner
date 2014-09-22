package com.amk2.musicrunner.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.amk2.musicrunner.R;

import java.io.InputStream;

/**
 * Created by ktlee on 9/16/14.
 */
public class GifView extends View {
    Integer duration;
    Integer movieDuration;
    Integer gifSrcId;
    Integer movieWidth;
    Integer movieHeight;
    Long movieStart;


    Movie gifMovie;
    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GifView, 0, 0);
        try {
            gifSrcId = ta.getResourceId(R.styleable.GifView_gif_src, 0);
        } finally {
            ta.recycle();
        }
        init();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(movieWidth, movieHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isInEditMode()) {
            canvas.drawColor(Color.TRANSPARENT);
            long now = android.os.SystemClock.uptimeMillis();
            if (gifMovie != null) {
                if (movieStart == 0) {
                    movieStart = now;
                }

                duration = gifMovie.duration();
                if (duration == 0) {
                    duration = 1000;
                }
                int relTime = (int) ((now - movieStart) % duration);
                gifMovie.setTime(relTime);
                gifMovie.draw(canvas, 0, 0);
                this.invalidate();
            }
        }
    }

    public void init() {
        if (gifSrcId != 0) {
            InputStream is = getContext().getResources().openRawResource(gifSrcId);
            gifMovie = Movie.decodeStream(is);
            movieWidth = gifMovie.width();
            movieHeight = gifMovie.height();
            movieDuration = gifMovie.duration();
            movieStart = Long.valueOf(0);
        } else {
            movieWidth = 0;
            movieHeight = 0;
        }
    }
}
