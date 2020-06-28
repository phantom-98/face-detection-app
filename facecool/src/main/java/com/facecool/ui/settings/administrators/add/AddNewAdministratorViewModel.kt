package com.facecool.ui.settings.administrators.add

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.cool.cache.Cache
import com.face.cool.databasa.Database
import com.face.cool.databasa.administrators.AdministratorEntity
import com.face.cool.manualsync.bitMapToString
import com.face.cool.manualsync.stringToBitMap
import com.facecool.R
import com.facecool.attendance.facedetector.FaceData
import com.facecool.attendance.facedetector.FaceDetectionEngine
import com.facecool.ui.camera.FaceDetectorCallback
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.camera.camerax.BaseImageAnalyzer
import com.facecool.ui.camera.face_detection.FaceContourDetectionProcessor
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.utils.asLiveData
import com.facecool.utils.liveEvent
import com.facecool.utils.loadUriIe
import com.facecool.utils.update
import com.google.gson.Gson
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNewAdministratorViewModel @Inject constructor(
    private val database: Database,
    private val userRepository: UserRepository,
    private val app: Application,
    private val faceDetectionEngine: FaceDetectionEngine,
    private val cache: Cache
) : ViewModel(), FaceDetectorCallback {

    private val _userData = liveEvent<AdministratorEntity>()
    val userData = _userData.asLiveData()

    private val _endEvent = liveEvent<Unit>()
    val endEvent = _endEvent.asLiveData()

    private val _nullUserData = liveEvent<Unit>()
    val nullUserData = _nullUserData.asLiveData()

    private val _profileImageLoading = liveEvent<ProgressStatus>()
    val profileImageLoading = _profileImageLoading.asLiveData()


    private var newUserData: AdministratorEntity? = null
    var currentFace: Face? = null

    private val processor = FaceContourDetectionProcessor(this)

    fun setExistingData(id: Long){
        viewModelScope.launch {
            var data = database.adminDao().getById(id)
//            Log.d("admin_table", "read record id="+data.first().uid)
            if (data.isNotEmpty()){
                loadImageFromBitmap(data.first().base64Image?.stringToBitMap()!!)
                setExistingData(data.first())
            }
        }
    }

    fun setExistingData(btm: Bitmap){
        viewModelScope.launch {
            loadImageFromBitmap(btm)
        }
    }
    fun setExistingData(model: CameraDetectionModel){
        var entity = generateAdministratorEntity(model)
        setExistingData(entity)
    }

    fun setExistingData(data: AdministratorEntity?) {
        if (newUserData!=null && newUserData?.uid != null){
            data?.uid = newUserData?.uid
            data?.creationTime = newUserData?.creationTime
        }
        newUserData = data
        if (newUserData == null) {
            _nullUserData.update(Unit)
        } else {
            newUserData?.let { _userData.update(it) }
        }
    }

    fun updateName(name: String) {
        initIfNUll()
        newUserData?.name = name
    }

    fun updateLastName(lastName: String) {
        initIfNUll()
        newUserData?.lastName = lastName
    }

    fun updateEmail(email: String) {
        initIfNUll()
        newUserData?.email = email
    }

    fun updatePhone(phone: String) {
        initIfNUll()
        newUserData?.phoneNumber = phone
    }

    fun updatePin(pin: String) {
        initIfNUll()
        newUserData?.pin = pin
    }

    fun saveItem() {
        viewModelScope.launch(Dispatchers.IO) {
            newUserData?.let {
                var res: String = ""

                var faceModel = FaceData(currentFace!!, it.realLiveProbability!!, it.base64Image?.stringToBitMap()!!)
                faceModel.faceFeatures = it.detectedFeatures
                res = faceModel?.let { it1 ->
                    if (faceDetectionEngine.checkFaceQuality(it1) < cache.getImageQualityThreshold()) {
                        app.getString(R.string.image_quality_low)
                    } else {
                        "OK"
                    }
                }!!
                if (res == "OK") {
                    res = faceModel?.let { it1 ->
                        if (userRepository.checkIfAdmin(it1.getDetectedFeatures())=="OK") "OK" else app.getString(R.string.face_already_exist)
                    }!!
                    if (res == "OK"){
//                        Log.d("admin_table", "saved record id="+it.uid)
                        database.adminDao().insert(it)
                    }
                }

                if (res != "OK"){
                    android.os.Handler(Looper.getMainLooper()).post {
                        Toast.makeText(app.applicationContext, res, Toast.LENGTH_SHORT).show()
                    }
                }
                if (currentFace != null) _endEvent.update(Unit)
            }
        }
    }

    private fun initIfNUll() {
        if (newUserData == null) {
            newUserData = AdministratorEntity(
                creationTime = System.currentTimeMillis()
            )
        }
    }

    fun loadImageFromUri(pictureUri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            _profileImageLoading.update(ProgressStatus.LOADING)
            val btm = pictureUri?.let { app.loadUriIe(it) }
            if (btm != null) {
                // will call onFaceDetected method
                processor.analyze(btm, 0)
            } else {
                _profileImageLoading.update(ProgressStatus.DONE)
            }
        }
    }

    fun loadImageFromBitmap(btm: Bitmap){
        viewModelScope.launch {
            _profileImageLoading.update(ProgressStatus.LOADING)

            if (btm != null) {
                // will call onFaceDetected method
                processor.analyze(btm, 0)
            } else {
                _profileImageLoading.update(ProgressStatus.DONE)
            }
        }
    }

    override fun onFaceDetected(results: Face, toBitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            val model = generateAdministratorEntity(results, toBitmap)
            setExistingData(model)
            _profileImageLoading.update(ProgressStatus.DONE)
        }
    }

    override fun onError(e: java.lang.Exception) {
        super.onError(e)
        _profileImageLoading.update(ProgressStatus.ERROR)
    }

    override fun onFaceDetected(results: List<Face>, toBitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            if (results.isNotEmpty()) {
                val model = generateAdministratorEntity(results.first(), toBitmap)
                setExistingData(model)
                _profileImageLoading.update(ProgressStatus.DONE)

            } else {
                setExistingData(null)
                _profileImageLoading.update(ProgressStatus.ERROR)
            }
        }
    }

    override fun onOutlineDetected(results: List<Face>, rect: Rect) = Unit

    private suspend fun generateAdministratorEntity(
        face: Face,
        bitmapWithContainingFace: Bitmap
    ): AdministratorEntity {
        try {
            var faceModel = faceDetectionEngine.getFaceFeature(bitmapWithContainingFace, face)
            val creationTime = System.currentTimeMillis()
            val gson = Gson()
            currentFace = face
            return AdministratorEntity(
                null,
                newUserData?.name,
                faceModel.getFaceImage()?.bitMapToString(),
                gson.toJson(face),
                faceModel.getRealLiveProbability(),
                faceModel.getDetectedFeatures(),
                newUserData?.email,
                newUserData?.lastName,
                creationTime,
                newUserData?.phoneNumber,
                newUserData?.pin
            )
        } catch (e: Exception) {
            return AdministratorEntity()
        }
    }
    private fun generateAdministratorEntity(cameraModel: CameraDetectionModel): AdministratorEntity{
        val creationTime = System.currentTimeMillis()
        val gson = Gson()
        currentFace = cameraModel.rez?.getDetectedFace()
        return AdministratorEntity(
            null,
            newUserData?.name,
            cameraModel.rez?.getFaceImage()?.bitMapToString(),
            gson.toJson(currentFace),
            cameraModel.rez?.getRealLiveProbability(),
            cameraModel.rez?.getDetectedFeatures(),
            newUserData?.email,
            newUserData?.lastName,
            creationTime,
            newUserData?.phoneNumber,
            newUserData?.pin
        )
    }

}