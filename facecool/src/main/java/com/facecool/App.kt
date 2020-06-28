package com.facecool

import android.app.Application
import com.facecool.attendance.facedetector.FaceDetectionEngine
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var faceEngine: FaceDetectionEngine

    override fun onCreate() {
        super.onCreate()
        faceEngine.initEngine(this, this.assets)
    }

}
