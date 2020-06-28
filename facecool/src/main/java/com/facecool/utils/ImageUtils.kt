package com.facecool.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.os.Build
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

fun ImageProxy.checkImageSharpness(): Float {
    val image = this
    val planes = image.planes
    val buffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * image.width
    val width = image.width
    val height = image.height

    val rgbaValues = ByteArray(buffer.remaining())
    buffer.get(rgbaValues)

    var minR = 255
    var maxR = 0
    var minG = 255
    var maxG = 0
    var minB = 255
    var maxB = 0

    var pixelOffset = 0
    for (y in 0 until height - 5 - pixelStride step 3) {
        for (x in 0 until width - 5 - pixelStride step 3) {
            val r = rgbaValues[pixelOffset].toInt() and 0xFF
            val g = rgbaValues[pixelOffset + 1].toInt() and 0xFF
            val b = rgbaValues[pixelOffset + 2].toInt() and 0xFF

            if (r < minR) minR = r
            if (r > maxR) maxR = r

            if (g < minG) minG = g
            if (g > maxG) maxG = g

            if (b < minB) minB = b
            if (b > maxB) maxB = b

            pixelOffset += pixelStride
        }
        pixelOffset += rowPadding
    }

    // Use the min and max RGB values for further analysis or processing
    // For example, you can calculate the sharpness based on these values
    val sharpness = calculateSharpness(minR, maxR, minG, maxG, minB, maxB)

    return sharpness
    // Print the sharpness value
//    println("Sharpness: $sharpness")

    // Close the ImageProxy after processing
//    image.close()
}

fun calculateSharpness(minR: Int, maxR: Int, minG: Int, maxG: Int, minB: Int, maxB: Int): Float {
    // Perform sharpness calculation based on the min and max RGB values
    // You can use any algorithm or formula suitable for your application
    // For simplicity, this example calculates the sharpness as the difference
    // between the maximum and minimum RGB values

    val sharpnessR = maxR - minR
    val sharpnessG = maxG - minG
    val sharpnessB = maxB - minB

    // You can use an aggregate sharpness value or individual channel sharpness values
    // depending on your requirements
    // For this example, the aggregate sharpness is calculated as the average of the channel sharpness
    val aggregateSharpness = (sharpnessR + sharpnessG + sharpnessB) / 3.toFloat()

    return aggregateSharpness
}


fun Image.toBitmap(rotationDegrees: Int): Bitmap? {
    val yBuffer = planes[0].buffer // Y
    val vuBuffer = planes[2].buffer // VU

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)

    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
    val imageBytes = out.toByteArray()

    val bfo = BitmapFactory.Options()
    bfo.inPreferredConfig = Bitmap.Config.valueOf("ARGB_8888")
    bfo.inMutable = true

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        bfo.inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.SRGB)
    }
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bfo)
        .rotateBitmap(rotationDegrees)
}

fun Bitmap.rotateBitmap(rotationDegrees: Int): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(rotationDegrees.toFloat())
    val rotatedBitmap =
        Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    if (rotatedBitmap != this) {
        this.recycle()
    }
    return rotatedBitmap
}


fun ImageProxy.toBitmap(): Bitmap? {
    val nv21 = yuv420888ToNv21(this)
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    return yuvImage.toBitmap()
}

private fun YuvImage.toBitmap(): Bitmap? {
    val out = ByteArrayOutputStream()
    if (!compressToJpeg(Rect(0, 0, width, height), 100, out))
        return null
    val imageBytes: ByteArray = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
    val pixelCount = image.cropRect.width() * image.cropRect.height()
    val pixelSizeBits = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888)
    val outputBuffer = ByteArray(pixelCount * pixelSizeBits / 8)
    imageToByteBuffer(image, outputBuffer, pixelCount)
    return outputBuffer
}

