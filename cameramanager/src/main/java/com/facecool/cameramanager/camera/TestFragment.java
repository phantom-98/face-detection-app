package com.facecool.cameramanager.camera;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.facecool.attendance.Constants;
import com.facecool.attendance.facedetector.FaceData;
import com.facecool.cameramanager.R;
import com.facecool.cameramanager.entity.CameraInfo;
import com.facecool.cameramanager.vlc.VLCCameraSource;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestFragment extends Fragment {

    private static final String url = "rtsp://10.0.2.15:8080/h264_ulaw.sdp";
    private LibVLC libVlc;
    private MediaPlayer mediaPlayer;
    private VLCVideoLayout videoLayout;

    public TestFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mRootView = inflater.inflate(R.layout.fragment_test, container, false);

        libVlc = new LibVLC(getActivity());
        mediaPlayer = new MediaPlayer(libVlc);
        mediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_4_3);
        videoLayout = mRootView.findViewById(R.id.videoLayout);

        return mRootView;
    }
    @Override
    public void onStart()
    {
        super.onStart();

        mediaPlayer.attachViews(videoLayout, null, false, false);

        Media media = new Media(libVlc, Uri.parse(url));
        media.setHWDecoderEnabled(true, false);
        media.addOption(":network-caching=600");

        mediaPlayer.setMedia(media);
        media.release();
        mediaPlayer.play();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        mediaPlayer.stop();
        mediaPlayer.detachViews();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mediaPlayer.release();
        libVlc.release();
    }
}
