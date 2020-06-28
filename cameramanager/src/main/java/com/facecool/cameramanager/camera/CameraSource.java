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

package com.facecool.cameramanager.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.common.images.Size;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;

/**
 * Manages the camera and allows UI updates on top of it (e.g. overlaying extra Graphics or
 * displaying extra information). This receives preview frames from the camera at a specified rate,
 * sending those frames to child classes' detectors / classifiers as fast as it is able to process.
 */
public abstract class CameraSource {

    @SuppressLint("InlinedApi")
    public static final int CAMERA_FACING_BACK = CameraInfo.CAMERA_FACING_BACK;

    @SuppressLint("InlinedApi")
    public static final int CAMERA_FACING_FRONT = CameraInfo.CAMERA_FACING_FRONT;

    public static final int IMAGE_FORMAT = ImageFormat.NV21;
    public static final int DEFAULT_REQUESTED_CAMERA_PREVIEW_WIDTH = 720; //1280;
    public static final int DEFAULT_REQUESTED_CAMERA_PREVIEW_HEIGHT = 480; //720;

    protected static final String TAG = "MIDemoApp:CameraSource";

    /**
     * The dummy surface texture must be assigned a chosen name. Since we never use an OpenGL context,
     * we can choose any ID we want here. The dummy surface texture is not a crazy hack - it is
     * actually how the camera team recommends using the camera without a preview.
     */
    protected static final int DUMMY_TEXTURE_NAME = 100;

    /**
     * If the absolute difference between a preview size aspect ratio and a picture size aspect ratio
     * is less than this tolerance, they are considered to be the same aspect ratio.
     */
    protected static final float ASPECT_RATIO_TOLERANCE = 0.01f;
    public ImageView pauseButton;
    public MediaController mediaController;
    public LinearLayout pauseContainer;

    protected Context context;


    protected Size previewSize;
    protected int rotationDegrees;

    public com.facecool.cameramanager.entity.CameraInfo cameraInfo;

    protected static final float REQUESTED_FPS = 30.0f;
    protected static final boolean REQUESTED_AUTO_FOCUS = true;

    // This instance needs to be held onto to avoid GC of its underlying resources. Even though it
    // isn't used outside of the method that creates it, it still must have hard references maintained
    // to it.
    public SurfaceTexture dummySurfaceTexture;

    public final GraphicOverlay graphicOverlay;
    public final TextView tvFPS;

    /**
     * Dedicated thread and associated runnable for calling into the detector with frames, as the
     * frames become available from the camera.
     */
    public Thread processingThread;

    public FrameProcessingRunnable processingRunnable;
    public final Object processorLock = new Object();

    public VisionImageProcessor frameProcessor;

    /**
     * Map to convert between a byte array, received from the camera, and its associated byte buffer.
     * We use byte buffers internally because this is a more efficient way to call into native code
     * later (avoids a potential copy).
     *
     * <p><b>Note:</b> uses IdentityHashMap here instead of HashMap because the behavior of an array's
     * equals, hashCode and toString methods is both useless and unexpected. IdentityHashMap enforces
     * identity ('==') check on the keys.
     */
    public final IdentityHashMap<byte[], ByteBuffer> bytesToByteBuffer = new IdentityHashMap<>();

    public CameraSourcePreview mCameraSourcePreview;

    protected CameraSource(Context context, GraphicOverlay overlay, TextView tvFPS, com.facecool.cameramanager.entity.CameraInfo cameraInfo) {
        this.cameraInfo = cameraInfo;
        this.context = context;
        graphicOverlay = overlay;
        graphicOverlay.clear();
        this.tvFPS = tvFPS;

    }


    public void setConnected() {
        tvFPS.setText("Connected");
        tvFPS.setTextColor(Color.parseColor("#FFFFFF"));
    }

    public void setConnectFailed() {
        tvFPS.setText("Failed");
        tvFPS.setTextColor(Color.RED);
    }