private fun imageToByteBuffer(image: ImageProxy, outputBuffer: ByteArray, pixelCount: Int) {
    assert(image.format == ImageFormat.YUV_420_888)

    val imageCrop = image.cropRect
    val imagePlanes = image.planes

    imagePlanes.forEachIndexed { planeIndex, plane ->
        // How many values are read in input for each output value written
        // Only the Y plane has a value for every pixel, U and V have half the resolution i.e.
        //
        // Y Plane            U Plane    V Plane
        // ===============    =======    =======
        // Y Y Y Y Y Y Y Y    U U U U    V V V V
        // Y Y Y Y Y Y Y Y    U U U U    V V V V
        // Y Y Y Y Y Y Y Y    U U U U    V V V V
        // Y Y Y Y Y Y Y Y    U U U U    V V V V
        // Y Y Y Y Y Y Y Y
        // Y Y Y Y Y Y Y Y
        // Y Y Y Y Y Y Y Y
        val outputStride: Int

        // The index in the output buffer the next value will be written at
        // For Y it's zero, for U and V we start at the end of Y and interleave them i.e.
        //
        // First chunk        Second chunk
        // ===============    ===============
        // Y Y Y Y Y Y Y Y    V U V U V U V U
        // Y Y Y Y Y Y Y Y    V U V U V U V U
        // Y Y Y Y Y Y Y Y    V U V U V U V U
        // Y Y Y Y Y Y Y Y    V U V U V U V U
        // Y Y Y Y Y Y Y Y
        // Y Y Y Y Y Y Y Y
        // Y Y Y Y Y Y Y Y
        var outputOffset: Int

        when (planeIndex) {
            0 -> {
                outputStride = 1
                outputOffset = 0
            }

            1 -> {
                outputStride = 2
                // For NV21 format, U is in odd-numbered indices
                outputOffset = pixelCount + 1
            }

            2 -> {
                outputStride = 2
                // For NV21 format, V is in even-numbered indices
                outputOffset = pixelCount
            }

            else -> {
                // Image contains more than 3 planes, something strange is going on
                return@forEachIndexed
            }
        }

        val planeBuffer = plane.buffer
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride

        // We have to divide the width and height by two if it's not the Y plane
        val planeCrop = if (planeIndex == 0) {
            imageCrop
        } else {
            Rect(
                imageCrop.left / 2,
                imageCrop.top / 2,
                imageCrop.right / 2,
                imageCrop.bottom / 2
            )
        }

        val planeWidth = planeCrop.width()
        val planeHeight = planeCrop.height()

        // Intermediate buffer used to store the bytes of each row
        val rowBuffer = ByteArray(plane.rowStride)

        // Size of each row in bytes
        val rowLength = if (pixelStride == 1 && outputStride == 1) {
            planeWidth
        } else {
            // Take into account that the stride may include data from pixels other than this
            // particular plane and row, and that could be between pixels and not after every
            // pixel:
            //
            // |---- Pixel stride ----|                    Row ends here --> |
            // | Pixel 1 | Other Data | Pixel 2 | Other Data | ... | Pixel N |
            //
            // We need to get (N-1) * (pixel stride bytes) per row + 1 byte for the last pixel
            (planeWidth - 1) * pixelStride + 1
        }

        for (row in 0 until planeHeight) {
            // Move buffer position to the beginning of this row
            planeBuffer.position(
                (row + planeCrop.top) * rowStride + planeCrop.left * pixelStride
            )

            if (pixelStride == 1 && outputStride == 1) {
                // When there is a single stride value for pixel and output, we can just copy
                // the entire row in a single step
                planeBuffer.get(outputBuffer, outputOffset, rowLength)
                outputOffset += rowLength
            } else {
                // When either pixel or output have a stride > 1 we must copy pixel by pixel
                planeBuffer.get(rowBuffer, 0, rowLength)
                for (col in 0 until planeWidth) {
                    outputBuffer[outputOffset] = rowBuffer[col * pixelStride]
                    outputOffset += outputStride
                }
            }
        }
    }
}
