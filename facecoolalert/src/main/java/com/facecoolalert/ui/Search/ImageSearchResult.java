package com.facecoolalert.ui.Search;

import com.facecool.attendance.facedetector.FaceData;

public class ImageSearchResult {

    private FaceData faceData;
    private String text;



    public ImageSearchResult(FaceData faceData, String text) {
        this.faceData = faceData;
        this.text = text;
    }

    public FaceData getFaceData() {
        return faceData;
    }

    public void setFaceData(FaceData faceData) {
        this.faceData = faceData;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
