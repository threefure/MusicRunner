package com.amk2.musicrunner.utilities;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

/**
 * Created by daz on 2014/6/21.
 */
public class PhotoLib {
    public static Bitmap resizeToFitTarget (String photoPath, int targetWidth, int targetHeight) {
        Log.d("target", targetWidth + " " + targetHeight + "");
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoWidth  = bmOptions.outWidth;
        int photoHeight = bmOptions.outHeight;
        Log.d("source", photoWidth + " " + photoWidth + "");
        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoWidth/targetWidth, photoHeight/targetHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

    public static Bitmap resizeToFitTarget (Resources res, int id, int targetWidth, int targetHeight) {
        Log.d("target", targetWidth + " " + targetHeight + "");
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(res, id, bmOptions);
        int photoWidth  = bmOptions.outWidth;
        int photoHeight = bmOptions.outHeight;
        Log.d("source", photoWidth + " " + photoWidth + "");
        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoWidth/targetWidth, photoHeight/targetHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeResource(res, id, bmOptions);
    }
    /*
    private void setPic() {
        // Get the dimensions of the View
        int targetW = picPreview.getWidth();
        int targetH = picPreview.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        Bitmap resized = Bitmap.createScaledBitmap(yourBitmap, newWidth, newHeight, true);


        // Determine how much to scale down the image
        //int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        //bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        picPreview.setImageBitmap(bitmap);
    }
    */
}

