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

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facecool.cameramanager.utils.BitmapUtils;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;


/**
 * Abstract base class for vision frame processors. Subclasses need to implement
 * onSuccess(Bitmap, Object, GraphicOverlay) to define what they want to with the detection results and
 * {@link #detectInImage(InputImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
public abstract class VisionProcessorBase<T> implements VisionImageProcessor {

    protected static final String MANUAL_TESTING_LOG = "LogTagForTest";
    private static final String TAG = "VisionProcessorBase";

    private final ActivityManager activityManager;
    private final Timer fpsTimer = new Timer();
    private final ScopedExecutor executor;

    // Whether this processor is already shut down
    private boolean isShutdown;

    // Used to calculate latency, running in the same thread, no sync needed.
    private int numRuns = 0;
    private long totalFrameMs = 0;
    private long maxFrameMs = 0;
    private long minFrameMs = Long.MAX_VALUE;
    private long totalDetectorMs = 0;
    private long maxDetectorMs = 0;
    private long minDetectorMs = Long.MAX_VALUE;

    // Frame count that have been processed so far in an one second interval to calculate FPS.
    private int frameProcessedInOneSecondInterval = 0;
    private int framesPerSecond = 0;

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private ByteBuffer latestImage;

    @GuardedBy("this")
    private FrameMetadata latestImageMetaData;
    // To keep the images and metadata in process.
    @GuardedBy("this")
    private ByteBuffer processingImage;

    @GuardedBy("this")
    private FrameMetadata processingMetaData;

  private float scale;
  private int viewWidth, viewHeight;
  private boolean isExternalScreen = false;
  private int currentOrientation = 0;
  private int currentMode = 0;

    @GuardedBy("this")
    private Bitmap latestBitmap;
    @GuardedBy("this")
    private Bitmap processingBitmap;

    protected VisionProcessorBase(Context context) {
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        executor = new ScopedExecutor(Executors.newSingleThreadExecutor());
        fpsTimer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        framesPerSecond = frameProcessedInOneSecondInterval;
                        frameProcessedInOneSecondInterval = 0;
                    }
                },
                /* delay= */ 0,
                /* period= */ 1000);
    }

    @Override
    public synchronized void processBitmapOfStream(Bitmap bitmap, final GraphicOverlay graphicOverlay) {
        latestBitmap = bitmap;
        if (processingBitmap == null) {
            processLatestBitmap(graphicOverlay);
        }
    }

    private synchronized void processLatestBitmap(final GraphicOverlay graphicOverlay) {
        processingBitmap = latestBitmap;
        latestBitmap = null;
        if (processingBitmap != null && !isShutdown) {
            processImage(processingBitmap, graphicOverlay);
        }
    }

    private void processImage(Bitmap bitmap, final GraphicOverlay graphicOverlay) {
        long frameStartMs = SystemClock.elapsedRealtime();
        requestDetectInImage(
                InputImage.fromBitmap(bitmap, 0),
                graphicOverlay,
                bitmap,
                null,
                frameStartMs)
                .addOnSuccessListener(executor, results -> processLatestBitmap(graphicOverlay));
    }

    // -----------------Code for processing single still image----------------------------------------
    @Override
    public void processBitmap(Bitmap bitmap, final GraphicOverlay graphicOverlay) {
        long frameStartMs = SystemClock.elapsedRealtime();
        requestDetectInImage(
                InputImage.fromBitmap(bitmap, 0),
                graphicOverlay,
                bitmap,
                null,
                frameStartMs);
    }

    // -----------------Code for processing live preview frame from Camera1 API-----------------------
    @Override
    public synchronized void processByteBuffer(
            ByteBuffer data, final FrameMetadata frameMetadata, final GraphicOverlay graphicOverlay) {
        latestImage = data;
        latestImageMetaData = frameMetadata;
        if (processingImage == null && processingMetaData == null) {
            processLatestImage(graphicOverlay);
        }
    }

    @Override
    public void UpdateSetting(Context con) {
        VisionProcessorBase.this.updateThreshould(con);
    }

    private synchronized void processLatestImage(final GraphicOverlay graphicOverlay) {
        processingImage = latestImage;
        processingMetaData = latestImageMetaData;
        latestImage = null;
        latestImageMetaData = null;
        if (processingImage != null && processingMetaData != null && !isShutdown) {
            processImage(processingImage, processingMetaData, graphicOverlay);
        }
    }

    private void processImage(
            ByteBuffer data, final FrameMetadata frameMetadata, final GraphicOverlay graphicOverlay) {
        long frameStartMs = SystemClock.elapsedRealtime();

        requestDetectInImage(
                InputImage.fromByteBuffer(
                        data,
                        frameMetadata.getWidth(),
                        frameMetadata.getHeight(),
                        frameMetadata.getRotation(),
                        InputImage.IMAGE_FORMAT_NV21),
                graphicOverlay,
                null,
                frameMetadata,
                frameStartMs)
                .addOnSuccessListener(executor, results -> processLatestImage(graphicOverlay));
    }

    // -----------------Common processing logic-------------------------------------------------------
    private Task<T> requestDetectInImage(
            final InputImage image,
            final GraphicOverlay graphicOverlay,
            @Nullable final Bitmap bmpFrame,
            FrameMetadata frameMetadata,
            long frameStartMs) {
        final long detectorStartMs = SystemClock.elapsedRealtime();
        return detectInImage(image)
                .addOnSuccessListener(
                        executor,
                        results -> {
                            long endMs = SystemClock.elapsedRealtime();
                            long currentFrameLatencyMs = endMs - frameStartMs;
                            long currentDetectorLatencyMs = endMs - detectorStartMs;
                            numRuns++;
                            frameProcessedInOneSecondInterval++;
                            totalFrameMs += currentFrameLatencyMs;
                            maxFrameMs = max(currentFrameLatencyMs, maxFrameMs);
                            minFrameMs = min(currentFrameLatencyMs, minFrameMs);
                            totalDetectorMs += currentDetectorLatencyMs;
                            maxDetectorMs = max(currentDetectorLatencyMs, maxDetectorMs);
                            minDetectorMs = min(currentDetectorLatencyMs, minDetectorMs);

                            // Only log inference info once per second. When frameProcessedInOneSecondInterval is
                            // equal to 1, it means this is the first frame processed during the current second.
                            if (frameProcessedInOneSecondInterval == 1) {
                                Log.d(TAG, "Num of Runs: " + numRuns);
                                Log.d(
                                        TAG,
                                        "Frame latency: max="
                                                + maxFrameMs
                                                + ", min="
                                                + minFrameMs
                                                + ", avg="
                                                + totalFrameMs / numRuns);
                                Log.d(
                                        TAG,
                                        "Detector latency: max="
                                                + maxDetectorMs
                                                + ", min="
                                                + minDetectorMs
                                                + ", avg="
                                                + totalDetectorMs / numRuns);
                                MemoryInfo mi = new MemoryInfo();
                                activityManager.getMemoryInfo(mi);
                                long availableMegs = mi.availMem / 0x100000L;
                                Log.d(TAG, "Memory available in system: " + availableMegs + " MB");
                            }

                            VisionProcessorBase.this.onSuccess(bmpFrame, results, graphicOverlay, processingImage, frameMetadata);
                        })
                .addOnFailureListener(
                        executor,
                        e -> {
                            graphicOverlay.clear();
                            graphicOverlay.postInvalidate();
                            String error = "Failed to process. Error: " + e.getLocalizedMessage();
                            new Handler(Looper.getMainLooper()).post(()->{
                            Toast.makeText(
                                            graphicOverlay.getContext(),
                                            error + "\nCause: " + e.getCause(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                            });
                            Log.d(TAG, error);
                            e.printStackTrace();
                            VisionProcessorBase.this.onFailure(e);
                        });
    }

    @Override
    public void stop() {
        executor.shutdown();
        isShutdown = true;
        numRuns = 0;
        totalFrameMs = 0;
        totalDetectorMs = 0;
        fpsTimer.cancel();
    }


    // -----------------Code for processing single still image----------------------------------------
    @Override
    public void detectFace(Bitmap bitmap) {
        long frameStartMs = SystemClock.elapsedRealtime();
        reqDetectInImage(
                InputImage.fromBitmap(bitmap, 0),
                bitmap,
                frameStartMs);
    }

    // -----------------Common processing logic-------------------------------------------------------
    private Task<T> reqDetectInImage(
            final InputImage image,
            Bitmap bmpFrame,
            long frameStartMs) {
        final long detectorStartMs = SystemClock.elapsedRealtime();
        return detectInImage(image)
                .addOnSuccessListener(
                        executor,
                        results -> {
                            long endMs = SystemClock.elapsedRealtime();
                            long currentFrameLatencyMs = endMs - frameStartMs;
                            long currentDetectorLatencyMs = endMs - detectorStartMs;
                            numRuns++;
                            frameProcessedInOneSecondInterval++;
                            totalFrameMs += currentFrameLatencyMs;
                            maxFrameMs = max(currentFrameLatencyMs, maxFrameMs);
                            minFrameMs = min(currentFrameLatencyMs, minFrameMs);
                            totalDetectorMs += currentDetectorLatencyMs;
                            maxDetectorMs = max(currentDetectorLatencyMs, maxDetectorMs);
                            minDetectorMs = min(currentDetectorLatencyMs, minDetectorMs);

                            // Only log inference info once per second. When frameProcessedInOneSecondInterval is
                            // equal to 1, it means this is the first frame processed during the current second.
                            if (frameProcessedInOneSecondInterval == 1) {
                                Log.d(TAG, "Num of Runs: " + numRuns);
                                Log.d(
                                        TAG,
                                        "Frame latency: max="
                                                + maxFrameMs
                                                + ", min="
                                                + minFrameMs
                                                + ", avg="
                                                + totalFrameMs / numRuns);
                                Log.d(
                                        TAG,
                                        "Detector latency: max="
                                                + maxDetectorMs
                                                + ", min="
                                                + minDetectorMs
                                                + ", avg="
                                                + totalDetectorMs / numRuns);
                                MemoryInfo mi = new MemoryInfo();
                                activityManager.getMemoryInfo(mi);
                                long availableMegs = mi.availMem / 0x100000L;
                                Log.d(TAG, "Memory available in system: " + availableMegs + " MB");
                            }

                            VisionProcessorBase.this.onSuccessReg(bmpFrame, results);
                        })
                .addOnFailureListener(
                        executor,
                        e -> {
                            String error = "Failed to process. Error: " + e.getLocalizedMessage();
                            Log.d(TAG, error);
                            e.printStackTrace();
                            VisionProcessorBase.this.onFailure(e);
                        });
    }

    protected abstract Task<T> detectInImage(InputImage image);

    protected abstract void onSuccess(@NonNull Bitmap bmpFrame, @NonNull T results, @NonNull GraphicOverlay graphicOverlay, ByteBuffer buffer, FrameMetadata frameMetadata);

    protected abstract void onSuccessReg(@NonNull Bitmap bmpFrame, @NonNull T results);

    protected abstract void onFailure(@NonNull Exception e);

    public abstract void updateThreshould(Context con);

    public abstract void createFaceDetector(Context context);

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public void setViewHeight(int height) {
        this.viewHeight = height;
    }

    public void setViewWidth(int width) {
        this.viewWidth = width;
    }

    public void setExternalScreen(boolean isExternalScreen) {this.isExternalScreen = isExternalScreen;}
    public boolean getIsExternalScreen() {return this.isExternalScreen;}

    public void setCurrentOrientation(int currentOrientation) {this.currentOrientation = currentOrientation;}
    public int getCurrentOrientation() {return this.currentOrientation;}

    public void setCurrentMode(int currentMode) {this.currentMode = currentMode;}
    public int getCurrentMode() {return this.currentMode;}

}
