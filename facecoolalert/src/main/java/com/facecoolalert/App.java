package com.facecoolalert;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;


import com.facecool.attendance.Constants;
import com.facecool.cameramanager.camera.FaceGraphic2;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.CrashLogDao;
import com.facecoolalert.database.entities.CrashLog;
import com.facecoolalert.ui.base.BaseActivity;
import com.facecoolalert.ui.detect.FaceDetectorProcessor;
import com.facecoolalert.utils.PrefManager;
import com.facecool.attendance.facedetector.FaceEngine;
import com.facecool.attendance.facedetector.FakeFaceEngine;
import com.facecool.cameramanager.entity.CameraInfo;
import com.facecoolalert.utils.RecognitionMode;
import com.facecoolalert.utils.RecognitionUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.CompletableFuture;


public class App extends Application {

    public static FirebaseUser currentUser;

    public static  boolean USE_FAKE = false;

    private boolean[] mSelectedCameras = new boolean[]{true, false, false, false, false};
    private static CameraInfo[] mCameraInfos = null;

    public static Boolean hasModelLoaded=false;

    public static String getDefaultHistoryFolder() {
        String location=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +
                File.separator + "facecoolAlert" + File.separator;
        try{
            File defaultFolder=new File(location);
            if(!defaultFolder.exists())
                defaultFolder.mkdirs();
        }catch (Exception es)
        {
            es.printStackTrace();

        }

        return location;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            startLogCapture();
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        mSelectedCameras = PrefManager.readCameraSelection(this);
        mCameraInfos = PrefManager.readCameraSettings(this);

        float similarityThreshHold = PrefManager.getSimilarityThreshold(this.getApplicationContext());
        RecognitionUtils.similarityThreshold = similarityThreshHold;
        RecognitionUtils.enrollmentQualityThreshold=PrefManager.getEnrollmentQualityThreshold(this.getApplicationContext());
        RecognitionUtils.recognitionQualityThreshold=PrefManager.getRecognitionQualityThreshold(this.getApplicationContext());


        int alertInterval=PrefManager.getAlertInterval(this);
        RecognitionUtils.alertIntervals=alertInterval;
        RecognitionUtils.recognitionMode=PrefManager.getRecognitionMode(this);


        //set recognition mode to default first match
        PrefManager.setRecognitionMode(RecognitionMode.MODE_FIRST_MATCH.getValue(), this);
        RecognitionUtils.recognitionMode=RecognitionMode.MODE_FIRST_MATCH.getValue();


        FaceGraphic2.detectionPropertiesVerboseMode=PrefManager.getBooleanDetectionVerboseMode(this);

        FaceDetectorProcessor.filterFaces=PrefManager.getBooleanFilterFaces(this);



        loadModel();
        Constants.setLiveFlag(this,false);




        Thread.setDefaultUncaughtExceptionHandler(
                new CustomUncaughtExceptionHandler(
                        getApplicationContext(),
                        Thread.getDefaultUncaughtExceptionHandler()));


        FirebaseApp.initializeApp(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        FirebaseCrashlytics.getInstance().sendUnsentReports();
    }

    public void loadModel()
    {
        try {
            AssetManager am = this.getAssets();
            int nInitFL = USE_FAKE ? FakeFaceEngine.loadLiveModel(am) : FaceEngine.loadLiveModel(am);
            if (nInitFL != 0) {
                Toast.makeText(getApplicationContext(), "Failed to load the live models", Toast.LENGTH_LONG).show();
                hasModelLoaded=false;
            }
            else
                hasModelLoaded=true;

            int nInitFF = USE_FAKE ? FakeFaceEngine.loadFeatureModel(this) : FaceEngine.loadFeatureModel(this);
            if (nInitFF != 0) {
                Toast.makeText(getApplicationContext(), "Failed to load the feature models", Toast.LENGTH_LONG).show();
                hasModelLoaded=false;
            }
            else
                hasModelLoaded=true&&hasModelLoaded;

        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            // Show an error message without crashing the app
            Toast.makeText(getApplicationContext(), "Failed to load Native Library", Toast.LENGTH_LONG).show();
            hasModelLoaded = false;
        } catch (Exception e) {
            e.printStackTrace();
            // Show an error message without crashing the app
            Toast.makeText(getApplicationContext(), "Failed to load Engine on this Device", Toast.LENGTH_LONG).show();
            hasModelLoaded = false;
        }
    }

    private void showErrorToast(String s) {


    }


    public boolean[] getSelectedCameras() {
        return mSelectedCameras;
    }

    public void setSelectedCameras(boolean[] selectedCameras) {
        mSelectedCameras = selectedCameras;
        PrefManager.saveCameraSelection(this, selectedCameras);
    }

    public int getSelectedCameraCount(boolean[] selectedCameras) {
        int count = 0;
        for (boolean b: selectedCameras) {
            if ( b ) count++;
        }
        return  count;
    }

    public CameraInfo[] getCameraInfos() {
        if ( mCameraInfos == null ) {
            mCameraInfos = new CameraInfo[4];
            for ( int i = 0 ; i < 4 ; i++ ) {
                mCameraInfos[i] = new CameraInfo();
                mCameraInfos[i].index = i;
            }
        }
        return mCameraInfos;
    }

    public static CameraInfo[] getmCameraInfos() {
        return mCameraInfos;
    }

    public void reloadCameraInfo()
    {
        mCameraInfos = PrefManager.readCameraSettings(this);
    }

    public void setCameraInfos(CameraInfo[] mCameraInfos) {
        this.mCameraInfos = mCameraInfos;
        PrefManager.saveCameraSettings(this, mCameraInfos);
    }

    private void startLogCapture() {
        new Thread(() -> {
            try {
                File logFile = new File(getExternalFilesDir(null), "app_logcat.txt");
                String logcatCommand = "logcat -d *:E | grep 'com.facecoolalert'";
                Process process = Runtime.getRuntime().exec(logcatCommand);

                try (BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                     FileWriter writer = new FileWriter(logFile)) {

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        writer.append(line).append("\n");
                    }
                }
            } catch (IOException e) {
                Log.e("MyApplication", "Error capturing logs", e);
            }
        }).start();
    }

