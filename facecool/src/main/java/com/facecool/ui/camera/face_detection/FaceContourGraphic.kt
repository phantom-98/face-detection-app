package com.facecool.ui.camera.face_detection

import android.app.Application
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.fragment.app.Fragment
import com.face.cool.cache.Cache
import com.facecool.R
import com.facecool.ui.camera.camerax.GraphicOverlay
import com.google.mlkit.vision.face.Face
import kotlin.math.abs


class FaceContourGraphic(
    overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect,
    private var name: String?,
    private val cameraFacing: Int?,
    private val isGreen:Boolean = false,
    private val cache: Cache,
    private val app: Fragment
) : GraphicOverlay.Graphic(overlay) {

    private val boxPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val linePaint: Paint = Paint()
    private val colors = arrayOf(Color.GREEN, Color.WHITE, Color.BLUE, Color.DKGRAY, Color.CYAN, Color.GRAY, Color.LTGRAY, Color.MAGENTA, Color.RED, Color.YELLOW)

    init {
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 2f
    }

    override fun draw(canvas: Canvas?) {
        textPaint.color = if (isGreen) Color.GREEN else Color.WHITE
        boxPaint.color = if (isGreen) Color.GREEN else Color.WHITE
        linePaint.color = if (isGreen) Color.GREEN else Color.WHITE
        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox
        )
        canvas?.drawRoundRect(rect,abs(rect.left - rect.right)/7f, abs(rect.top - rect.bottom)/7f, boxPaint)

        if (cache.getDebugMode()) {
            face.allLandmarks.forEach {

                var p = calculatePoint(
                    imageRect.height().toFloat(),
                    imageRect.width().toFloat(),
                    it.position
                )
                canvas?.drawPoint(p.x, p.y, boxPaint)
            }
        }
        val x = if (cameraFacing == CameraSelector.LENS_FACING_FRONT) rect.left else rect.right
        val fs = (abs(rect.left - rect.right) / 10f)
        textPaint.textSize =  fs
        if (name == null) {
            canvas?.drawText(
                app.getString(R.string.camera_detect_processing),
                x,
                rect.bottom + fs+1,
                textPaint
            )
        } else {
            val text = name ?: ""

            text.split("<^>").forEachIndexed { index, s -> canvas?.drawText(
                s,
                x,
                rect.bottom + fs*(index+1) + 1,
                textPaint
            ) }
        }
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }

}