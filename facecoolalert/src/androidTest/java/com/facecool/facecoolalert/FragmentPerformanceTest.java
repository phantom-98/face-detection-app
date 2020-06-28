package com.facecool.facecoolalert;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertTrue;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.facecool.attendance.Constants;
import com.facecool.attendance.facedetector.FaceData;
import com.facecool.cameramanager.entity.CameraInfo;
import com.facecoolalert.App;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.ui.MainActivity;
import com.facecoolalert.ui.detect.FaceDetectorProcessor;
import com.facecoolalert.ui.subject.enrollments.EnrollSubject;
import com.facecoolalert.utils.PrefManager;
import com.facecoolalert.utils.RecognitionUtils;
import com.github.javafaker.Faker;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import okio.BufferedSink;
import okio.Okio;

// used in order to check if mlkit detected all the faces
// is is also helpful to track the recognition results
// the test starts the whole app and downloads the video and the faces images for doing the test
@RunWith(AndroidJUnit4.class)
public class FragmentPerformanceTest {

    private static final int NUMBER_OF_FRAGMENTS = 4; // DO NOT CHANGE
    private static final boolean[] CAMERA_SELECTION = {true, false, false, false}; // setup up visibility of fragments
    private static final int[] FRAGMENTS_VIDEOS = new int[4];
    private final SubjectDao subjectDao = MyDatabase.getInstance(getApplicationContext()).subjectDao();
    private List<TestObject> testObjects;
    private ExecutorService executorService;
    private static final String TAG = "FragmentPerformanceTest";
    List<String> faceUrls1;
    private static final int expectedTotalDetectedFaces = 4;

    @Before
    public void setUp() {
        clearDatabaseAndPreferences();
        setupTestObjects();
    }

    @After
    public void tearDown() {
//        clearDatabaseAndPreferences();
    }

    private void setupTestObjects() {
        testObjects = new ArrayList<>();

        faceUrls1 = Arrays.asList(
                "https://drive.google.com/uc?export=download&id=1WAUBytiq8_3SRSiGlPpM9LbZ5d3DcXQo",
                "https://drive.google.com/uc?export=download&id=1WKv7_-qfb07eEw3cba5KxJrOZhzUtOec",
                "https://drive.google.com/uc?export=download&id=1WNlP1lwV6d6DVtHgQ-4iNz7lU6dlB30T",
                "https://drive.google.com/uc?export=download&id=1WNxPy53hnO1Fr-C3-SXtm_tD6YHv9QGA");
        testObjects.add(new TestObject("https://www.pexels.com/download/video/8125926/?fps=25.0&h=540&w=960", faceUrls1));

        FRAGMENTS_VIDEOS[0] = 0;
        FRAGMENTS_VIDEOS[1] = 0;
        FRAGMENTS_VIDEOS[2] = 0;
        FRAGMENTS_VIDEOS[3] = 0;
    }