    private static class CustomUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private final Thread.UncaughtExceptionHandler defaultUnCaughtHandler;
        private CrashLogDao crashLogDao;
        private Context context;



        public CustomUncaughtExceptionHandler(Context context, Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler) {
            this.context = context;
            this.defaultUnCaughtHandler = defaultUncaughtExceptionHandler;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
            crashLogDao=MyDatabase.getInstance(context).crashLogDao();

            // Log or handle the uncaught exception here
            Log.e("MyUncaughtExceptionHandler", "Unhandled exception caught", e);
            CrashLog crashLog=new CrashLog();
            crashLog.setTitle(e.getMessage());
            crashLog.setContent(Log.getStackTraceString(e));
            crashLog.setDate(new Date().getTime());
            crashLog.setResourcesStatus(BaseActivity.resourceManager.getCurrentState());

//            writeExceptionToFile(crashLog);
            CompletableFuture<Boolean> saveCrashLog=CompletableFuture.supplyAsync(()->{
                try {
                    crashLogDao.insertCrashLog(crashLog);
                    return true;
                }catch (Exception es)
                {
                    es.printStackTrace();
                    return false;
                }
            });


            saveCrashLog.thenAccept(result->{
                Log.d("Crash",String.format("saved to database : %s , ",result));
                try{FirebaseCrashlytics.getInstance().sendUnsentReports(); Thread.sleep(1500);  }catch (Exception es){}//give allowance to submit to crashlytics
                Thread.UncaughtExceptionHandler existinghandler = t.getUncaughtExceptionHandler();
                if(!(existinghandler instanceof CustomUncaughtExceptionHandler))
                    existinghandler.uncaughtException(t,e);
                if(defaultUnCaughtHandler!=null) {
                    try {
                        defaultUnCaughtHandler.uncaughtException(t, e);
                    }catch (Exception es)
                    {
                        es.printStackTrace();
                    }
                }
                else
                    recordException(e);
                try{FirebaseCrashlytics.getInstance().sendUnsentReports(); Thread.sleep(1500);  }catch (Exception es){}//give allowance to submit to crashlytics
            });

            try{FirebaseCrashlytics.getInstance().sendUnsentReports(); Thread.sleep(1500);  }catch (Exception es){}//give allowance to submit to crashlytics
        }

        private void writeExceptionToFile(CrashLog crashLog) {
            File externalDir = context.getExternalFilesDir(null);
            File crashLogFile = new File(externalDir, "uncaught_exceptions.csv");

            try (FileWriter fileWriter = new FileWriter(crashLogFile, true)) {
                fileWriter.append(crashLog.getTitle())
                        .append(",")
                        .append(crashLog.getContent())
                        .append(",")
                        .append(Long.toString(crashLog.getDate()))
                        .append("\n");
                fileWriter.flush();
            } catch (IOException ex) {
                Log.e("Write Exception", ex.getMessage());
            }
        }
    }



    public static void recordException(Throwable throwable) {
//            Crashlytics.logException(throwable);

        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();


        firebaseCrashlytics.log("crash");
        firebaseCrashlytics.setCustomKey("fatal",true);
        firebaseCrashlytics.recordException(throwable);
//        firebaseCrashlytics.setCustomKey("fatal",true);


//        f .log(Log.ERROR , "tag", "message")



        firebaseCrashlytics.sendUnsentReports();
    }

    public static void saveException(Throwable e, Context context) {//method to record exceptions from anywhere
        CrashLog crashLog=new CrashLog();
        crashLog.setTitle(e.getMessage());
        crashLog.setContent(Log.getStackTraceString(e));
        crashLog.setDate(new Date().getTime());
        crashLog.setResourcesStatus(BaseActivity.resourceManager.getCurrentState());


        CrashLogDao crashLogDao=MyDatabase.getInstance(context).crashLogDao();
        new Thread(() -> {
            try {
                crashLogDao.insertCrashLog(crashLog);
            }catch (Exception es)
            {
                es.printStackTrace();
            }
        }).start();

        recordException(e);
    }

    public static void recordLog(String contentString) {
        FirebaseCrashlytics firebaseCrashlytics = FirebaseCrashlytics.getInstance();
        firebaseCrashlytics.log(contentString);
    }
}
