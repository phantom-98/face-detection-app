package com.facecool.ui.students.details

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.common.ProgressStatus
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentEnrolledClassesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val classRepository: ClassRepository,
    private val app: Application
):ViewModel(){

    private val _studentEnrolledClasses = MutableLiveData<List<StudentEnrolledClassModel>>()
    val studentEnrolledClasses = _studentEnrolledClasses.toLiveData()

    private val _enrolledClassesProgress = MutableLiveData<ProgressStatus>()
    val enrolledClassesProgress = _enrolledClassesProgress.toLiveData()

    fun getStudentEnrolledClasses(studentId: Long) {
        _enrolledClassesProgress.postValue(ProgressStatus.LOADING)
        viewModelScope.launch(Dispatchers.IO) {
            val classes = classRepository.getAllCLasses()
            val enrolledClasses = classes.filter {
                it.enrolledStudents.contains(studentId)
            }.map {
                StudentEnrolledClassModel(it.name, it.uuid)
            }
            _studentEnrolledClasses.postValue(enrolledClasses)
            _enrolledClassesProgress.postValue(ProgressStatus.DONE)
        }
    }

    fun onRemoveClass(studentEnrolledClassModel: StudentEnrolledClassModel, studentId: Long) {
        studentEnrolledClassModel.id
        viewModelScope.launch(Dispatchers.IO) {
            val classData = classRepository.getClassById(studentEnrolledClassModel.id)
            val students = classData?.enrolledStudents?.toMutableList() ?: mutableListOf()
            students.remove(studentId)
            classData?.enrolledStudents = students
            if (classData != null) {
                classRepository.updateClass(classData)
                getStudentEnrolledClasses(studentId)
            }
        }
    }
}