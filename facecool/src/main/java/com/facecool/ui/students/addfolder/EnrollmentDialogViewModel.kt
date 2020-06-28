package com.facecool.ui.students.addfolder

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.cool.cache.Cache
import com.face.cool.databasa.Database
import com.face.cool.databasa.users.ErrorStatus
import com.face.cool.databasa.users.ErrorStatus.FACE_ALREADY_ENROLLED
import com.face.cool.databasa.users.ErrorStatus.ID_ENROLLED
import com.face.cool.databasa.users.ErrorStatus.IMAGE_NOT_PRESENT
import com.face.cool.databasa.users.ErrorStatus.LAST_NAME_ERROR
import com.face.cool.databasa.users.ErrorStatus.NAME_ERROR
import com.face.cool.databasa.users.ErrorStatus.NO_FACE_DETECTED
import com.facecool.R
import com.facecool.attendance.facedetector.FaceDetectionEngine
import com.facecool.ui.camera.FaceDetectorCallback
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.camera.camerax.BaseImageAnalyzer
import com.facecool.ui.camera.face_detection.FaceContourDetectionProcessor
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.utils.liveEvent
import com.facecool.utils.loadUriIe
import com.facecool.utils.toLiveData
import com.facecool.utils.update
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.Math.max
import java.lang.Math.min
import javax.inject.Inject
import kotlin.jvm.Throws
import kotlin.random.Random

