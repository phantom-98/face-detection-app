package com.facecool.attendance.facedetector

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.widget.Toast
import com.google.mlkit.vision.face.Face

class NativeFaceDetectionEngine : FaceDetectionEngine {

    private val faceDetector = FaceDetector()

    override fun initEngine(context: Context, asserManager: AssetManager) {
        val nInitFL: Int = FaceEngine.loadLiveModel(asserManager)
        if (nInitFL != 0) {
            Toast.makeText(context, "Failed to load the live models", Toast.LENGTH_LONG)
                .show()
        }
        val nInitFF: Int = FaceEngine.loadFeatureModel(context)
        if (nInitFF != 0) {
            Toast.makeText(
                context,
                "Failed to load the feature models",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override suspend fun getFaceFeature(frame: Bitmap, face: Face): FaceModel {
        return faceDetector.getFaceLiveFeature(frame, face)
    }

    override suspend fun getFaceFeatureSimilarity(feature1: ByteArray, feature2: ByteArray): Float {
        return FaceEngine.getSimilarity(feature1, feature2)
    }

    override suspend fun checkFaceQuality(facemodel: FaceModel): Int {
        return faceDetector.checkFaceQuality(facemodel)
    }

    override suspend fun checkImageBlurness(bmp: Bitmap): Int {
        return FaceEngine.getSharpness(bmp).toInt()
    }
}