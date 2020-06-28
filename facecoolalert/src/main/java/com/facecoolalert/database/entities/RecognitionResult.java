package com.facecoolalert.database.entities;

import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.facecool.attendance.facedetector.FaceData;
import com.facecoolalert.utils.converters.BitmapConverter;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

@Entity(tableName = "RecognitionResult")
public class RecognitionResult {

    @NotNull
    @PrimaryKey(autoGenerate = false)
    private String uid;

    @Ignore
    private Subject subject;

    private String subjectId;

    @Ignore
    private FaceData faceData;

    private Long date;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] features;

    @TypeConverters(BitmapConverter.class)
    private Bitmap bmp;

    private float live;

    private float scoreMatch;

    private String location;

    private int camera;


    private Long lastChange;

    private Double imageQuality;
    

    public RecognitionResult()
    {
        this.uid = UUID.randomUUID().toString();
    }

    public RecognitionResult(Subject subject, FaceData faceData) {
        this.uid = UUID.randomUUID().toString();
        this.subject=subject;
        if(subject!=null)
            this.subjectId=this.subject.getUid();

        this.faceData=faceData;
        if(faceData!=null)
        {
            this.features=faceData.getFeatures();
            this.bmp=faceData.getBestImage();
            this.live=faceData.getLive();
            this.scoreMatch=faceData.getScoreMatch();
            this.imageQuality=faceData.getImageQualityScore();

        }

        this.date=new Date().getTime();
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public FaceData getFaceData() {
        return faceData;
    }

    public void setFaceData(FaceData faceData) {
        this.faceData = faceData;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public byte[] getFeatures() {
        return features;
    }

    public void setFeatures(byte[] features) {
        this.features = features;
    }

    public Bitmap getBmp() {
        return bmp;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public float getLive() {
        return live;
    }

    public void setLive(float live) {
        this.live = live;
    }

    public float getScoreMatch() {
        return scoreMatch;
    }

    public void setScoreMatch(float scoreMatch) {
        this.scoreMatch = scoreMatch;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCamera() {
        return camera;
    }

    public void setCamera(int camera) {
        this.camera = camera;
    }

    public FaceData genFaceData()
    {
        FaceData fd=new FaceData(null,getLive(),getBmp());
        fd.setScoreMatch(getScoreMatch());
        fd.setFeatures(getFeatures());
        fd.setImageQualityScore(getImageQuality());
        if(subject!=null)
            fd.setName(subject.getName());
        return fd;
    }

    public Long getLastChange() {
        return lastChange;
    }

    public void setLastChange(Long lastChange) {
        this.lastChange = lastChange;
    }

    public Double getImageQuality() {
        if(imageQuality==null)
            return 0.0;
        else
            return imageQuality;
    }

    public void setImageQuality(Double imageQuality) {
        this.imageQuality = imageQuality;
    }
}
