package com.facecool.cameramanager.camera.media;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StreamViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StreamViewFragment extends BaseFragment implements TextureView.SurfaceTextureListener, ViewTreeObserver.OnGlobalLayoutListener {
    protected LibVLC libVlc;
    protected MediaPlayer mediaPlayer;
    protected TextureView textureView;

    protected TextView textFPS;
    private long prevTimeCaptured = 0;
    private long timerCounter = 0;
    private int fpsVideo = 0;

    public StreamViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        libVlc = new LibVLC(this.getContext());
        mediaPlayer = new MediaPlayer(libVlc);
    }

    public void attachViewSurface()
    {
        if (mediaPlayer.getVLCVout().areViewsAttached()) {
            mediaPlayer.getVLCVout().detachViews();
        }
        if (!mediaPlayer.getVLCVout().areViewsAttached())
        {
            mediaPlayer.getVLCVout().setVideoView(textureView);
            mediaPlayer.getVLCVout().setWindowSize(textureView.getWidth(), textureView.getHeight());
            mediaPlayer.getVLCVout().attachViews();
            textureView.setKeepScreenOn(true);
            textureView.setSurfaceTextureListener(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void playStream()
    {
        attachViewSurface();
        if (getCameraInfo().url == "") {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            return;
        }
        Media media = new Media(libVlc, Uri.parse(getCameraInfo().url));
        media.setHWDecoderEnabled(true, false);
        media.addOption(":network-caching=600");
        mediaPlayer.setMedia(media);
        media.release();
        mediaPlayer.play();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_stream_view, container, false);
        textureView = rootView.findViewById(R.id.textureView_Stream);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        textFPS = rootView.findViewById(R.id.tv_stream_fps);
        TextView textCamera = rootView.findViewById(R.id.tv_stream_camera_name);
        textCamera.setText("Camera " + (getCameraInfo().index + 1));
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
        playStream();
    }
}