    public long mStartTime = 0;
    public int mFrameCount = 0;
    public float increaseFrameCount(int frameCount) {
        float fps = 0;
        if (mStartTime == 0) {
            mStartTime = System.currentTimeMillis();
        } else {
            long elapsed = System.currentTimeMillis() - mStartTime;
            //mFrameCount++;
            mFrameCount += frameCount;
            if (elapsed >= 1000) {
                fps = (float) mFrameCount / ((float) elapsed / 1000.0f);
                //mStartTime = System.currentTimeMillis();
                mFrameCount = 0;
                setFpsOnScreen(fps);
            }
        }
        return fps;
    }
    public void increaseFrameCount() {
        if (mStartTime == 0) {
            mStartTime = System.currentTimeMillis();
        } else {
            long elapsed = System.currentTimeMillis() - mStartTime;
            mFrameCount++;
            if (elapsed >= 1000) {
                float fps = (float) mFrameCount / ((float) elapsed / 1000.0f);
                mStartTime = System.currentTimeMillis();
                mFrameCount = 0;
                setFpsOnScreen(fps);
            }
        }
    }

    public void setFpsOnScreen(float fps){
        String previewSizeString = "";
        if(previewSize != null){
            if(isImageFlipped()){
                previewSizeString = String.format("%dx%d", previewSize.getHeight(), previewSize.getWidth());
            } else {
                previewSizeString = String.format("%dx%d", previewSize.getWidth(), previewSize.getHeight());
            }
        }

//        tvFPS.setText(String.format("%.0f fps\n%s", fps, previewSizeString));
//        tvFPS.setGravity(Gravity.CENTER);
        tvFPS.setText(String.format("%.0f fps %s", fps, previewSizeString));
    }

    // ==============================================================================================
    // Public
    // ==============================================================================================

    /**
     * Stops the camera and releases the resources of the camera and underlying detector.
     */
    public void release() {
        synchronized (processorLock) {
            stop();
            cleanScreen();

            if (frameProcessor != null) {
                frameProcessor.stop();
            }
        }
    }
    public ArrayList<Bitmap> bitmaps = new ArrayList<>();
    public void updateBitmap(Bitmap bitmap, boolean shouldAppend) {
        ((Activity)context).runOnUiThread(()->{
            mCameraSourcePreview.mView.setImageBitmap(bitmap);
            if ( shouldAppend ) {
                bitmaps.add(bitmap);
            }
        });

    }

    /**
     * Opens the camera and starts sending preview frames to the underlying detector. The preview
     * frames are not displayed.
     *
     * @throws IOException if the camera's preview texture or display could not be initialized
     */

    public synchronized CameraSource start() throws IOException {
        if (isCameraCreated()) {
            return this;
        }

        dummySurfaceTexture = new SurfaceTexture(DUMMY_TEXTURE_NAME);
        createCameraAndStartPreview(dummySurfaceTexture);

        processingThread = new Thread(processingRunnable);
        processingRunnable.setActive(true);
        processingThread.start();
        return this;
    }

    /**
     * Opens the camera and starts sending preview frames to the underlying detector. The supplied
     * surface holder is used for the preview so frames can be displayed to the user.
     *
     * @param surfaceHolder the surface holder to use for the preview frames
     * @throws IOException if the supplied surface holder could not be used as the preview display
     */

    public synchronized CameraSource start(SurfaceHolder surfaceHolder, SurfaceView surfaceView) throws IOException {
        if (isCameraCreated()) {
            return this;
        }

        createCameraAndStartPreview(surfaceHolder, surfaceView);


        processingThread = new Thread(processingRunnable);
        processingRunnable.setActive(true);
        processingThread.start();
        return this;
    }

