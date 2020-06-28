package com.facecool.attendance.facedetector

import android.graphics.Bitmap
import com.google.mlkit.vision.face.Face

interface FaceModel {
    fun getDetectedFace(): Face
    fun getRealLiveProbability(): Float
    fun getFaceImage(): Bitmap?
    fun getDetectedFeatures(): ByteArray
}
