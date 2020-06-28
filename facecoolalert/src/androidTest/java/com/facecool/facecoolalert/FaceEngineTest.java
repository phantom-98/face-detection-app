package com.facecool.facecoolalert;

import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.facecool.attendance.Constants;
import com.facecool.attendance.facedetector.FaceEngine;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;


// tests the sharpness c++ function by using images in different sharpness's
@RunWith(AndroidJUnit4.class)
public class FaceEngineTest {

    private static final String TAG = "FaceEngineTest";

    @Test
    public void testOriginalImageSharpness() {
        Bitmap originalImage = downloadImage("https://drive.google.com/uc?export=download&id=1FhM4ef_HGRaZgCkYO8Ykk_zp3Tzl_7Ox");
        float sharpness = FaceEngine.getSharpness(originalImage);
        Constants.logInfo("Sharpness of original image: " + sharpness, TAG);
        assertTrue(sharpness > 0);  // Basic check to ensure some sharpness is calculated
    }

    @Test
    public void testLightBlurredImageSharpness() {
        Bitmap lightBlurredImage = downloadImage("https://drive.google.com/uc?export=download&id=14Vj6TTNqp0F1WnURv1k7npGcc1bPLAvP");
        float sharpness = FaceEngine.getSharpness(lightBlurredImage);
        Constants.logInfo("Sharpness of lightly blurred image: " + sharpness, TAG);
        assertTrue(sharpness > 0);
    }

    @Test
    public void testMediumBlurredImageSharpness() {
        Bitmap mediumBlurredImage = downloadImage("https://drive.google.com/uc?export=download&id=15pPqEMx8VwdmbljrQRqN85lVube7hej9");
        float sharpness = FaceEngine.getSharpness(mediumBlurredImage);
        Constants.logInfo("Sharpness of medium blurred image: " + sharpness, TAG);
        assertTrue(sharpness > 0);
    }

    @Test
    public void testHeavyBlurredImageSharpness() {
        Bitmap heavyBlurredImage = downloadImage("https://drive.google.com/uc?export=download&id=1gixgtkv-FM8DtX1307rVc4QTbZDUk-aH");
        float sharpness = FaceEngine.getSharpness(heavyBlurredImage);
        Constants.logInfo("Sharpness of heavily blurred image: " + sharpness, TAG);
        assertTrue(sharpness > 0);
    }

    private Bitmap downloadImage(String imageUrl) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(imageUrl).build();
            Response response = client.newCall(request).execute();

            InputStream inputStream = response.body().byteStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            return bitmap;
        } catch (IOException e) {
            Log.e("ImageDownload", "Failed to download image", e);
            return null;
        }
    }
}

