package com.facecoolalert.ui.Account;

import java.sql.Timestamp;

public class UserDevice {

    private String userId;

    private String deviceId;

    private Long loggedInTime;

    private String deviceName;

    public UserDevice()
    {

    }

    public UserDevice(String userId, String deviceId, Long loggedInTime, String deviceName) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.loggedInTime = loggedInTime;
        this.deviceName = deviceName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getLoggedInTime() {
        return loggedInTime;
    }

    public void setLoggedInTime(Long loggedInTime) {
        this.loggedInTime = loggedInTime;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }


}
