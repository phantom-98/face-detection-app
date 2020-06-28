package com.facecool.attendance.facedetector

import android.R.attr
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.ln
import kotlin.math.tan


/**
 * Created by Kevin on 01-July-22
 */
class FaceDetector {
    private fun getLandmarkPosition(
        face: Face?,
        landmarkX: FloatArray,
        landmarkY: FloatArray
    ): Boolean {
        val ret = false
        if (face == null) return ret
        for (i in 0..4) {
            val landmark = face.getLandmark(landMarkTypes[i])
            if (landmark != null) {
                val landmarkPosition = landmark.position
                landmarkX[i] = landmarkPosition.x
                landmarkY[i] = landmarkPosition.y
            }
        }
        return true
    }
    fun calculateBrightnessOfBitmap(bitmap: Bitmap): Float{
        var R = 0
        var G = 0
        var B = 0
        val height: Int = bitmap.getHeight()
        val width: Int = bitmap.getWidth()
        var n = 0
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var i = 0
        while (i < pixels.size) {
            val color = pixels[i]
            R += Color.red(color)
            G += Color.green(color)
            B += Color.blue(color)
            n++
            i += 1
        }

        return (R + B + G) / (n * 3f)
    }

    fun checkFaceOrientation(face: Face): Float {
//        Log.d("Image Quality", "angle(x="+ face.headEulerAngleX+", y=" + face.headEulerAngleY+", z="+face.headEulerAngleZ+")")
        return (abs(face.headEulerAngleX) + abs(face.headEulerAngleY) + abs(face.headEulerAngleZ))/3
    }

    fun checkFaceQuality(faceModel: FaceModel): Int{
//        val brightness = faceModel.getFaceImage()?.let { calculateBrightnessOfBitmap(it) }
        val brightness = faceModel.getFaceImage()?.let {
            FaceEngine.getBrightness(it)
        } ?: 0f
        val sharpness = faceModel.getFaceImage()?.let {
            FaceEngine.getSharpness(it)
        } ?: 0f

        val orient = faceModel.getDetectedFace()?.let { checkFaceOrientation(it) } ?: 100f

//        Log.d("Image Quality", "Brightness="+ brightness+", Sharpness=" + sharpness+", orient="+orient)


        val b = (30f * atan(brightness / 5  - 9.5)) + 50
        val s = (30f * atan(sharpness / 5 - 12)) + 50
        val o= (30f * atan(100 - orient * 4)) + 50
        val f = faceModel.getDetectedFace()?.let {
            Log.d("quality", "left=${it.leftEyeOpenProbability}, right=${it.rightEyeOpenProbability}, smile=${it.smilingProbability}")
            (it.leftEyeOpenProbability!!*1.5f + it.rightEyeOpenProbability!!*1.5f + 1 - it.smilingProbability!!) * 25f
        }?:0f
//        Log.d("Image Quality", "B="+ b+", S=" + s+", O="+o + ", total="+(b+s+o)/3)
        return ((b+s+o+f * 2)/5).toInt()
    }

    fun getFaceLiveFeature(bmpFrame: Bitmap, face: Face): FaceData {

        try {
            val rtFace = face.boundingBox
            val landmarksX = FloatArray(5)
            val landmarksY = FloatArray(5)
            getLandmarkPosition(face, landmarksX, landmarksY)
            var bmpFace: Bitmap?
            val left = Math.max(0, rtFace.left)
            val right = Math.min(bmpFrame.width, rtFace.right)
            val top = Math.max(0, rtFace.top)
            val bottom = Math.min(bmpFrame.height, rtFace.bottom)
            // TODO - ensure bitmap size is correct, no - values and no over the board values on the bitmap creation
            try {

                val w = right - left
                val h = bottom - top

                if (w < 1) {
                    return FaceData(face, -1f, Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565))
                }
                if (h < 1) {
                    return FaceData(face, -1f, Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565))
                }

                bmpFace = Bitmap.createBitmap(bmpFrame, left, top, w, h)
            } catch (e: Exception) {
//                Log.d("EXCEPTION", "error creating bitmap, trackingId = ${face.trackingId}")
                return FaceData(face, -1f, Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565))
            }
            val fLive = 1.0f
            val photo = Bitmap.createScaledBitmap(bmpFace, FACE_WIDTH, FACE_HEIGHT, true)
            bmpFace.recycle()
            val faceData = FaceData(face, fLive, photo)
            FaceEngine.extractLiveFeature(
                bmpFrame,
                left,
                top,
                right,
                bottom,
                landmarksX,
                landmarksY,
                faceData
            )

            return faceData
        } catch (e: java.lang.Exception) {
            return FaceData(face, -1f, Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565))
        }
    }

    companion object {
        private val landMarkTypes = intArrayOf(
            FaceLandmark.LEFT_EYE, FaceLandmark.RIGHT_EYE,
            FaceLandmark.NOSE_BASE,
            FaceLandmark.MOUTH_LEFT, FaceLandmark.MOUTH_RIGHT
        )

        const val FACE_WIDTH = 80
        const val FACE_HEIGHT = 80
    }
}