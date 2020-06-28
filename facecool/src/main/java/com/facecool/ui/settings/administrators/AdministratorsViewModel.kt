package com.facecool.ui.settings.administrators

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.cool.databasa.Database
import com.face.cool.databasa.administrators.AdministratorEntity
import com.face.cool.manualsync.bitMapToString
import com.facecool.attendance.facedetector.FaceDetectionEngine
import com.facecool.ui.camera.FaceDetectorCallback
import com.facecool.ui.camera.camerax.BaseImageAnalyzer
import com.facecool.ui.camera.face_detection.FaceContourDetectionProcessor
import com.facecool.ui.common.ProgressStatus
import com.facecool.utils.toLiveData
import com.google.mlkit.vision.face.Face
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdministratorsViewModel @Inject constructor(
    private val database: Database,
    private val faceDetectionEngine: FaceDetectionEngine,
    private val app: Application,
) : ViewModel() {
    
    private val _admins = MutableLiveData<List<AdministratorEntity>>()
    val admins = _admins.toLiveData()

    private val _generalProgress = MutableLiveData<ProgressStatus>()
    val generalProgress = _generalProgress

    private val cachedAdminList = mutableListOf<AdministratorEntity>()
    
//    private val processor = FaceContourDetectionProcessor(this, this)
    
    fun onAdminClicked(admin: AdministratorEntity) {

    }

    fun getAdmins() {
        _generalProgress.postValue(ProgressStatus.LOADING)
        viewModelScope.launch(IO) {
            val admins = database.adminDao().getAll()
            cachedAdminList.clear()
            cachedAdminList.addAll(admins)
            _admins.postValue(admins)
            _generalProgress.postValue(ProgressStatus.DONE)
        }
    }

    fun searchForAdmin(searchTerm: String) {
        if (searchTerm.isEmpty()) {
            _admins.postValue(cachedAdminList)
        } else {
            val matchAdmins = cachedAdminList.filter {
                (it.name?.lowercase() + it.lastName?.lowercase()).contains(searchTerm.lowercase())
            }
            _admins.postValue(matchAdmins)
        }
    }

    fun removeAdmin(admin: AdministratorEntity) {
        viewModelScope.launch(IO) {
            database.adminDao().delete(admin)
            getAdmins()
        }
    }
//
//    private val lst = mutableMapOf<Bitmap, String>()
//
//
//    override fun onFaceDetected(results: List<Face>, toBitmap: Bitmap) {
//
//        viewModelScope.launch(IO) {
//            val name = lst.remove(toBitmap) ?: ""
//            results.forEach {
//                val model = generateAdministratorEntity(it, toBitmap, name)
//                database.adminDao().insert(model)
//                Log.d("TAG_R", "saved model : ${model.name}   \n")
//            }
//            toBitmap.recycle()
//        }
//    }
//
//    private suspend fun generateAdministratorEntity(
//        face: Face,
//        bitmapWithContainingFace: Bitmap,
//        name: String,
//        lastName: String,
//        phoneNumber: String,
//        email: String,
//        pin: String,
//    ): AdministratorEntity {
//        try {
//            val faceModel = faceDetectionEngine.getFaceFeature(bitmapWithContainingFace, face)
//            val creationTime = System.currentTimeMillis()
//
//            return AdministratorEntity(
//                null,
//                name,
//                faceModel.getFaceImage()?.bitMapToString(),
//                Gson().toJson(faceModel.getDetectedFace(), Face::class.java),
//                faceModel.getRealLiveProbability(),
//                faceModel.getDetectedFeatures(),
//                email,
//                lastName,
//                creationTime,
//                phoneNumber,
//                pin
//            )
//        } catch (e: Exception) {
//            return AdministratorEntity()
//        }
//    }
//
//
//    override fun onFaceDetected(results: Face, toBitmap: Bitmap) {
//
//    }
//
//    override fun onOutlineDetected(results: List<Face>, rect: Rect) {
//    }
//
//    override fun isIdle() = false

}
