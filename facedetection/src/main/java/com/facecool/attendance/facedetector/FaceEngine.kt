package com.facecool.attendance.facedetector

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.Keep
import org.json.JSONArray
import org.json.JSONException
import java.io.*
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.sqrt

internal object FaceEngine {
    init {
        System.loadLibrary("faceengine")
    }

    fun loadLiveModel(assetsManager: AssetManager): Int {
        val listConfigs = parseConfig(assetsManager)
        return nativeLoadLiveModel(assetsManager, listConfigs)
    }

    fun SetCheckLiveness(isCheckLiveness: Boolean) {
        nativeSetCheckLiveness(isCheckLiveness)
    }

    fun detectLive(
        data: ByteBuffer,
        w: Int,
        h: Int,
        orientation: Int,
        lFace: Int,
        tFace: Int,
        rFace: Int,
        bFace: Int
    ): Float {
        return nativeDetectYuv(data.array(), w, h, orientation, lFace, tFace, rFace, bFace)
    }

    fun detect_bmp(bmp: Bitmap, lFace: Int, tFace: Int, rFace: Int, bFace: Int): Float {
        return nativeDetectBmp(bmp, lFace, tFace, rFace, bFace)
    }

    fun loadFeatureModel(con: Context): Int {
        val sPathModel = writeFileToPrivateStorage("model.prototxt", con)
        val sPathWeight = writeFileToPrivateStorage("weight.caffemodel", con)
        return if (sPathModel == "" || sPathWeight == "") -1 else nativeLoadFeatureModelFromFile(
            sPathModel,
            sPathWeight
        )
    }

    fun extractLiveFeature(
        bmp: Bitmap,
        lFace: Int,
        tFace: Int,
        rFace: Int,
        bFace: Int,
        landmarksX: FloatArray,
        landmarksY: FloatArray,
        fd: FaceData
    ) {
        val fvalues = FloatArray(128)
        val live = nativeExtractLiveFeature(
            bmp,
            lFace,
            tFace,
            rFace,
            bFace,
            landmarksX,
            landmarksY,
            fvalues
        )
        fd.isRealLiveProbability = live
        fd.faceFeatures = floatsToBytes(fvalues)
    }

    fun getBrightness(bmp: Bitmap): Float{
        return 100-nativeGetDarknessWithHistogram(bmp)
    }
    fun getSharpness(bmp: Bitmap): Float{
        return nativeGetSharpnessWithCustom(bmp)
//        return nativeGetSharpness(bmp)
    }

    fun getSimilarity(ft1: ByteArray, ft2: ByteArray): Float {
        val feature1 = bytesToFloats(ft1)
        val feature2 = bytesToFloats(ft2)
//        return nativeSimilarity(feature1, feature2, 128)
        return calculateSimilarity(feature1, feature2, 128)
    }

    private fun calculateSimilarity(f1:FloatArray, f2:FloatArray, len:Int): Float{
        if (f1.size == 0 || f2.size == 0) return 0f
        var dot_product = 0f
        var len1 = 0f
        var len2 = 0f
        var multi_len = 0f
        for (i in 0 until len){
            dot_product += f1[i] * f2[i]
            len1 += f1[i] * f1[i]
            len2 += f2[i] * f2[i]
        }
        len1 = sqrt(len1)
        len2 = sqrt(len2)
        multi_len = len1 * len2
        var sim = abs(dot_product/multi_len)
        sim += 0.1f
        if (sim>1) sim = 1f

        return sim
    }

    private fun parseConfig(assetManager: AssetManager): List<ModelConfig> {
        val listConfig: MutableList<ModelConfig> = ArrayList()
        var line = ""
        line = try {
            val `is` = assetManager.open("live/config.json")
            val br = BufferedReader(InputStreamReader(`is`))
            br.readLine()
        } catch (e: IOException) {
            return listConfig
        }
        if (line == "") return listConfig
        try {
            val jsonArray = JSONArray(line)
            for (i in 0 until jsonArray.length()) {
                val config = jsonArray.getJSONObject(i)
                val modelConfig = ModelConfig()
                modelConfig.name = config.optString("name")
                modelConfig.width = config.optInt("width")
                modelConfig.height = config.optInt("height")
                modelConfig.scale = config.optDouble("scale").toFloat()
                modelConfig.shift_x = config.optDouble("shift_x").toFloat()
                modelConfig.shift_y = config.optDouble("shift_y").toFloat()
                modelConfig.org_resize = config.optBoolean("org_resize")
                listConfig.add(modelConfig)
            }
        } catch (jsonEx: JSONException) {
        }
        return listConfig
    }

    // Upload file to storage and return a path.
    private fun writeFileToPrivateStorage(file: String, context: Context): String {
        val assetManager = context.assets
        var inputStream: BufferedInputStream? = null
        try {
            // Read data from assets.
            inputStream = BufferedInputStream(assetManager.open(file))
            val data = ByteArray(inputStream.available())
            inputStream.read(data)
            inputStream.close()
            // Create copy file in storage.
            val outFile = File(context.filesDir, file)
            val os = FileOutputStream(outFile)
            os.write(data)
            os.close()
            // Return a path to file which may be read in common way.
            return outFile.absolutePath
        } catch (ex: IOException) {
//			Constants.LogDebug("Failed to upload a file");
        }
        return ""
    }

    fun floatsToBytes(floats: FloatArray): ByteArray {
        val bytes = ByteArray(java.lang.Float.BYTES * floats.size)
        ByteBuffer.wrap(bytes).asFloatBuffer().put(floats)
        return bytes
    }

    fun bytesToFloats(bytes: ByteArray): FloatArray {
        if (bytes.size % java.lang.Float.BYTES != 0) throw RuntimeException("Illegal length")
        val floats = FloatArray(bytes.size / java.lang.Float.BYTES)
        ByteBuffer.wrap(bytes).asFloatBuffer()[floats]
        return floats
    }

    @Keep
    private external fun nativeLoadLiveModel(
        assetsManager: AssetManager,
        modelConfig: List<ModelConfig>
    ): Int

    @Keep
    private external fun nativeSetCheckLiveness(isCheckLiveness: Boolean)
    @Keep
    private external fun nativeDetectYuv(
        yuv: ByteArray, previewWidth: Int, previewHeight: Int,
        orientation: Int, leftFace: Int, topFace: Int, rightFace: Int, bottomFace: Int
    ): Float

    @Keep
    private external fun nativeDetectBmp(
        bmp: Bitmap,
        leftFace: Int,
        topFace: Int,
        rightFace: Int,
        bottomFace: Int
    ): Float

    @Keep
    private external fun nativeLoadFeatureModelFromFile(modelpath: String, weightpath: String): Int
    @Keep
    private external fun nativeLoadFeatureModel(
        pModel: ByteArray,
        lenModel: Int,
        pWeight: ByteArray,
        lenWeight: Int
    ): Int

    @Keep
    private external fun nativeSimilarity(
        vFeat1: FloatArray,
        vFeat2: FloatArray,
        feature_len: Int
    ): Float

    @Keep
    private external fun nativeExtractLiveFeature(
        bmp: Bitmap, leftFace: Int, topFace: Int, rightFace: Int, bottomFace: Int,
        landmarksX: FloatArray, landmarksY: FloatArray, features: FloatArray
    ): Float

    @Keep
    private external fun nativeGetSharpness( bmp: Bitmap): Float
    @Keep
    private external fun nativeGetSharpnessWithCustom( bmp: Bitmap): Float
    @Keep
    private external fun nativeGetDarknessWithHistogram(bmp: Bitmap): Float
}