@HiltViewModel
class EnrollmentDialogViewModel @Inject constructor(
    private val app: Application,
//    private val database: Database,
    private val userRepository: UserRepository,
    private val faceDetectionEngine: FaceDetectionEngine,
    private val cache: Cache,
) : ViewModel(), FaceDetectorCallback {

    companion object {
        private const val STUDENT_ID = 0
        private const val STUDENT_NAME = 1
        private const val STUDENT_LAST_NAME = 2

        private fun getStringBetweenLastSlashAndFirstDot(input: String): String {
            val lastSlashIndex = input.lastIndexOf("/")
            val firstDotIndex = input.indexOf(".", lastSlashIndex)

            return if (lastSlashIndex >= 0 && firstDotIndex > lastSlashIndex) {
                input.substring(lastSlashIndex + 1, firstDotIndex)
            } else {
                ""
            }
        }

        private fun getAutomaticFields(string: String): Map<Int, String>? {
            val parts = string.split("_").toMutableList()
            val studentID = parts.firstOrNull() ?: string
            val map = mutableMapOf<Int, String>()
            map[STUDENT_ID] = studentID
            if (parts.isNotEmpty()) {
                parts.removeFirstOrNull()
                val firstName = parts.firstOrNull() ?: studentID
                map[STUDENT_NAME] = firstName
                if (parts.isNotEmpty()) {
                    parts.removeFirstOrNull()
                    val lastnameName = parts.joinToString(separator = " ")
                    map[STUDENT_LAST_NAME] = lastnameName
                } else {
                    map[STUDENT_LAST_NAME] = firstName
                }
            } else {
                map[STUDENT_NAME] = studentID
                map[STUDENT_LAST_NAME] = studentID
            }
            return map
        }

    }

    private var processor: FaceContourDetectionProcessor? = FaceContourDetectionProcessor(this)

    private val _enrollmentItems = MutableLiveData<List<EnrollmentModel>>()
    val enrollmentItems = _enrollmentItems.toLiveData()

    private val _scrollTo = MutableLiveData<Int>()
    val scrollTo = _scrollTo.toLiveData()

    private val _progressInformation = MutableLiveData<String>()
    val progressInformation = _progressInformation.toLiveData()

    private val _treashold = MutableLiveData<String>()
    val treashold = _treashold.toLiveData()

    private val itemsToDetect = hashMapOf<Int, EnrollmentModel>()

    private val faceProcessorFlow = MutableSharedFlow<Pair<EnrollmentModel.Waiting, Int>>()


    private val enrolledData = mutableListOf<Pair<String?, ByteArray?>>()
    private val rejectedData = mutableListOf<Pair<Long?, ByteArray?>>()

    private val comparisonThreshold = cache.getFaceDetectionThreshold()

    init {
        viewModelScope.launch(IO) {
            faceProcessorFlow
                .onEach {
                    performEnrollment(it)
                }.collect()
        }
    }

    fun getExistingData(){
        viewModelScope.launch {
            enrolledData.clear()
            rejectedData.clear()
            var list = userRepository.getAllEntity()
            enrolledData.addAll(
                list.filter { it.enrolmentStatus == EnrollmentStatus.ENROLLED.name }.map { it.studentId to it.detectedFeatures }
            )
            rejectedData.addAll(
                list.filter { it.enrolmentStatus == EnrollmentStatus.REJECTED.name }.map { it.uid to it.detectedFeatures }
            )
        }
    }

    @SuppressLint("StringFormatMatches")
    @Synchronized
    private fun updateUI() {
        val listToDisplay = itemsToDetect.map { it.value }
        _enrollmentItems.postValue(listToDisplay)

        val totalItems = itemsToDetect.size
        val inPRogress = listToDisplay.size - listToDisplay.count { it is EnrollmentModel.Waiting }
//        val enrolled = listToDisplay.count { it is EnrollmentModel.Enrolled }
        val errorList = listToDisplay.filter { it is EnrollmentModel.Error }
        val error = errorList.size
        val enrolled = inPRogress - error
        val errorMap = mutableMapOf<String, Int>()
        errorList.forEach {
            val e = it as EnrollmentModel.Error
            errorMap[e.reason] = errorMap.getOrDefault(e.reason, 0) + 1
        }

        var stringToDisplay = app.getString(R.string.folder_enrollment_status, inPRogress, totalItems, enrolled, error)
        errorMap.forEach { t, u ->
            stringToDisplay += "\n\t\t$t: $u"
        }
        _progressInformation.postValue(stringToDisplay)
    }

    fun updateUrls(uris: List<Uri>) {
        uris.forEachIndexed { index, uri ->
            val model = EnrollmentModel.Waiting(uri, getStringBetweenLastSlashAndFirstDot(uri.toString()))
            itemsToDetect[index] = model
        }

        updateUI()
    }

    var pointer = 0
    var loop = true
    private val _isFinished = MutableLiveData<Boolean>()
    val isFinished = _isFinished.toLiveData()
    fun start() {
        Log.i("add folder", "start method")
        viewModelScope.launch(IO) {
            try {
                loop = true
                for (i in pointer until itemsToDetect.size){
                    if (loop.not()){
                        pointer = i
                        return@launch
                    }
                    val item = (itemsToDetect[i] as? EnrollmentModel.Waiting)
                    if (item != null)
                        performEnrollment(item to i)
                    else
                        updateUI()
                }
            } catch (e: Exception){
                Log.d("Face Process", "error : ${e.message}")
            }
            _isFinished.postValue(true)
        }
//        itemsToDetect.forEach { (index, itemToDetect) ->
//            viewModelScope.launch(IO) {
//                val item = (itemToDetect as? EnrollmentModel.Waiting)
//                if (item != null)
//                    faceProcessorFlow.emit(item to index)
//                else
//                    updateUI()
//            }
//        }
    }
    fun stop(){
        loop = false
    }

    @Throws(Exception::class)
    private suspend fun performEnrollment(data: Pair<EnrollmentModel.Waiting, Int>) {

        val time = System.currentTimeMillis()

        val model = CameraDetectionModel(
            "",
            EnrollmentStatus.ENROLLED,
            null,
            imageName = "face_cool_" + time.toString() + Random.nextInt(),
            creationTime = time
        )

        val item = data.first
        val index = data.second
        _scrollTo.postValue(index + 3)
        val uri = item.imageUri

        item.isProgress = true

        updateUI()

        var bitmap = app.loadUriIe(uri)

        var imgQuality = 0
        var loop = true

        while(loop){
            loop = false
            if (item.detectedName.isEmpty()) {
                model.error.add(NAME_ERROR)
                model.enrolmentStatus = EnrollmentStatus.REJECTED
            }

            val separateFields = getAutomaticFields(item.detectedName)

            val id = separateFields?.get(STUDENT_ID)
            model.studentId = id ?: ""
            if (id.isNullOrEmpty()) {
                model.error.add(ID_ENROLLED)
                model.enrolmentStatus = EnrollmentStatus.REJECTED
            }

            val name = separateFields?.get(STUDENT_NAME)
            model.name = name ?: ""
            if (name.isNullOrEmpty()) {
                model.error.add(NAME_ERROR)
                model.enrolmentStatus = EnrollmentStatus.REJECTED
            }

            val lastName = separateFields?.get(STUDENT_LAST_NAME)
            model.lastName = lastName ?: ""
            if (lastName.isNullOrEmpty()) {
                model.error.add(LAST_NAME_ERROR)
                model.enrolmentStatus = EnrollmentStatus.REJECTED
            }

            if (bitmap == null) {
                model.error.add(IMAGE_NOT_PRESENT)
                model.enrolmentStatus = EnrollmentStatus.REJECTED
                break
            }

            val face = bitmap?.let { processor?.analyze(it) }
            if (face == null) {
                model.error.add(NO_FACE_DETECTED)
                model.enrolmentStatus = EnrollmentStatus.REJECTED
                break
            }

            val faceModel = bitmap?.let {
                face?.let { it1 ->
                    faceDetectionEngine.getFaceFeature(
                        it,
                        it1
                    )
                }
            }
            model.rez = faceModel

            enrolledData.forEach { (studentId, existingFeatures) ->
                var comp = faceDetectionEngine.getFaceFeatureSimilarity(
                    existingFeatures!!,
                    faceModel?.getDetectedFeatures()!!
                )

                if (comp > comparisonThreshold) {
                    model.error.add(FACE_ALREADY_ENROLLED)
                    model.enrolmentStatus = EnrollmentStatus.REJECTED
                }
                if (studentId == model.studentId) {
                    model.error.add(ID_ENROLLED)
                    model.enrolmentStatus = EnrollmentStatus.REJECTED
                }
            }

            faceModel.let { imgQuality = faceDetectionEngine.checkFaceQuality(it!!) }

            if (faceModel!=null && imgQuality < cache.getImageQualityThreshold()){
                model.error.add(ErrorStatus.IMAGE_QUALITY_IS_LOW)
                model.enrolmentStatus = EnrollmentStatus.REJECTED
            }

            if (model.enrolmentStatus == EnrollmentStatus.REJECTED) {
                rejectedData.forEachIndexed { i, p ->
                    var comp = faceDetectionEngine.getFaceFeatureSimilarity(faceModel?.getDetectedFeatures()!!, p.second!!)
                    if (comp > comparisonThreshold) {
                        model.id = p.first
                        rejectedData[i] = p.first to faceModel?.getDetectedFeatures()
                        return@forEachIndexed
                    }
                }
            }
        }

        userRepository.updateUser(model)
        if (model.enrolmentStatus == EnrollmentStatus.ENROLLED) enrolledData.add(model.studentId to model.rez?.getDetectedFeatures())

        if (itemsToDetect[index] !is EnrollmentModel.Error) {
            itemsToDetect[index] = EnrollmentModel.Enrolled(
                item.detectedName,
                model.imageName,
                imgQuality
            )
        }

        if (model.error.isNotEmpty()) {
            setErrorItem(index, uri, model.error.first(), imgQuality)
        }

        updateUI()
        bitmap?.recycle()
    }

    fun closeDetector(){
        Log.i("Face Processor", "Close Processor")
        processor?.stop()
        processor = null
//        System.runFinalization()
//        Runtime.getRuntime().gc()
        System.gc()
    }
    private fun setErrorItem(index: Int, image: Uri, reason: ErrorStatus, imgQuality: Int) {
        itemsToDetect[index] = EnrollmentModel.Error(
            reason.name,
            image,
            imgQuality
        )
    }


    override fun onFaceDetected(results: List<Face>, toBitmap: Bitmap) {

    }

    override fun onFaceDetected(results: Face, toBitmap: Bitmap) {

    }

    override fun onOutlineDetected(results: List<Face>, rect: Rect) {

    }

}

