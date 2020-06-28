package com.facecool.cameramanager;

import android.content.Intent;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.facecool.cameramanager.settings.CameraSettingFragment;
import com.facecool.cameramanager.camera.CameraViewFragment;
import com.facecool.cameramanager.camera.VisionImageProcessor;
import com.facecool.cameramanager.entity.CameraInfo;

import java.util.Arrays;

public class CameraManagerHelper {
    public static final String TAG_CAMERAS = "camera_setting_cameras";
    public static CameraSettingFragment OpenCameraSettings(FragmentActivity activity, CameraInfo[] cameraInfos, int containerId, Handler handler) {

        CameraSettingFragment fragment = new CameraSettingFragment();

        fragment.handlerActivity = handler;

        CameraInfo[] mCameras = new CameraInfo[cameraInfos.length];

        for ( int i = 0 ; i < cameraInfos.length; i++ ) {
            Parcel parcel = Parcel.obtain();
            cameraInfos[i].writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            mCameras[i] =  CameraInfo.CREATOR.createFromParcel(parcel);
            parcel.recycle();
        }

        fragment.mCameras = mCameras;
        fragment.containerId = containerId;

        activity.getSupportFragmentManager().beginTransaction()
                .add(containerId, fragment)
                .addToBackStack(null)
                .commit();
        return fragment;
    }

    public static View getCameraView(CameraInfo cameraInfo, String title) {
        return null;
    }

    public static CameraInfo[] getCameraInfos(Intent intent) {
        Parcelable[] data = intent.getParcelableArrayExtra(TAG_CAMERAS);
        if ( data == null ) {
            return null;
        }
        return Arrays.copyOf(data, data.length, CameraInfo[].class);
    }

    public static Fragment setupCameraView(FragmentManager fm, int containerId, CameraInfo cameraInfo, VisionImageProcessor processor, boolean showCameraName, int currentMode, ImageView toggleCameraIcon) {

        CameraViewFragment fragment = new CameraViewFragment();
        fragment.showCameraName = showCameraName;
        fragment.setFaceDetectProcessor(processor);
        fragment.setCameraInfo(cameraInfo);
        fragment.currentMode = currentMode;
        fragment.containerId=containerId;
        if(toggleCameraIcon!=null) {
            fragment.toggleCameraIcon = toggleCameraIcon;
            if(cameraInfo!=null&&cameraInfo.type==CameraInfo.TYPE_ANDROID)
                toggleCameraIcon.setVisibility(View.VISIBLE);
        }
        fm.beginTransaction()
                .replace(containerId, fragment)
                .commit();
        return fragment;
    }


    public static void updateCameraView(FragmentManager fm, int containerId, Fragment fragment) {

        try {//not usable, causes problems
            FragmentManager oldParent = fragment.getParentFragmentManager();
            int numOfFrag = oldParent.getFragments().size();
            for (int i = 0; i < numOfFrag; i++)
                oldParent.popBackStackImmediate();
//        fragment.getParentFragmentManager().
        }catch (Exception es)
        {

        }

        try {
            fm.beginTransaction()
                    .replace(containerId, fragment)
                    .commit();
        }catch (Exception es)
        {

        }
    }




}
