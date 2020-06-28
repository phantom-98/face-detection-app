package com.facecool.cameramanager.camera;

public abstract class FrameProcessingRunnable implements Runnable {
    public abstract void setActive(boolean active);
    public abstract void setNextFrame(Object frame);
}
