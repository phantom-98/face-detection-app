package com.facecoolalert.ui.Account;


public class AccountUser {

    private String userId;
    private String email;
    private String phone;
    private String name;
    private String device;

    public AccountUser(String email, String phone, String name, String device) {
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.device = device;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

