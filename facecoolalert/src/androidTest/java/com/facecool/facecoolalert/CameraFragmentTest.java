package com.facecool.facecoolalert;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertTrue;

import android.content.Context;
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

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CameraFragmentTest {

    public static final String FACES8_FADEIN_HIGH = "faces8_fadein_high";
    public static final String FACES8_FADEIN_LOW = "faces8_fadein_low";
    public static final String FACES8_FLICKERING_HIGH = "faces8_flickering_high";
    public static final String FACES8_FLICKERING_LOW = "faces8_flickering_low";
    public static final String FACES8_MOVING_HIGH_10S = "rtsp://192.168.0.8:8554/stream";
    public static final String FACES8_MOVING_HIGH = "faces8_moving_high";
    public static final String FACES8_MOVING_LOW = "faces8_moving_low";
    public static final String FACES8_TUMBLE_HIGH = "faces8_tumble_high";
    public static final String FACES8_TUMBLE_LOW = "faces8_tumble_low";
    private String[] selectionArray = {FACES8_MOVING_LOW, FACES8_MOVING_LOW, FACES8_MOVING_LOW, FACES8_MOVING_LOW};
    public static final String PROFILE_PHOTO_PREFIX = "f"; // prefix of users photos
    String[] photoNames = {"f46", "f44", "f43", "f39", "f35", "f30", "f38", "f32"};
    private static final int NUMBER_OF_USERS = 8; // how many users you want to setup
    private static final int NUMBER_OF_FRAGMENTS = 4; // do not change
    private static final boolean[] CAMERA_SELECTION = {true, false, false, false}; // setup up visibility of fragments
    private static final int NUMBERS_OF_RECOGNIZED_USERS = 8; // how many users should be recognised?
    private static final int IN_SAMPLE_SIZE = 1; // The inSampleSize constant determines the reduction factor for the image resolution.
    private static final Double SIMILARITY_THRESHOLD = 70d;
    private final SubjectDao subjectDao = MyDatabase.getInstance(getApplicationContext()).subjectDao();

    private ExecutorService executorService;

    @Before
    public void setUp() {
        clearDatabaseAndPreferences();
    }

    @After
    public void tearDown() {
        clearDatabaseAndPreferences();
    }


//    @Test
//    public void test00ClearDatabaseAndPreferences() {
//        // Setup users and save camera settings
//        saveCameraSettings(createCameraInfo());
//        setupUsers();
//
//        // Call the method to test
//        clearDatabaseAndPreferences();
//
//        // Check if the database is empty
//        List<User> users = userDao.getAllUsers();
//        assertTrue("Database should be empty", users.isEmpty());
//
//        // Check if the preferences are cleared
//        Context context = ApplicationProvider.getApplicationContext();
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        assertTrue("Preferences should be empty", preferences.getAll().isEmpty());
//    }

/*    @Test
    public void test01UserSetup() {
        setupUsers();
        List<User> users = userDao.getAllUsers();
        assertEquals(NUMBER_OF_USERS, users.size());
    }

    @Test
    public void test02CameraSetup() {
        CameraInfo[] cameraInfoArray = createCameraInfo();
        saveCameraSettings(cameraInfoArray);

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String savedCameraInfosString = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_CAMERA_SETTINGS, "");
        CameraInfo[] savedCameraInfoArray = new Gson().fromJson(savedCameraInfosString, CameraInfo[].class);

        assertEquals(cameraInfoArray.length, savedCameraInfoArray.length);
        for (int i = 0; i < cameraInfoArray.length; i++) {
            assertEquals(cameraInfoArray[i].cameraName, savedCameraInfoArray[i].cameraName);
            assertEquals(cameraInfoArray[i].location, savedCameraInfoArray[i].location);
            assertEquals(cameraInfoArray[i].type, savedCameraInfoArray[i].type);
            assertEquals(cameraInfoArray[i].videoPath, savedCameraInfoArray[i].videoPath);
        }
    }

    @Test
    public void test03FourCameraView() {
        activityRule.getScenario().onActivity(activity -> {
            App app = (App) activity.getApplication();
            app.setSelectedCameras(new boolean[]{true, true, true, true});
        });

        onView(withId(R.id.tv_camera_select)).perform(click());
        onView(withText("All Cameras")).perform(click());

        onView(allOf(withId(R.id.layout_camera1), isDescendantOfA(withId(R.id.layout_frame_cameras)))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.layout_camera2), isDescendantOfA(withId(R.id.layout_frame_cameras)))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.layout_camera3), isDescendantOfA(withId(R.id.layout_frame_cameras)))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.layout_camera4), isDescendantOfA(withId(R.id.layout_frame_cameras)))).check(matches(isDisplayed()));
    }*/

    @Test
    public void test04RecognitionPerformance() {
        try (ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class)) {
            Constants.logInfo("Starting MainActivity important for initialization", null);
            try {
                Thread.sleep(1000); // just for better understanding and seeing flow on screen
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try (ActivityScenario<EnrollSubject> enrollScenario = ActivityScenario.launch(EnrollSubject.class)) {
            Constants.logInfo("Setting up users in the database during EnrollActivity not important but helpful for understanding the flow", null);
            setupUsers();

            Constants.logInfo("Storing camera info and camera selection", null);
            saveCameraSettings(createCameraInfo());
        }

        Constants.logInfo("Start activity to start performance measurement", null);
        Constants.logInfo("Starting to monitor recognitionResults for face recognition...", null);
        long startTime = System.currentTimeMillis(); // important it is outside of scenario
        try (ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class)) {
            long timeout = 20000; // 10 seconds timeout to jump out of thread

            AtomicBoolean allFacesProcessed = new AtomicBoolean(false);
            AtomicReference<HashMap<String, RecognitionResult>> recognitionResults = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            Thread thread = new Thread(() -> {
                Constants.logInfo("listener thread started", null);
                RecognitionUtils.setRecognitionListener(detectedAndRecognitionCompleted -> {
                    Constants.logInfo("Currently processed faces: " + detectedAndRecognitionCompleted.size(), null);
                    boolean allResultsPositive = detectedAndRecognitionCompleted.values().stream()
                            .allMatch(result -> result.getScoreMatch() >= 0);
                    if (detectedAndRecognitionCompleted.size() >= NUMBERS_OF_RECOGNIZED_USERS && allResultsPositive) {
                        allFacesProcessed.set(true);
                        recognitionResults.set(new HashMap<>(detectedAndRecognitionCompleted));
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
            Constants.logInfo("Time taken for face processing: " + elapsedTime + "ms", null);
            Constants.logInfo("End-to-end test completed.", null);

            // 7. quality test
            HashMap<String, RecognitionResult> detectedResults = recognitionResults.get();
            // Filter out results below the threshold
            List<RecognitionResult> belowThresholdResults = detectedResults == null ? Collections.emptyList() :
                    detectedResults.values().stream()
                            .filter(result -> result.getScoreMatch() <= SIMILARITY_THRESHOLD)
                            .collect(Collectors.toList());
            boolean allMatchesAboveThreshold = belowThresholdResults.isEmpty();
            // If not all matches are above the threshold, log the ones that aren't
            if (!allMatchesAboveThreshold) {
                for (RecognitionResult result : belowThresholdResults) {
                    if(result.getSubject() != null){
                        Constants.logInfo("Failed recognition: " + result.getSubject().getFirstName() + " "
                                + result.getSubject().getLastName() + " with match score: " + result.getScoreMatch(), null);
                    } else {
                        Constants.logInfo("Failed recognition: " + result.getScoreMatch(), null);
                    }
                }
            }
            assert allMatchesAboveThreshold : "One or more faces has not been recognised";
        }
    }

    private void clearDatabaseAndPreferences() {
        Constants.logInfo("clearDatabaseAndPreferences", null);
        subjectDao.deleteAll();

        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        Constants.logInfo("clearDatabaseAndPreferences end", null);
    }

    private CameraInfo[] createCameraInfo() {
        CameraInfo[] cameraInfoArray = new CameraInfo[NUMBER_OF_FRAGMENTS];
        for (int i = 0; i < NUMBER_OF_FRAGMENTS && i < selectionArray.length; i++) {
            String videoPath;
            CameraInfo info = new CameraInfo();
            if (selectionArray[i].startsWith("rtsp://")) {
                videoPath = selectionArray[i];
                info.type = CameraInfo.TYPE_IP_CAM;
            } else {
                videoPath = copyVideoToInternalStorage(selectionArray[i]);
                info.type = CameraInfo.TYPE_VIDEO_FILE;
            }
            Constants.logInfo("videoPath: " + videoPath, null);
            info.index = i;
            info.cameraName = "Video Camera " + (i + 1);
            info.location = "Location " + (i + 1);
            info.videoPath = videoPath;
            cameraInfoArray[i] = info;
        }
        Constants.logInfo("createCameraInfo in test " + cameraInfoArray.length, null);
        return cameraInfoArray;
    }

    private int getRawResourceId(String name) {
        Context context = ApplicationProvider.getApplicationContext();
        return context.getResources().getIdentifier(name, "raw", context.getPackageName());
    }

    private String copyVideoToInternalStorage(String videoName) {
        Context context = ApplicationProvider.getApplicationContext();
        File internalStorage = context.getFilesDir();
        File videoFile = new File(internalStorage, videoName + ".mp4");
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = context.getResources().openRawResource(getRawResourceId(videoName));
            out = new FileOutputStream(videoFile, false);  // false means do not append, overwrite the file
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            Log.e("CameraFragmentTest - Test", "Failed to copy video file", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e("VideoCopy", "Failed to close streams", e);
            }
        }
        return videoFile.getAbsolutePath();
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
        for (int i = 0; i < NUMBER_OF_USERS; i++) {
            Constants.logInfo("create user " + i, null);
            startUserSetupThread(i);
        }
        executorService.shutdown();
    }

    private void startUserSetupThread(int i) {
        final CountDownLatch latch = new CountDownLatch(1);
        executorService.submit(() -> {
            try {
                createUserAndProcessImage(i, latch);
            } catch (Exception e) {
                Log.e("CameraFragmentTest - Test", "Error processing image for user " + i, e);
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("CameraFragmentTest - Test", "Thread was interrupted while waiting for image processing", e);
        }
    }

    private Bitmap getBitmapForUser(String name) {
        Constants.logInfo("image name " + name, null);
        int resId = getApplicationContext().getResources().getIdentifier(name, "drawable",
                getApplicationContext().getPackageName());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = IN_SAMPLE_SIZE;
        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), resId, options);
        if (bitmap == null) {
            Log.e("CameraFragmentTest - Test", "Failed to load bitmap for user name: " + name + ", resId: " + resId);
        }
        return bitmap;
    }

    private void createUserAndProcessImage(int i, CountDownLatch latch) {
        Subject subject = createRandomSubject();
        String imageName;
        if (photoNames != null && photoNames.length > i) {
            imageName = photoNames[i];
        } else {
            imageName =  PROFILE_PHOTO_PREFIX + i;
        }
        Bitmap bitmap = getBitmapForUser(imageName);
        if (bitmap == null) {
            latch.countDown();
            return;
        }
        Constants.logInfo("get bitmap " + bitmap.getByteCount() + " for user " + i, null);

        FaceDetectorProcessor.ResultListener resultListener = new FaceDetectorProcessor.ResultListener() {
            @Override
            public void onSuccess(FaceData faceData, boolean bOnlyReg) {
                try {
                    if (faceData.getFeatures() == null) {
                        faceData.setFeatures(new byte[512]);
                        Constants.logInfo("Extracted Null for user " + i, null);
                    }
                    saveImageAndSetNum(subject, bitmap);
                    Constants.logInfo("insert ended " + i, null);
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