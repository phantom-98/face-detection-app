package com.facecool.cameramanager.exoplayer;
import android.media.MediaFormat;

import androidx.annotation.Nullable;
import androidx.media3.common.Format;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.video.VideoFrameMetadataListener;

@UnstableApi
public class FrameRateTracker implements VideoFrameMetadataListener {

    private long lastFrameTime = 0;
    private int frameCount = 0;
    private long totalFrameDuration = 0;

    @Override
    public void onVideoFrameAboutToBeRendered(long presentationTimeUs, long releaseTimeNs, Format format, @Nullable MediaFormat mediaFormat) {
        long currentTime = System.nanoTime();
        if (lastFrameTime != 0) {
            long frameDuration = currentTime - lastFrameTime;
            totalFrameDuration += frameDuration;
            frameCount++;
        }
        lastFrameTime = currentTime;
    }

    public float getAverageFPS() {
        if (frameCount == 0) {
            return 0;
        }
        return 1_000_000_000.0f / (totalFrameDuration / frameCount);
    }

    public void reset() {
        lastFrameTime = 0;
        frameCount = 0;
        totalFrameDuration = 0;
    }
}
