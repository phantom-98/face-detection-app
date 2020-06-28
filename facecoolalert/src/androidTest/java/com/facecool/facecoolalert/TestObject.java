package com.facecool.facecoolalert;

import java.util.List;

public class TestObject {
    private String videoUrl;
    private List<String> faceUrls;

    public TestObject(String videoUrl, List<String> faceUrls) {
        this.videoUrl = videoUrl;
        this.faceUrls = faceUrls;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public List<String> getFaceUrls() {
        return faceUrls;
    }
}