    @Test
    public void testRecognitionPerformance() {
        try (ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class)) {
            Constants.logInfo("Starting MainActivity important for initialization", TAG);
            try {
                Thread.sleep(1000); // just for better understanding and seeing flow on screen
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try (ActivityScenario<EnrollSubject> enrollScenario = ActivityScenario.launch(EnrollSubject.class)) {
            Constants.logInfo("Setting up users in the database during EnrollActivity not important but helpful for understanding the flow", null);
            setupUsers();
            Constants.logInfo("Storing camera info and camera selection", TAG);
            saveCameraSettings(createCameraInfo());
        }

        Constants.logInfo("Start activity to start performance measurement", TAG);
        Constants.logInfo("Starting to monitor recognitionResults for face recognition...", TAG);
        long startTime = System.currentTimeMillis(); // important it is outside of scenario
        RecognitionUtils.recognitionResults = new HashMap<>();
        try (ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class)) {
            long timeout = 10000; // 10 seconds timeout to jump out of thread

            AtomicBoolean allFacesProcessed = new AtomicBoolean(false);
            AtomicReference<HashMap<String, RecognitionResult>> recognitionResults = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            Thread thread = new Thread(() -> {
                Constants.logInfo("listener thread started", TAG);
                RecognitionUtils.setRecognitionListener(detectedFaces -> {
                    Constants.logInfo("Currently processed faces: " + detectedFaces.size(), TAG);
                    if (detectedFaces.size() >= faceUrls1.size()) {
                        allFacesProcessed.set(true);
                        recognitionResults.set(new HashMap<>(detectedFaces));
                        latch.countDown();
                    }
                });

                try {
                    if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                        allFacesProcessed.set(false);
                        latch.countDown();
                    }
                } catch (InterruptedException e) {
                    allFacesProcessed.set(false);
                    latch.countDown();
                }
            });
            thread.setDaemon(true);
            thread.start();

            try {
                latch.await();
            } catch (InterruptedException e) {
                allFacesProcessed.set(false);
            }

            // 6. Assert that all faces were processed within the given time and print the time it took.
            long elapsedTime = System.currentTimeMillis() - startTime;
            assertTrue("Not all faces were processed within the given time.", allFacesProcessed.get());
            Constants.logInfo("Time taken for face processing: " + elapsedTime + "ms", TAG);
            Constants.logInfo("End-to-end test completed.", TAG);

            // 7. quality test
            HashMap<String, RecognitionResult> detectedResults = recognitionResults.get();
            int totalDetectedFaces = detectedResults == null ? 0 : detectedResults.size();

            assertTrue("Total number of detected faces is less than expected count",
                    totalDetectedFaces >= expectedTotalDetectedFaces);

            int recognisedFacesWithSubjects = 0;
            for (RecognitionResult result : detectedResults.values()) {
                if (result.getSubject() != null) {
                    recognisedFacesWithSubjects++;
                }
            }
            Constants.logInfo("Total number of recognized faces with subjects: " + recognisedFacesWithSubjects, TAG);

            if (totalDetectedFaces > expectedTotalDetectedFaces) {
                Constants.logInfo("Warning: Total number of detected results is greater than expected count", TAG);
            }

            for (Map.Entry<String, RecognitionResult> entry : detectedResults.entrySet()) {
                RecognitionResult result = entry.getValue();
                Constants.logInfo("Detected result: " + entry.getKey() + ", Score Match: " + result.getScoreMatch(), TAG);
            }
        }
    }

    private void clearDatabaseAndPreferences() {
        Constants.logInfo("clearDatabaseAndPreferences", TAG);
        subjectDao.deleteAll();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        Constants.logInfo("clearDatabaseAndPreferences end", TAG);
    }

    private CameraInfo[] createCameraInfo() {
        CameraInfo[] cameraInfoArray = new CameraInfo[NUMBER_OF_FRAGMENTS];
        for (int i = 0; i < NUMBER_OF_FRAGMENTS; i++) {
            CameraInfo info = new CameraInfo();
            String videoPath = downloadVideo(testObjects.get(FRAGMENTS_VIDEOS[i]).getVideoUrl(), "test");
            info.type = CameraInfo.TYPE_VIDEO_FILE;
            Constants.logInfo("videoPath: " + videoPath, TAG);
            info.index = i;
            info.cameraName = "Video Camera " + (i + 1);
            info.location = "Location " + (i + 1);
            info.videoPath = videoPath;
            cameraInfoArray[i] = info;
        }
        Constants.logInfo("createCameraInfo in test " + cameraInfoArray.length, TAG);
        return cameraInfoArray;
    }

    private String downloadVideo(String videoUrl, String videoName) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(videoUrl).build();
            Response response = client.newCall(request).execute();

            File videoFile = new File(getApplicationContext().getFilesDir(), videoName + ".mp4");

            BufferedSink sink = Okio.buffer(Okio.sink(videoFile));
            sink.writeAll(response.body().source());
            sink.close();

            return videoFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e("VideoDownload", "Failed to download video", e);
            return null;
        }
    }

    private void saveCameraSettings(CameraInfo[] cameraInfoArray) {
        App appContext = (App) ApplicationProvider.getApplicationContext();
        PrefManager.saveCameraSettings(appContext, cameraInfoArray);
        appContext.setCameraInfos(cameraInfoArray);
        saveCameraSelection();
    }

    private void saveCameraSelection() {
        App appContext = (App) ApplicationProvider.getApplicationContext();
        PrefManager.saveCameraSelection(appContext, CAMERA_SELECTION);
        appContext.setSelectedCameras(CAMERA_SELECTION);
    }

    private void setupUsers() {
        executorService = Executors.newFixedThreadPool(5);
        for (TestObject testObject : testObjects) {
            for (int i = 0; i < testObject.getFaceUrls().size(); i++) {
                String faceUrl = testObject.getFaceUrls().get(i);
                startUserSetupThread(i, faceUrl);
            }
        }
        executorService.shutdown();
    }

    private void startUserSetupThread(int userId, String faceUrl) {
        final CountDownLatch latch = new CountDownLatch(1);
        executorService.submit(() -> {
            try {
                createUserAndProcessImage(userId, faceUrl, latch);
            } catch (Exception e) {
                Log.e("FragmentPerformanceTest", "Error processing image for user " + userId, e);
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("FragmentPerformanceTest", "Thread was interrupted while waiting for image processing", e);
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
            RecognitionUtils.subjects.add(subject);
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