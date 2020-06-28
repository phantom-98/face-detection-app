package com.facecoolalert.ui.camera;

import com.facecool.attendance.Constants;
import com.facecool.attendance.facedetector.FaceData;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.utils.RecognitionUtils;

public class DetectedItemsItemsUtils {

    public static DetectItem createNewFakeNoDetection() {
        DetectItem o = new DetectItem();
        o.subject = new Subject();
        o.subject.setFirstName("Jimmy");
        o.subject.setLastName("Brown");
        return o;
    }

    public static DetectItem createNewFakeDetection() {
        DetectItem o = new DetectItem();
        o.subject = null;
        return o;
    }

    public static DetectItem createDetection(FaceData faceData) {
        DetectItem o = new DetectItem();
        o.subject = new Subject();

        String[] names="? ".split(" ");

        if(faceData.getScoreMatch() > RecognitionUtils.similarityThreshold &&!faceData.getName().equals(Constants.Name_NOTIDENTIFIED))
            names=faceData.getName().split(" ");

        o.subject.setFirstName(names[0]);//faceData.getName();
        //o.subject.profile_picture=faceData.getBmp();
        o.subject.profilePhoto=faceData.getBmp();
        o.subject.setLastName(names.length>1?names[1]:"");//faceData.getName();

        return o;
    }
}
