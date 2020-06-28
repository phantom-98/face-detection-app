package com.facecool.ui.camera

import android.app.Application
import android.graphics.Bitmap
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.face.cool.cache.Cache
import com.face.cool.databasa.Database
import com.face.cool.databasa.administrators.AdministratorEntity
import com.face.cool.databasa.classes.ClassEntity
import com.face.cool.databasa.detection_events.EventEntity
import com.face.cool.manualsync.stringToBitMap
import com.facecool.R
import com.facecool.attendance.facedetector.FaceData
import com.facecool.attendance.facedetector.FaceDetectionEngine
import com.facecool.attendance.facedetector.FaceModel
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.events.EventMapper
import com.facecool.ui.camera.businesslogic.events.EventModeModel
import com.facecool.ui.camera.businesslogic.events.EventModel
import com.facecool.ui.camera.businesslogic.events.EventRepository
import com.facecool.ui.camera.businesslogic.getNullCameraDetectionModel
import com.facecool.ui.camera.businesslogic.user.UserMapper
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.camera.data.LatestDetectedInformation
import com.facecool.ui.classes.add.time.LessonModel
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.common.lessons.LessonRepository
import com.facecool.ui.common.unknownUserImage
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.utils.Notify
import com.facecool.utils.asLiveData
import com.facecool.utils.getDate
import com.facecool.utils.getReadableDate
import com.facecool.utils.liveEvent
import com.facecool.utils.toLiveData
import com.facecool.utils.update
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.DateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.timerTask
import kotlin.math.abs


