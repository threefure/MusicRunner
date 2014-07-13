package com.amk2.musicrunner.utilities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.amk2.musicrunner.R;
import com.facebook.AccessToken;

/**
 * Created by ktlee on 7/13/14.
 */
public class ShowImageActivity extends Activity {

    public static String PHOTO_PATH = "com.amk2.musicrunner.photopath";
    private String photoPath;
    private ImageView imageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        getActionBar().hide();

        imageContainer = (ImageView) findViewById(R.id.image_container);
        setImage();
    }

    private void setImage() {
        Intent intent = getIntent();
        photoPath = intent.getStringExtra(PHOTO_PATH);
        if (photoPath != null) {
            Bitmap resizedPhoto = PhotoLib.resizeToFitTarget(photoPath, imageContainer.getLayoutParams().width, imageContainer.getLayoutParams().height);
            imageContainer.setImageBitmap(resizedPhoto);
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

}
