package com.facecool.ui.camera.camerax

import android.content.Context
import android.util.Log
import android.view.Surface.ROTATION_0
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.facecool.ui.camera.CameraMode
import com.facecool.ui.camera.FaceDetectorCallback
import com.facecool.ui.camera.face_detection.FaceContourDetectionProcessor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.mlkit.vision.face.FaceDetectorOptions


class CameraManager(
    private val context: Context,
    private val finderView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val faceDetectorCallback: FaceDetectorCallback,
    private val graphicOverlay: GraphicOverlay
) {

    private var preview: Preview? = null
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelectorOption = CameraSelector.LENS_FACING_FRONT
    private var cameraProvider: ProcessCameraProvider? = null

    private var imageAnalyzer: ImageAnalysis? = null

    private val analizer = selectAnalyzer()

//    private val imageCapture = ImageCapture.Builder().build()

    init {
        createNewExecutor()
    }

    fun getCameraFacing() = cameraSelectorOption

    private fun createNewExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun startCamera(rotation: Int = ROTATION_0) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder()
                    .setTargetRotation(rotation)
                    .build()
                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetRotation(rotation)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, analizer)
                    }
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorOption)
                    .build()
                setCameraConfig(cameraProvider, cameraSelector)
            }, ContextCompat.getMainExecutor(context)
        )
    }

    private fun selectAnalyzer(): FaceContourDetectionProcessor {
        return FaceContourDetectionProcessor(faceDetectorCallback)
    }

    private fun setCameraConfig(
        cameraProvider: ProcessCameraProvider?,
        cameraSelector: CameraSelector
    ) {
        try {
            cameraProvider?.unbindAll()
//            preview?.targetRotation = ROTATION_0
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer,
            )
            preview?.setSurfaceProvider(
                finderView.surfaceProvider
            )
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    fun changeCameraSelector(rotation: Int = ROTATION_0) {
        cameraProvider?.unbindAll()
        cameraSelectorOption =
            if (cameraSelectorOption == CameraSelector.LENS_FACING_BACK)
                CameraSelector.LENS_FACING_FRONT
            else
                CameraSelector.LENS_FACING_BACK
        graphicOverlay.toggleSelector()
        startCamera(rotation)
    }

    fun changeCameraSelector(mode: com.facecool.ui.common.CameraMode, rotation: Int = ROTATION_0){
        if (mode == com.facecool.ui.common.CameraMode.KIOSK && cameraSelectorOption == CameraSelector.LENS_FACING_FRONT) return
        if (mode == com.facecool.ui.common.CameraMode.MANUAL && cameraSelectorOption == CameraSelector.LENS_FACING_BACK) return
        cameraProvider?.unbindAll()
        if (mode == com.facecool.ui.common.CameraMode.KIOSK){
            cameraSelectorOption = CameraSelector.LENS_FACING_FRONT
        }else{
            cameraSelectorOption = CameraSelector.LENS_FACING_BACK
        }
        graphicOverlay.toggleSelector(mode)
        startCamera(rotation)
    }

//    fun takePicture(
//        externalMediaDirs: Array<File>,
//        resources: Resources,
//        filesDir: File,
//        photo: (Bitmap, File) -> Unit
//    ) {
//        fun getOutputDirectory(): File {
//            val mediaDir = externalMediaDirs.firstOrNull()?.let {
//                File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
//            }
//            return if (mediaDir != null && mediaDir.exists())
//                mediaDir else filesDir
//        }
//
//        val photoFile = File(
//            getOutputDirectory(),
//            System.currentTimeMillis().toString() + ".jpg"
//        )
//
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//
//        imageCapture.takePicture(
//            outputOptions,
//            cameraExecutor,
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                    val savedUri = Uri.fromFile(photoFile)
//                    val bitmap2 =
//                        MediaStore.Images.Media.getBitmap(context.contentResolver, savedUri)
//                    photo(bitmap2, photoFile)
//                }
//
//                override fun onError(exc: ImageCaptureException) {
//                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//                }
//            }
//        )
//    }

    fun stopCamera() {
        try {
            analizer.stop()
            cameraExecutor.shutdown()
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var mode = CameraMode.IDLE

    fun setMode(mode: CameraMode?) {
        this.mode = mode ?: CameraMode.IDLE
    }

    fun getMode()  = this.mode

    companion object {
        private const val TAG = "CameraXBasic"
    }

//    override fun isIdle(): Boolean {
//        Log.d("CAMERAIDLE ", " CM : is : isIdle() = [${mode == CameraMode.RECORDING}]")
//        if (mode == CameraMode.RECORDING) return false
//        return true
//    }

}
