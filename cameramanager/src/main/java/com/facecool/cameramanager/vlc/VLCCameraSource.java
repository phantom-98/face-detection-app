/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facecool.cameramanager.vlc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;

import com.arthenica.ffmpegkit.AsyncFFmpegExecuteTask;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.FFmpegSessionCompleteCallback;
import com.arthenica.ffmpegkit.LogCallback;
import com.arthenica.ffmpegkit.Statistics;
import com.arthenica.ffmpegkit.StatisticsCallback;
import com.facecool.attendance.CallRateMeter;
import com.facecool.attendance.Constants;
import com.facecool.cameramanager.camera.CameraSource;
import com.facecool.cameramanager.camera.FrameProcessingRunnable;
import com.facecool.cameramanager.camera.GraphicOverlay;
import com.facecool.cameramanager.entity.CameraInfo;
import com.facecool.cameramanager.utils.TestStateHelper;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.tasks.Task;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IVLCVout;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages the camera and allows UI updates on top of it (e.g. overlaying extra Graphics or
 * displaying extra information). This receives preview frames from the camera at a specified rate,
 * sending those frames to child classes' detectors / classifiers as fast as it is able to process.
 */
@SuppressWarnings("ALL")
public class VLCCameraSource extends CameraSource implements IVLCVout.Callback {

    LibVLC libVLC;
    MediaPlayer mediaPlayer;
    public boolean isCapturing = false;
    FileInputStream fis = null;
    String url = "";
    private TextureView textureView;
    FrameCapture frameCapture;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private long startTime = -1;
    private int frameCount = 0;
    private float fps = 0;

    private String rtmpInput = "rtmp://0.0.0.0:8889/drone/input";
    private String rtmpOutput = "rtmp://0.0.0.0:1935/drone/output";
    private String rtmpCommand = "-listen 1 -i " + rtmpInput + " -c copy -f flv -listen 1 " + rtmpOutput;
    private Fragment currentFragment;
    private boolean surfaceSet = false;
    FFmpegSession session;
    private Boolean endReached=false;

    public VLCCameraSource(Context context, GraphicOverlay overlay, TextView tvFPS, CameraInfo cameraInfo, Fragment currentFragment) {
        super(context, overlay, tvFPS, cameraInfo);
        this.processingRunnable = new VLCFrameProcessingRunnable();
        this.currentFragment = currentFragment;
    }


    @Override
    protected boolean isCameraCreated() {
        return isCapturing;
    }

    @Override
    @RequiresPermission(Manifest.permission.CAMERA)
    protected void createCameraAndStartPreview(SurfaceTexture texture) throws IOException {


    }

    private void processFrame() {
//        CallRateMeter.measureCallRate();
        executorService.submit(() -> {
            Bitmap bitmap = textureView.getBitmap();
            previewSize = new com.google.android.gms.common.images.Size(textureView.getWidth(), textureView.getHeight());
            frameProcessor.setViewWidth(textureView.getWidth());
            frameProcessor.setViewHeight(textureView.getHeight());
            frameProcessor.setScale(1);
            frameProcessor.processBitmapOfStream(bitmap, graphicOverlay); // VisionProcessorBase takes care and sends it to mlkit if mlkit is ready for the next image
        });
    }

    @Override
    @RequiresPermission(Manifest.permission.CAMERA)
    public void createCameraAndStartPreview(TextureView textureView){
        this.textureView=textureView;
        mStartTime = 0;
        mFrameCount = 0;
        fps = 0;
        url = cameraInfo.type == CameraInfo.TYPE_VIDEO_FILE ? cameraInfo.videoPath : cameraInfo.url;

        if (cameraInfo.type != CameraInfo.TYPE_VIDEO_FILE && !TextUtils.isEmpty(cameraInfo.username) && cameraInfo.type != CameraInfo.TYPE_DRONE_WIFI) {
            if ( url.indexOf("://") < 0 ) {
                return;
            }
            int insertPos = url.indexOf("://") + 3;
            url = url.substring(0, insertPos) + cameraInfo.username + ":" + cameraInfo.password + "@" + url.substring(insertPos);
        }

        if (cameraInfo.type == CameraInfo.TYPE_DRONE_WIFI) {
            ArrayList<String> options = new ArrayList<>();
            options.add("--file-caching=2000");
            options.add("--rtsp-tcp");
            libVLC = new LibVLC(context, options);
        } else {
            libVLC = new LibVLC(context);
        }

        mediaPlayer = new MediaPlayer(libVLC);
        mediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_FILL);

