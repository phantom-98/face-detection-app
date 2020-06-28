package com.facecoolalert.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity
public class Alert {
    @NotNull
    @PrimaryKey(autoGenerate = false)
    private String uid;

    private String name;

    private String watchlist_id;

    private String distributionList_id;

    private String location;

    private Long created;

    public Alert()
    {
        this.uid = UUID.randomUUID().toString();
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWatchlist_id() {
        return watchlist_id;
    }

    public void setWatchlist_id(String watchlist_id) {
        this.watchlist_id = watchlist_id;
    }

    public String getDistributionList_id() {
        return distributionList_id;
    }

    public void setDistributionList_id(String distributionList_id) {
        this.distributionList_id = distributionList_id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