@HiltViewModel
class CameraViewModel @Inject constructor(
    private val app: Application,
    private val faceDetectionEngine: FaceDetectionEngine,
    private val database: Database,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val classRepository: ClassRepository,
    private val lessonRepository: LessonRepository,
    private val eventMapper: EventMapper,
//    private val userMapper: UserMapper,
    private val cache: Cache
) : AndroidViewModel(app) {

    companion object {
        // used to ignore enrolled users from processing multiple times
        private const val ID_ENROLLED = 1
        private const val LATEST_EVENT_LIMIT = 20
    }

    fun getCache() = cache
    var requestChangeMode = false
    var KioskMode = false
    private val _kiosk = liveEvent<Boolean>()
    val kiosk = _kiosk.toLiveData()
    private val _askPin = liveEvent<String>()
    val askPin = _askPin.toLiveData()
    var countDown:Int = 5

//    val mutex = Mutex()

    private val _cameraDetectionList = liveEvent<List<EventModel>>()
    val cameraDetectionList = _cameraDetectionList.toLiveData()

    private val _classList = liveEvent<List<String>>()
    val classList = _classList.toLiveData()

    private val _spinnerNoClassView = liveEvent<Visibility>()
    val spinnerNoClassView = _spinnerNoClassView.toLiveData()

    private val _spinnerClassView = liveEvent<Visibility>()
    val spinnerClassView = _spinnerClassView.toLiveData()

    private val _overlayData = liveEvent<HashMap<Int, String>>()
    val overlayData = _overlayData.toLiveData()

    private val _latestDetectedData = liveEvent<LatestDetectedInformation>()
    val latestDetectedData = _latestDetectedData.asLiveData()

    private val _onDetectedUser = liveEvent<Long>()
    val onDetectedUser = _onDetectedUser.toLiveData()

    private val _onUnDetectedUser = liveEvent<CameraDetectionModel>()
    val onUnDetectedUser = _onUnDetectedUser.toLiveData()

    private val _generalProgress = liveEvent<Boolean>()
    val generalProgress = _generalProgress.toLiveData()

    private val _cameraMode = liveEvent<CameraMode>()
    val cameraMode = _cameraMode.toLiveData()

    private val _eventMode = liveEvent<Boolean>()
    val eventMode = _eventMode.toLiveData()

    private val _spinnerPosition = liveEvent<Int>()
    val spinnerPosition = _spinnerPosition.toLiveData()

    private val _lessonSelect = liveEvent<List<LessonModel>>()
    val lessonSelect = _lessonSelect.toLiveData()

    private val _askToAdminDialog = liveEvent<Boolean>()
    val askToAdminDialog = _askToAdminDialog.toLiveData()

    private val _alert = liveEvent<Int>()
    val alert = _alert.toLiveData()

    val latestEvents = eventRepository.getLatestEvents(LATEST_EVENT_LIMIT)

    val autoKiosk = MutableLiveData<Boolean>()

    private var delayedStartTimer: CountDownTimer? = null
    private var delayedStopTimer: CountDownTimer? = null

    /**
     * this flag is added to ignore the class updates when the user goes to a different screen
     * TODO: find a better solutions for this issue, flags are error prone and adds to much complexity
     */
    private var newSeenFlag = false

    private val trackingStatus = mutableMapOf<Int, Int>()
    private val trackingTime = hashMapOf<Int, Long>()
    private val trackingNotification = hashMapOf<Int, Boolean>()
    private val trackingAdmin = hashMapOf<Int, Boolean>()
    private val detectedNames = hashMapOf<Int, String>()
    private val eventSavedForID = hashMapOf<Int, Boolean>()
    private val faceProcessorFlow = MutableSharedFlow<Pair<List<Face>, Bitmap>>()
    private var localCachedFaces = mutableListOf<CameraDetectionModel>()
    private var localCachedClasses = listOf<ClassEntity>()
    private var eventModeValue = EventModeModel.IN
    private var cameraModeValue = CameraMode.IDLE
    private var livenessTimeout: Int = 0
    private var enableLiveness: Boolean = true
    private var livenessThreshold: Float = 0f

    val classes = classRepository.getAllLive()

    val threadPool = Executors.newCachedThreadPool()

    init {
        viewModelScope.launch(IO) {
            faceProcessorFlow
                .buffer(0, BufferOverflow.DROP_OLDEST)
                .onEach {
                    it.first.forEach { face ->
                        processFaces(face, it.second)
                    }
                    it.second.recycle()
                }.collect()
        }
    }

    fun loadLivenessVariables() {
        livenessTimeout = cache.getLivenessTimeout()
        enableLiveness = enableLiveness && cache.getEnableLiveness()
        livenessThreshold = cache.getLivenessThreshold()
    }
    fun cacheFaces(){
        viewModelScope.launch(IO) {
            localCachedFaces.clear()
            localCachedFaces.addAll(userRepository.getAllByEnrollmentNot(EnrollmentStatus.REJECTED))
        }
    }
    fun toggleCameraRecord() {
        if (cameraModeValue == CameraMode.IDLE) {
            cameraModeValue = CameraMode.RECORDING
        } else {
            cameraModeValue = CameraMode.IDLE
        }
        _cameraMode.update(cameraModeValue)
    }

    fun startTracking() {
        cameraModeValue = CameraMode.RECORDING
        _cameraMode.update(cameraModeValue)
    }

    fun stopTracking() {
        cameraModeValue = CameraMode.IDLE
        _cameraMode.update(cameraModeValue)
        findLessonToStart()
    }

    fun findLessonToStart(){
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val comingLessons = lessonRepository.getAllByTime( now - cache.getTrackingSpareTime() * 60000, now + 3600 * 1000)
            val lesson = comingLessons.firstOrNull() ?: return@launch
            var toTime = lesson.startLesson!! - cache.getTrackingSpareTime() * 60000 - now
            val clas = localCachedClasses.indexOfFirst {
                it.uid == lesson.lessonClassId
            }
            Timer().schedule(timerTask {
                _spinnerPosition.update(clas + 1)
            }, maxOf(0, toTime))
        }
    }

    fun updateEventMode(mode: EventModeModel) {
        eventModeValue = mode
        _eventMode.update(eventModeValue==EventModeModel.OUT)
    }

    fun onCloseHandler(){
        if (!KioskMode){
            saveCameraMode(false)
            saveInOutState(false)
            saveSpinnerPosition(0)
        }
    }
    fun onStartHandler(){
        if (loadKioskMode()){
            loadSpinnerPosition()
            loadInOutState()
        } else {
            loadSpinnerPosition()
            loadCameraMode()
            loadInOutState()
            findLessonToStart()
        }
    }

    fun saveCameraMode(force: Boolean = true){
        cache.booleanValue("CameraMode", cameraModeValue == CameraMode.RECORDING && force)
    }
    fun loadCameraMode(){
        val mode = cache.booleanValue("CameraMode")
        if (mode) {
            startTracking()
        }
    }
    fun saveInOutState(force: Boolean = true){
        cache.booleanValue("InOutMode", eventModeValue == EventModeModel.OUT && force)
    }
    fun loadInOutState(){
        val mode = cache.booleanValue("InOutMode")
        if (mode){
            updateEventMode(EventModeModel.OUT)
        }
    }
    fun saveSpinnerPosition(position: Int = 0){
        cache.intValue("SpinnerPos", position)
    }
    fun loadSpinnerPosition(){
        var pos = cache.intValue("SpinnerPos")
        if (pos > localCachedClasses.size) pos = 0
        _spinnerPosition.update(pos)
    }

    fun saveKioskMode(force: Boolean = true){
        cache.booleanValue("KioskMode", KioskMode && force)
    }

    fun loadKioskMode(): Boolean{
        val mode = cache.booleanValue("KioskMode")
        if (mode){
            _kiosk.update(true)
            countDown = 5
            startTracking()
        }
        return mode
    }

    fun getClasses(classList: List<ClassEntity>) {
            viewModelScope.launch {
                localCachedClasses = classList
                if (localCachedClasses.isNotEmpty()) {
                    _classList.update(localCachedClasses.map { it.className?:"" })
                }
            }
    }

    fun setupClassVisibility() {
        if (localCachedClasses.isEmpty()) {
            _spinnerNoClassView.update(Visibility.VISIBLE)
            _spinnerClassView.update(Visibility.GONE)
        } else {
            _spinnerNoClassView.update(Visibility.GONE)
            _spinnerClassView.update(Visibility.VISIBLE)
        }
    }

    fun onFaceDetected(faces: List<Face>, bitmap: Bitmap) {
        if (cameraModeValue == CameraMode.IDLE && requestChangeMode.not()) return
        viewModelScope.launch(IO) {
            if (faces.isNotEmpty())
                faceProcessorFlow.emit(faces to bitmap)
        }
//        if (faces.isEmpty()) return
//        faces.forEach {
//            viewModelScope.launch(IO) {
//                processFaces(it, bitmap)
//            }
//        }
    }

    private suspend fun processFaces(
        face: Face,
        bitmap: Bitmap
    ) {
        try {
            val faceID = face.trackingId ?: return
            if (trackingStatus[faceID] != ID_ENROLLED) {
//--------------------------------------------------------------------------------------------------
                val eulerX = face.headEulerAngleX
                val eulerY = face.headEulerAngleY
                val eulerZ = face.headEulerAngleZ
                val isLookingStraight = abs(eulerX) < 30 && abs(eulerY) < 30 && abs(eulerZ) < 30
                if (!isLookingStraight) {
                    detectedNames[faceID] = app.getString(R.string.camera_screen_look_directly_at_camera)
                    _overlayData.update(detectedNames)
                    return
                }
//--------------------------------------------------------------------------------------------------

                val start = System.currentTimeMillis()
                if (trackingTime[faceID] == null) {
                    trackingTime[faceID] = start
                }
                val detectedModel = generateCameraDetectionModel(face, bitmap)

                val databaseModel = getFaceDataPresentInDatabase(detectedModel)
                val end = System.currentTimeMillis()
                if (databaseModel != null) {

                        if (databaseModel.enrolmentStatus == EnrollmentStatus.ENROLLED) {

                            if ( enableLiveness && KioskMode){
                                if (end - trackingTime[faceID]!! > livenessTimeout * 1000){
                                    detectedNames[faceID] = app.getString(R.string.camera_detect_no_live_user)
                                    _overlayData.update(detectedNames)
                                    displayLatestDetectedFace(detectedModel, databaseModel, true)
                                    return
                                }
                                if (detectedModel.rez?.getRealLiveProbability()!! < livenessThreshold){
                                    // fake
                                    if (eventSavedForID[faceID] != true) {
                                        eventSavedForID[faceID] = true
                                        saveEventInDatabase(databaseModel, detectedModel, true)
                                    }
                                    displayLatestDetectedFace(detectedModel, databaseModel, true)
                                     return
                                }
                            }
                            trackingStatus[faceID] = ID_ENROLLED
                            detectedNames[faceID] = app.getString(R.string.camera_detect_student, databaseModel.name)
                            if (requestChangeMode){

                                if (databaseModel.creationTime == 0L){
                                    detectedNames[faceID] = app.getString(R.string.camera_detect_admin, databaseModel.name)
                                    _kiosk.update(!KioskMode)
                                    countDown = 5
                                    startTracking()
                                    _alert.update(2)
                                } else {
                                    if (KioskMode){
                                        if (trackingAdmin[faceID] == null) {
                                            countDown--
                                            trackingAdmin[faceID] = true
                                        }
                                        if (countDown <= 0) {
                                            _askPin.update(cache.getAdminPin())
                                        }
                                    } else {
                                        _askToAdminDialog.update(true)
                                    }
                                }
                            }
                            if (cache.getDebugMode()) {
                                detectedNames[faceID] += "<^>Recognition Time: ${end-start}ms"
                                detectedNames[faceID] += "<^>Similarity Score: ${databaseModel.similarity}"
                                detectedNames[faceID] += "<^>Liveness Score: ${detectedModel.rez?.getRealLiveProbability()}"
                            }

                            _overlayData.update(detectedNames)
                            if (eventSavedForID[faceID] != true) {
                                eventSavedForID[faceID] = true
                                saveEventInDatabase(databaseModel, detectedModel)
                            }

                            displayLatestDetectedFace(detectedModel, databaseModel)

                            if (cache.getEnableNotification() && trackingNotification[faceID] == null) {
                                trackingNotification[faceID] = true
                                val bmp = detectedModel.rez?.getFaceImage()?: unknownUserImage(app)
                                val name = databaseModel.name + " " + databaseModel.lastName
                                val time = start.getReadableDate(pattern = "yyyy-MM-dd HH:mm")
                                val msg = app.getString(R.string.parent_notification_message, name, time)
                                threadPool.submit {
                                    if (databaseModel.enableSMS) {
                                        databaseModel.parentSMSNumbers?.let {
                                            for (phone in it.split(",")){
                                                Notify.sendSMS(msg, phone, app)
                                            }
                                        }
                                    }
                                    if (databaseModel.enableEmail) {
                                        databaseModel.parentEmails?.let {
                                            for (email in it.split(",")){
                                                Notify.sendEmail(cache.getEmailService(), cache.getEmail(), cache.getPassword(), app.getString(R.string.parent_notification_title), msg, email, bmp)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (eventSavedForID[faceID] != true) {
                                eventSavedForID[faceID] = true
                                saveEventInDatabase(databaseModel, detectedModel)
                            }
                            detectedNames[faceID] = app.getString(R.string.camera_detect_unenrolled_user)
                            _overlayData.update(detectedNames)
                            displayLatestDetectedFace(detectedModel, databaseModel)
                            if (requestChangeMode){
                                if (KioskMode){
                                    if (trackingAdmin[faceID] == null) {
                                        countDown--
                                        trackingAdmin[faceID] = true
                                    }
                                    if (countDown <= 0) {
                                        _askPin.update(cache.getAdminPin())
                                    }
                                } else {
                                    _askToAdminDialog.update(true)
                                }
                            }
                        }
                } else {

                    if (eventSavedForID[faceID] != true) {
                        eventSavedForID[faceID] = true
                        val savedUser = saveUnknownUserInDatabase(detectedModel)
                        saveEventInDatabase(savedUser, detectedModel)
                    }
                    detectedNames[faceID] = app.getString(R.string.camera_detect_unknown_user)
                    _overlayData.update(detectedNames)
                    displayLatestDetectedFace(detectedModel, databaseModel)
                    if (requestChangeMode){
                        if (KioskMode){
                            if (trackingAdmin[faceID] == null) {
                                countDown--
                                trackingAdmin[faceID] = true
                            }
                            if ( countDown <= 0) {
                                _askPin.update(cache.getAdminPin())
                            }
                        } else {
                            _askToAdminDialog.update(true)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cleanCachedData() {
        trackingStatus.clear()
        detectedNames.clear()
        eventSavedForID.clear()
        trackingTime.clear()
        trackingAdmin.clear()
    }
    fun clearCachedFace() {
        localCachedFaces.clear()
        requestChangeMode = false
    }

    private fun displayLatestDetectedFace(
        detectedModel: CameraDetectionModel?,
        databaseModel: CameraDetectionModel?,
        fake: Boolean = false
    ) {
        if (cameraModeValue==CameraMode.IDLE) return
        val detectedImage = detectedModel?.rez?.getFaceImage() ?: unknownUserImage(app)
        var databaseImage = databaseModel?.rez?.getFaceImage() ?: unknownUserImage(app)

        if (databaseModel?.enrolmentStatus != EnrollmentStatus.ENROLLED || fake)
            databaseImage = unknownUserImage(app)
        var detectedName = if (fake) app.getString(R.string.camera_detect_no_live_user) else
            databaseModel?.name?.plus(" ${databaseModel.lastName ?: ""}")
                ?: app.getString(R.string.camera_screen_unidentified_label)
        var status = if (fake) EnrollmentStatus.UNKNOWN else databaseModel?.enrolmentStatus ?: EnrollmentStatus.UNKNOWN

        val latestDetectedData = LatestDetectedInformation(
            detectedFaceImage = detectedImage,
            databaseMatchingImage = databaseImage,
            name = detectedName,
            enrollmentStatus = status
        )
        detectedModel?.id = databaseModel?.id
        detectedModel?.creationTime = databaseModel?.creationTime ?: 1
        latestDetectedData.data = detectedModel
        _latestDetectedData.update(latestDetectedData)
    }

    private suspend fun saveUnknownUserInDatabase(
        detectedModel: CameraDetectionModel?,
    ): CameraDetectionModel? {
        if (cameraModeValue==CameraMode.IDLE) return null
        if (detectedModel != null) {
            detectedModel.enrolmentStatus = EnrollmentStatus.UNKNOWN
            localCachedFaces.add(detectedModel)
            return userRepository.saveUser(
                detectedModel,
                EnrollmentStatus.UNKNOWN
            )
        }
        return null
    }

    private suspend fun saveEventInDatabase(
        model: CameraDetectionModel?,
        detectedModel: CameraDetectionModel,
        fake: Boolean = false
    ) {
        if (cameraModeValue==CameraMode.IDLE) return
        model ?: return
        if (model.creationTime == 0L) return
        val detectedImage = detectedModel.rez?.getFaceImage() ?: return

//        if (fake) {
//            model.enrolmentStatus = EnrollmentStatus.UNKNOWN
//        }

        eventRepository.saveEventForUser(model, detectedImage, eventModeValue, fake)
    }

    private suspend fun generateCameraDetectionModel(
        face: Face,
        bitmapWithContainingFace: Bitmap
    ): CameraDetectionModel {
        var cameraModel: CameraDetectionModel?
        try {
            var faceFeatures = faceDetectionEngine.getFaceFeature(bitmapWithContainingFace, face)
            val creationTime = System.currentTimeMillis()
            cameraModel = CameraDetectionModel(
                app.getString(R.string.camera_screen_unidentified_label),
                EnrollmentStatus.UNKNOWN,
                faceFeatures,
                imageName = "face_cool_$creationTime",
                creationTime = creationTime
            )

        } catch (e: Exception) {
            cameraModel = getNullCameraDetectionModel()
        }
        return cameraModel!!
    }

    private suspend fun getFaceDataPresentInDatabase(item: CameraDetectionModel): CameraDetectionModel? {
        var threshold = cache.getFaceDetectionThreshold()
        var twinThreshold = cache.getTwinThreshold()

        val f2 = item.rez?.getDetectedFeatures()
        var maxSymilarity = -1f
        var result: CameraDetectionModel? = null

//        Log.d("KioskModeTest", "detected face feature: $f2;")
        if (requestChangeMode){
//            Log.d("KioskModeTest", "Trying to detect admins;")
            val admins = database.adminDao().getAll()
            var ms = -1f
            var administrator: AdministratorEntity? = null
            for (admin in admins){
                val f1 = admin.detectedFeatures
                val sym = checkSimilarity(f1, f2)
//                Log.d("KioskModeTest", "sym=$sym, admin feature=$f1;")
                if (ms < sym) {
                    ms = sym
                    administrator = admin
//                    result = CameraDetectionModel(
//                        admin.name!!,
//                        EnrollmentStatus.ENROLLED,
//                        FaceData(item.rez?.getDetectedFace()!!,admin.realLiveProbability!!, admin.base64Image?.stringToBitMap()!!),
//                        sym,
//                        admin.uid,
//                        "",
//                        0//mark as admin
//                    )
                }
            }
            if (ms > threshold){
//                Log.d("KioskModeTest", "Admin detected!")
                return CameraDetectionModel(
                    administrator?.name!!,
                    EnrollmentStatus.ENROLLED,
                    FaceData(item.rez?.getDetectedFace()!!,administrator.realLiveProbability!!, administrator.base64Image?.stringToBitMap()!!),
                    ms,
                    administrator.uid,
                    "Admin",
                    0//mark as admin
                )
            }
        }

        // enrolled check first

        val enrolled = localCachedFaces.filter { it.enrolmentStatus == EnrollmentStatus.ENROLLED }

        for (localCachedFace in enrolled) {
            val f1 = localCachedFace.rez?.getDetectedFeatures()
            val sym = checkSimilarity(f1, f2)
//            Log.d("KioskModeTest", "sym=$sym, enrolled user feature=$f1;")
            if (maxSymilarity < sym) {
                if (localCachedFace.twin && sym < twinThreshold) {}
                else {
                    maxSymilarity = sym
                    result = localCachedFace
                }
            }
        }

        if (maxSymilarity > threshold) {
            result?.similarity = maxSymilarity
//            Log.d("KioskModeTest", "Enrolled user detected!")
            return result
        }
        for (localCachedFace in localCachedFaces) {
            val f1 = localCachedFace.rez?.getDetectedFeatures()
            val sym = checkSimilarity(f1, f2)
//            Log.d("KioskModeTest", "sym=$sym, user feature=$f1;")
            if (maxSymilarity < sym) {
                if (localCachedFace.twin && sym < twinThreshold) {}
                else {
                    maxSymilarity = sym
                    result = localCachedFace
                }
            }
        }
        if (maxSymilarity > threshold) {
            result?.similarity = maxSymilarity
//            Log.d("KioskModeTest", "User detected!")
            return result
        }
        return null
    }

    private suspend fun checkSimilarity(f1: ByteArray?, f2: ByteArray?): Float {
        f1 ?: return -1f
        f2 ?: return -1f
        return faceDetectionEngine.getFaceFeatureSimilarity(f1, f2)
    }

    fun processEvents(eventEntityList: List<EventEntity>?) {
        eventEntityList?.let {
            viewModelScope.launch(IO) {
                val list = it.map {
                    eventMapper.fromEntityToModel(it)
                }
                if (list.isNotEmpty()) {
                    val listToDisplay = list.drop(1)
                    _cameraDetectionList.update(listToDisplay)
                }
                if (_latestDetectedData.value == null) {
                    list.getOrNull(0)?.let {
                        val user = userRepository.getUser(it.userId) ?: return@let
                        val databaseImage = user.rez?.getFaceImage() ?: unknownUserImage(app)
                        val detectedImage = it.detectedImage ?: databaseImage
                        val detectedName = user.name
                        val status = user.enrolmentStatus
                        val info = LatestDetectedInformation(
                            detectedFaceImage = detectedImage,
                            databaseMatchingImage = databaseImage,
                            name = detectedName,
                            enrollmentStatus = status
                        )
                        info.data = user
                        _latestDetectedData.update(info)
                    }
                }
            }
        }
    }

    fun onEventItemClicked(item: EventModel) {
//        newSeenFlag = true
        _generalProgress.update(true)
        when (item.enrollmentStatus) {
            EnrollmentStatus.ENROLLED -> _onDetectedUser.update(item.userId)
            EnrollmentStatus.REJECTED,
            EnrollmentStatus.UNKNOWN -> {
                viewModelScope.launch(IO) {
                    val user = userRepository.getUser(item.userId) ?: return@launch
                    when (user.enrolmentStatus) {
                        EnrollmentStatus.ENROLLED -> _onDetectedUser.update(user.id ?: -1)
                        EnrollmentStatus.REJECTED,
                        EnrollmentStatus.UNKNOWN -> _onUnDetectedUser.update(user)
                    }
                    _generalProgress.update(false)
                }
            }
        }
    }

    fun onClassSelectedAtPosition(position: Int, isUserAction: Boolean = false) {
        cameraModeValue = CameraMode.IDLE
        _cameraMode.update(CameraMode.IDLE)
        cancelTimer()
        viewModelScope.launch(IO) {

            val selectedClass = localCachedClasses.getOrNull(position) ?: return@launch
            val clasID = selectedClass.uid!!
            val currentTime = System.currentTimeMillis()
            val timeToSpear = cache.getTrackingSpareTime() * 1000L * 60L
            val lessons = lessonRepository.getAllByClassAndCurrentTime(clasID, currentTime, timeToSpear*5)
            if (lessons.isNotEmpty()) {
                val firstLesson = lessons.first()
                enableLiveness = enableLiveness && firstLesson.liveness // liveness enable status of lesson
                if (currentTime < firstLesson.endLesson!!){
                    val toStart = maxOf(0, firstLesson.startLesson!! - timeToSpear - currentTime)
                    val toStop = firstLesson.endLesson!! - currentTime
                    setTimer(toStart, toStop, firstLesson.autoKiosk)
                }
            }
            if (isUserAction){
                val items = lessonRepository.getAllByClassID(clasID).filter {
                    it.startLesson!! >= currentTime || (it.startLesson!! >= currentTime.getDate() && eventRepository.getAllByTime(it.startLesson!!-timeToSpear, it.endLesson!!).isEmpty())
                }
                if (items.isNotEmpty()) _lessonSelect.update(items)
            }
        }
    }

    fun adjustLesson(lesson: LessonModel) {

        viewModelScope.launch {
            val interval = lesson.endLesson!! - lesson.startLesson!!
            lesson.startLesson = System.currentTimeMillis() + cache.getTrackingSpareTime() * 60000
            lesson.endLesson = lesson.startLesson!! + interval
            lessonRepository.addAll(listOf(lesson))

            enableLiveness = enableLiveness && lesson.liveness // liveness enable status of lesson

            setTimer(0, cache.getTrackingSpareTime() * 60000 + interval, lesson.autoKiosk)
        }
    }

    private fun setTimer(timeToStart: Long, timeToStop: Long, AUTOKIOSK: Boolean = false){
        viewModelScope.launch {
            cancelTimer()
            delayedStartTimer = object : CountDownTimer(timeToStart, timeToStart){
                override fun onTick(millisUntilFinished: Long) = Unit

                override fun onFinish() {
                    startTracking()
                    eventModeValue = EventModeModel.IN
                    _eventMode.update(false)
                    if (AUTOKIOSK){
                        autoKiosk.postValue(true)
                        countDown = 5
                    }
                    _alert.update(0)
                }
            }
            delayedStartTimer?.start()
            if (timeToStop <= 0) return@launch
            delayedStopTimer = object : CountDownTimer(timeToStop, timeToStop){
                override fun onTick(millisUntilFinished: Long) = Unit

                override fun onFinish() {
                    stopTracking()
                    if (KioskMode){
                        autoKiosk.postValue(false)
                        countDown = 5
                    }
                    _alert.update(1)
                }
            }
            delayedStopTimer?.start()
        }
    }

    fun cancelTimer() {
//        timer?.cancel()
        delayedStartTimer?.cancel()
        delayedStopTimer?.cancel()
    }

    fun updateFlag() {
        newSeenFlag = true
    }

    fun getCameraMode(): CameraMode{
        return cameraModeValue
    }
}

enum class Visibility {
    VISIBLE,
    GONE
}

enum class CameraMode {
    IDLE, RECORDING
}
