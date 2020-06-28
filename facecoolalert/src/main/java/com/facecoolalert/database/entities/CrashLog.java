package com.facecoolalert.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;


@Entity
public class CrashLog {

    @NotNull
    @PrimaryKey(autoGenerate = false)
    private String uid;

    private String title;

    private String content;

    private String resourcesStatus;

    private Long date;

    private Long lastUpload;

    public CrashLog()
    {
        this.uid = UUID.randomUUID().toString();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getResourcesStatus() {
        return resourcesStatus;
    }

    public void setResourcesStatus(String resourcesStatus) {
        this.resourcesStatus = resourcesStatus;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }


    public Long getLastUpload() {
        return lastUpload;
    }

    public void setLastUpload(Long lastUpload) {
        this.lastUpload = lastUpload;
    }
}
