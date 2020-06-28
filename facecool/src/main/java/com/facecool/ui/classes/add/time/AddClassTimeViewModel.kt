package com.facecool.ui.classes.add.time

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.R
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.classes.common.SharedAddNewClassData
import com.facecool.ui.classes.list.ClassModel
import com.facecool.ui.common.lessons.LessonRepository
import com.facecool.utils.getReadableDate
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddClassTimeViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val classRepository: ClassRepository,
    private val app: Application
) : ViewModel() {

    private val _weekDays = MutableLiveData<List<DayModel>>()
    val weekDays = _weekDays.toLiveData()

    private val _customDays = MutableLiveData<List<CustomDayModel>>()
    val customDays = _customDays.toLiveData()

    private val _repeatType = MutableLiveData<List<String>>()
    val repeatType = _repeatType.toLiveData()

    private val _viewMode = MutableLiveData<RepeatType>()
    val viewMode = _viewMode.toLiveData()

    private val _startDate = MutableLiveData<String>()
    val startDate = _startDate.toLiveData()

    private val _endDate = MutableLiveData<String>()
    val endDate = _endDate.toLiveData()

    private val _startTime = MutableLiveData<String>()
    val startTime = _startTime.toLiveData()

    private val _endTime = MutableLiveData<String>()
    val endTime = _endTime.toLiveData()

    private val _onLessonsCreated = MutableLiveData<Int>()
    val onLessonsCreated = _onLessonsCreated.toLiveData()

    private val _onClassCreated = MutableLiveData<Long>()
    val onClassCreated = _onClassCreated.toLiveData()

    private val days = mutableListOf<DayModel>()
    private val customs = mutableListOf<CustomDayModel>(
        CustomDayModel("Monday", 0,0)
    )
    private val repeatTypeList = mutableListOf<String>()

    private val lessons = mutableListOf<LessonModel>()

    fun updateRepeatType() {
        val repeatTypeList = RepeatType.values().map { it.name.capitalize() }
        this.repeatTypeList.clear()
        this.repeatTypeList.addAll(repeatTypeList)
        _repeatType.postValue(repeatTypeList)
    }

    fun onDaySelected(day: DayModel) {
        days.forEach {
            if (it.day == day.day) {
                it.isSelected = !it.isSelected
                return@forEach
            }
        }
        _weekDays.postValue(days)
    }

    fun onDataSelected(day: CustomDayModel){
        customs.forEach {
            if (it.day == day.day){
                it.startTime = day.startTime
                it.endTime = day.endTime
                return@forEach
            }
        }

        _customDays.postValue(customs)
    }

    fun updateWeekData() {
        days.clear()
        days.addAll(
            listOf(
                DayModel(app.getString(R.string.lesson_day_sunday), false),
                DayModel(app.getString(R.string.lesson_day_monday), false),
                DayModel(app.getString(R.string.lesson_day_tuesday), false),
                DayModel(app.getString(R.string.lesson_day_wednesday), false),
                DayModel(app.getString(R.string.lesson_day_thursday), false),
                DayModel(app.getString(R.string.lesson_day_friday), false),
                DayModel(app.getString(R.string.lesson_day_saturday), false),
            )
        )

        _weekDays.postValue(days)
    }

    fun updateMonthData(){
        days.clear()
        for (i in 1 until 32)
            days.add(DayModel(i.toString(), false))
        _weekDays.postValue(days)
    }

    fun repeatTypeSelected(position: Int) {

        when(repeatTypeList[position]){
            "DAILY" -> _viewMode.postValue(RepeatType.DAILY)
            "WEEKLY" -> _viewMode.postValue(RepeatType.WEEKLY)
            "MONTHLY" -> _viewMode.postValue(RepeatType.MONTHLY)

            else -> _viewMode.postValue(RepeatType.CUSTOM)
        }
    }

    fun updateStartDate(startDate: Long) {
        SharedAddNewClassData.startDate = startDate
        _startDate.postValue(startDate.getReadableDate(pattern = "MMM dd, yyyy"))
    }

    fun updateEndDate(endDate: Long) {
        SharedAddNewClassData.endDate = endDate
        _endDate.postValue(endDate.getReadableDate(pattern = "MMM dd, yyyy"))
    }

    fun updateStartTime(startTime: Long) {
        SharedAddNewClassData.startTime = startTime
        _startTime.postValue(startTime.getReadableDate(pattern = "HH:mm"))
    }

    fun updateEndTime(endTime: Long) {
        SharedAddNewClassData.endTime = endTime
        _endTime.postValue(endTime.getReadableDate(pattern = "HH:mm"))
    }

    fun updateAutoKiosk(kiosk: Boolean){
        SharedAddNewClassData.autoKiosk = kiosk
//        _autoKiosk.postValue(kiosk)
    }

    fun updateLiveness(liveness: Boolean){
        SharedAddNewClassData.liveness = liveness
    }

    fun generateLessons() {
        lessons.clear()
        var currentDate = SharedAddNewClassData.startDate
        if (SharedAddNewClassData.startTime >= SharedAddNewClassData.endTime){
            SharedAddNewClassData.endTime += 24*60*60*1000
        }
        while (currentDate <= SharedAddNewClassData.endDate) {
            when (_viewMode.value?:RepeatType.DAILY){
                RepeatType.DAILY ->{}
                RepeatType.WEEKLY -> {
                    if (days[currentDate.getDayOfWeek()].isSelected.not()) {
                        currentDate = addDayToTimestamp(currentDate)
                        continue
                    }
                }
                RepeatType.MONTHLY -> {
                    if (days[currentDate.getDayOfMonth()].isSelected.not()) {
                        currentDate = addDayToTimestamp(currentDate)
                        continue
                    }
                }
                RepeatType.CUSTOM -> {
                    if (days[currentDate.getDayOfWeek()].isSelected){

                    }
                    currentDate = addDayToTimestamp(currentDate)
                    continue
                }
            }

            val newLesson = LessonModel(
                lessonName = "" + SharedAddNewClassData.className,
                startLesson = currentDate.setTimeToDate(SharedAddNewClassData.startTime),
                endLesson = currentDate.setTimeToDate(SharedAddNewClassData.endTime),
                autoKiosk = SharedAddNewClassData.autoKiosk,
                liveness = SharedAddNewClassData.liveness
            )

            lessons.add(newLesson)
            currentDate = addDayToTimestamp(currentDate)
        }
        _onLessonsCreated.postValue(lessons.size)
    }

    private fun Long.getDayOfWeek(): Int {
        val c = Calendar.getInstance()
        c.timeInMillis = this

        return c[Calendar.DAY_OF_WEEK]-1
    }

    private fun Long.getDayOfMonth(): Int {
        val c = Calendar.getInstance()
        c.timeInMillis = this

        return c[Calendar.DAY_OF_MONTH]-1
    }

    private fun Long.setTimeToDate(timeWithTime: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this

        val c = Calendar.getInstance()
        c.timeInMillis = timeWithTime
        val hour = c[Calendar.HOUR_OF_DAY]
        val minute = c[Calendar.MINUTE]

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.add(Calendar.DAY_OF_MONTH, c[Calendar.DAY_OF_MONTH]-1)
//        Log.d("time calc", "Date=$this(${this.getReadableDate()}), time=$timeWithTime(${timeWithTime.getReadableDate()}), result=${calendar.timeInMillis}(${calendar.timeInMillis.getReadableDate()})")
        return calendar.timeInMillis
    }

    private fun addDayToTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.timeInMillis
    }

    fun createClass() {
        viewModelScope.launch(Dispatchers.IO) {
            val classId = classRepository.addClass(
                ClassModel(
                    name = SharedAddNewClassData.className,
                    id = SharedAddNewClassData.classID,
                    uuid = -1,
                    description = "",
                    status = app.getString(R.string.lesson_status_scheduled),
                    nextLessonTime = SharedAddNewClassData.startDate.getReadableDate(pattern = "MMM dd"),
                    enrolledStudents = SharedAddNewClassData.selectedStudents.map { it.studentId }
                )
            )

            val l = lessons.onEach { it.lessonClassId = classId }
            lessonRepository.addAll(l)
            _onClassCreated.postValue(classId)
        }
    }

}
