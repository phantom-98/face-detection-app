package com.facecool.cameramanager.camera.media;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.facecool.cameramanager.R;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.Calendar;

import kotlin.time.DurationUnit;


public class VideoViewFragment extends BaseFragment implements TextureView.SurfaceTextureListener, ViewTreeObserver.OnGlobalLayoutListener {

    protected LibVLC libVlc;
    protected MediaPlayer mediaPlayer;
    protected TextureView textureView;
    protected TextView textFPS;
    private long prevTimeCaptured = 0;
    private long timerCounter = 0;
    private int fpsVideo = 0;
    public VideoViewFragment() {
        // Required empty public constructor
    }

    public void attachViewSurface()
    {
        if (mediaPlayer.getVLCVout().areViewsAttached()) {
            mediaPlayer.getVLCVout().detachViews();
        }
        mediaPlayer.getVLCVout().setVideoView(textureView);
        mediaPlayer.getVLCVout().setWindowSize(textureView.getWidth(), textureView.getHeight());
        mediaPlayer.getVLCVout().attachViews();
        textureView.setKeepScreenOn(true);
        textureView.setSurfaceTextureListener(this);
    }

    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume()
    {
        super.onResume();
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {

        }
    }

    public void playVideo()
    {
        attachViewSurface();
        Uri uri = Uri.parse(getCameraInfo().videoPath);
        ContentResolver resolver = getActivity().getApplicationContext().getContentResolver();
        String readOnlyMode = "r";
        try {
            ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, readOnlyMode);
            Media media = new Media(libVlc, pfd.getFileDescriptor());
            media.setHWDecoderEnabled(true, false);
            media.addOption(":network-caching=600");

            mediaPlayer.setMedia(media);
            media.release();

            mediaPlayer.play();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        libVlc = new LibVLC(this.getContext());
        mediaPlayer = new MediaPlayer(libVlc);

        int permission = getActivity().checkSelfPermission(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            getActivity().requestPermissions(
                    new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.MANAGE_EXTERNAL_STORAGE},
                    1);
        }
    }

    private void onClickTexture(View view) {
        playVideo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_video_view, container, false);
        textureView = rootView.findViewById(R.id.textureView_Video);
        textureView.setOnClickListener(this::onClickTexture);
        textFPS = rootView.findViewById(R.id.tv_video_fps);
        TextView textCamera = rootView.findViewById(R.id.tv_video_camera_name);
        textCamera.setText("Camera " + (getCameraInfo().index + 1));
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        return rootView;
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        long curTime = Calendar.getInstance().getTimeInMillis();
        if (prevTimeCaptured > 0) {
            long latency = curTime - prevTimeCaptured;
            timerCounter += latency;
            fpsVideo += 1;
            if (timerCounter >= 1000) {
                textFPS.setText(String.valueOf(fpsVideo) + " fps");
                fpsVideo = 0;
                timerCounter = 0;
            }
        }
        prevTimeCaptured = curTime;
    }

    @Override
    public void onGlobalLayout() {

    }
}