package com.facecool.cameramanager.camera;

import android.graphics.ImageFormat;

public abstract class FrameProcessingRunnableForExternalScreen implements Runnable {
    public abstract void setActive(boolean active);
    public abstract void setNextFrame(byte[] frame, int width, int height, int imageFormat);
}
