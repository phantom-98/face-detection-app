package com.facecoolalert.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import com.facecool.attendance.Constants;
import com.facecool.attendance.facedetector.FaceData;
import com.facecool.attendance.facedetector.FaceEngine;
import com.facecool.attendance.facedetector.FakeFaceEngine;
import com.facecoolalert.App;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.SubjectDao;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.database.entities.Subject;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecognitionUtils {


    public static int alertIntervals;
    public static int recognitionMode=0;

    private static Boolean hasLoaded=false;



    public static List<Subject> subjects;
    private static List<byte[]> featuresList = new ArrayList<>();

    public static Context context;

    private static final int FACE_WIDTH = 80;
    private static final int FACE_HEIGHT = 80;

    public static float similarityThreshold = 80;

    public static float enrollmentQualityThreshold=80;
    public static float recognitionQualityThreshold=80;

    private static RecognitionListener listener;
    public static Map<String, RecognitionResult> recognitionResults = new HashMap<>();
    public interface RecognitionListener {
        void onRecognitionResultsChanged(Map<String, RecognitionResult> results);
    }
    public static void setRecognitionListener(RecognitionListener listener) {
        RecognitionUtils.listener = listener;
    }
    public static void putRecognitionResult(int index, int id, RecognitionResult result) {
        recognitionResults.put(index + "-" + id, result);
        notifyListener();
    }
    private static void notifyListener() {
        if (listener != null) {
            listener.onRecognitionResultsChanged(new HashMap<>(recognitionResults));
        }
    }

    public static void loadModel()
    {
        if(context==null)
            return;

        if(!hasLoaded)
        {
            //FaceEngine.loadFeatureModel(context);
            hasLoaded=true;
        }
    }

    


    public static void refreshHistory()
    {
        // recognitionResults=new ConcurrentHashMap<>();
        // workingOn= new ConcurrentSkipListSet<Integer>();
    }

    public static void refreshSubjects() {
        refreshHistory();
        if(context==null)
            return;

        try {
            //FaceEngine.loadFeatureModel(context);
            if (subjects != null){
                subjects.clear();
                featuresList.clear();
            }

            subjects = new LinkedList<>();
            SubjectDao subjectDao =MyDatabase.getInstance(context).subjectDao();
            new Thread() {
                public void run() {
                    try {
                        for (Subject s : subjectDao.getAllSubjects()) {
                            featuresList.add(s.getFeatures());
                            subjects.add(s);
                        }
                    }catch (Exception es)
                    {
                        es.printStackTrace();
                    }
                }
            }.start();
        }catch (Exception eytr)
        {
            eytr.printStackTrace();
        }
    }

    public static void recognizeFace(FaceData faceData) {
        if(subjects == null || subjects.isEmpty()) {
            return;
        }

        Map<Subject, Float> highest = findBestMatchingSubject(subjects, faceData.getFeatures());
        Subject highestSubject = highest.keySet().stream().findFirst().get();
        float highestMatching = highest.values().stream().findFirst().get();
        if (highestSubject == null) {
            faceData.setScoreMatch(0);
            faceData.setName(Constants.Name_NOTIDENTIFIED);
            faceData.subject = null;
        } else {
            faceData.setName(highestSubject.getName());
            faceData.setScoreMatch(highestMatching);
            faceData.subject = highestSubject;
        }
    }

    private static Map<Subject, Float> findBestMatchingSubject(List<Subject> searchPool, byte[] queryFeatures) {
        Subject highest = null;
        float highestMatching = 0;

        for (Subject subject : searchPool) {
            if (subject.getFeatures() == null) continue;

            float matching = FaceEngine.getSimilarity(queryFeatures, subject.getFeatures()) * 100f;

            if (matching > highestMatching) {
                highestMatching = matching;
                highest = subject;
                if(matching >= similarityThreshold) break;
            }
        }
        return  Collections.singletonMap(highest, highestMatching);
    }

    private static Bitmap createFaceBitmap(Bitmap bmpFrame, Face face) {
        Rect rtFace = face.getBoundingBox();
        int x = Math.max(0, rtFace.left);
        int y = Math.max(0, rtFace.top);
        int width = Math.min(rtFace.width(), bmpFrame.getWidth() - x);
        int height = Math.min(rtFace.height(), bmpFrame.getHeight() - y);

        return Bitmap.createBitmap(bmpFrame, x, y, width, height);
    }

    private static Bitmap createPhotoBitmap(Bitmap bmpFace) {
        return Bitmap.createScaledBitmap(bmpFace, FACE_WIDTH, FACE_HEIGHT, true);
    }

    public static FaceData createFaceData(Bitmap bmpFrame, Face face) {
        Bitmap bmpFace = createFaceBitmap(bmpFrame, face);
        Bitmap photo = createPhotoBitmap(bmpFace);
        Log.d("FaceData",String.format("Quality Image Size : %,d Scaled Image Size : %,d",bmpFace.getAllocationByteCount(),photo.getAllocationByteCount()));
        FaceData faceData = new FaceData(face, 1.0f, photo);
        faceData.setQualityImage(bmpFace);
        return faceData;
    }

    private static boolean isFaceWithinBounds(Bitmap bmp, Rect faceRect) {
        return faceRect.left >= 0 && faceRect.top >= 0 &&
                faceRect.right <= bmp.getWidth() && faceRect.bottom <= bmp.getHeight();
    }

    public static FaceData extractFeatures(Bitmap bmpFrame, FaceData faceData) {
        Face face = faceData.getFace();

        float[] landmarksX = new float[5];
        float[] landmarksY = new float[5];
        GetLandmarkPosition(face, landmarksX, landmarksY);

        Rect rtFace = face.getBoundingBox();

        if (App.USE_FAKE ) {
            FakeFaceEngine.extractLiveFeature(bmpFrame, rtFace.left, rtFace.top, rtFace.right, rtFace.bottom, landmarksX, landmarksY, faceData);
        } else {
            FaceEngine.extractLiveFeature(bmpFrame, rtFace.left, rtFace.top, rtFace.right, rtFace.bottom, landmarksX, landmarksY, faceData);
        }

        return faceData;
    }

    private static int[] landMarkTypes = new int[] {
            FaceLandmark.LEFT_EYE, FaceLandmark.RIGHT_EYE,
            FaceLandmark.NOSE_BASE,
            FaceLandmark.MOUTH_LEFT, FaceLandmark.MOUTH_RIGHT,
    };

    private static boolean GetLandmarkPosition(Face face, float[] landmarkX, float[] landmarkY){
        boolean ret = false;
        if( face == null )
            return ret;
        for (int i = 0; i < 5; i++){//landMarkTypes.length; i++) {
            FaceLandmark landmark = face.getLandmark(landMarkTypes[i]);
            if (landmark != null) {
                PointF landmarkPosition = landmark.getPosition();
                //String landmarkPositionStr = String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y);
//                    Log.v(
//                            MANUAL_TESTING_LOG,
//                            "Position for face landmark: "
//                                    + landMarkTypesStrings[i]
//                                    + " is :"
//                                    + landmarkPositionStr);
//				fLandmark[2*i] = landmarkPosition.x;
//				fLandmark[2*i+ 1] = landmarkPosition.y;
                landmarkX[i] = landmarkPosition.x;
                landmarkY[i] = landmarkPosition.y;
            }
        }
        return true;
    }


}
