package com.facecool.cameramanager.exoplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.TransferListener;
import androidx.media3.datasource.rtmp.RtmpDataSource;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.video.VideoRendererEventListener;

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

import java.io.File;
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
public class ExoPlayerCameraSource extends CameraSource {

    public boolean isCapturing = false;
    FileInputStream fis = null;
    private TextureView textureView;
    ExoPlayerCameraSource.FrameCapture frameCapture;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private long startTime = -1;
    private long lastTimestamp = 0;
    private static final int FPS_UPDATE_INTERVAL_MS = 1000;
    private float fps = 0;
    private Runnable fpsTask;
    private Handler mainThread;
    private FrameRateTracker tracker;
    private String rtmpInput = "rtmp://0.0.0.0:8889/drone/input";
    private String rtmpOutput = "rtmp://0.0.0.0:1935/drone/output";
    private String rtmpCommand = "-listen 1 -i " + rtmpInput + " -c copy -f flv -listen 1 " + rtmpOutput;
    private Fragment currentFragment;
    FFmpegSession session;
    private Boolean endReached=false;
    private ExoPlayer player;
    private int exoPlayerFrameCount = 0;

    private Player.Listener rtmpPlayerListener = new Player.Listener() {
        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            Log.e("RTMP", "Error on ExoPlayer");
            Log.e("RTMP", error.getMessage());
            Log.e("RTMP", error.getLocalizedMessage());
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            if ((playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY) && isCapturing == false) {
                tvFPS.setText("Connected");
                tvFPS.setTextColor(Color.WHITE);
                isCapturing = true;
            } else if (playbackState == Player.STATE_BUFFERING && isCapturing == true){
                Log.i("RTMP", "Buffer finished, restarting server");
                if (player != null) {
                    try {
                        player.stop();
                    }catch (Exception es)
                    {
                        es.printStackTrace();
                    }
                    try {
                        player.removeListener(rtmpPlayerListener);
                        player.release();
                    }catch (Exception es)
                    {
                        es.printStackTrace();
                    }
                }
                if (frameCapture != null) {
                    frameCapture.stopCapturing();
                }
                mainThread.removeCallbacks(fpsTask);
                isCapturing = false;
                graphicOverlay.clear();
                createCameraAndStartPreview(textureView);
            }
        }
    };

    private Player.Listener rtspPlayerListener = new Player.Listener() {
        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            Log.e("RTSP", "Error on ExoPlayer");
            Log.e("RTSP", error.getMessage());
            Log.e("RTSP", error.getLocalizedMessage());
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            if ((playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY) && isCapturing == false) {
                tvFPS.setText("Connected");
                tvFPS.setTextColor(Color.WHITE);
                isCapturing = true;
            } else if (playbackState == Player.STATE_ENDED) {
                Log.i("RTSP", "Buffer finished");
                if (player != null) {
                    try {
                        player.stop();
                    }catch (Exception es)
                    {
                        es.printStackTrace();
                    }
                    try {
                        player.removeListener(rtspPlayerListener);
                        player.release();
                    }catch (Exception es)
                    {
                        es.printStackTrace();
                    }
                }
                if (frameCapture != null) {
                    frameCapture.stopCapturing();
                }
                isCapturing = false;
                mainThread.removeCallbacks(fpsTask);
                graphicOverlay.clear();
                createCameraAndStartPreview(textureView);
            }
        }
    };

    private Player.Listener videoFilePlayerListener = new Player.Listener() {
        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            Log.e("VIDEO FILE", "Error on ExoPlayer");
            Log.e("VIDEO FILE", error.getMessage());
            Log.e("VIDEO FILE", error.getLocalizedMessage());
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            if ((playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY) && isCapturing == false) {
                tvFPS.setText("Playing");
                tvFPS.setTextColor(Color.WHITE);
                isCapturing = true;
            } else if (playbackState == Player.STATE_ENDED) {
                Log.i("VIDEO FILE", "Buffer finished");
                if (player != null) {
                    try {
                        player.stop();
                    }catch (Exception es)
                    {
                        es.printStackTrace();
                    }
                    try {
                        player.removeListener(videoFilePlayerListener);
                        player.release();
                    }catch (Exception es)
                    {
                        es.printStackTrace();
                    }
                }
                if (frameCapture != null) {
                    frameCapture.stopCapturing();
                }
                isCapturing = false;
                mainThread.removeCallbacks(fpsTask);
                graphicOverlay.clear();
                createCameraAndStartPreview(textureView);
            }
        }

        @Override
        public void onVideoSizeChanged(VideoSize videoSize) {
            Log.i("VIDEO FILE", "Video size detected, calculating aspect ratio");
            int viewWidth = textureView.getWidth();
            int viewHeight = textureView.getHeight();
            double aspectRatio = (double) videoSize.height / videoSize.width;

            int newWidth, newHeight;
            if (viewHeight > (int) (viewWidth * aspectRatio)) {
                // limited by narrow width; restrict height
                newWidth = viewWidth;
                newHeight = (int) (viewWidth * aspectRatio);
            } else {
                // limited by short height; restrict width
                newWidth = (int) (viewHeight / aspectRatio);
                newHeight = viewHeight;
            }
            int xoff = (viewWidth - newWidth) / 2;
            int yoff = (viewHeight - newHeight) / 2;

            Matrix txform = new Matrix();
            textureView.getTransform(txform);
            txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
            txform.postTranslate(xoff, yoff);
            textureView.setTransform(txform);
            frameCapture = new com.facecool.cameramanager.exoplayer.ExoPlayerCameraSource.FrameCapture(textureView);
            frameCapture.startCapturing();
            startFPSCounter();
        }
    };

    public ExoPlayerCameraSource(Context context, GraphicOverlay overlay, TextView tvFPS, CameraInfo cameraInfo, Fragment currentFragment) {
        super(context, overlay, tvFPS, cameraInfo);
        this.processingRunnable = new com.facecool.cameramanager.exoplayer.ExoPlayerCameraSource.ExoPlayerFrameProcessingRunnable();
        this.currentFragment = currentFragment;
        this.mainThread = new Handler(Looper.getMainLooper());
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
        executorService.submit(() -> {
            Bitmap bitmap = textureView.getBitmap();
            if (cameraInfo.type == CameraInfo.TYPE_VIDEO_FILE) {
                Matrix matrix = new Matrix();
                textureView.getTransform(matrix);
                Bitmap adjustedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                Canvas canvas = new Canvas(adjustedBitmap);
                canvas.setMatrix(matrix);
                canvas.drawBitmap(bitmap, 0, 0, null);
                previewSize = new com.google.android.gms.common.images.Size(textureView.getWidth(), textureView.getHeight());
                frameProcessor.setViewWidth(textureView.getWidth());
                frameProcessor.setViewHeight(textureView.getHeight());
                frameProcessor.setScale(1);
                frameProcessor.processBitmapOfStream(adjustedBitmap, graphicOverlay);
            } else {
                previewSize = new com.google.android.gms.common.images.Size(textureView.getWidth(), textureView.getHeight());
                frameProcessor.setViewWidth(textureView.getWidth());
                frameProcessor.setViewHeight(textureView.getHeight());
                frameProcessor.setScale(1);
                frameProcessor.processBitmapOfStream(bitmap, graphicOverlay);
            }
        });
    }

    @Override
    @RequiresPermission(Manifest.permission.CAMERA)
    public void createCameraAndStartPreview(TextureView textureView) {
        this.textureView=textureView;
        startTime = -1;
        mStartTime = 0;
        mFrameCount = 0;
        fps = 0;
        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        300,
                        20000,
                        100,
                        300
                ).build();
        player = new ExoPlayer.Builder(currentFragment.requireActivity())
                .setLoadControl(loadControl)
                .build();
        player.setVideoTextureView(this.textureView);
        tracker = new FrameRateTracker();
        player.setVideoFrameMetadataListener(tracker);
        MediaSource mediaSource = generateMediaSource();
        if (mediaSource == null) {
            tvFPS.setText("Connection Failed");
            return;
        }
        player.setMediaSource(mediaSource);
        if (lastTimestamp != 0) {
            SurfaceTexture st = this.textureView.getSurfaceTexture();
            if (st != null) {
                st.setOnFrameAvailableListener(null);
            }
            this.textureView.setSurfaceTextureListener(null);
        }
        playMedia();
    }

    @Override
    public void handlePause() {
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cameraInfo.isPaused) {
                    tvFPS.setText("Connected");
                    pauseButton.setVisibility(View.INVISIBLE);
                    player.play();
                    frameCapture.startCapturing();
                } else {
                    tvFPS.setText("Paused");
                    pauseButton.setVisibility(View.VISIBLE);
                    player.pause();
                    if(frameCapture!=null)
                        frameCapture.stopCapturing();
                }
                cameraInfo.isPaused=!cameraInfo.isPaused;
            }
        };

        pauseButton.setOnClickListener(onClickListener);
        pauseContainer.setOnClickListener(onClickListener);
    }

    private MediaSource generateMediaSource() {
        switch(cameraInfo.type) {
            case CameraInfo.TYPE_DRONE_WIFI:
                RtmpDataSource.Factory rtmpSource = new RtmpDataSource.Factory();
                MediaItem mediaItem = new MediaItem.Builder()
                        .setUri(rtmpOutput)
                        .build();
                MediaSource mediaSource = new ProgressiveMediaSource.Factory(rtmpSource)
                        .createMediaSource(mediaItem);
                return mediaSource;
            case CameraInfo.TYPE_IP_CAM:
            case CameraInfo.TYPE_IP_WIFI:
                if ( cameraInfo.url.indexOf("://") < 0 ) {
                    return null;
                }
                int insertPos = cameraInfo.url.indexOf("://") + 3;
                String url = cameraInfo.url.substring(0, insertPos) + cameraInfo.username + ":" + cameraInfo.password + "@" + cameraInfo.url.substring(insertPos);
                MediaItem rstpItem = MediaItem.fromUri(url);
                RtspMediaSource rstpSource = new RtspMediaSource.Factory()
                        .setForceUseRtpTcp(true)
                        .createMediaSource(rstpItem);
                return rstpSource;
            case CameraInfo.TYPE_VIDEO_FILE:
                File videoFile = new File(cameraInfo.videoPath);
                if (videoFile.exists() == false) {
                    return null;
                }
                Uri uri = Uri.fromFile(videoFile);
                MediaItem videoItem = MediaItem.fromUri(uri);
                DefaultDataSource.Factory videoFactory = new DefaultDataSource.Factory(currentFragment.getContext());
                MediaSource fileSource = new ProgressiveMediaSource.Factory(videoFactory)
                        .createMediaSource(videoItem);
                return fileSource;
        }
        return null;
    }

    private void playMedia() {
        switch(cameraInfo.type) {
            case CameraInfo.TYPE_DRONE_WIFI:
                try {
                    runRTMPServer();
                }catch (Exception es) {
                    tvFPS.setText("Connection Failed");
                    es.printStackTrace();
                }
                break;
            case CameraInfo.TYPE_IP_CAM:
            case CameraInfo.TYPE_IP_WIFI:
                player.addListener(rtspPlayerListener);
                try {
                    player.prepare();
                    player.setPlayWhenReady(true);
                    frameCapture = new com.facecool.cameramanager.exoplayer.ExoPlayerCameraSource.FrameCapture(textureView);
                    frameCapture.startCapturing();
                    startFPSCounter();
                } catch (Exception es) {
                    es.printStackTrace();
                }
                break;
            case CameraInfo.TYPE_VIDEO_FILE:
                player.addListener(videoFilePlayerListener);
                try {
                    player.prepare();
                    player.setPlayWhenReady(true);
                } catch (Exception es) {
                    es.printStackTrace();
                }
                break;
        }
    }

    private void startFPSCounter() {
        fpsTask = new Runnable() {
            @Override
            public void run() {
                fps = tracker.getAverageFPS();
                setFpsOnScreen(fps);
                tracker.reset();
                if (isCapturing == true) {
                    mainThread.postDelayed(this, FPS_UPDATE_INTERVAL_MS);
                }
            }
        };
        mainThread.post(fpsTask);
    }

    public class FrameCapture {
        private TextureView textureView;
        private Handler handler;
        private Runnable frameCaptureRunnable;
        private final long startTime = System.currentTimeMillis();
        private final int targetFPS = 25;
        private boolean isRunning = false;

        public FrameCapture(TextureView textureView) {
            this.textureView = textureView;
            this.handler = new Handler();
            this.frameCaptureRunnable = new Runnable() {
                @Override
                public void run() {
                    captureFrame();
                    int delay = calculateNextFrameDelay(fps);
                    Constants.logInfo("EXOPLAYER fps: " + fps + " delay: " + delay, "fps-exo");
                    if (isRunning == true) {
                        handler.postDelayed(this, 80);
                    }
                }
            };
        }

        public void startCapturing() {
            isRunning = true;
            handler.post(frameCaptureRunnable);
        }

        public void stopCapturing() {
            isRunning = false;
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
        Log.d("Triggered Stop","stopped on Exo Player");
        if (player != null) {
            player.stop();

            isCapturing = false;
            player.removeListener(rtmpPlayerListener);
            player.release();
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
        mainThread.removeCallbacks(fpsTask);
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
                        Log.i("RTMP", "RTMP signal finished, playing the rest of the buffer");
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
                            player.addListener(rtmpPlayerListener);
                            try {
                                player.prepare();
                                player.setPlayWhenReady(true);
                                frameCapture = new com.facecool.cameramanager.exoplayer.ExoPlayerCameraSource.FrameCapture(textureView);
                                frameCapture.startCapturing();
                                startFPSCounter();
                            } catch (Exception es) {
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

    public class ExoPlayerFrameProcessingRunnable extends FrameProcessingRunnable {

        // This lock guards all of the member variables below.
        public final Object lock = new Object();
        public boolean active = true;

        // These pending variables hold the state associated with the new frame awaiting processing.
        public Bitmap pendingBitmap;

        ExoPlayerFrameProcessingRunnable() {
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

