package com.facecoolalert.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.facecool.attendance.Constants;
import com.facecool.cameramanager.camera.FaceGraphic2;
import com.facecool.cameramanager.entity.CameraInfo;
import com.facecoolalert.App;
import com.facecoolalert.ui.detect.FaceDetectorProcessor;
import com.facecoolalert.utils.Alert.email.EmailSettings;
import com.facecoolalert.resources.ResourceManager;
import com.facecoolalert.ui.settings.Rule;
import com.facecoolalert.utils.AutoExportRecognitionHistory.AutoExportThread;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrefManager {

    private static final String KEY_SIMILARITY_THRESHOLD = "similarity_threshold";
    private static final String KEY_RECOGNITION_MODE = "recognition_mode";
    private static final String KEY_THERMAL_LIMIT = "thermal_limit";
    private static final String KEY_HISTORY_EXPORT_FOLDER = "history_export_folder";
    private static final String KEY_HISTORY_DELETE_SIZE = "history_delete_size";
    private static final String KEY_MAXIMUM_HISTORY_SIZE = "maximum_history_size";
    static String KEY_CAMERA_SETTINGS = "camera_settings";
    static String KEY_CAMERA_SELECTION = "camera_selection";

    static String KEY_ALERT_INTERVAL="alert_interval";

    private static final String KEY_EMAIL_SETTINGS = "email_settings";

    private static final String KEY_SMS_SIMCARD = "sms_simcard";

    private static final String KEY_RULE_PREFIX = "rule_";

    private static final String KEY_ENROLLMENT_QUALITY_THRESHOLD="enrollment_image_quality_threshold";
    private static final String KEY_RECOGNITION_QUALITY_THRESHOLD="recognition_image_quality_threshold";


    private static final String KEY_DETECTION_PROPERTIES_VERBOSE_MODE="detection_properties_verbose_mode";


    private static String KEY_FILTER_FACES = "FILTER_FACES";


    public static SharedPreferences.Editor getSharedEditor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .edit();
    }

    public static void saveString(Context context, String key, String value) {
        getSharedEditor(context).putString(key, value).apply();
    }

    public static String readString(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
    }

    public static void saveCameraSettings(Context context, CameraInfo[] cameraInfos) {
        String val = new Gson().toJson(cameraInfos);
        saveString(context, KEY_CAMERA_SETTINGS, val);
    }

    public static CameraInfo[] readCameraSettings(Context context) {
        String v = readString(context, KEY_CAMERA_SETTINGS);
        if (TextUtils.isEmpty(v)) {
            return null;
        }

        CameraInfo[] cameraInfos=new Gson().fromJson(v, CameraInfo[].class); 
        for(int i=0;i<cameraInfos.length;i++)
            cameraInfos[i].index=i;

        return cameraInfos;
    }

    public static void saveCameraSelection(Context context, boolean[] cameraSelections) {
        String val = new Gson().toJson(cameraSelections);
        saveString(context, KEY_CAMERA_SELECTION, val);
    }

    public static boolean[] readCameraSelection(Context context) {
        String v = readString(context, KEY_CAMERA_SELECTION);
        if (TextUtils.isEmpty(v)) {
            return new boolean[]{true, false, false, false};
        }
        return new Gson().fromJson(v, boolean[].class);
    }

    public static void setSimilarityThreshold(Context context, float similarityThresHold) {
        float similarityThreshold=similarityThresHold;
        if(similarityThreshold<1)
            similarityThreshold*=100;

        RecognitionUtils.similarityThreshold = similarityThreshold;
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putFloat(KEY_SIMILARITY_THRESHOLD, similarityThreshold);
        editor.apply();
    }

    public static float getSimilarityThreshold(Context context) {
        float res = 80;
        try {
            try {
                res = PreferenceManager.getDefaultSharedPreferences(context)
                        .getFloat(KEY_SIMILARITY_THRESHOLD, 80);
            }catch (Exception e2)
            {
//                e2.printStackTrace();
//                res = PreferenceManager.getDefaultSharedPreferences(context)
//                        .getFloat(KEY_SIMILARITY_THRESHOLD, 80);
            }
            RecognitionUtils.similarityThreshold = res;
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        if(res<1)
            res*=100.0;

        return res;
    }

    public static int getAlertInterval(Context context)
    {
        int interval=60;

        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            interval = sharedPreferences.getInt(KEY_ALERT_INTERVAL, 60);

        }catch (Exception es)
        {
            es.printStackTrace();
        }

        RecognitionUtils.alertIntervals=interval;
        return interval;
    }

    public static void setAlertInterval(int alertInterval,Context context)
    {
        RecognitionUtils.alertIntervals=alertInterval;
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putInt(KEY_ALERT_INTERVAL,alertInterval);
        editor.apply();

    }

    public static void saveEmailSettings(Context context, EmailSettings emailSettings) {
        String val = new Gson().toJson(emailSettings);
        saveString(context, KEY_EMAIL_SETTINGS, val);
    }




    public static EmailSettings readEmailSettings(Context context) {
        String v = readString(context, KEY_EMAIL_SETTINGS);
        if (TextUtils.isEmpty(v)) {
            return new EmailSettings();
        }
        return new Gson().fromJson(v, EmailSettings.class);
    }



    public static int getSMSSimcard(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(KEY_SMS_SIMCARD,0);

    }

    public static void setSmsSimcard(int simcard,Context context)
    {
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putInt(KEY_SMS_SIMCARD,simcard);
        editor.apply();
    }


    public static int getRecognitionMode(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(KEY_RECOGNITION_MODE,RecognitionMode.MODE_BEST_MATCH.getValue());

    }

    public static void setRecognitionMode(int recognitionMode,Context context)
    {
        RecognitionUtils.recognitionMode=recognitionMode;
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putInt(KEY_RECOGNITION_MODE,recognitionMode);
        editor.apply();
    }

    public static int getThermalLimit(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(KEY_THERMAL_LIMIT,0);
    }

    public static void setThermalLimit(int limit,Context context)
    {
        ResourceManager.thermalLimit=limit;
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putInt(KEY_THERMAL_LIMIT,limit);
        editor.apply();
    }


    public static String getHistoryExportFolder(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String folderPath = sharedPreferences.getString(KEY_HISTORY_EXPORT_FOLDER, "");

        // If the folderPath is empty, set the default folder as Documents/facecoolAlert/
        if (TextUtils.isEmpty(folderPath)) {
            // Adjust this path based on your desired default folder
            folderPath = App.getDefaultHistoryFolder();
        }

        return folderPath;
    }

    public static void setHistoryExportFolder(String folder,Context context)
    {
        AutoExportThread.exportLocation=folder;
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putString(KEY_HISTORY_EXPORT_FOLDER,folder);
        editor.apply();
    }



    public static float getMaxHistorySize(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPreferences.getFloat(KEY_MAXIMUM_HISTORY_SIZE, 10.0f);
        }catch (Exception es)
        {
            return sharedPreferences.getInt(KEY_MAXIMUM_HISTORY_SIZE,10)*1F;
        }

    }


    public static void setMaxHistorySize(float maxsize,Context context)
    {
        AutoExportThread.maxSize= (double) maxsize;
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putFloat(KEY_MAXIMUM_HISTORY_SIZE,maxsize);
        editor.apply();
    }

    public static float getHistoryDeleteSize(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return sharedPreferences.getFloat(KEY_HISTORY_DELETE_SIZE, 1.0F);
        }catch (Exception es)
        {
            return sharedPreferences.getInt(KEY_HISTORY_DELETE_SIZE, 1)*1F;
        }
    }

    public static void setHistoryDeleteSize(float size,Context context)
    {
        AutoExportThread.deleteSize= (double) size;
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putFloat(KEY_HISTORY_DELETE_SIZE,size);
        editor.apply();
    }

    public static void saveRule(Context context, String ruleName, Rule rule) {
        String ruleJson = new Gson().toJson(rule);
        getEditor(context).putString(KEY_RULE_PREFIX + ruleName, ruleJson).apply();
    }

    public static Rule getRule(Context context, String ruleName) {
        String ruleJson = getPreferences(context).getString(KEY_RULE_PREFIX + ruleName, null);
        return ruleJson != null ? new Gson().fromJson(ruleJson, Rule.class) : null;
    }

    public static List<Rule> getRules(Context context) {
        SharedPreferences prefs = getPreferences(context);
        List<Rule> rules = new ArrayList<>();

        String[] ruleNames = {
                Constants.RULE_1, Constants.RULE_2, Constants.RULE_3,
                Constants.RULE_4, Constants.RULE_5
        };

        for (String ruleName : ruleNames) {
            String ruleJson = prefs.getString(ruleName, null);
            if (ruleJson != null) {
                Rule rule = new Gson().fromJson(ruleJson, Rule.class);
                if (rule != null) {
                    rules.add(rule);
                }
            }
        }

        return rules;
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void removeAllRules(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        Map<String, ?> allEntries = getPreferences(context).getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith(KEY_RULE_PREFIX)) {
                editor.remove(entry.getKey());
            }
        }
        editor.apply();
    }


    public static void setEnrollmentQualityThreshold(Context context, float enrollmentThreshold) {
        float threshold=enrollmentThreshold;
        if(threshold<1)
            threshold*=100;

        RecognitionUtils.enrollmentQualityThreshold = threshold;
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putFloat(KEY_ENROLLMENT_QUALITY_THRESHOLD, threshold);
        editor.apply();
    }

    public static float getEnrollmentQualityThreshold(Context context) {
        float res = 80;
        try {
            try {
                res = PreferenceManager.getDefaultSharedPreferences(context)
                        .getFloat(KEY_ENROLLMENT_QUALITY_THRESHOLD, 80);
            }catch (Exception e2)
            {
                res = PreferenceManager.getDefaultSharedPreferences(context)
                        .getFloat(KEY_ENROLLMENT_QUALITY_THRESHOLD, 80);
            }
            RecognitionUtils.enrollmentQualityThreshold = res;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        if(res<1)
            res*=100.0;

        return res;
    }

    public static void setRecognitionQualityThreshold(Context context, float recognitionThreshold) {
        float threshold=recognitionThreshold;
        if(threshold<1)
            threshold*=100;

        RecognitionUtils.recognitionQualityThreshold = threshold;
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putFloat(KEY_RECOGNITION_QUALITY_THRESHOLD, threshold);
        editor.apply();
    }

    public static float getRecognitionQualityThreshold(Context context) {
        float res = 80;
        try {
            try {
                res = PreferenceManager.getDefaultSharedPreferences(context)
                        .getFloat(KEY_RECOGNITION_QUALITY_THRESHOLD, 80);
            }catch (Exception e2)
            {
                res = PreferenceManager.getDefaultSharedPreferences(context)
                        .getInt(KEY_RECOGNITION_QUALITY_THRESHOLD, 80);
            }
            RecognitionUtils.recognitionQualityThreshold = res;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        if(res<1)
            res*=100.0;

        return res;
    }

    public static boolean getBooleanDetectionVerboseMode(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(KEY_DETECTION_PROPERTIES_VERBOSE_MODE,false);
    }

    public static void setBooleanDetectionVerboseMode(Boolean status,Context context) {
        FaceGraphic2.detectionPropertiesVerboseMode=status;
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putBoolean(KEY_DETECTION_PROPERTIES_VERBOSE_MODE,status);
        editor.apply();
    }


    public static boolean getBooleanFilterFaces(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(KEY_FILTER_FACES,true);
    }

    public static void setBooleanFilterFaces(Boolean status,Context context) {
        FaceDetectorProcessor.filterFaces=status;
        SharedPreferences.Editor editor = getSharedEditor(context);
        editor.putBoolean(KEY_FILTER_FACES,status);
        editor.apply();
    }




}
