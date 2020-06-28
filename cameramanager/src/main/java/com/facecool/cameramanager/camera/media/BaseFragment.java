package com.facecool.cameramanager.camera.media;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.facecool.cameramanager.R;
import com.facecool.cameramanager.camera.VisionImageProcessor;
import com.facecool.cameramanager.entity.CameraInfo;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BaseFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public abstract class BaseFragment extends Fragment {

    public boolean showCameraName;

    public VisionImageProcessor _faceDetectProcessor;

    protected CameraInfo mCameraInfo;

    public CameraInfo getCameraInfo() {
        return mCameraInfo;
    }

    public void setCameraInfo(CameraInfo mCameraInfo) {
        this.mCameraInfo = mCameraInfo;
    }

    public VisionImageProcessor getFaceDetectProcessor() {
        return _faceDetectProcessor;
    }

    public void setFaceDetectProcessor(VisionImageProcessor _faceDetectProcessor) {
        this._faceDetectProcessor = _faceDetectProcessor;
    }


    public boolean isShowCameraName() {
        return showCameraName;
    }

    public void setShowCameraName(boolean showCameraName) {
        this.showCameraName = showCameraName;
    }


    public BaseFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media_base, container, false);
    }
}