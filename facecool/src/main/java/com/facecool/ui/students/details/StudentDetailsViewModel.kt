package com.facecool.ui.students.details

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.cool.cache.Cache
import com.face.cool.databasa.users.ErrorStatus
import com.facecool.R
import com.facecool.attendance.facedetector.FaceDetectionEngine
import com.facecool.ui.camera.FaceDetectorCallback
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.camera.camerax.BaseImageAnalyzer
import com.facecool.ui.camera.face_detection.FaceContourDetectionProcessor
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.ui.students.details.ItemAction.STUDENT_ADDRESS
import com.facecool.ui.students.details.ItemAction.STUDENT_EMAIL
import com.facecool.ui.students.details.ItemAction.STUDENT_ID
import com.facecool.ui.students.details.ItemAction.STUDENT_LAST_NAME
import com.facecool.ui.students.details.ItemAction.STUDENT_NAME
import com.facecool.ui.students.details.ItemAction.STUDENT_PARENT_EMAIL
import com.facecool.ui.students.details.ItemAction.STUDENT_PARENT_NAMES
import com.facecool.ui.students.details.ItemAction.STUDENT_PARENT_SMS
import com.facecool.ui.students.details.ItemAction.STUDENT_PARENT_WHATSAPP
import com.facecool.ui.students.details.ItemAction.STUDENT_PHONE_NUMBER
import com.facecool.utils.asLiveData
import com.facecool.utils.liveEvent
import com.facecool.utils.loadUriIe
import com.facecool.utils.toLiveData
import com.facecool.utils.update
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class StudentDetailsViewModel @Inject constructor(
    private val userRepository: UserRepository,
//    private val classRepository: ClassRepository,
    private val app: Application,
    private val faceDetectionEngine: FaceDetectionEngine,
    private val cache: Cache
) : ViewModel(), FaceDetectorCallback {

    private val _studentDetailsItems = MutableLiveData<List<StudentDetailItemModel>>()
    val studentDetailsItems = _studentDetailsItems.toLiveData()

//    private val _studentEnrolledClasses = MutableLiveData<List<StudentEnrolledClassModel>>()
//    val studentEnrolledClasses = _studentEnrolledClasses.toLiveData()

    private val _userImage = liveEvent<Pair<String, String>>()
    val userImage = _userImage.asLiveData()

    private val _userTwin = liveEvent<Boolean>()
    val userTwin = _userTwin.asLiveData()

    private val _enableNotification = liveEvent<Boolean>()
    val enableNotification = _enableNotification.toLiveData()

    private val _enableSMS = liveEvent<Boolean>()
    val enableSMS = _enableSMS.toLiveData()

    private val _enableEmail = liveEvent<Boolean>()
    val enableEmail = _enableEmail.toLiveData()

    private val _enableWhatsapp = liveEvent<Boolean>()
    val enableWhatsapp = _enableWhatsapp.toLiveData()

    private val _parentDetailsItems = MutableLiveData<List<StudentDetailItemModel>>()
    val parentDetailsItems = _parentDetailsItems.toLiveData()

    private val _pictureLoading = liveEvent<ProgressStatus>()
    val pictureLoading = _pictureLoading.asLiveData()

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage = _errorMessage.toLiveData()

    private val _errorList = MutableLiveData<List<String>>()
    val errorList = _errorList.toLiveData()

//    private val _enrolledClassesProgress = MutableLiveData<ProgressStatus>()
//    val enrolledClassesProgress = _enrolledClassesProgress.toLiveData()

    private val _quality = MutableLiveData<Int>()
    val quality = _quality.toLiveData()

    private var cachedStudent: CameraDetectionModel? = null

    private val processor = FaceContourDetectionProcessor(this)


    fun checkForErrors(user: CameraDetectionModel): List<ErrorStatus>{
        val errors = user.error;

        if (user.name.isNullOrEmpty()){
            errors.add(ErrorStatus.NAME_ERROR)
        }
        if (user.lastName.isNullOrEmpty()){
            errors.add(ErrorStatus.LAST_NAME_ERROR)
        }
        if (user.studentId.isNullOrEmpty()){
            errors.add(ErrorStatus.ID_ENROLLED)
        }
        if (user.rez?.getFaceImage() == null){
            errors.add(ErrorStatus.IMAGE_NOT_PRESENT)
        }
        if (user.imageName.isNullOrEmpty()){
            errors.add(ErrorStatus.IMAGE_NOT_PRESENT)
        }
        if (user.rez?.getDetectedFeatures()?.isEmpty() == true){
            errors.add(ErrorStatus.IMAGE_NOT_PRESENT)
        }
        return errors.distinct()
    }


    fun checkStudentById(studentId: Long) {
        viewModelScope.launch(IO) {
            val student = userRepository.getUser(studentId)
            val student2 = userRepository.getUser(studentId)
            cachedStudent = student


            val errores = student2?.let { checkForErrors(it) }?.map {
                when(it){
                    ErrorStatus.ID_ENROLLED -> app.getString(R.string.id_already_enrolled)
                    ErrorStatus.NAME_ERROR -> app.getString(R.string.first_name_not_found)
                    ErrorStatus.LAST_NAME_ERROR -> app.getString(R.string.last_name_not_found)
                    ErrorStatus.NO_FACE_DETECTED -> app.getString(R.string.face_not_detected_on_image)
                    ErrorStatus.FACE_ALREADY_ENROLLED -> app.getString(R.string.user_already_present_in_db)
                    ErrorStatus.IMAGE_NOT_PRESENT -> app.getString(R.string.image_not_found)
                    ErrorStatus.IMAGE_QUALITY_IS_LOW -> app.getString(R.string.image_quality_low)
                }
            } ?: listOf()

            if (errores.isEmpty()){
                cachedStudent?.error = mutableListOf()
                cachedStudent?.error2 = mutableListOf()
                cachedStudent?.enrolmentStatus = EnrollmentStatus.ENROLLED
                cachedStudent?.let { userRepository.updateUser(it) }
            }

            _errorList.postValue(errores)

            val studentName = student?.name
            val studentLastName = student?.lastName
            val name = studentName?.let {
                "$it $studentLastName"
            } ?: ""

            student?.imageName?.let {
                _userImage.update(it to name)
            }

            student?.rez.let {
                _quality.postValue(faceDetectionEngine.checkFaceQuality(student?.rez!!))
            }

            student?.twin?.let { _userTwin.update(it) }

            student?.enableNotification?.let { _enableNotification.update(it) }

            student?.enableSMS?.let { _enableSMS.update(it) }

            student?.enableEmail?.let { _enableEmail.update(it) }

            student?.enableWhatsapp?.let { _enableWhatsapp.update(it) }

            _studentDetailsItems.postValue(
                listOf(
                    StudentDetailItemModel(
                        R.drawable.ic_name, student?.name ?: "", STUDENT_NAME, app.getString(R.string.first_name)
                    ),
                    StudentDetailItemModel(
                        R.drawable.ic_name, student?.lastName ?: "", STUDENT_LAST_NAME, app.getString(R.string.last_name)
                    ),
                    StudentDetailItemModel(
                        R.drawable.ic_email, student?.email ?: "", STUDENT_EMAIL, app.getString(R.string.email)
                    ),
                    StudentDetailItemModel(
                        R.drawable.ic_email, student?.studentId ?: "", STUDENT_ID, app.getString(R.string.id)
                    ),
                    StudentDetailItemModel(
                        R.drawable.ic_name, student?.address ?: "", STUDENT_ADDRESS, app.getString(R.string.address)
                    ),
                    StudentDetailItemModel(
                        R.drawable.ic_email, student?.phoneNumber ?: "", STUDENT_PHONE_NUMBER, app.getString(R.string.phone_number)
                    )
                )
            )
            _parentDetailsItems.postValue(
                listOf(
                    StudentDetailItemModel(
                        R.drawable.ic_name, student?.parentNames ?: "", STUDENT_PARENT_NAMES, app.getString(R.string.parent_names)
                    ),
                    StudentDetailItemModel(
                        R.drawable.ic_name, student?.parentSMSNumbers ?: "", STUDENT_PARENT_SMS, app.getString(R.string.parent_sms_numbers)
                    ),
                    StudentDetailItemModel(
                        R.drawable.ic_email, student?.parentEmails ?: "", STUDENT_PARENT_EMAIL, app.getString(R.string.parent_emails)
                    ),
                    StudentDetailItemModel(
                        R.drawable.ic_name, student?.parentWhatsappNumbers ?: "", STUDENT_PARENT_WHATSAPP, app.getString(R.string.parent_whatsapp)
                    ),
                )
            )
        }
    }

    fun updateTwin(twin: Boolean){
        viewModelScope.launch {
            cachedStudent?.twin = twin
            cachedStudent?.let { userRepository.updateUser(it) }
            cachedStudent?.id?.let { checkStudentById(it) }
        }
    }

    fun updateNotification(enable: Boolean){
        viewModelScope.launch {
            cachedStudent?.enableNotification = enable
            cachedStudent?.let { userRepository.updateUser(it) }
            cachedStudent?.id?.let { checkStudentById(it) }
        }
    }
    fun updateSMS(enable: Boolean){
        viewModelScope.launch {
            cachedStudent?.enableSMS = enable
            cachedStudent?.let { userRepository.updateUser(it) }
            cachedStudent?.id?.let { checkStudentById(it) }
        }
    }
    fun updateEmail(enable: Boolean){
        viewModelScope.launch {
            cachedStudent?.enableEmail = enable
            cachedStudent?.let { userRepository.updateUser(it) }
            cachedStudent?.id?.let { checkStudentById(it) }
        }
    }
    fun updateWhatsapp(enable: Boolean){
        viewModelScope.launch {
            cachedStudent?.enableWhatsapp = enable
            cachedStudent?.let { userRepository.updateUser(it) }
            cachedStudent?.id?.let { checkStudentById(it) }
        }
    }

    fun updateField(dataTOReplace: String, code: ItemAction) {
        viewModelScope.launch {
            when (code) {
                STUDENT_NAME -> {
                    cachedStudent?.name = dataTOReplace
                    cachedStudent?.let { userRepository.updateUser(it) }
                    cachedStudent?.id?.let { checkStudentById(it) }
                }

                STUDENT_LAST_NAME -> {
                    cachedStudent?.lastName = dataTOReplace
                    cachedStudent?.let { userRepository.updateUser(it) }
                    cachedStudent?.id?.let { checkStudentById(it) }
                }

                STUDENT_EMAIL -> {
                    cachedStudent?.email = dataTOReplace
                    cachedStudent?.let { userRepository.updateUser(it) }
                    cachedStudent?.id?.let { checkStudentById(it) }
                }

                STUDENT_ID -> {
                    cachedStudent?.studentId = dataTOReplace
                    cachedStudent?.let { userRepository.updateUser(it) }
                    cachedStudent?.id?.let { checkStudentById(it) }
                }

                STUDENT_ADDRESS -> {
                    cachedStudent?.address = dataTOReplace
                    cachedStudent?.let { userRepository.updateUser(it) }
                    cachedStudent?.id?.let { checkStudentById(it) }
                }

                STUDENT_PHONE_NUMBER -> {
                    cachedStudent?.phoneNumber = dataTOReplace
                    cachedStudent?.let { userRepository.updateUser(it) }
                    cachedStudent?.id?.let { checkStudentById(it) }
                }

                STUDENT_PARENT_NAMES -> {
                    cachedStudent?.parentNames = dataTOReplace
                    cachedStudent?.let { userRepository.updateUser(it) }
                    cachedStudent?.id?.let { checkStudentById(it) }
                }

                STUDENT_PARENT_SMS -> {
                    cachedStudent?.parentSMSNumbers = dataTOReplace
                    cachedStudent?.let { userRepository.updateUser(it) }
                    cachedStudent?.id?.let { checkStudentById(it) }
                }

                STUDENT_PARENT_EMAIL -> {
                    cachedStudent?.parentEmails = dataTOReplace
                    cachedStudent?.let { userRepository.updateUser(it) }
                    cachedStudent?.id?.let { checkStudentById(it) }
                }

                STUDENT_PARENT_WHATSAPP -> {
                    cachedStudent?.parentWhatsappNumbers = dataTOReplace
                    cachedStudent?.let { userRepository.updateUser(it) }
                    cachedStudent?.id?.let { checkStudentById(it) }
                }
            }
        }
    }

    fun loadImageFromUri(pictureUri: Uri?) {
        _pictureLoading.update(ProgressStatus.LOADING)
        viewModelScope.launch(IO) {
            val btm = pictureUri?.let { app.loadUriIe(it) }
            if (btm != null) {
                processor.analyze(btm, 0)
            }
            else _pictureLoading.update(ProgressStatus.DONE)
        }
    }

    fun loadImageFromBitmap(btm: Bitmap){
        _pictureLoading.update(ProgressStatus.LOADING)
        viewModelScope.launch {
            if (btm != null) {
                processor.analyze(btm, 0)
            }
            else _pictureLoading.update(ProgressStatus.DONE)
        }
    }

    override fun onFaceDetected(results: List<Face>, toBitmap: Bitmap) {
        results.firstOrNull()?.let {
            onFaceDetected(it, toBitmap)
        } ?: _pictureLoading.update(ProgressStatus.DONE)
    }

    override fun onError(e: Exception) {
        super.onError(e)
        _pictureLoading.update(ProgressStatus.ERROR)
    }

    override fun onFaceDetected(results: Face, toBitmap: Bitmap) {
        viewModelScope.launch(IO) {
            val faceFeatures = faceDetectionEngine.getFaceFeature(toBitmap, results)
            val q = faceDetectionEngine.checkFaceQuality(faceFeatures)
            if (q >= cache.getImageQualityThreshold() ) {
                cachedStudent?.rez = faceFeatures
                cachedStudent?.let {
                    userRepository.updateUser(it)
                }
                cachedStudent?.id?.let { checkStudentById(it) }
                _quality.postValue(q)
            } else {
                _quality.postValue(0)
            }
            _pictureLoading.update(ProgressStatus.DONE)
        }
    }

    override fun onOutlineDetected(results: List<Face>, rect: Rect) = Unit

}