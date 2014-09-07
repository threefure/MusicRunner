package com.amk2.musicrunner.utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ktlee on 9/7/14.
 */
public class CameraLib {
    private static String AlbumName = "MusicRunner";

    public static void galleryAddPic(Context context, String photoPath) {
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + photoPath)));
    }

    public static File getAlbumStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), AlbumName);
        if (!file.mkdirs() || file.isDirectory()) {
            Log.e("Album", "Directory not created");
        }
        return file;
    }

    public static File createImageFile(File dir) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(imageFileName, ".jpg", dir);
        //photoPath = image.getAbsolutePath();
        return image;
    }
}
