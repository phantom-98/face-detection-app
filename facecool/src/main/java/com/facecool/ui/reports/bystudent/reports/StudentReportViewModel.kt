package com.facecool.ui.reports.bystudent.reports

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.cool.cache.Cache
import com.facecool.R
import com.facecool.ui.camera.businesslogic.events.EventModeModel
import com.facecool.ui.camera.businesslogic.events.EventRepository
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.common.lessons.LessonRepository
import com.facecool.ui.reports.byclass.report.ClassReportViewModel
import com.facecool.ui.reports.byclass.report.ReportData
import com.facecool.utils.getReadableDate
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class StudentReportViewModel @Inject constructor(
//    private val userRepository: UserRepository,
    private val classRepository: ClassRepository,
    private val lessonRepository: LessonRepository,
    private val eventRepository: EventRepository,
    private val cache: Cache,
    private val app: Application
) : ViewModel(){

    companion object {
        private const val CVS_COMMA = ","
    }

    private val _reportFileData = MutableLiveData<String>()
    val reportFileData = _reportFileData.toLiveData()

    private val _studentReportList = MutableLiveData<List<StudentReportModel>>()
    val studentReportList = _studentReportList

    private val _generalLoading = MutableLiveData<ProgressStatus>()
    val generalLoading = _generalLoading.toLiveData()

    private val report = mutableListOf<StudentReportModel>()

    fun getStudentReportData(studentId: Long) {
        _generalLoading.postValue(ProgressStatus.LOADING)
        viewModelScope.launch {
            val classIds = classRepository.getAllEntity().filter {
                it.enrolledStudents?.contains(studentId)?:false
            }.map {
                it.uid
            }
            val lessonList = lessonRepository.getAll().filter {
                classIds.contains(it.lessonClassId) && it.startLesson!!-cache.getTrackingSpareTime()*60000 < System.currentTimeMillis()
            }

            report.clear()
            lessonList.forEach {
                val events = eventRepository.getAllIdAndTime(studentId, it.startLesson!!-cache.getTrackingSpareTime()*60000, it.endLesson!!)
                val ins = events.filter { it.eventMode == EventModeModel.IN }
                val outs = events.filter { it.eventMode == EventModeModel.OUT }
                var timeIn = app.getString(R.string.class_report_absent_label)
                var timeOut = app.getString(R.string.class_report_absent_label)
                if (ins.isNotEmpty()){
                    timeIn = ins.first().timeInMilesOfCreation.getReadableDate(pattern = "HH:mm")
                }
                if (outs.isNotEmpty()){
                    timeOut = outs.first().timeInMilesOfCreation.getReadableDate(pattern = "HH:mm")
                }
                report.add(
                    StudentReportModel(
                        it.lessonName,
                        it.lessonClassId.toString(),
                        it.startLesson?.getReadableDate(pattern = "MMM dd")?:"",
                        timeIn,
                        timeOut
                    )
                )
            }
            _studentReportList.postValue(report)
            _generalLoading.postValue(ProgressStatus.DONE)
        }
    }

    fun prepareReportForSharing() {
        val cvs = report.joinToString(separator = "\n") {
            it.studentClass + CVS_COMMA + it.date + CVS_COMMA + it.timeIn + CVS_COMMA + it.timeOut
        }
        _reportFileData.postValue(cvs)
    }

}
