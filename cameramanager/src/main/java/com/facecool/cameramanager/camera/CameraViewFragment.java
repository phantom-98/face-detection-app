package com.facecool.cameramanager.camera;

import static com.facecool.cameramanager.entity.CameraInfo.RESOLUTION_1080p;
import static com.facecool.cameramanager.entity.CameraInfo.RESOLUTION_480p;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.viewmodel.CreationExtras;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import com.facecool.attendance.Constants;
import com.facecool.attendance.facedetector.*;
import com.facecool.cameramanager.CameraManagerHelper;
import com.facecool.cameramanager.entity.CameraInfo;
import com.facecool.cameramanager.R;
import com.facecool.cameramanager.exoplayer.ExoPlayerCameraSource;
import com.facecool.cameramanager.utils.TestStateHelper;
import com.facecool.cameramanager.picker.PickerFragment;
import com.facecool.cameramanager.settings.CameraSettingsDialog;
import com.facecool.cameramanager.vlc.VLCCameraSource;
import com.facecool.cameramanager.camera.CameraSource;
import com.facecool.cameramanager.camera.GraphicOverlay;
import com.facecool.cameramanager.camera.VisionImageProcessor;
import com.facecool.cameramanager.camera.AndroidCameraSource;
import com.jiangdg.ausbc.MultiCameraClient;
import com.jiangdg.ausbc.base.CameraFragment;
import com.jiangdg.ausbc.callback.ICameraStateCallBack;
import com.jiangdg.ausbc.callback.IPreviewDataCallBack;
import com.jiangdg.ausbc.camera.CameraUVC;
import com.jiangdg.ausbc.camera.bean.CameraRequest;
import com.jiangdg.ausbc.camera.bean.PreviewSize;
import com.jiangdg.ausbc.render.env.RotateType;
import com.jiangdg.ausbc.utils.bus.BusKey;
import com.jiangdg.ausbc.utils.bus.EventBus;
import com.jiangdg.ausbc.widget.IAspectRatio;

import org.videolan.libvlc.util.VLCVideoLayout;

public class CameraViewFragment extends CameraFragment {

    private static final int PERMISSION_REQUESTS = 1;
    private final static String FILE_EXTENSION_SEPARATOR = ".";
    public int containerId;
    public Runnable onSave;


    private View mRootView = null;
    Context mContext;
    public CameraSource cameraSource = null;
    //SERDAR private CameraSourcePreview preview;
    private VLCVideoLayout preview;

    private TextureView videoPreview;
    private GraphicOverlay graphicOverlay;
    private FrameLayout externalScreenContainer;
    private CustomAspectRatioTextureView aspectRatioTextureView;
    public VisionImageProcessor frameProcessorForExternalScreen;
    public FrameProcessingRunnableForExternalScreen processingRunnableForExternalScreen;
    public Thread processingThreadForExternalScreen;
    protected static final String TAG = "MIDemoApp:CameraSource";
    public final Object processorLockForExternalScreen = new Object();

    private ImageView resumeButton;

    private MediaController mediaController;

    TextView tvCameraName;
    TextView tvFPS;

    public boolean showCameraName;

    public  FaceData _faceData;
    private VisionImageProcessor _faceDetectProcessor;

    public CameraInfo mCameraInfo;
    private CameraSourcePreview cameraSourcePreview;

    private LinearLayout pauseContainer;
    public int currentOrientation;
    public int currentMode = 0;
    private boolean updateResolution = true;
    private PreviewSize standardResolution = new PreviewSize(720, 480);
    private PreviewSize hdResolution = new PreviewSize(1280, 720);
    private PreviewSize fullHdResolution = new PreviewSize(1920, 1080);

    private ImageView settingsIcon;

    public ImageView toggleCameraIcon;
    private EventBus.BusLiveData<Integer> liveData;

