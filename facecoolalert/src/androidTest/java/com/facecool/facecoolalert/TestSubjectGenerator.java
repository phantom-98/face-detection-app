package com.facecool.facecoolalert;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.facecool.attendance.Constants;
import com.facecool.attendance.facedetector.FaceData;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.ui.detect.FaceDetectorProcessor;
import com.github.javafaker.Faker;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class TestSubjectGenerator {

    private static final String TAG = "TestSubjectGenerator";
    private SubjectDao subjectDao;
    private List<String> faceUrls;

    public TestSubjectGenerator(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    private void setFaces() {
        faceUrls = Arrays.asList(
                "https://drive.google.com/uc?export=download&id=1WAUBytiq8_3SRSiGlPpM9LbZ5d3DcXQo",
                "https://drive.google.com/uc?export=download&id=1WKv7_-qfb07eEw3cba5KxJrOZhzUtOec",
                "https://drive.google.com/uc?export=download&id=1WNlP1lwV6d6DVtHgQ-4iNz7lU6dlB30T",
                "https://drive.google.com/uc?export=download&id=1WNxPy53hnO1Fr-C3-SXtm_tD6YHv9QGA");
    }

    void setupUsers(int numUsers) {
        setFaces();
        for (int i = 0; i < numUsers; i++) {
            Log.d(TAG, "Creating user " + i);
            String faceUrl = faceUrls.get(i % faceUrls.size());
            startUserSetupThread(i, faceUrl);
        }
    }

    private void startUserSetupThread(int userId, String faceUrl) {
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            createUserAndProcessImage(userId, faceUrl, latch);
        } catch (Exception e) {
            Log.e(TAG, "Error processing image for user " + userId, e);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "Thread was interrupted while waiting for image processing", e);
        }
    }

    private void createUserAndProcessImage(int userId, String faceUrl, CountDownLatch latch) {
        Subject subject = createRandomSubject();
        Bitmap bitmap = downloadImage(faceUrl);
        if (bitmap == null) {
            latch.countDown();
            return;
        }
        Constants.logInfo("get bitmap " + bitmap.getByteCount() + " for user " + userId, TAG);

        FaceDetectorProcessor.ResultListener resultListener = new FaceDetectorProcessor.ResultListener() {
            @Override
            public void onSuccess(FaceData faceData, boolean bOnlyReg) {
                try {
                    if (faceData.getFeatures() == null) {
                        faceData.setFeatures(new byte[512]);
                        Constants.logInfo("Extracted Null for user " + userId, TAG);
                    }
                    subject.setFeatures(faceData.getFeatures());
                    saveImageAndSetNum(subject, bitmap);
                    Constants.logInfo("insert ended " + userId, TAG);
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onSuccessNull() {
                Log.e("CameraFragmentTest - Test", "Gotten null");
                latch.countDown();
            }

            @Override
            public void onError(Exception exp) {
                Log.e("CameraFragmentTest - Test", "Error: ", exp);
                latch.countDown();
            }
        };

        FaceDetectorProcessor faceDetectorProcessor = new FaceDetectorProcessor(getApplicationContext(), resultListener);
        faceDetectorProcessor.detectFace(bitmap);
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

    private void saveImageAndSetNum(Subject subject, Bitmap bitmap) {
        AsyncTask.execute(() -> {
            subjectDao.insertSubject(subject);
            subject.saveImage(getApplicationContext(), bitmap);
        });
    }

    private Subject createRandomSubject() {
        Faker faker = new Faker();
        Subject subject = new Subject();

        subject.setFirstName(faker.name().firstName());
        subject.setLastName(faker.name().lastName());
        subject.setEmail(faker.internet().emailAddress());
        subject.setID(faker.idNumber().valid());
        subject.setAddress(faker.address().fullAddress());
        subject.setPhone((faker.phoneNumber().phoneNumber()));

        return subject;
    }
}

