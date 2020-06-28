package com.facecool.attendance.facedetector;

import androidx.annotation.Keep;

@Keep
public class ModelConfig {
    float scale = 0.0F;
    float shift_x = 0.0F;
    float shift_y = 0.0F;
    int height = 0;
    int width = 0;
    String name = "";
    boolean org_resize = false;
}