    public CameraViewFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //Replaces OnCreateView
    @Nullable
    @Override
    protected View getRootView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {

        if(toggleCameraIcon!=null)
            toggleCameraIcon.setVisibility(View.GONE);
        // Inflate the layout for this fragment
        if(mCameraInfo!=null&&mCameraInfo.type==CameraInfo.TYPE_ANDROID) {
            mRootView = layoutInflater.inflate(R.layout.fragment_android_camera_view, viewGroup, false);
            this.cameraSourcePreview=mRootView.findViewById(R.id.android_camera_source_preview);
            if(cameraSourcePreview==null)
                Constants.LogDebug("Camera "+mCameraInfo.index+" Preview is null");
            this.resumeButton=mRootView.findViewById(R.id.pauseButton);
            if ( showCameraName ) {
                toggleCameraIcon = mRootView.findViewById(R.id.toogle_camera);
            }

            if(toggleCameraIcon!=null) {
                toggleCameraIcon.setVisibility(View.VISIBLE);
                toggleCameraIcon.setOnClickListener(view -> toggleCamera());
            }

        } else {
            mRootView = layoutInflater.inflate(R.layout.fragment_video_camera_view, viewGroup, false);
            videoPreview = mRootView.findViewById(R.id.video_camera_source_preview);
//            this.mediaController=mRootView.findViewById(R.id.mediaController);
//            this.resumeButton=mRootView.findViewById(R.id.pauseButton);
            if(videoPreview==null)
                Constants.LogDebug("Camera "+mCameraInfo.index+" Preview is null");
            if ( showCameraName ) {
                toggleCameraIcon = mRootView.findViewById(R.id.toogle_camera);
            }
            if(toggleCameraIcon!=null)
                toggleCameraIcon.setVisibility(View.GONE);
        }
        this.resumeButton=mRootView.findViewById(R.id.pauseButton);
        this.pauseContainer=mRootView.findViewById(R.id.pauseContainer);
        this.settingsIcon=mRootView.findViewById(R.id.settings_action);

//        if (preview == null) { was for vlcvideolayout preview
//            Constants.LogDebug("Preview is null");
//        }

        currentOrientation = this.getResources().getConfiguration().orientation;

        graphicOverlay = mRootView.findViewById(R.id.graphics_overlay);
        if (graphicOverlay == null) {
            Constants.LogDebug("graphicOverlay is null");
        }

        externalScreenContainer = mRootView.findViewById(R.id.external_screen_container);
        externalScreenContainer.setVisibility(View.GONE);

        tvCameraName = mRootView.findViewById(R.id.tv_camera_name);
        tvFPS = mRootView.findViewById(R.id.tv_fps);

        if ( !showCameraName ) {
            if(tvCameraName!=null)
                tvCameraName.setVisibility(View.GONE);
            if(settingsIcon!=null)
                settingsIcon.setVisibility(View.GONE);
        }
        else {
            if ((settingsIcon!=null))
                configureSettings(settingsIcon);
        }


        if (!allPermissionsGranted())
            getRuntimePermissions();

        createCameraSource();//selectedModel)

        return mRootView.getRootView();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startCameraSource();
    }

