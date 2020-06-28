package com.facecool.ui.camera.camerax

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.Log

suspend fun isBitmapContrastGoodEnough(bitmap: Bitmap, threshold: Float = 0.3f): Boolean {
    // Create a color matrix that increases the contrast of the image
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f)
    colorMatrix.setScale(1.2f, 1.2f, 1.2f, 1f)

    // Apply the color matrix to the bitmap
    val contrastBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
    val contrastCanvas = android.graphics.Canvas(contrastBitmap)
    val contrastPaint = android.graphics.Paint()
    contrastPaint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    contrastCanvas.drawBitmap(bitmap, 0f, 0f, contrastPaint)

    // Calculate the average luminance of the bitmap
    var luminanceSum = 0.0
    for (y in 0 until contrastBitmap.height) {
        for (x in 0 until contrastBitmap.width) {
            val pixel = contrastBitmap.getPixel(x, y)
            val luminance =
                0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)
            luminanceSum += luminance
        }
    }
    val averageLuminance = luminanceSum / (contrastBitmap.width * contrastBitmap.height)

    // Calculate the contrast ratio between the average luminance and the maximum and minimum luminance values
    val maxLuminance = 255.0
    val minLuminance = 0.0
    val contrastRatio = (maxLuminance - averageLuminance) / (averageLuminance - minLuminance)

    // Return true if the contrast ratio is above the threshold, indicating that the image has good contrast
    return contrastRatio >= threshold
}


//D/BITMAP_CHECK: averageContrast 49.05455129086849
//D/BITMAP_CHECK: averageLuminosity 132.29228906740042
//D/BITMAP_CHECK: time 445
//D/BITMAP_CHECK: averageContrast 45.64763712659828
//D/BITMAP_CHECK: averageLuminosity 204.28250196139513
//D/BITMAP_CHECK: time 401
//D/BITMAP_CHECK: averageContrast 42.31242803059032
//D/BITMAP_CHECK: averageLuminosity 218.010238519622
//D/BITMAP_CHECK: time 375
//D/BITMAP_CHECK: averageContrast 44.164406924422046
//D/BITMAP_CHECK: averageLuminosity 148.0994081423716
//D/BITMAP_CHECK: time 378
//D/BITMAP_CHECK: averageContrast 45.66435593268739
//D/BITMAP_CHECK: averageLuminosity 163.58246250817712
//D/BITMAP_CHECK: time 378

fun isImageContrastAndLuminosityGoodEnough(
    bitmap: Bitmap,
    contrastThreshold: Double = 0.3,
    luminosityThreshold: Double = 0.5
): Boolean {
    val runTIme1 = System.currentTimeMillis()
    val width = bitmap.width
    val height = bitmap.height
    val pixelCount = width * height

    // Calculate average luminosity
    var sumLuminosity = 0.0
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = bitmap.getPixel(x, y)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            val luminosity = 0.2126 * r + 0.7152 * g + 0.0722 * b // ITU-R BT.709 formula
            sumLuminosity += luminosity
        }
    }
    val averageLuminosity = sumLuminosity / pixelCount

    // Calculate contrast
    var contrastSum = 0.0
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = bitmap.getPixel(x, y)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            val luminosity = 0.2126 * r + 0.7152 * g + 0.0722 * b // ITU-R BT.709 formula
            val contrast = Math.abs(luminosity - averageLuminosity)
            contrastSum += contrast
        }
    }
    val averageContrast = contrastSum / pixelCount
    val runTIme2 = System.currentTimeMillis()

//    Log.d("BITMAP_CHECK", "averageContrast $averageContrast")
//    Log.d("BITMAP_CHECK", "averageLuminosity $averageLuminosity")
//    Log.d("BITMAP_CHECK", "time ${runTIme2 - runTIme1}")
    // Return true if contrast and luminosity are above thresholds
    return averageContrast >= contrastThreshold && averageLuminosity >= luminosityThreshold
}



