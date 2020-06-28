package com.facecool.ui.classes.details.ocurrences

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.R
import com.facecool.ui.classes.add.time.LessonModel
import com.facecool.ui.common.lessons.LessonRepository
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassesOccurrencesViewModel @Inject constructor(
    private val app: Application,
    private val lessonRepository: LessonRepository
) : ViewModel() {

    private val _lessons = MutableLiveData<List<LessonModel>>()
    val lessons = _lessons.toLiveData()

    private var classId: Long = -1

    fun getClassData(classId: Long) {
        this.classId = classId
        viewModelScope.launch {
            val currentDate = System.currentTimeMillis()
            val lessons = lessonRepository.getAllByClassID(classId)
                .map {
                    if (currentDate > it.endLesson!!) {
                        it.status = app.getString(R.string.lesson_status_finished)
                    } else
                        if ( (currentDate > it.startLesson!!) && (  currentDate < it.endLesson!! )   ) {
                        it.status = app.getString(R.string.lesson_status_progress)
                    } else {
                        it.status = app.getString(R.string.lesson_status_scheduled)
                    }
                    it
                }.sortedBy { it.startLesson }
            _lessons.postValue(lessons)
        }
    }

    fun deleteOccurrence(lesson: LessonModel) {
        viewModelScope.launch {
            lessonRepository.deleteLesson(lesson)
            getClassData(classId)
        }
    }

}
