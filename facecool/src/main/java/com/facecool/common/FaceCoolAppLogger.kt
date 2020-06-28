package com.facecool.common

import android.util.Log
import com.facecool.BuildConfig

class FaceCoolAppLogger : AppLogger {

    override fun <T> log(tag: T, obj: Any) {
        if (BuildConfig.DEBUG) {
            tag?.let {
                val tagName = "FACE_COOL_" + it::class.java.name
                Log.d(tagName, obj.toString())
            }
        }
    }

}
