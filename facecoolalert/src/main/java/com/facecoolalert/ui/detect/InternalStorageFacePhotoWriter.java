package com.facecoolalert.ui.detect;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class InternalStorageFacePhotoWriter {

    public static void saveImageToInternalStorage(Context context, Bitmap bitmap, String fileName) {
        File externalDir = context.getExternalFilesDir(null);
        File face = new File(externalDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(face);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            Log.e("Save Image", e.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Log.e("Save Image", e.getMessage());
            }
        }
    }
}

