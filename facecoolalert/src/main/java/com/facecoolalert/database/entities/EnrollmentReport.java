package com.facecoolalert.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity
public class EnrollmentReport {

    @NotNull
    @PrimaryKey(autoGenerate = false)
    private String uid;

    private Long date;

    private String imageFile;

    private String status;

    public EnrollmentReport()
    {
        this.uid = UUID.randomUUID().toString();
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
