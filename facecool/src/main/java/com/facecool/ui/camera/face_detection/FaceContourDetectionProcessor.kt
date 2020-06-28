package com.facecool.ui.camera.face_detection

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.facecool.ui.camera.FaceDetectorCallback
import com.facecool.ui.camera.camerax.BaseImageAnalyzer
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceDetectorOptions.PerformanceMode
import java.io.IOException

class FaceContourDetectionProcessor(
    private val faceDetectorCallback: FaceDetectorCallback,
//    private val idleListener: IdleListener,
//    @PerformanceMode private val performanceMode: Int = FaceDetectorOptions.PERFORMANCE_MODE_FAST
) : BaseImageAnalyzer<Face>() {

    private val detector: FaceDetector
    init {
        val realTimeOpts = FaceDetectorOptions.Builder()
//        .setPerformanceMode(performanceMode)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.3f)
            .enableTracking()
            .build()
        detector = FaceDetection.getClient(realTimeOpts)
    }

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
            Log.e(TAG, "Face detector closed.")
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun onFacesDetected(results: List<Face>, toBitmap: Bitmap, data: Map<Int, String>) {
        faceDetectorCallback.onFacesDetected(results, toBitmap, data)
    }

    override fun onFacesDetected(results: List<Face>, toBitmap: Bitmap) {
        faceDetectorCallback.onFaceDetected(results, toBitmap)
    }

    override fun onFacesDetected(results: Face, toBitmap: Bitmap) {
        faceDetectorCallback.onFaceDetected(results, toBitmap)
    }

    override fun onOutlineDetected(results: List<Face>, rect: Rect) {
        faceDetectorCallback.onOutlineDetected(results, rect)
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
        faceDetectorCallback.onError(e)
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }

}
