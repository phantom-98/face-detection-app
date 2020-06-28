package com.facecool.attendance;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Constants {
    public static final String TAG = "FaceCool";

    public static final String BR_LOW = "BR low";
    public static final String BR_HIGH = "BR high";
    public static final String SHARPNESS = "Sharpness";
    public static final String SIZE = "Size";
    public static final String ABS_YAW = "Abs Yaw";
    public static final String RULE_PARAM_TAG_CHAR = "_";
    public static final String RULE_1 = "Rule 1";
    public static final String RULE_2 = "Rule 2";
    public static final String RULE_3 = "Rule 3";
    public static final String RULE_4 = "Rule 4";
    public static final String RULE_5 = "Rule 5";

    public static final int PERMISSIONS_REQUEST = 1;
    public static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    public static final String NAME_UN_KNOWN = "unknown";

    public static final String Name_NOTIDENTIFIED = "Not Identified";

    public static final String DIR_MODEL = "model";
    public static void LogDebug(Object msg){
        Log.d(TAG, msg.toString());
    }
    public static void LogInfo(Object msg){
        Log.i(TAG, msg.toString());
    }


    public static final String MyPREFERENCES = "MyPrefs";



    public static final String THRESHOLD_LIVE = "Threshold_Live";
    public static void setThresholdLive(Context context, float fThreshold) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.edit().putFloat(Constants.THRESHOLD_LIVE, fThreshold).commit();
    }
    public static float getThresholdLive(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
        float fThreshold = sharedPreferences.getFloat(Constants.THRESHOLD_LIVE, 0.85f);
        return fThreshold;
    }

    public static final String THRESHOLD_SIMILAR = "Threshold_Similar";
    public static void setThresholdSimilar(Context context, float fThreshold) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.edit().putFloat(Constants.THRESHOLD_SIMILAR, fThreshold).commit();
    }
    public static float getThresholdSimilar(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
        float fThreshold = sharedPreferences.getFloat(Constants.THRESHOLD_SIMILAR, 0.70f);
        return fThreshold;
    }

    public static final String MIN_FACE_SIZE = "Min_Face_Size";
    public static void setMinFaceSiz(Context context, float fSize) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.edit().putFloat(Constants.MIN_FACE_SIZE, fSize).commit();
    }
    public static float getMinFaceSize(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
        float fSize = sharedPreferences.getFloat(Constants.MIN_FACE_SIZE, 0.30f);
        return fSize;
    }
    public static final String FACE_MORE_ACCURATE = "Face_More_Accurate";
    public static void setFaceMoreAccurate(Context context, boolean bMoreAccurate) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Constants.FACE_MORE_ACCURATE, bMoreAccurate).commit();
    }
    public static boolean getFaceMoreAccurate(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
        boolean bMoreAccurate = sharedPreferences.getBoolean(Constants.FACE_MORE_ACCURATE, false);
        return bMoreAccurate;
    }
    public static final String LIVE_FLAG = "live_flag";
    public static void setLiveFlag(Context context, boolean bLiveness) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(Constants.LIVE_FLAG, bLiveness).commit();
    }
    public static boolean getLiveFlag(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MyPREFERENCES, Context.MODE_PRIVATE);
        boolean bLiveness = sharedPreferences.getBoolean(Constants.LIVE_FLAG, true);
        return bLiveness;
    }

    public static void logInfo(String message, String label) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[3];
        String className = caller.getClassName();
        String methodName = caller.getMethodName();
        int lineNumber = caller.getLineNumber();
        String logMessage = "Class: " + className + ", Method: " + methodName + ", Line: " + lineNumber + ", Thread: "
                + Thread.currentThread().getName() + ", Message: " + message;
        if(label == null) {
            Log.i("CameraFragmentTest", logMessage);
        } else {
            Log.i(label, logMessage);
        }
    }
}
