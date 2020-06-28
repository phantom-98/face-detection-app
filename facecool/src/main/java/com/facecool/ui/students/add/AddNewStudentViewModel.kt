package com.facecool.ui.students.add

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Looper
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.cool.cache.Cache
import com.face.cool.databasa.Database
import com.face.cool.databasa.users.ErrorStatus
import com.facecool.R
import com.facecool.attendance.facedetector.FaceDetectionEngine
import com.facecool.ui.camera.FaceDetectorCallback
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.getNullCameraDetectionModel
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.camera.camerax.BaseImageAnalyzer
import com.facecool.ui.camera.face_detection.FaceContourDetectionProcessor
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.utils.asLiveData
import com.facecool.utils.liveEvent
import com.facecool.utils.loadUriIe
import com.facecool.utils.toLiveData
import com.facecool.utils.update
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AddNewStudentViewModel @Inject constructor(
//    private val database: Database,
    private val userRepository: UserRepository,
    private val app: Application,
    private val faceDetectionEngine: FaceDetectionEngine,
    private val cache: Cache
) : ViewModel(), FaceDetectorCallback {

    companion object {

        private const val STUDENT_ID = 0
        private const val STUDENT_NAME = 1
        private const val STUDENT_LAST_NAME = 2

        private fun queryName(app: Application, uri: Uri): String? {
            val resolver = app.contentResolver
            val returnCursor = resolver.query(uri, null, null, null, null)
            val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex == null) {
                returnCursor?.close()
                return null
            }
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            return name
        }

        private fun getAutomaticFields(string: String): Map<Int, String>? {
            val parts = string.split("_").toMutableList()
            if (parts.size != 3) {
                return null
            }
            var lastName = parts.lastOrNull() ?: ""
            lastName = lastName.split(".").firstOrNull() ?: ""
            parts.removeLastOrNull()
            parts.add(lastName)
            val map = mutableMapOf<Int, String>()
            map[STUDENT_ID] = parts.getOrNull(0) ?: ""
            map[STUDENT_NAME] = parts.getOrNull(1) ?: ""
            map[STUDENT_LAST_NAME] = parts.getOrNull(2) ?: ""
            return map
        }

    }

    private val _userData = liveEvent<CameraDetectionModel>()
    val userData = _userData.asLiveData()

    private val _nullUserData = liveEvent<Unit>()
    val nullUserData = _nullUserData.asLiveData()

    private val _endEvent = liveEvent<Unit>()
    val endEvent = _endEvent.asLiveData()

    private val _automaticId = liveEvent<String>()
    val automaticId = _automaticId.asLiveData()

    private val _automaticName = liveEvent<String>()
    val automaticName = _automaticName.asLiveData()

    private val _automaticLastName = liveEvent<String>()
    val automaticLastName = _automaticLastName.asLiveData()

    private val _profileImageLoading = liveEvent<ProgressStatus>()
    val profileImageLoading = _profileImageLoading.asLiveData()

    private val _remainingUsers = liveEvent<List<CameraDetectionModel>>()
    val remainingUsers = _remainingUsers.asLiveData()

    private val _imgQuality = liveEvent<Int>()
    val imgQuality = _imgQuality.toLiveData()

    private var newUserData: CameraDetectionModel? = null

    private var autoDetectedFields = mutableMapOf<Int, String>()

    private val processor = FaceContourDetectionProcessor(this)

    fun setExistingData(data: CameraDetectionModel?) {
        newUserData = data
        if (newUserData == null) {
            _nullUserData.update(Unit)
            _automaticId.update("")
            _automaticName.update("")
            _automaticLastName.update("")
        } else {
            newUserData?.let {
                _userData.update(it)
                it.rez?.let {
                    viewModelScope.launch { _imgQuality.update(faceDetectionEngine.checkFaceQuality(it)) }
                }
            }
        }
    }

    fun updateName(name: String) {
        initIfNUll()
        newUserData?.name = name
        autoDetectedFields[STUDENT_NAME] = name
    }

    fun updateLastName(lastName: String) {
        initIfNUll()
        newUserData?.lastName = lastName
        autoDetectedFields[STUDENT_LAST_NAME] = lastName
    }

    fun updateEmail(email: String) {
        initIfNUll()
        newUserData?.email = email
    }

    fun updateTwin(twin: Boolean){
        initIfNUll()
        newUserData?.twin = twin
    }

    fun updateAddress(address: String) {
        initIfNUll()
        newUserData?.address = address
    }

    fun updateId(id: String) {
        initIfNUll()
        newUserData?.studentId = id
        autoDetectedFields[STUDENT_ID] = id

    }

    fun updatePhoneNumber(phone: String) {
        newUserData?.phoneNumber = phone
    }
    fun updateEnableNotification(enable: Boolean) {
        newUserData?.enableNotification = enable
    }
    fun updateParentNames(names: String) {
        newUserData?.parentNames = names
    }
    fun updateEnableSMS(enable: Boolean) {
        newUserData?.enableSMS = enable
    }
    fun updateEnableEmail(enable: Boolean) {
        newUserData?.enableEmail = enable
    }
    fun updateEnableWhatsapp(enable: Boolean) {
        newUserData?.enableWhatsapp = enable
    }
    fun updateSMSNumbers(number: String){
        newUserData?.parentSMSNumbers = number
    }
    fun updateEmailAddress(email: String) {
        newUserData?.parentEmails = email
    }
    fun updateWhatsappNumbers(number: String) {
        newUserData?.parentWhatsappNumbers = number
    }
    fun saveItem() {
        viewModelScope.launch(IO) {
            newUserData?.let {
                var res: String = it.rez?.let { it1 ->
                    if (faceDetectionEngine.checkFaceQuality(it1) < cache.getImageQualityThreshold()){
                        app.getString(R.string.image_quality_low)
                    } else {
                        "OK"
                    }
                }!!
                if (res == "OK") {
                    res = newUserData!!.rez?.let { it1 -> userRepository.checkIfEnrolled(it1.getDetectedFeatures(), it.twin) }!!
                    if (res == "OK"){
                        updateFields()
                        userRepository.saveUser(it, EnrollmentStatus.ENROLLED)
                    } else {
                        res = app.getString(R.string.user_already_present_in_db)
                        it.id = newUserData!!.rez?.let { it1 -> userRepository.checkIfRejected(it1.getDetectedFeatures(), it.twin) }
                        it.error.add(ErrorStatus.FACE_ALREADY_ENROLLED)
                    }
                } else {
                    it.error.add(ErrorStatus.IMAGE_QUALITY_IS_LOW)
                }
                if (res != "OK"){
                    android.os.Handler(Looper.getMainLooper()).post {
                        Toast.makeText(app.applicationContext, res, Toast.LENGTH_LONG).show()
                    }
                    updateFields()
                    userRepository.saveUser(it, EnrollmentStatus.REJECTED)
                }
            }
            _endEvent.update(Unit)
        }
    }

    private fun updateFields() {
        autoDetectedFields.let {
            it.get(STUDENT_ID)?.let { field ->
//                newUserData?.studentId?.let {
                    updateId(field)
                    _automaticId.update(field)
//                }
            }
            it.get(STUDENT_NAME)?.let { field ->
//                newUserData?.name?.let {
                    updateName(field)
                    _automaticName.update(field)
//                }
            }
            it.get(STUDENT_LAST_NAME)?.let { field ->
//                newUserData?.lastName?.let {
                    updateLastName(field)
                    _automaticLastName.update(field)
//                }
            }
        }
    }

    private fun initIfNUll() {
        if (newUserData == null) {
            newUserData = CameraDetectionModel(
                "",
                EnrollmentStatus.UNKNOWN,
                null,
                0f,
                null,
                "",
                System.currentTimeMillis()
            )
        }
    }

    fun loadImageFromUri(pictureUri: Uri?) {
        viewModelScope.launch(IO) {
            autoDetectedFields.clear()
            checkForFormatedName(pictureUri)
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

    private fun checkForFormatedName(pictureUri: Uri?) {
        val imageName = pictureUri?.let { queryName(app, it) } ?: ""
        getAutomaticFields(imageName)?.let {
            autoDetectedFields = it.toMutableMap()
        }
        updateFields()
    }

    fun getUsersFromGalery(galeryPhotoUri: Uri?) {
        loadImageFromUri(galeryPhotoUri)
    }

//    override fun isIdle(): Boolean {
//        return false
//    }

    override fun onFaceDetected(results: Face, toBitmap: Bitmap) {
        viewModelScope.launch(IO) {
            val model = generateCameraDetectionModel(results, toBitmap)
            setExistingData(model)
            _profileImageLoading.update(ProgressStatus.DONE)
        }
    }

    override fun onError(e: java.lang.Exception) {
        super.onError(e)
        _profileImageLoading.update(ProgressStatus.ERROR)
    }

    override fun onFaceDetected(results: List<Face>, toBitmap: Bitmap) {
        viewModelScope.launch(IO) {
            if (results.size == 1) {
                results.firstOrNull()?.let {
                    val model = generateCameraDetectionModel(it, toBitmap)
                    setExistingData(model)
                }
                _profileImageLoading.update(ProgressStatus.DONE)
            } else if (results.size > 1) {
                results.firstOrNull()?.let {
                    val model = generateCameraDetectionModel(it, toBitmap)
                    setExistingData(model)
                }
                val remaining = results.toMutableList()
                remaining.removeFirstOrNull()
                val remainingPairs = remaining.map {
                    val rez = faceDetectionEngine.getFaceFeature(toBitmap, it)
                    val creationTime = System.currentTimeMillis()
                    CameraDetectionModel(
                        "",
                        enrolmentStatus = EnrollmentStatus.UNKNOWN,
                        rez = rez,
                        creationTime = creationTime,
                        imageName = "face_cool_$creationTime",
                    )
                }
                _remainingUsers.update(remainingPairs)
                _profileImageLoading.update(ProgressStatus.DONE)
            } else {
                setExistingData(null)
                _profileImageLoading.update(ProgressStatus.ERROR)
            }
        }
    }

    override fun onOutlineDetected(results: List<Face>, rect: Rect) = Unit

    private suspend fun generateCameraDetectionModel(
        face: Face,
        bitmapWithContainingFace: Bitmap
    ): CameraDetectionModel {
        try {
            val faceFeatures = faceDetectionEngine.getFaceFeature(bitmapWithContainingFace, face)
            val creationTime = System.currentTimeMillis()

            val quality = faceDetectionEngine.checkFaceQuality(faceFeatures)
            _imgQuality.update(quality)


            return CameraDetectionModel(
                app.getString(R.string.camera_screen_unidentified_label),
                EnrollmentStatus.UNKNOWN,
                faceFeatures,
                imageName = "face_cool_$creationTime",
                creationTime = creationTime
            )


        } catch (e: Exception) {
            return getNullCameraDetectionModel()
        }
    }

}