    private void configureSettings(ImageView settingsIcon) {
        settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettings();
            }
        });
    }



    private void createCameraSource(){//String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null && mCameraInfo != null ) {
            if ( mCameraInfo.type == CameraInfo.TYPE_ANDROID) {
                cameraSource = new AndroidCameraSource(mContext, graphicOverlay, tvFPS, mCameraInfo);
                cameraSource.pauseButton=resumeButton;
                cameraSource.pauseContainer=pauseContainer;
                cameraSource.handlePause();
                tvCameraName.setText("Cam "+(mCameraInfo.index+1));
            } else if (mCameraInfo.type == CameraInfo.TYPE_EXTERNAL_SCREEN||mCameraInfo.type == CameraInfo.TYPE_USB) {
                tvCameraName.setText("External screen");
                if(mCameraInfo.type==CameraInfo.TYPE_USB)
                    tvCameraName.setText("Usb Camera");
                tvFPS.setText("Waiting for connection");
                aspectRatioTextureView = new CustomAspectRatioTextureView(requireContext());
            } else if (mCameraInfo.type == CameraInfo.TYPE_DRONE_WIFI || mCameraInfo.type == CameraInfo.TYPE_IP_WIFI || mCameraInfo.type == CameraInfo.TYPE_IP_CAM || mCameraInfo.type == CameraInfo.TYPE_VIDEO_FILE) {
                tvCameraName.setText("Cam "+(mCameraInfo.index+1));
                cameraSource = new ExoPlayerCameraSource(mContext, graphicOverlay, tvFPS, mCameraInfo, this);
//                cameraSource.mediaController=mediaController;
                cameraSource.pauseButton=resumeButton;
                cameraSource.pauseContainer=pauseContainer;
                cameraSource.handlePause();
            } else {
                tvCameraName.setText("Cam "+(mCameraInfo.index+1));
                cameraSource = new VLCCameraSource(mContext, graphicOverlay, tvFPS, mCameraInfo, this);
//                cameraSource.mediaController=mediaController;
                cameraSource.pauseButton=resumeButton;
                cameraSource.pauseContainer=pauseContainer;
                cameraSource.handlePause();
            }

        }

        if ( cameraSource == null && mCameraInfo != null && mCameraInfo.type != CameraInfo.TYPE_EXTERNAL_SCREEN&&mCameraInfo.type!=CameraInfo.TYPE_USB) {
            tvFPS.setText("No Camera Source");
        }
        setVisionDetector();
    }

    public void setFaceDetectProcessor(VisionImageProcessor processor) {
        try {
            processor.createFaceDetector(getContext());
        }catch (Exception es)
        {
            es.printStackTrace();
//            Toast.makeText(getContext(), "Unable to create detector ", Toast.LENGTH_SHORT).show();
        }
        _faceDetectProcessor = processor;
    }

    private void setVisionDetector(){
        try {
            if ( cameraSource != null ) {
                Constants.LogDebug("Using Face Detector Processor");
                cameraSource.setMachineLearningFrameProcessor(_faceDetectProcessor);
            } else if (mCameraInfo.type == CameraInfo.TYPE_EXTERNAL_SCREEN || mCameraInfo.type == CameraInfo.TYPE_USB) {
                synchronized (processorLockForExternalScreen) {
                    graphicOverlay.clear();
                    if (frameProcessorForExternalScreen != null && frameProcessorForExternalScreen != _faceDetectProcessor) {
                        frameProcessorForExternalScreen.stop();
                    }
                    frameProcessorForExternalScreen = _faceDetectProcessor;
                }
            }
        } catch (RuntimeException e) {
            Constants.LogDebug("Can not create face processor. Error : " + e.getMessage());
//            Toast.makeText( mContext.getApplicationContext(),
//                    "Can not create image processor: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startCameraSource() {
        if (cameraSource != null) {
//SERDAR            try {
//                if (preview == null) {
//                    Constants.LogDebug("resume: Preview is null");
//                }
//                if (graphicOverlay == null) {
//                    Constants.LogDebug("resume: graphOverlay is null");
//                }
//                //SERDAR preview.start(cameraSource, graphicOverlay);
            //cameraSource.createCameraAndStartPreview(preview,)
//            } catch (IOException e) {
//                Constants.LogDebug("Unable to start camera source. Error : " + e.getMessage());
//                cameraSource.release();
//                cameraSource = null;
//            }
            if(cameraSource.cameraInfo.type==CameraInfo.TYPE_ANDROID) {
                try{
                    if(cameraSourcePreview == null)
                    {
                        Constants.LogDebug("resume: Preview is null");
                    }
                    if (graphicOverlay == null) {
                        Constants.LogDebug("resume: graphOverlay is null");
                    }
                    cameraSourcePreview.start(cameraSource,graphicOverlay);
                    tvFPS.setTextColor(Color.parseColor("#FFFFFF"));
                }catch (Exception ew)
                {
                    ew.printStackTrace();
                    Constants.LogDebug("Unable to start camera source. Error : " + ew.getMessage());
                    cameraSource.release();
                    cameraSource = null;
                    tvFPS.setText("Unable To Start Camera");
                    tvFPS.setTextColor(Color.RED);
                }
            } else {
                try {
                    cameraSource.createCameraAndStartPreview(videoPreview);
                } catch (Exception e) {
                    e.printStackTrace();
                    Constants.LogDebug("Unable to start Vlc Source : "+e);
                }
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constants.LogDebug("onResume");
        if (cameraSource!=null&&cameraSource.cameraInfo!=null&&cameraSource.cameraInfo.type == CameraInfo.TYPE_ANDROID) {
            if (isAdded()) {
                startCameraSource();
            }
        }
    }

    /** Stops the camera. */
    @Override
    public void onPause() {
        super.onPause();
        if (cameraSource!=null&&cameraSource.cameraInfo!=null&&cameraSource.cameraInfo.type == CameraInfo.TYPE_ANDROID) {
            cameraSource.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //SERDAR preview.stop();
        stopMedia();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopMedia();
    }

    private void stopMedia() {
        if (cameraSource != null) {
            try{
                cameraSource.stop();
            }catch (Exception es)
            {
                es.printStackTrace();
            }
            cameraSource.release();
        }

        if(frameProcessorForExternalScreen!=null)
            frameProcessorForExternalScreen.stop();
        Log.d("Triggered Stop","stopped on CameraViewFragment");
    }


    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    mContext.getPackageManager()
                            .getPackageInfo(mContext.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(mContext, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(mContext, permission)) {
                allNeededPermissions.add(permission);
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            requestPermissions(
                    allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Constants.LogDebug("Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource();//selectedModel);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Constants.LogDebug("Permission granted: " + permission);
            return true;
        }
        Constants.LogDebug("Permission NOT granted: " + permission);
        return false;
    }

    @NonNull
    @Override
    public CreationExtras getDefaultViewModelCreationExtras() {
        return super.getDefaultViewModelCreationExtras();
    }

    public void setCameraInfo(CameraInfo cameraInfo) {
        this.mCameraInfo = cameraInfo;
    }

    @Override
    protected void initView() {
        if (mCameraInfo != null && (mCameraInfo.type == CameraInfo.TYPE_EXTERNAL_SCREEN || mCameraInfo.type == CameraInfo.TYPE_USB)) {
            //preview.setVisibility(View.GONE);
            //graphicOverlay.setVisibility(View.GONE);
            externalScreenContainer.setVisibility(View.VISIBLE);
            super.initView();
        }
    }

    @Nullable
    @Override
    protected IAspectRatio getCameraView() {
        return aspectRatioTextureView;
    }

    @Nullable
    @Override
    protected ViewGroup getCameraViewContainer() {
        return externalScreenContainer;
    }

    @Override
    public void onCameraState(@NonNull MultiCameraClient.ICamera iCamera, @NonNull State state, @Nullable String s) {
        switch (state) {
            case OPENED:
                handleCameraOpened();
                break;
            case CLOSED:
                handleCameraClosed();
                break;
            case ERROR:
                handleCameraError(s);
                break;
        }
    }

    private void handleCameraError(String msg) {
        Constants.LogInfo("camera opened error: " + msg);
        tvFPS.setText("Error");
    }

    private void handleCameraClosed() {
        Constants.LogInfo("camera closed success");
        tvFPS.setText("Disconnected");
        if (processingRunnableForExternalScreen != null) {
            processingRunnableForExternalScreen.setActive(false);
        }
        if (processingThreadForExternalScreen != null) {
            try {
                processingThreadForExternalScreen.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "Frame processing thread interrupted on release.");
            }
        }
        if (liveData != null) {
            liveData.removeObservers(this);
        }
        processingThreadForExternalScreen = null;
        graphicOverlay.clear();
    }

    private void handleCameraOpened() {
        Constants.LogInfo("camera opened success");
        if (frameProcessorForExternalScreen == null) {
            _faceDetectProcessor.createFaceDetector(mRootView.getRootView().getContext());
            setVisionDetector();
        }
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            tvFPS.setText("Connected (" + getCameraRequest().getPreviewWidth() + "p)");
        } else {
            tvFPS.setText("Connected (" + getCameraRequest().getPreviewHeight() + "p)");
        }
        processingRunnableForExternalScreen = new ExternalScreenFrameProcessingRunnable(aspectRatioTextureView);
        processingThreadForExternalScreen = new Thread(processingRunnableForExternalScreen);
        processingRunnableForExternalScreen.setActive(true);
        setProcessorViewData();
        Objects.requireNonNull(getCurrentCamera()).addPreviewDataCallBack((bytes, width, height, dataFormat) -> {
            int localDataFormat;
            if (dataFormat == IPreviewDataCallBack.DataFormat.NV21) {
                localDataFormat = ImageFormat.NV21;
            } else {
                localDataFormat = ImageFormat.FLEX_RGBA_8888;
            }
            processingRunnableForExternalScreen.setNextFrame(bytes, width, height, localDataFormat);
        });
        processingThreadForExternalScreen.start();
        liveData = EventBus.INSTANCE.with(BusKey.KEY_FRAME_RATE);
        liveData.observe(this, (Observer<Integer>) integer -> tvFPS.setText("Connected (" + getCameraRequest().getPreviewHeight() + "p, " + integer + " fps)"));
    }

    @NonNull
    @Override
    protected CameraRequest getCameraRequest() {
        int width;
        int height;
        RotateType rotateType;
        PreviewSize selectedPreviewSize;
        switch(mCameraInfo.resolution) {
            case RESOLUTION_480p:
                selectedPreviewSize = standardResolution;
                break;
            case RESOLUTION_1080p:
                selectedPreviewSize = fullHdResolution;
                break;
            default:
                selectedPreviewSize = hdResolution;
                break;
        }
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            width = selectedPreviewSize.getHeight();
            height = selectedPreviewSize.getWidth();
        } else {
            width = selectedPreviewSize.getWidth();
            height = selectedPreviewSize.getHeight();
        }
        rotateType = RotateType.ANGLE_0;
        return new CameraRequest.Builder()
                .setPreviewWidth(width)
                .setPreviewHeight(height)
                .setRenderMode(CameraRequest.RenderMode.OPENGL)
                .setRawPreviewData(true)
                .setDefaultRotateType(rotateType)
                .setAudioSource(CameraRequest.AudioSource.NONE)
                .create();
    }

    void setProcessorViewData() {
        float widthScale = (float) aspectRatioTextureView.getWidth() / getCameraRequest().getPreviewWidth();
        float heightScale = (float) aspectRatioTextureView.getHeight() / getCameraRequest().getPreviewHeight();

        // Choose the smaller scale factor to fit the view within the parent
        float scale = Math.max(widthScale, heightScale);
        frameProcessorForExternalScreen.setScale(scale);
        frameProcessorForExternalScreen.setViewWidth(aspectRatioTextureView.getWidth());
        frameProcessorForExternalScreen.setViewHeight(aspectRatioTextureView.getHeight());
        frameProcessorForExternalScreen.setExternalScreen(true);
        frameProcessorForExternalScreen.setCurrentOrientation(currentOrientation);
        frameProcessorForExternalScreen.setCurrentMode(currentMode);
    }

    public class ExternalScreenFrameProcessingRunnable extends FrameProcessingRunnableForExternalScreen {

        // This lock guards all of the member variables below.
        public final Object lock = new Object();
        public boolean active = true;

        // These pending variables hold the state associated with the new frame awaiting processing.
        public ByteBuffer pendingFrameData;
        public int pendingFrameWidth;
        public int pendingFrameHeight;
        public int pendingFrameRotation;
        public CustomAspectRatioTextureView txView;

        ExternalScreenFrameProcessingRunnable(CustomAspectRatioTextureView txView) {
            this.txView = txView;
        }

        /**
         * Marks the runnable as active/not active. Signals any blocked threads to continue.
         */
        public void setActive(boolean active) {
            synchronized (lock) {
                this.active = active;
                lock.notifyAll();
            }
        }

        /**
         * Sets the frame data received from the camera. This adds the previous unused frame buffer (if
         * present) back to the camera, and keeps a pending reference to the frame data for future use.
         */
        @SuppressWarnings("ByteBufferBackingArray")
        public void setNextFrame(byte[] frame, int width, int height, int imageFormat) {
            ByteBuffer buffer = ByteBuffer.wrap(frame);
            if (!buffer.hasArray()) {
                // I don't think that this will ever happen.  But if it does, then we wouldn't be
                // passing the preview content to the underlying detector later.
                throw new IllegalStateException("Failed to create valid buffer for camera source.");
            }

            synchronized (lock) {
                pendingFrameData = buffer;
                pendingFrameWidth = width;
                pendingFrameHeight = height;
                pendingFrameRotation = 0;

                // Notify the processor thread if it is waiting on the next frame (see below).
                lock.notifyAll();
            }
            TestStateHelper.onTestStateChange(TestStateHelper.STATE_FRAME_DATA, pendingFrameData);
        }

        /**
         * As long as the processing thread is active, this executes detection on frames continuously.
         * The next pending frame is either immediately available or hasn't been received yet. Once it
         * is available, we transfer the frame info to local variables and run detection on that frame.
         * It immediately loops back for the next frame without pausing.
         *
         * <p>If detection takes longer than the time in between new frames from the camera, this will
         * mean that this loop will run without ever waiting on a frame, avoiding any context switching
         * or frame acquisition time latency.
         *
         * <p>If you find that this is using more CPU than you'd like, you should probably decrease the
         * FPS setting above to allow for some idle time in between frames.
         */
        @SuppressLint("InlinedApi")
        @SuppressWarnings({"GuardedBy", "ByteBufferBackingArray"})
        @Override
        public void run() {
            ByteBuffer data;
            int width;
            int height;
            int rotation;

            while (true) {
                synchronized (lock) {
                    while (active && (pendingFrameData == null)) {
                        try {
                            // Wait for the next frame to be received from the camera, since we
                            // don't have it yet.
                            lock.wait();
                        } catch (InterruptedException e) {
                            Log.d(TAG, "Frame processing loop terminated.", e);
                            return;
                        }
                    }

                    if (!active) {
                        // Exit the loop once this camera source is stopped or released.  We check
                        // this here, immediately after the wait() above, to handle the case where
                        // setActive(false) had been called, triggering the termination of this
                        // loop.
                        return;
                    }

                    // Hold onto the frame data locally, so that we can use this for detection
                    // below.  We need to clear pendingFrameData to ensure that this buffer isn't
                    // recycled back to the camera before we are done using that data.
                    data = pendingFrameData;
                    width = pendingFrameWidth;
                    height = pendingFrameHeight;
                    rotation = pendingFrameRotation;
                    pendingFrameData = null;
                }

                // The code below needs to run outside of synchronization,b  because this will allow
                // the camera to add pending frame(s) while we are running detection on the current
                // frame.

                try {
                    synchronized (processorLockForExternalScreen) {
                        frameProcessorForExternalScreen.processByteBuffer(
                                data,
                                new FrameMetadata.Builder()
                                        .setWidth(width)
                                        .setHeight(height)
                                        .setRotation(rotation)
                                        .build(),
                                graphicOverlay);
                    }
                } catch (Exception t) {
                    Log.e(TAG, "Exception thrown from receiver.", t);
                }
            }
        }
    }

    public void openSettings() {

        CameraSettingsDialog cameraSettingsDialog=new CameraSettingsDialog(this);
        cameraSettingsDialog.setInitialCamerasValues(mCameraInfo);
        cameraSettingsDialog.show();

    }

    public void saveSettings() {
        try{
            if (cameraSource != null) {
                cameraSource.stop();
                cameraSource.release();
            }
            onDestroy();
        }catch (Exception es)
        {

        }
        Toast.makeText(mContext, "Updating Settings ... ", Toast.LENGTH_SHORT).show();
        if(onSave!=null) {
            onSave.run();
        }
    }

    private void toggleCamera() {
        if(mCameraInfo.type==CameraInfo.TYPE_ANDROID)
        {
            float fromRotation = 0f;
            float toRotation = 360f;
            //start of rotate animation
            RotateAnimation rotateAnimation = new RotateAnimation(fromRotation, toRotation,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(2000);
            toggleCameraIcon.post(()->{
            toggleCameraIcon.startAnimation(rotateAnimation);
            });
            //end of rotate animation
            mCameraInfo.isFront=!mCameraInfo.isFront;
            if(onSave!=null) {
                onSave.run();
            }
        }
    }
}
