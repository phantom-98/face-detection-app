package com.facecoolalert.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;


@Entity
public class Subscriber {

    @NotNull
    @PrimaryKey(autoGenerate = false)
    private String uid;

    private String name;

    private String alertVia;

    private String email;

    private String phone;

    public Subscriber()
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

    public String getAlertVia() {
        return alertVia;
    }

    public void setAlertVia(String alertVia) {
        this.alertVia = alertVia;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
