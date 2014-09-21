package com.amk2.musicrunner.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.amk2.musicrunner.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;

/**
 * Created by ktlee on 9/16/14.
 */
public class GifView extends WebView {
    public GifView(Context context) {
        super(context);
        //Uri path = Uri.parse("android.resource://" + context.getPackageName() + "/running_gif.gif");
        String path = "file:///android_asset/running_gif.gif";
        loadUrl(path.toString());
    }
    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Uri path = Uri.parse("android.resource://" + context.getPackageName() + "/running_gif.gif");
        String path = "file:///android_asset/running_gif.gif";
        loadUrl(path);
    }
    /*
    Integer gifSrcId;
    long movieTime;

    Movie movie;
    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GifView, 0, 0);
        try {
            gifSrcId = ta.getIndex(R.styleable.GifView_gif_src);
        } finally {
            ta.recycle();
        }

        init();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        super.onDraw(canvas);


        long now = android.os.SystemClock.uptimeMillis();
        if (movieTime == 0) {
            movieTime = now;
        }
        if (movie != null) {
            int relTime = (int) ((now - movieTime) % movie.duration());
            movie.setTime(relTime);
            movie.draw(canvas, 10, 10);
            this.invalidate();
        }
    }

    public void init() {
        Log.d("dadfaasdf", "Adfafsdfadfasdf");
        movieTime = 0;
        InputStream is = getContext().getResources().openRawResource(R.raw.running_gif);
        movie = Movie.decodeStream(is);

    }*/


}
