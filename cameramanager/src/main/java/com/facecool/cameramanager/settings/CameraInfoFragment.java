package com.facecool.cameramanager.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.facecool.cameramanager.R;
import com.facecool.cameramanager.entity.CameraInfo;
import com.facecool.cameramanager.picker.PickerFragment;

public class CameraInfoFragment extends Fragment implements AdapterView.OnItemSelectedListener, PickerFragment.OnPickListener {

    public static final String TAG_CAMERA = "camera_info_camera";
    public static final String TAG_ANDROID = "android_camera";
    public CameraInfo mCameraInfo;
    public int mCount = 0;
    public int containerId;

    View rootView;

    CameraSettingFragment mCameraSettingFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_camera_info, null);
        ((Spinner)rootView.findViewById(R.id.sp_camera)).setOnItemSelectedListener(this);

        rootView.findViewById(R.id.btn_back).setOnClickListener(this::onBackClick);
        rootView.findViewById(R.id.btn_save).setOnClickListener(this::onSaveClick);
        rootView.findViewById(R.id.btn_File).setOnClickListener(this::onBrowseClick);

        showInfo();
        return rootView;
    }


    public void showInfo() {
        setTextOfView(R.id.tv_title, "Camera " + (mCameraInfo.index+1));
        ((Spinner)rootView.findViewById(R.id.sp_camera)).setSelection(mCameraInfo.type);

        setTextOfView(R.id.edt_location, mCameraInfo.location);
        setTextOfView(R.id.edt_url, mCameraInfo.url);
        setTextOfView(R.id.edt_username, mCameraInfo.username);
        setTextOfView(R.id.edt_password, mCameraInfo.password);
        setTextOfView(R.id.edt_video_path, mCameraInfo.videoPath);

        ((Switch)rootView.findViewById(R.id.swi_android_camera)).setChecked(mCameraInfo.isFront);

    }

    public boolean retrieveInfo() {
        CameraInfo tempCamera = new CameraInfo();
        tempCamera.type = ((Spinner)rootView.findViewById(R.id.sp_camera)).getSelectedItemPosition();
        tempCamera.location = getTextOfView(R.id.edt_location);
        tempCamera.url = getTextOfView(R.id.edt_url);
        tempCamera.username = getTextOfView(R.id.edt_username);
        tempCamera.password = getTextOfView(R.id.edt_password);
        tempCamera.videoPath = getTextOfView(R.id.edt_video_path);
        tempCamera.location = getTextOfView(R.id.edt_location);
        tempCamera.isFront = ((Switch)rootView.findViewById(R.id.swi_android_camera)).isChecked();
        tempCamera.index = mCameraInfo.index;

//        if (TextUtils.isEmpty(tempCamera.location)) {
//            Toast.makeText(this, "You should specify the location of camera", Toast.LENGTH_SHORT).show();
//            return false;
//        }



        switch (tempCamera.type) {
            case CameraInfo.TYPE_ANDROID:
                if ( mCameraInfo.type != CameraInfo.TYPE_ANDROID && mCount > 0  ) {
                    Toast.makeText(getActivity(), "You have already configured android camera.\nPlease take other type.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case CameraInfo.TYPE_USB:
                break;
            case CameraInfo.TYPE_IP_CAM:
            case CameraInfo.TYPE_IP_WIFI:
                if (TextUtils.isEmpty(tempCamera.url)) {
                    Toast.makeText(getActivity(), "You should specify the url of camera", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case CameraInfo.TYPE_VIDEO_FILE:
                if (TextUtils.isEmpty(tempCamera.videoPath)) {
                    Toast.makeText(getActivity(), "You should select the video file", Toast.LENGTH_SHORT).show();
                    return false;
                }




                break;
        }
//        try {
//            new File(FileUtils.getVideoFile(this, mCameraInfo)).delete();
//        } catch (Exception e) {
//
//        }
        mCameraInfo = tempCamera;
        return true;
    }

    public void onBrowseClick(View v) {
        PickerFragment fragment = new PickerFragment();

        fragment.mPickListener = this;
        fragment.filters = new String[] {
                ".mp4",
                ".avi",
                ".mov",
                ".wmv",
                ".flv",
                ".mkv",
                ".mpg",
                ".3gp",
                ".webm",
                ".m4v",
        };

        getActivity().getSupportFragmentManager().beginTransaction()
                .add(containerId, fragment)
                .addToBackStack(null)
                .commit();
    }

//    public void onSelectFile(String file) {
//        setTextOfView(R.id.edt_video_path, file);
//    }


    public void setTextOfView(int resId, String text) {
        ((TextView)rootView.findViewById(resId)).setText(text);
    }

    public String getTextOfView(int resId) {
        return ((TextView)rootView.findViewById(resId)).getText().toString();
    }

    public void onBackClick(View v) {
        mCameraSettingFragment.onBackClick(v);
    }


    public void onSaveClick(View v) {
        if ( !retrieveInfo() ) {
            return;
        }
        mCameraSettingFragment.onChangeCamera(mCameraInfo);
        getActivity().getSupportFragmentManager().popBackStack();

    }

    public void setVisible(int resId, boolean visible) {
        rootView.findViewById(resId).setVisibility(visible ? View.VISIBLE: View.GONE);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mCameraInfo.type = i;
        setVisible(R.id.layout_url, false);
        setVisible(R.id.layout_username, false);
        setVisible(R.id.layout_password, false);
        setVisible(R.id.layout_video, false);
        setVisible(R.id.layout_phone, false);

        switch (i) {
            case CameraInfo.TYPE_ANDROID:
                setVisible(R.id.layout_phone, true);
                break;
            case CameraInfo.TYPE_EXTERNAL_SCREEN:
            case CameraInfo.TYPE_DRONE_WIFI:
            case CameraInfo.TYPE_USB:
                break;
            case CameraInfo.TYPE_IP_CAM:
            case CameraInfo.TYPE_IP_WIFI:
                setVisible(R.id.layout_url, true);
                setVisible(R.id.layout_username, true);
                setVisible(R.id.layout_password, true);
                break;
            case CameraInfo.TYPE_VIDEO_FILE:
                setVisible(R.id.layout_video, true);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onPick(String path, boolean isDir) {
        if (!isDir ) {
            setTextOfView(R.id.edt_video_path, path);
        }
    }
}
