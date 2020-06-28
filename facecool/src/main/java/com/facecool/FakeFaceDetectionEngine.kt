package com.facecool

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import com.facecool.attendance.facedetector.FaceData
import com.facecool.attendance.facedetector.FaceDetectionEngine
import com.facecool.attendance.facedetector.FaceModel
import com.google.mlkit.vision.face.Face

class FakeFaceDetectionEngine : FaceDetectionEngine {
    override fun initEngine(context: Context, asserManager: AssetManager) {

    }

    override suspend fun getFaceFeature(frame: Bitmap, face: Face): FaceModel {
        return FaceData(face, -1f, Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565))
    }

    override suspend fun getFaceFeatureSimilarity(feature1: ByteArray, feature2: ByteArray): Float {
        return 0.4f
    }
    override suspend fun checkFaceQuality(facemodel: FaceModel): Int {
        return 0
    }

    override suspend fun checkImageBlurness(bmp: Bitmap): Int {
        return 0
    }

}
