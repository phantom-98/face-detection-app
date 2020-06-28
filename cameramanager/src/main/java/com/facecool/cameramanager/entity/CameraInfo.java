package com.facecool.cameramanager.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class CameraInfo implements Parcelable {
    public static final int TYPE_IP_CAM = 0;
    public static final int TYPE_IP_WIFI = 1;
    public static final int TYPE_USB = 2;
    public static final int TYPE_ANDROID = 3;
    public static final int TYPE_VIDEO_FILE = 4;
    public static final int TYPE_EXTERNAL_SCREEN = 5;
    public static final int TYPE_DRONE_WIFI = 6;
    public static final int RESOLUTION_480p = 0;
    public static final int RESOLUTION_720p = 1;
    public static final int RESOLUTION_1080p = 2;

    public int index;
    public String cameraName = "";
    public String location = "";

    public String description="";

    public int type = -1;
    public String url = "";
    public String username = "";
    public String password = "";
    public String videoPath = "";
    public boolean isFront = false;

    public boolean isPaused=false;
    public int resolution = 1;
    public CameraInfo() {

    }

    protected CameraInfo(Parcel in) {
        index = in.readInt();
        cameraName = in.readString();
        location = in.readString();
        description = in.readString();
        type = in.readInt();
        url = in.readString();
        username = in.readString();
        password = in.readString();
        videoPath = in.readString();
        isFront = in.readInt() > 0 ;
        resolution = in.readInt();
    }

    public static final Creator<CameraInfo> CREATOR = new Creator<CameraInfo>() {
        @Override
        public CameraInfo createFromParcel(Parcel in) {
            return new CameraInfo(in);
        }

        @Override
        public CameraInfo[] newArray(int size) {
            return new CameraInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(index);
        parcel.writeString(cameraName);
        parcel.writeString(location);
        parcel.writeString(description);
        parcel.writeInt(type);
        parcel.writeString(url);
        parcel.writeString(username);
        parcel.writeString(password);
        parcel.writeString(videoPath);
        parcel.writeInt(isFront ? 1 : 0);
        parcel.writeInt(resolution);
    }
}
