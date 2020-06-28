package com.facecool.attendance.facedetector

import android.graphics.Bitmap
import com.google.mlkit.vision.face.Face

data class FaceData(
    val face: Face,
    var isRealLiveProbability: Float,
    val faceBitmap: Bitmap
) : FaceModel {

    var faceFeatures: ByteArray? = null

    override fun getDetectedFace(): Face {
        return face
    }

    override fun getRealLiveProbability(): Float {
        return isRealLiveProbability
    }

    override fun getFaceImage(): Bitmap {
        return faceBitmap
    }

    override fun getDetectedFeatures(): ByteArray {
        return faceFeatures ?: ByteArray(0)
    }
}