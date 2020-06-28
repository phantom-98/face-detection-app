package com.facecoolalert.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Entity
public class SubscriberDistributionListCrossRef {

    @NotNull
    @PrimaryKey(autoGenerate = false)
    private String uid;
    private String subscriber_id;
    private String distributionList_id;

    public SubscriberDistributionListCrossRef()
    {
        this.uid = UUID.randomUUID().toString();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSubscriber_id() {
        return subscriber_id;
    }

    public void setSubscriber_id(String subscriber_id) {
        this.subscriber_id = subscriber_id;
    }

    public String getDistributionList_id() {
        return distributionList_id;
    }

    public void setDistributionList_id(String distributionList_id) {
        this.distributionList_id = distributionList_id;
    }
}