        isCapturing = true;
        Media media = null;
        if ( cameraInfo.type == CameraInfo.TYPE_VIDEO_FILE ) {
            try{
                fis = new FileInputStream(cameraInfo.videoPath);
                FileDescriptor fd = fis.getFD();

                media= new Media(libVLC, fd);
            } catch (Exception e){
                e.printStackTrace();
                media = new Media(libVLC, Uri.parse(cameraInfo.videoPath));
            }
            media.setHWDecoderEnabled(true, false);
        } else if (cameraInfo.type == CameraInfo.TYPE_DRONE_WIFI) {
            try {
                runRTMPServer();
            }catch (Exception es)
            {
                tvFPS.setText("Connection Failed");
                es.printStackTrace();
            }
        } else if(cameraInfo.type == CameraInfo.TYPE_USB) {
            //implementation of the usb camera here/. This is just a placeholder
            String usbCameraUrl = "/dev/video0";
            try{
                media= new Media(libVLC, usbCameraUrl);
            } catch (Exception e){
                e.printStackTrace();
                media = new Media(libVLC, Uri.parse(usbCameraUrl));
            }
            media.setHWDecoderEnabled(true, false);
        } else {
            media = new Media(libVLC, Uri.parse(url));
        }

        if (cameraInfo.type != CameraInfo.TYPE_VIDEO_FILE && cameraInfo.type != CameraInfo.TYPE_DRONE_WIFI) {
            media.addOption(":network-caching=600");
        }

        mediaPlayer.setEventListener(event -> {
            if (event.type == MediaPlayer.Event.EncounteredError) {
                tvFPS.setText("Connection Failed");
                tvFPS.setTextColor(Color.RED);
            } else if ( event.type == MediaPlayer.Event.EndReached ) {
                frameCapture.stopCapturing();

                endReached=true;
            } else if ( event.type == MediaPlayer.Event.Buffering ) {
                tvFPS.setText("Connected");
                tvFPS.setTextColor(Color.WHITE);
            } else if ( event.type == MediaPlayer.Event.TimeChanged) {
                IMedia.Stats stats = mediaPlayer.getMedia().getStats();
                fps = increaseFrameCount(stats.displayedPictures);
                if (startTime == -1) {
                    startTime = System.currentTimeMillis();
                }

                frameCount++;
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;

                if (elapsedTime > 0) {
                    float fps = frameCount / (elapsedTime / 1000.0f);
                    Constants.logInfo("VLC fps before mlkit: " + fps, null);
                }
//                processFrame();

            }
        });
        mediaPlayer.setMedia(media);

        if (surfaceSet == true) {
            IVLCVout vout= mediaPlayer.getVLCVout();
            vout.detachViews();
            vout.setVideoView(textureView);
            vout.setWindowSize(textureView.getWidth(),textureView.getHeight());

            vout.attachViews();
            textureView.setKeepScreenOn(true);
            if (cameraInfo.type != CameraInfo.TYPE_DRONE_WIFI) {
                mediaPlayer.play();
                if(cameraInfo.isPaused) {
                    mediaPlayer.pause();
                    pauseButton.setVisibility(View.VISIBLE);
                }
            }
        }

        // if the textureView is ready start video
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                IVLCVout vout= mediaPlayer.getVLCVout();
                vout.detachViews();
                vout.setVideoView(textureView);
                vout.setWindowSize(textureView.getWidth(),textureView.getHeight());

