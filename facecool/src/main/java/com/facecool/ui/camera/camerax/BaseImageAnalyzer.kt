package com.facecool.ui.camera.camerax

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.facecool.utils.rotateBitmap
import com.facecool.utils.toBitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs

abstract class BaseImageAnalyzer<T : Face> //constructor(private val idleListener: IdleListener)
    : ImageAnalysis.Analyzer {

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun analyze(imageBitmap: Bitmap): Face? {

        return suspendCancellableCoroutine {

            val image: InputImage = InputImage.fromBitmap(imageBitmap, 0)

            detectInImage(image)
                .addOnSuccessListener { results ->
//                    val items = results?.mapNotNull {
//                        val rightEyeOpenProbability = it.rightEyeOpenProbability ?: return@mapNotNull null
//                        if (rightEyeOpenProbability < 0.8) return@mapNotNull null
//                        val leftEyeOpenProbability = it.leftEyeOpenProbability ?: return@mapNotNull null
//                        if (leftEyeOpenProbability < 0.8) return@mapNotNull null
//                        val eulerX = it.headEulerAngleX
//                        val eulerY = it.headEulerAngleY
//                        val eulerZ = it.headEulerAngleZ
//                        val isLookingStraight =
//                            abs(eulerX) < 30 && abs(eulerY) < 30 && abs(eulerZ) < 30
//                        if (!isLookingStraight) return@mapNotNull null
//
//                        it
//                    }
                    it.resume(if (results.isNullOrEmpty()) null else results.firstOrNull()) {
                        it.printStackTrace()
                    }
                }
                .addOnFailureListener {e ->
                    it.resume(null)
                }
        }
    }

    fun analyze(imageBitmap: Bitmap, rotationDegrees: Int) {

        val image: InputImage = InputImage.fromBitmap(imageBitmap, rotationDegrees)
        detectInImage(image)
            .addOnSuccessListener { results ->
//                val items = results?.mapNotNull {
//                    val rightEyeOpenProbability = it.rightEyeOpenProbability ?: return@mapNotNull null
//                    if (rightEyeOpenProbability < 0.8) return@mapNotNull null
//                    val leftEyeOpenProbability = it.leftEyeOpenProbability ?: return@mapNotNull null
//                    if (leftEyeOpenProbability < 0.8) return@mapNotNull null
//                    val eulerX = it.headEulerAngleX
//                    val eulerY = it.headEulerAngleY
//                    val eulerZ = it.headEulerAngleZ
//                    val isLookingStraight =
//                        abs(eulerX) < 30 && abs(eulerY) < 30 && abs(eulerZ) < 30
//                    if (!isLookingStraight) return@mapNotNull null
//
//                    it
//                }

                if (results.isNullOrEmpty()) {
                    onFailure(java.lang.Exception("No image found"))
                } else {
                    onFacesDetected(results, imageBitmap)
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override fun analyze(imageProxy: ImageProxy) {

//        val t1 = System.currentTimeMillis()

        val btm = imageProxy.toBitmap() ?: return

        val image: InputImage =
            InputImage.fromBitmap(btm, imageProxy.imageInfo.rotationDegrees)
        detectInImage(image)
            .addOnSuccessListener { results ->
//                val items = results.mapNotNull {
////                    val rightEyeOpenProbability = it.rightEyeOpenProbability ?: return@mapNotNull null
////                    val leftEyeOpenProbability = it.leftEyeOpenProbability ?: return@mapNotNull null
////                    if (leftEyeOpenProbability < 0.8 && rightEyeOpenProbability < 0.8) return@mapNotNull null
//
////                    val eulerX = it.headEulerAngleX
////                    val eulerY = it.headEulerAngleY
////                    val eulerZ = it.headEulerAngleZ
////                    val isLookingStraight = abs(eulerX) < 30 && abs(eulerY) < 30 && abs(eulerZ) < 30
////                    if (!isLookingStraight) return@mapNotNull null
//
//                    if ( it.boundingBox.left < 0) return@mapNotNull null
//                    if (it.boundingBox.right> image.width) return@mapNotNull null
//                    if (it.boundingBox.top < 0) return@mapNotNull null
//                    if (it.boundingBox.bottom > image.height) return@mapNotNull null
//
//                    it
//                }

                val w = btm.width
                val h = btm.height
                val rotated = btm.rotateBitmap(imageProxy.imageInfo.rotationDegrees)
                rotated?.let {
                    onFacesDetected(results, it)
                    onOutlineDetected(results, Rect(0,0, w, h))
                }

                btm.recycle()
                imageProxy.close()
            }
            .addOnFailureListener {
                onFailure(it)
                imageProxy.close()
            }
    }


    protected abstract fun detectInImage(image: InputImage): Task<List<T>>

    abstract fun stop()

    protected abstract fun onOutlineDetected(
        results: List<T>,
        rect: Rect,
    )

    protected abstract fun onFacesDetected(
        results: T,
        toBitmap: Bitmap
    )

    protected abstract fun onFacesDetected(
        results: List<T>,
        toBitmap: Bitmap
    )

    protected open fun onFacesDetected(
        results: List<T>,
        toBitmap: Bitmap,
        data: Map<Int, String>
    ) {

    }

    protected abstract fun onFailure(e: Exception)

//
//    interface IdleListener {
//        fun isIdle(): Boolean
//    }
//
//    interface Callback {
//        fun onReady(face: Face, bitmap: Bitmap)//, extra: Map<Int, String>)
//        fun onError()
//    }

}
