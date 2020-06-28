package com.facecool.attendance.facedetector

import androidx.annotation.Keep

@Keep
internal class ModelConfig {
    var scale = 0.0f
    var shift_x = 0.0f
    var shift_y = 0.0f
    var height = 0
    var width = 0
    var name = ""
    var org_resize = false
}