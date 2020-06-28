package com.facecool.ui.settings

import android.app.Application
import android.net.Uri
import android.os.Debug
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.cool.cache.Cache
import com.face.cool.manualsync.ExpertDataWrapper
import com.face.cool.manualsync.ManualSyncing
import com.facecool.ui.camera.businesslogic.events.EventRepository
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.common.lessons.LessonRepository
import com.facecool.utils.loadData
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cache: Cache,
    private val syncUtil: ManualSyncing<ExpertDataWrapper, String>,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val classRepository: ClassRepository,
    private val lessonRepository: LessonRepository,
    private val app: Application
) : ViewModel() {

    private val _faceComparisonThreshold = MutableLiveData<Int>()
    val faceComparisonThreshold = _faceComparisonThreshold.toLiveData()

    private val _enableLiveness = MutableLiveData<Boolean>()
    val enableLiveness = _enableLiveness.toLiveData()

    private val _livenessThreshold = MutableLiveData<Int>()
    val livenessThreshold = _livenessThreshold.toLiveData()

    private val _livenessTimeout = MutableLiveData<Int>()
    val livenessTimeout = _livenessTimeout.toLiveData()

    private val _enableNotification = MutableLiveData<Boolean>()
    val enableNotification = _enableNotification.toLiveData()

    private val _emailService = MutableLiveData<Int>()
    val emailService = _emailService.toLiveData()

    private val _emailAddress = MutableLiveData<String>()
    val emailAddress = _emailAddress.toLiveData()

    private val _emailPassword = MutableLiveData<String>()
    val emailPassword = _emailPassword.toLiveData()

    private val _generalProgress = MutableLiveData<ProgressStatus>()
    val generalProgress = _generalProgress.toLiveData()

    private val _exportData = MutableLiveData<String>()
    val exportData = _exportData.toLiveData()

    private val _timeToSpear = MutableLiveData<Int>()
    val timeToSpear = _timeToSpear.toLiveData()

    private val _debugMode = MutableLiveData<Boolean>()
    val debugMode = _debugMode.toLiveData()

    private val _imageQualityThreshold = MutableLiveData<Int>()
    val imageQualityThreshold = _imageQualityThreshold.toLiveData()

    private val _twinThreshold = MutableLiveData<Int>()
    val twinThreshold = _twinThreshold.toLiveData()

    private val _adminPin = MutableLiveData<String>()
    val adminPin = _adminPin.toLiveData()

    fun getInitialFaceDetectionThreshold() {
        val value = cache.getFaceDetectionThreshold()
        _faceComparisonThreshold.postValue((value * 100f).toInt())
        val enable = cache.getEnableLiveness()
        _enableLiveness.postValue(enable)
        val liveness = cache.getLivenessThreshold()
        _livenessThreshold.postValue((liveness * 100f).toInt())
        val timeout = cache.getLivenessTimeout()
        _livenessTimeout.postValue(timeout)
        val twin = cache.getTwinThreshold()
        _twinThreshold.postValue((twin * 100f).toInt())
        val time = cache.getTrackingSpareTime()
        _timeToSpear.postValue(time.toInt())
        val debug = cache.getDebugMode()
        _debugMode.postValue(debug)
        val pin = cache.getAdminPin()
        _adminPin.postValue(pin)
        val notification = cache.getEnableNotification()
        _enableNotification.postValue(notification)
        val service = cache.getEmailService()
        _emailService.postValue(service)
        val email = cache.getEmail()
        _emailAddress.postValue(email)
        val pwd = cache.getPassword()
        _emailPassword.postValue(pwd)
        val quality = cache.getImageQualityThreshold()
        _imageQualityThreshold.postValue(quality)
    }

    fun changeFaceDetectionThreshold(progress: Int) {
        val treashold = progress.toFloat() / 100f
        cache.updateFaceDetectionThreshold(treashold)
        _faceComparisonThreshold.postValue((treashold * 100f).toInt())
    }

    fun changeEnableLiveness(enable: Boolean){
        cache.updateEnableLiveness(enable)
    }

    fun changeLivenessThreshold(progress: Int) {
        val threshold = progress.toFloat() / 100f
        cache.updateLivenessThreshold(threshold)
        _livenessThreshold.postValue((threshold * 100f).toInt())
    }

    fun changeLivenessTimeout(timeout: String) {
        val sec = timeout.toIntOrNull() ?: 4
        cache.updateLivenessTimeout(sec)
    }

    fun changeEnableNotification(enable: Boolean) {
        cache.updateEnableNotification(enable)
        viewModelScope.launch {
            userRepository.enableNotification(enable)
        }
    }

    fun changeEmailService(index: Int) {
        cache.updateEmailService(index)
    }

    fun changeEmail(email: String) {
        cache.updateEmail(email)
    }

    fun changePassword(pwd: String) {
        cache.updatePassword(pwd)
    }

    fun changeTwinDetectionThreshold(progress: Int) {
        val threshold = progress.toFloat() / 100f
        cache.updateTwinThreshold(threshold)
        _twinThreshold.postValue((threshold * 100f).toInt())
    }

    fun updatePreLessonTime(time: String) {
        val minutes = time.toLongOrNull() ?: 15
        cache.updateTrackingSpareTime(minutes)
    }

    fun updateDebugMode(debug: Boolean){
        cache.updateDebugMode(debug)
    }

    fun updateAdminPin(pin: String){
        cache.updateAdminPin(pin)
    }

    fun updateImageQualityThreshold(threshold: Int){
        cache.updateImageQualityThreshold(threshold)
        _imageQualityThreshold.postValue(threshold)
    }

    fun exportUserData(
        saveUsers: Boolean = false,
        saveEvents: Boolean = false,
        saveClasses: Boolean = false,
        saveLessons: Boolean = false,
    ) {
        viewModelScope.launch(IO) {
            _generalProgress.postValue(ProgressStatus.LOADING)
            try {

                val usersEntityList = if (saveUsers) userRepository.getAllEntity() else null
                val events = if (saveEvents) eventRepository.getAllEntity() else null
                val classes = if (saveClasses) classRepository.getAllEntity() else null
                val lessons = if (saveLessons) lessonRepository.getAllEntity() else null

                val wrapperToExport = syncUtil.generateExpertDataWrapper(
                    usersEntityList,
                    events,
                    classes,
                    lessons
                )

                val exportData = syncUtil.generateDB(wrapperToExport)
                _exportData.postValue(exportData)

            } catch (e: Exception) {
                e.printStackTrace()
            }
            _generalProgress.postValue(ProgressStatus.DONE)
        }
    }

    fun importData(uri: Uri?) {
        val notNullUri = uri ?: return
        _generalProgress.postValue(ProgressStatus.LOADING)
        viewModelScope.launch(IO) {
            try {
                val realPath = getRealPathFromURI(notNullUri, app)
                val file = realPath.second
                val data = loadData(file)
                val dbData = syncUtil.syncDB(data)
                dbData.eventList?.let {
                    val toSave = it.mapNotNull { it?.event }
                    eventRepository.saveAllEvents(toSave)
                }
                dbData.userList?.let {
                    val toSave = it.mapNotNull { it?.user }
                    userRepository.saveAll(toSave)
                }
                dbData.classList?.let {
                    classRepository.saveAllEntity(it.filterNotNull())
                }
                dbData.lessonList?.let {
                    lessonRepository.addAllEntity(it.filterNotNull())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _generalProgress.postValue(ProgressStatus.DONE)
        }

    }

}
