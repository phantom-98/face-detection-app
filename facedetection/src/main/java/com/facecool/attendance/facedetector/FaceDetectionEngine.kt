package com.facecool.attendance.facedetector

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import com.google.mlkit.vision.face.Face

interface FaceDetectionEngine {

    fun initEngine(context: Context, asserManager: AssetManager)

    suspend fun getFaceFeature(frame: Bitmap, face: Face): FaceModel

    suspend fun getFaceFeatureSimilarity(feature1: ByteArray, feature2: ByteArray): Float

    suspend fun checkFaceQuality(facemodel: FaceModel): Int

    suspend fun checkImageBlurness(bmp: Bitmap): Int

}