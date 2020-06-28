package com.facecool.cameramanager.utils;

public class TestStateHelper {

    public static TestStateHelper _instace = null;
    public static void onTestStateChange(int state, Object data) {
        if ( _instace != null ) {
            _instace.onStateChange(state, data);
        }
    }
    
    public static final int STATE_INIT = 0;
    public static final int STATE_FRAME_DATA = 1;
    public static final int STATE_DETECT_FACE = 2;
    public void onStateChange(int state, Object data) {

    }


}
