package com.facecool.cameramanager.exoplayer;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.video.MediaCodecVideoRenderer;
import androidx.media3.exoplayer.video.VideoRendererEventListener;
import androidx.media3.exoplayer.video.VideoSink;

@UnstableApi public class FrameSkipVideoRenderer extends MediaCodecVideoRenderer {
    private int framesSkipped = 0;
    private long startTime = 0;
    private static final int THREE_SECOND_INTERVAL_uS = 3000000;


    @OptIn(markerClass = UnstableApi.class) public FrameSkipVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, @Nullable Handler eventHandler, @Nullable VideoRendererEventListener eventListener, int maxDroppedFramesToNotify) {
        super(context, mediaCodecSelector, allowedJoiningTimeMs, eventHandler, eventListener, maxDroppedFramesToNotify);
    }

    @Override
    public boolean shouldDropFrame(long earlyUs, long elapsedRealtimeUs, boolean isLastFrame) {
        framesSkipped++;
        if (startTime == 0) {
            startTime = elapsedRealtimeUs;
        }
        if (elapsedRealtimeUs - startTime < THREE_SECOND_INTERVAL_uS) {
            Log.i("RTMP", "Frame skipped to catch up with live, frame #" + framesSkipped);
            return true;
        }
        return super.shouldDropFrame(earlyUs, elapsedRealtimeUs, isLastFrame);
    }
}