                vout.attachViews();
                textureView.setKeepScreenOn(true);
                if (cameraInfo.type != CameraInfo.TYPE_DRONE_WIFI) {
                    mediaPlayer.play();
                    if(cameraInfo.isPaused) {
                        mediaPlayer.pause();
                        pauseButton.setVisibility(View.VISIBLE);
                        tvFPS.setText("Paused");
                    }
                }
                surfaceSet = true;
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                System.out.println("Surface Changed");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                System.out.println("Surface updated");
            }
        });

        if (cameraInfo.type != CameraInfo.TYPE_DRONE_WIFI) {
            frameCapture = new FrameCapture(textureView);
            frameCapture.startCapturing();
        }
    }

    @Override
    public void handlePause() {
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cameraInfo.isPaused)
                {

                    pauseButton.setVisibility(View.INVISIBLE);
                    try {
                        if(!isMediaPlayerCompleted()){
                            mediaPlayer.play();
                            frameCapture.startCapturing();
                        } else {//restart video
                            Log.d("VLC PLAYER","REstarting");
//                            mediaPlayer.prepare();
//                            mediaPlayer.setTime(0);
//                            mediaPlayer.setPosition(1f);
//                            mediaController.show();
//                            mediaPlayer.setMedia(mediaPlayer.getMedia());
//                            mediaPlayer.play();
//                            frameCapture.startCapturing();
//                            stopCameraPreviewAndRelease();
//                            createCameraAndStartPreview(textureView);
//                            System.out.println("restarting video");
//                            mediaPlayer.setPosition(0.0f);
//                            mediaPlayer.start();
//                            stopCameraPreviewAndRelease();

//                            mediaPlayer.stop();
//                            frameCapture.stopCapturing();
//                            isCapturing=false;
//                            mediaPlayer.release();
//                            libVLC.release();
//                            createCameraAndStartPreview(textureView);

                            fis = new FileInputStream(cameraInfo.videoPath);
                            FileDescriptor fd = fis.getFD();

                            Media media = new Media(libVLC, fd);
                            mediaPlayer.setMedia(media);
                            frameCapture.startCapturing();
                            mediaPlayer.play();


                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    tvFPS.setText("Paused");
                    pauseButton.setVisibility(View.VISIBLE);
                    mediaPlayer.pause();
                    if(frameCapture!=null)
                        frameCapture.stopCapturing();
                }
                cameraInfo.isPaused=!cameraInfo.isPaused;
            }
        };

        pauseButton.setOnClickListener(onClickListener);
        pauseContainer.setOnClickListener(onClickListener);
    }

    // frame capturing happens by using an handler in a runnnable
    // in order to get the bitmap from Textureview you need a loop
    // the loop has the speed of the video fps
    public class FrameCapture {
        private TextureView textureView;
        private Handler handler;
        private Runnable frameCaptureRunnable;
        private final long startTime = System.currentTimeMillis();
        private final int targetFPS = 25;

        public FrameCapture(TextureView textureView) {
            this.textureView = textureView;
            this.handler = new Handler();
            this.frameCaptureRunnable = new Runnable() {
                @Override
                public void run() {
                    captureFrame();
                    int delay = calculateNextFrameDelay(fps);
                    Constants.logInfo("VLC fps: " + fps + " delay: " + delay, "fps-vlc");
                    handler.postDelayed(this, 80);
                }
            };
        }

        public void startCapturing() {
            handler.post(frameCaptureRunnable);
        }

        public void stopCapturing() {
            handler.removeCallbacks(frameCaptureRunnable);
        }

        private void captureFrame() {
            if (textureView.isAvailable()) {
                processFrame();
            }
        }

        private int calculateNextFrameDelay(double currentFPS) {
            if (currentFPS > 0) {
                int currentDelay = (int) (1000.0 / currentFPS);
                return Math.max(currentDelay, 0);
            } else {
                return 1000 / targetFPS;
            }
        }

    }

    private boolean isMediaPlayerCompleted() {
        return endReached;
    }

    @Override
    @RequiresPermission(Manifest.permission.CAMERA)
    protected void createCameraAndStartPreview(SurfaceHolder surfaceHolder, SurfaceView surfaceView) throws IOException {
    }

    @Override
    protected void processExistingPendingBuffer(ByteBuffer pendingFrameData) {
    }

    protected int isPortraitMode() {
        return -1;
    }

    @Override
    protected void stopCameraPreviewAndRelease() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();

            isCapturing = false;
            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.removeCallback(this);
            vout.detachViews();

            mediaPlayer.release();
        }
        if (libVLC != null) {
            libVLC.release();
        }

        if ( fis != null ) {
            try {
                fis.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            fis = null;
        }

        if (frameCapture != null) {
            frameCapture.stopCapturing();
        }
        if (session != null) {
            FFmpegKit.cancel();
        }
    }

    @Override
    protected void onProcessFrameCompleted(ByteBuffer buffer) {

    }

    public void runRTMPServer() {
        Log.i("RTMP", "Starting RTMP server");
        session = FFmpegKit.executeAsync(rtmpCommand, new FFmpegSessionCompleteCallback() {
            @Override
            public void apply(FFmpegSession session) {
                currentFragment.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("RTMP", "RTMP signal finished, restarting server");
                        isCapturing = false;
                        if (mediaPlayer != null) {
                            try {
                                mediaPlayer.stop();
                            }catch (Exception es)
                            {
                                es.printStackTrace();
                            }
                            try {
                                mediaPlayer.detachViews();
                            }catch (Exception es)
                            {
                                es.printStackTrace();
                            }
                        }
                        if (frameCapture != null) {
                            frameCapture.stopCapturing();
                        }
                        graphicOverlay.clear();
                        createCameraAndStartPreview(textureView);
                    }
                });
            }
        }, new LogCallback() {
            @Override
            public void apply(com.arthenica.ffmpegkit.Log log) {
                Activity currectActivity = null;
                if(currentFragment!=null)
                    currectActivity = currentFragment.getActivity();

                if (log.getMessage().contains(rtmpInput)&&currectActivity!=null) {
                    Log.i("RTMP", "Signal received, processing...");
                    currectActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Media media = new Media(libVLC, Uri.parse(rtmpOutput));
                            media.addOption(":network-caching=150");
                            media.addOption(":clock-jitter=0");
                            media.addOption(":clock-synchro=0");
                            mediaPlayer.setEventListener(event -> {
                                if (event.type == MediaPlayer.Event.EncounteredError) {
                                    tvFPS.setText("Connection Failed");
                                    tvFPS.setTextColor(Color.RED);
                                } else if ( event.type == MediaPlayer.Event.EndReached ) {
                                    frameCapture.stopCapturing();
                                } else if ( event.type == MediaPlayer.Event.Buffering ) {
                                    tvFPS.setText("Connected");
                                    tvFPS.setTextColor(Color.WHITE);
                                } else if ( event.type == MediaPlayer.Event.TimeChanged) {
                                    IMedia.Stats stats = mediaPlayer.getMedia().getStats();
                                    fps = increaseFrameCount(stats.displayedPictures);
                                    if (startTime == -1) {
                                        startTime = System.currentTimeMillis();
                                    }

                                    frameCount++;
                                    long currentTime = System.currentTimeMillis();
                                    long elapsedTime = currentTime - startTime;

                                    if (elapsedTime > 0) {
                                        float fps = frameCount / (elapsedTime / 1000.0f);
                                        Constants.logInfo("VLC fps before mlkit: " + fps, null);
                                    }
//                processFrame();
                                }
                            });
                            try {
                                mediaPlayer.setMedia(media);
                                media.release();
                                mediaPlayer.play();
                                frameCapture = new FrameCapture(textureView);
                                frameCapture.startCapturing();
                            }catch (Exception es)
                            {
                                es.printStackTrace();
                            }
                        }
                    });
                }
            }
        }, new StatisticsCallback() {
            @Override
            public void apply(Statistics statistics) {
                //Not needed
            }
        });
        tvFPS.setText("Listening");
    }

    // ==============================================================================================
    // Frame processing
    // ==============================================================================================

    long lastTime = 0;
    long curTime = 0;

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    public class VLCFrameProcessingRunnable extends FrameProcessingRunnable {

        // This lock guards all of the member variables below.
        public final Object lock = new Object();
        public boolean active = true;

        // These pending variables hold the state associated with the new frame awaiting processing.
        public Bitmap pendingBitmap;

        VLCFrameProcessingRunnable() {
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
        public void setNextFrame(Object frame) {

            synchronized (lock) {
                if (pendingBitmap != null)
                    bitmaps.add(pendingBitmap);
                pendingBitmap = (Bitmap) frame;
                lock.notifyAll();
            }

            TestStateHelper.onTestStateChange(TestStateHelper.STATE_FRAME_DATA, pendingBitmap);
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
            Bitmap data;

            while (true) {
                synchronized (lock) {
                    while (active && (pendingBitmap == null)) {
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
                    data = pendingBitmap;
                    pendingBitmap = null;
                }

                // The code below needs to run outside of synchronization, because this will allow
                // the camera to add pending frame(s) while we are running detection on the current
                // frame.

                try {
                    synchronized (processorLock) {
                        frameProcessor.processBitmap(
                                data,
                                graphicOverlay);
                    }
                } catch (Exception t) {
                    Log.e(TAG, "Exception thrown from receiver.", t);
                } finally {
                    bitmaps.add(data);
                }
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
////                    throw new RuntimeException(e);
//                    e.printStackTrace();
//                }
            }
        }
    }

    /**
     * Called when the camera has a new preview frame.
     */
//    private class CameraPreviewCallback implements Camera.PreviewCallback {
//        @Override
//        public void onPreviewFrame(byte[] data, Camera camera) {
//            increaseFrameCount();
//            processingRunnable.setNextFrame(data);
//            if (!bytesToByteBuffer.containsKey(data)) {
//                Log.d(
//                        TAG,
//                        "Skipping frame. Could not find ByteBuffer associated with the image "
//                                + "data from the camera.");
//                return;
//            }
//        }
//    }

}
