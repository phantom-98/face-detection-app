/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facecool.cameramanager.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.Image.Plane;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.exifinterface.media.ExifInterface;

import com.facecool.cameramanager.camera.FrameMetadata;
import com.facecool.cameramanager.entity.CameraInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Utils functions for bitmap conversions.
 */
public class FileUtils {
//    public static String getVideoFile(Context context,CameraInfo cameraInfo) {
//        return new File(context.getFilesDir(), "video" + cameraInfo.videoPath.hashCode()).getAbsolutePath();
//    }
//
//    public static void createVideoFile(Context context, CameraInfo cameraInfo) {
//        String path = getVideoFile(context, cameraInfo);
//        try {
//
//            FileOutputStream fos = new FileOutputStream(path);
//            InputStream is = context.getContentResolver().openInputStream(Uri.parse(cameraInfo.videoPath));
//            byte[] buffer = new byte[20480];
//            int nRead;
//            while ((nRead = is.read(buffer)) > 0 ) {
//                fos.write(buffer, 0, nRead);
//            }
//            is.close();
//            fos.flush();
//            fos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }
}
