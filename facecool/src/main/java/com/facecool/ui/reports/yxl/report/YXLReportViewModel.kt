package com.facecool.ui.reports.yxl.report

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.cool.cache.Cache
import com.facecool.R
import com.facecool.ui.camera.businesslogic.events.EventRepository
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.common.ProgressStatus
import com.facecool.ui.common.lessons.LessonRepository
import com.facecool.ui.reports.yxl.report.ReportData.Attendance
import com.facecool.ui.reports.yxl.report.ReportData.ClassName
import com.facecool.ui.reports.yxl.report.ReportData.Date
import com.facecool.ui.reports.yxl.report.ReportData.StudentName
import com.facecool.ui.reports.yxl.selection.ClassSelectionModel
import com.facecool.utils.getReadableDate
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YXLReportViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val classRepository: ClassRepository,
    private val lessonRepository: LessonRepository,
    private val eventRepository: EventRepository,
    private val cache: Cache,
    private val app: Application
) : ViewModel() {

    companion object {
        private const val CVS_COMMA = ","
    }

    private val _reportFileData = MutableLiveData<String>()
    val reportFileData = _reportFileData.toLiveData()

    private val _reportList = MutableLiveData<MutableList<MutableList<ReportData>>>()
    val reportList = _reportList.toLiveData()

    private val _generalLoading = MutableLiveData<ProgressStatus>()
    val generalLoading = _generalLoading.toLiveData()

    private var report = mutableListOf<MutableList<ReportData>>()

    fun getReportFor(classSelectionModel: ClassSelectionModel?) {
        _generalLoading.postValue(ProgressStatus.LOADING)
        viewModelScope.launch(Dispatchers.IO) {
            val classId = classSelectionModel?.classID ?: -1
            val classDetails = classRepository.getClassById(classId)
            val lessons = lessonRepository.getAllByClassID(classId).filter {
                it.startLesson!! - cache.getTrackingSpareTime()*60000 < System.currentTimeMillis()
            }
            val students =
                userRepository.getAllById((classDetails?.enrolledStudents?.toList() ?: listOf()))

            report.clear()

            // add class name
            report.add(
                mutableListOf(
                    Attendance("",""),//upon student photos
                    ClassName(
                        classDetails?.name ?: "",
                        classDetails?.id ?: ""
                    )
                )
            )

            // add lesson dates
            lessons.forEach {
                report.get(0).add(
                    Date(
                        it.startLesson?.getReadableDate(pattern = "MMM dd HH:mm-") + it.endLesson?.getReadableDate(pattern = "HH:mm"),
                        it.startLesson!!,
                        it.endLesson!!
                    )
                )
            }

            // add student names
            students.forEach {
                report.add(
                    mutableListOf(
                        ReportData.StudentPhoto(
                            it.imageName,
                            it.id ?: -1
                        ),
                        StudentName(
                            it.name,
                            it.lastName ?: "",
                            it.id ?: -1,
                            it.studentId ?: "",
                        )
                    )
                )
            }

            // add attendance
            report.forEachIndexed { indexColumn, columnData ->
                val dates = report.getOrNull(0) ?: return@launch
                dates.forEachIndexed { indexRow, rowData ->
                    if (indexColumn > 0 && indexRow > 1) {
                        val studentId = when (val s = columnData[1]) {
                            is StudentName -> s.studentId
                            else -> -1
                        }

                        val startLesson = when (val a = rowData) {
                            is Date -> a.startLesson
                            else -> -1L
                        }
                        val endLesson = when (val a = rowData) {
                            is Date -> a.endLesson
                            else -> -1L
                        }
                        val events = eventRepository.getAllIdAndTime(
                            studentId,
                            startLesson - (cache.getTrackingSpareTime() * 1000L * 60L),
                            endLesson
                        )
                        var value: String = app.getString(R.string.class_report_absent_label)
                        var date: String = ""

                        if (!events.isEmpty()){
                            val d = events.sortedBy { it.timeInMilesOfCreation }.firstOrNull()
                                ?.timeInMilesOfCreation

                            if (d!! > startLesson){
                                value = app.getString(R.string.class_report_Late_label)
                            } else {
                                value = app.getString(R.string.class_report_present_label)
                            }
                            date = d?.getReadableDate(pattern = "HH:mm")!!
                        }

                        columnData.add(
                            Attendance(
                                value,
                                date
                            )
                        )
                    }
                }
            }

            _reportList.postValue(report)
            _generalLoading.postValue(ProgressStatus.DONE)

        }
    }

    fun prepareReportForSharing() {
        val cvs = report.joinToString(separator = "\n") {
            it.subList(1, it.size-1).joinToString(separator = CVS_COMMA) { reportData ->
                when (reportData) {
                    is ClassName -> String.format(
                        app.getString(R.string.class_report_cvs_colum_labels),
                        reportData.classID,
                        reportData.name
                    )

                    is Attendance -> reportData.value
                    is Date -> {
                        val startLessonDate =
                            reportData.startLesson.getReadableDate(pattern = "MM/dd/yyyy ")
                        val startLessonTime =
                            reportData.startLesson.getReadableDate(pattern = "HH:mm")
                        val endLessonTime = reportData.endLesson.getReadableDate(pattern = "HH:mm")
                        "${startLessonDate}${startLessonTime}-${endLessonTime}"
                    }

                    is StudentName -> "${reportData.displayStudentId} , ${reportData.firstName} , ${reportData.lastName}  "
                    is ReportData.StudentPhoto -> "I"
                }
            }
        }
        _reportFileData.postValue(cvs)
    }

}