fun isImageContrastAndLuminosityGoodEnoughOpt1(
    bitmap: Bitmap,
    contrastThreshold: Double = 0.3,
    luminosityThreshold: Double = 0.5
): Boolean {
    val runTIme1 = System.currentTimeMillis()
    val width = bitmap.width
    val height = bitmap.height
//    val pixelCount = width * height


//    val pixMemoisation = Map<>

    val lumnosityList = mutableListOf<Double>()

    val h1 = height/4
    val h2 = height - h1

    val w1 = width/4
    val w2 = width - w1


//    Log.d("BITMAP_CHECK", "height  $height")
//    Log.d("BITMAP_CHECK", "width  $width")
//    Log.d("BITMAP_CHECK", "h2  $h2")
//    Log.d("BITMAP_CHECK", "w2  $w2")


    val pixelCount = w1 * h1

    // Calculate average luminosity
    var sumLuminosity = 0.0
    for (y in h1 until h2) {
        for (x in w1 until w2) {
            val pixel = bitmap.getPixel(x, y)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            val luminosity = 0.2126 * r + 0.7152 * g + 0.0722 * b // ITU-R BT.709 formula
            lumnosityList.add(luminosity)
            sumLuminosity += luminosity
        }
    }
    val averageLuminosity = sumLuminosity / pixelCount

    // Calculate contrast
//    var contrastSum = 0.0



    val averageLuminance = sumLuminosity / (w2 * h2)

    // Calculate the contrast ratio between the average luminance and the maximum and minimum luminance values
    val maxLuminance = 255.0
    val minLuminance = 0.0
    val contrastRatio = (maxLuminance - averageLuminance) / (averageLuminance - minLuminance)




//    lumnosityList.forEach {
//        val contrast = abs(it - averageLuminosity)
//        contrastSum += contrast
//    }

//    for (y in h1 until h2) {
//        for (x in w1 until w2) {
//            val pixel = bitmap.getPixel(x, y)
//            val r = Color.red(pixel)
//            val g = Color.green(pixel)
//            val b = Color.blue(pixel)
//            val luminosity = 0.2126 * r + 0.7152 * g + 0.0722 * b // ITU-R BT.709 formula
//            val contrast = Math.abs(luminosity - averageLuminosity)
//            contrastSum += contrast
//        }
//    }
//    val averageContrast = contrastSum / pixelCount
    val runTIme2 = System.currentTimeMillis()

//    Log.d("BITMAP_CHECK", "averageContrast $contrastRatio")
//    Log.d("BITMAP_CHECK", "averageLuminosity $averageLuminosity")
//    Log.d("BITMAP_CHECK", "time ${runTIme2 - runTIme1}")
    // Return true if contrast and luminosity are above thresholds
    return contrastRatio >= contrastThreshold && averageLuminosity >= luminosityThreshold
}



//suspend fun isImageContrastAndLuminosityGoodEnoughCoroutines(
//    scope: CoroutineScope,
//    bitmap: Bitmap,
//    contrastThreshold: Double = 0.3,
//    luminosityThreshold: Double = 0.5
//): Boolean {
//
//    val runTIme1 = System.currentTimeMillis()
//
//    val width = bitmap.width
//    val height = bitmap.height
//    val pixelCount = width * height
//
//    val sumList = mutableListOf<Deferred<Double>>()
//
////    var sumLuminosity = AtomicReference<Double>(0.0)
//    var sumLuminosity = 0.0
//
//    for (y in 0 until height) {
//        for (x in 0 until width) {
//            val ask = scope.async {
//                val pixel = bitmap.getPixel(x, y)
//                val r = Color.red(pixel)
//                val g = Color.green(pixel)
//                val b = Color.blue(pixel)
//                return@async 0.2126 * r + 0.7152 * g + 0.0722 * b // ITU-R BT.709 formula
//            }
//            sumList.add(ask)
//        }
//    }
//    sumList.forEach {
//        sumLuminosity += it.await()
//    }
//    val averageLuminosity = sumLuminosity / pixelCount
//    var contrastSum = 0.0
//    val sumContrastList = mutableListOf<Deferred<Double>>()
//
//    for (y in 0 until height) {
//        for (x in 0 until width) {
//
//            val ask = scope.async {
//
//                val pixel = bitmap.getPixel(x, y)
//                val r = Color.red(pixel)
//                val g = Color.green(pixel)
//                val b = Color.blue(pixel)
//                val luminosity = 0.2126 * r + 0.7152 * g + 0.0722 * b // ITU-R BT.709 formula
//                return@async Math.abs(luminosity - averageLuminosity)
//            }
//            sumContrastList.add(ask)
//        }
//    }
//
//    sumList.forEach {
////        val calcVal = it.await()
//        contrastSum +=  it.await()
////        contrastSum.getAndUpdate { oldValue -> oldValue + calcVal }
//    }
//    val averageContrast = contrastSum / pixelCount
//
//    val runTIme2 = System.currentTimeMillis()
//    Log.d("BITMAP_CHECK", "averageContrast $averageContrast")
//    Log.d("BITMAP_CHECK", "averageLuminosity $averageLuminosity")
//    Log.d("BITMAP_CHECK", "time ${runTIme2 - runTIme1}")
//
//    // Return true if contrast and luminosity are above thresholds
//    return averageContrast >= contrastThreshold && averageLuminosity >= luminosityThreshold
//}
