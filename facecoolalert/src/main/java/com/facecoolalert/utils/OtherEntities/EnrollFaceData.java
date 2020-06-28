package com.facecoolalert.utils.OtherEntities;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facecool.attendance.facedetector.FaceData;

public class EnrollFaceData implements Parcelable {
    private byte[] features;
    private Bitmap bitmap;

    public EnrollFaceData(FaceData faceData) {
        this.features = faceData.getFeatures();
        this.bitmap = faceData.getBmp();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    protected EnrollFaceData(Parcel in) {
        // Read the byte array containing features from the Parcel
        features = in.createByteArray();
        // Read the Bitmap from the Parcel as a Parcelable
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<EnrollFaceData> CREATOR = new Creator<EnrollFaceData>() {
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        public EnrollFaceData createFromParcel(Parcel in) {
            return new EnrollFaceData(in);
        }

        @Override
        public EnrollFaceData[] newArray(int size) {
            return new EnrollFaceData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // Write the byte array containing features to the Parcel
        dest.writeByteArray(features);

        // Write the Bitmap to the Parcel as a Parcelable
        dest.writeParcelable(bitmap, flags);
    }

    public FaceData getFaceData() {
        FaceData fd = new FaceData(null, 0f, this.bitmap);
        fd.setFeatures(this.features);

        return fd;
    }
}
