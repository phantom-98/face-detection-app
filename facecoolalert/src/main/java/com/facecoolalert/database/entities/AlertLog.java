package com.facecoolalert.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity
public class AlertLog {
    @NotNull
    @PrimaryKey(autoGenerate = false)
    private String uid;

    private String alertName;

    private String recognitionResult_id;


    private Long smsSent;

    private Long emailSent;

    public AlertLog()
    {
        this.uid = UUID.randomUUID().toString();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public String getRecognitionResult_id() {
        return recognitionResult_id;
    }

    public void setRecognitionResult_id(String recognitionResult_id) {
        this.recognitionResult_id = recognitionResult_id;
    }

    public Long getSmsSent() {
        return smsSent;
    }

    public void setSmsSent(Long smsSent) {
        this.smsSent = smsSent;
    }

    public Long getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(Long emailSent) {
        this.emailSent = emailSent;
    }
}