    /**
     * Closes the camera and stops sending frames to the underlying frame detector.
     *
     * <p>This camera source may be restarted again by calling {@link #start()} or {@link
     * #start(SurfaceHolder, SurfaceView)}.
     *
     * <p>Call {@link #release()} instead to completely shut down this camera source and release the
     * resources of the underlying detector.
     */
    public synchronized void stop() {
        processingRunnable.setActive(false);
        if (processingThread != null) {
            try {
                // Wait for the thread to complete to ensure that we can't have multiple threads
                // executing at the same time (i.e., which would happen if we called start too
                // quickly after stop).
                processingThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "Frame processing thread interrupted on release.");
            }
            processingThread = null;
        }

        if (isCameraCreated()) {
            dummySurfaceTexture = null;
            stopCameraPreviewAndRelease();
        }

        // Release the reference to any image buffers, since these will no longer be in use.
        bytesToByteBuffer.clear();
    }

    /**
     * Returns the preview size that is currently in use by the underlying camera.
     */
    public Size getPreviewSize() {
        return previewSize;
    }

    public void createCameraAndStartPreview(TextureView videoPreview) {
    }

    public void handlePause() {
    }

    public static class SizePair {
        public final Size preview;
        @Nullable
        public final Size picture;

        SizePair(Camera.Size previewSize, @Nullable Camera.Size pictureSize) {
            preview = new Size(previewSize.width, previewSize.height);
            picture = pictureSize != null ? new Size(pictureSize.width, pictureSize.height) : null;
        }

        public SizePair(Size previewSize, @Nullable Size pictureSize) {
            preview = previewSize;
            picture = pictureSize;
        }
    }

    /**
     * Creates one buffer for the camera preview callback. The size of the buffer is based off of the
     * camera preview size and the format of the camera image.
     *
     * @return a new preview buffer of the appropriate size for the current camera settings
     */
    @SuppressLint("InlinedApi")
    protected byte[] createPreviewBuffer(Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(IMAGE_FORMAT);
        long sizeInBits = (long) previewSize.getHeight() * previewSize.getWidth() * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0d) + 1;

        // Creating the byte array this way and wrapping it, as opposed to using .allocate(),
        // should guarantee that there will be an array to work with.
        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {
            // I don't think that this will ever happen.  But if it does, then we wouldn't be
            // passing the preview content to the underlying detector later.
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }

        bytesToByteBuffer.put(byteArray, buffer);
        return byteArray;
    }


    public void setMachineLearningFrameProcessor(VisionImageProcessor processor) {
        synchronized (processorLock) {
            cleanScreen();
            if (frameProcessor != null && frameProcessor != processor) {
                frameProcessor.stop();
            }
            frameProcessor = processor;
        }
    }


    /**
     * This runnable controls access to the underlying receiver, calling it to process frames when
     * available from the camera. This is designed to run detection on frames as fast as possible
     * (i.e., without unnecessary context switching or waiting on the next frame).
     *
     * <p>While detection is running on a frame, new frames may be received from the camera. As these
     * frames come in, the most recent frame is held onto as pending. As soon as detection and its
     * associated processing is done for the previous frame, detection on the mostly recently received
     * frame will immediately start on the same thread.
     */




    /**
     * Cleans up graphicOverlay and child classes can do their cleanups as well .
     */
    public void cleanScreen() {
        graphicOverlay.clear();
    }

    public void UpdateSetting() {
        frameProcessor.UpdateSetting(context);
    }

    public boolean isImageFlipped() {
        return false;
    }

    protected abstract void onProcessFrameCompleted(ByteBuffer buffer);

    protected abstract boolean isCameraCreated();

    protected abstract void createCameraAndStartPreview(SurfaceTexture texture) throws IOException;

    protected abstract void createCameraAndStartPreview(SurfaceHolder surfaceHolder, SurfaceView surfaceView) throws IOException;

    protected abstract void processExistingPendingBuffer(ByteBuffer pendingFrameData);

    protected abstract void stopCameraPreviewAndRelease();

    protected abstract  int isPortraitMode() ;

}
