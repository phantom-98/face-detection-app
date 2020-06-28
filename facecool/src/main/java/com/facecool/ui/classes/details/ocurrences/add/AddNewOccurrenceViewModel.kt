package com.facecool.ui.classes.details.ocurrences.add

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.R
import com.facecool.ui.classes.add.time.LessonModel
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.common.lessons.LessonRepository
import com.facecool.utils.getReadableDate
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddNewOccurrenceViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val lessonRepository: LessonRepository,
    private val app: Application
) : ViewModel() {

    private val _finishEvent = MutableLiveData<Unit>()
    val finishEvent = _finishEvent.toLiveData()

    private val _genericProgress = MutableLiveData<ProgressStatus>()
    val genericProgress = _genericProgress.toLiveData()

    private val _genericErrorMessage = MutableLiveData<String>()
    val genericErrorMessage = _genericErrorMessage.toLiveData()
//
//    private var lessonId: Long? = null
//    private var classId: Long? = null

    private val _date = MutableLiveData<Long>()
    val date = _date.toLiveData()
    private val _startTime = MutableLiveData<Long>()
    val startTime = _startTime.toLiveData()
    private val _endTime = MutableLiveData<Long>()
    val endTime = _endTime.toLiveData()
    private val _autoKiosk = MutableLiveData<Boolean>()
    val autoKiosk = _autoKiosk.toLiveData()
    private val _liveness = MutableLiveData<Boolean>()
    val liveness = _liveness.toLiveData()

    var className = MutableLiveData<String>()

    var lessonModel = LessonModel("")

    fun setupForAddingNewClass(classID: Long) {
        lessonModel.lessonClassId = classID
        viewModelScope.launch {
            val classModel = classRepository.getClassById(classID)
            classModel?.let {
                lessonModel.lessonName = it.name ?: ""
            }
        }
    }

    fun getClassName(classID: Long){
        viewModelScope.launch {
            val classModel = classRepository.getClassById(classID)
            classModel?.let {
                className.postValue(it.name)
            }
        }
    }

    fun setupForEditLesson(lessonId: Long){
        viewModelScope.launch {
            lessonModel = lessonRepository.getByLessonID(lessonId)
            lessonModel.let {
                it.date = it.startLesson
                _date.postValue(it.date!!)
                _startTime.postValue(it.startLesson!!)
                _endTime.postValue(it.endLesson!!)
                _autoKiosk.postValue(it.autoKiosk)
                _liveness.postValue(it.liveness)
            }
        }
    }

    fun addNewLesson() {
        viewModelScope.launch {
            _genericProgress.postValue(ProgressStatus.LOADING)

            if (lessonModel.lessonClassId == null || lessonModel.lessonClassId == -1L){
                _genericErrorMessage.postValue(app.getString(R.string.lesson_error_message))
                _genericProgress.postValue(ProgressStatus.ERROR)
                return@launch
            }

            if (lessonModel.startLesson!! >= lessonModel.endLesson!!){
                lessonModel.endLesson = lessonModel.endLesson!! + 24*60*60*1000
            }

            lessonRepository.addAll(
                listOf(
                    lessonModel
                )
            )
            _genericProgress.postValue(ProgressStatus.DONE)
            _finishEvent.postValue(Unit)

        }
    }

    private fun getTimeWIthHour(date: Long, startTime: Long): Calendar {
        val startTimeToSet = Calendar.getInstance()
        startTimeToSet.timeInMillis = date
        val tim = Calendar.getInstance()
        tim.timeInMillis = startTime
//        val day = tim.get(Calendar.DAY_OF_MONTH)
        val hour = tim.get(Calendar.HOUR_OF_DAY)
        val min = tim.get(Calendar.MINUTE)
//        startTimeToSet.add(Calendar.DAY_OF_MONTH, day-1)
        startTimeToSet.set(Calendar.HOUR_OF_DAY, hour)
        startTimeToSet.set(Calendar.MINUTE, min)
        return startTimeToSet
    }

    fun onNewDateSelected(date: Long) {
        lessonModel.date = date
        lessonModel.startLesson?.let {
            lessonModel.startLesson =
                getTimeWIthHour(lessonModel.date?:0, it?:0)
                    .timeInMillis
        }
        lessonModel.endLesson?.let {
            lessonModel.endLesson =
                getTimeWIthHour(lessonModel.date?:0, it?:0)
                    .timeInMillis
        }
    }

    fun onNewStartTimeSelected(startTime: Long) {
        lessonModel.date.let { lessonModel.startLesson = getTimeWIthHour(it?:0, startTime).timeInMillis }
    }

    fun onNewEndTimeSelected(endTime: Long) {
        lessonModel.date.let { lessonModel.endLesson = getTimeWIthHour(it?:0, endTime).timeInMillis }
    }

    fun onNewAutoKiosk(kiosk: Boolean){
        lessonModel.autoKiosk = kiosk
    }

    fun onNewLiveness(liveness: Boolean){
        lessonModel.liveness = liveness
    }

}
