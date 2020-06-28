package com.facecool.ui.classes.details.ocurrences.details

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.face.cool.cache.Cache
import com.facecool.R
import com.facecool.ui.camera.businesslogic.events.EventMapper
import com.facecool.ui.camera.businesslogic.events.EventModeModel
import com.facecool.ui.camera.businesslogic.events.EventRepository
import com.facecool.ui.camera.businesslogic.events.ManualEvent
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.classes.add.time.LessonModel
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.students.common.Attendance
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.utils.getReadableDate
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassOccurrenceDetailsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val classRepository: ClassRepository,
    private val eventMapper: EventMapper,
    private val cache: Cache,
    private val app: Application
) : ViewModel() {

    companion object {
        private const val OFFSET_TIME = 1000L

    }

    private var classOccurrence: LessonModel? = null

    private val _students = MutableLiveData<List<ClassOccurrenceStudentModel>>()
    val students = _students.toLiveData()

    private val _title = MutableLiveData<String>()
    val title = _title.toLiveData()

    private val _lessonDetails = MutableLiveData<String>()
    val lessonDetails = _lessonDetails.toLiveData()

    fun setClassOccurrence(classOccurrence: LessonModel) {
        this.classOccurrence = classOccurrence

        val title = app.getString(R.string.lesson_details_for, classOccurrence.lessonName)
        _title.postValue(title)

        val date = "${classOccurrence.startLesson?.getReadableDate(pattern = "MMMM dd, HH:mm")} - ${
            classOccurrence.endLesson?.getReadableDate(pattern = "HH:mm")
        } "
        _lessonDetails.postValue(date)

        viewModelScope.launch(IO) {

            val startLesson = classOccurrence.startLesson
            val endLesson = classOccurrence.endLesson

            val currentTIme = System.currentTimeMillis()

            val classDetails = classRepository.getClassById(classOccurrence.lessonClassId)
            val students = classDetails?.enrolledStudents?.let { userRepository.getAllById(it) }
                ?: listOf()

            if (currentTIme > (startLesson!! - (cache.getTrackingSpareTime() * 1000L * 60L))) {


                val attendance = students.map {
                    val studentID = it.id ?: -1
                    var events = eventRepository.getAllIdAndTime(
                        studentID,
                        (startLesson!! - (cache.getTrackingSpareTime() * 1000L * 60L)),
                        endLesson!!
                    ).filter { it.eventMode != EventModeModel.OUT }.toMutableList()
                    val event = eventRepository.getAllByLessonId(studentID, classOccurrence.lessonDdId!!)
                    if (event.isNotEmpty())
                        events.add(eventMapper.fromEntityToModel(event.first()))
                    //TODO use this to unify every call to time
                    val time = events.sortedBy { it.timeInMilesOfCreation }
                        .firstOrNull()?.timeInMilesOfCreation ?: -1

                    var studentStatus = Attendance.ABSENT
                    var eventId: Long? = null
                    var prevStatus: String? = null
                    if (events.isNotEmpty()) {
                        eventId = events.first().uid
                        prevStatus = events.first().prevStatus
                        studentStatus = Attendance.PRESENT
                        if (time > startLesson!!) {
                            studentStatus = Attendance.LATE
                        }
                        if (events.first().eventMode == EventModeModel.MANUAL){
                            studentStatus = Attendance.MANUAL
                        }
                    }


                    ClassOccurrenceStudentModel(
                        studentID,
                        " ${it.studentId} - ${it.name} ${it.lastName}",
                        studentStatus,
                        time,
                        it.imageName,
                        eventId,
                        prevStatus
                    )
                }
                _students.postValue(attendance)
            } else {

                val attendance = students.map {
                    val studentID = it.id ?: -1
                    ClassOccurrenceStudentModel(
                        studentID,
                        " ${it.studentId} - ${it.name} ${it.lastName}",
                        Attendance.TBD,
                        -1,
                        it.imageName
                    )
                }
                _students.postValue(attendance)
            }
        }

    }

    fun onStudentClicked(student: ClassOccurrenceStudentModel) {

    }

    fun setStudentToPResent(student: ClassOccurrenceStudentModel) {
        val clas = classOccurrence ?: return
        if (student.attendance == Attendance.ABSENT)
            student.time = System.currentTimeMillis()
        student.prevStatus = student.attendance.name
        student.attendance = Attendance.MANUAL
        viewModelScope.launch(IO) {
            val event = ManualEvent(
                userId = student.id,
                timeInMilesOfCreation = student.time,
                imagePathName = student.imageName,
                enrollmentStatus = EnrollmentStatus.ENROLLED,
                userName = student.name,
                eventMode = EventModeModel.MANUAL,
                prevStatus = student.prevStatus?:"",
                lessonId = clas.lessonDdId!!
            )
            student.eventId = eventRepository.saveEvent(event)
//            setClassOccurrence(clas)
        }
    }

    fun revertStudentManually(student: ClassOccurrenceStudentModel){
        student.attendance = when(student.prevStatus){
            Attendance.LATE.name -> Attendance.LATE
            Attendance.ABSENT.name -> Attendance.ABSENT
            else -> Attendance.MANUAL
        }
        val eventId = student.eventId
        val prevStatus = student.prevStatus
        student.eventId = null
        student.prevStatus = null

        viewModelScope.launch(IO) {
            eventId?.let {
                if (prevStatus == Attendance.ABSENT.name){
                    eventRepository.deleteEventById(it)
                } else {
                    val event = eventRepository.getById(it)
                    event.eventMode = EventModeModel.IN.name
                    event.lessonId = null
                    event.prevStatus = null
                    eventRepository.saveAllEvents(listOf(event))
                }
            }
//            setClassOccurrence(clas)
        }
    }

}