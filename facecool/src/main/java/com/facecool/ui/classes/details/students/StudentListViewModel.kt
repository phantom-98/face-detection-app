package com.facecool.ui.classes.details.students

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _students = MutableLiveData<List<CameraDetectionModel>>()
    val students = _students.toLiveData()

    private var classID: Long = -1

    fun onDeleteStudentClicked(student: CameraDetectionModel) {
        viewModelScope.launch(IO) {
            val classDetails = classRepository.getClassById(classID)
            val studentIdList = classDetails?.enrolledStudents?.toMutableList() ?: mutableListOf()
            studentIdList.remove(student.id)
            classDetails?.let {
                it.enrolledStudents = studentIdList
                classRepository.updateClass(it)
                getStudents(classID)
            }
        }
    }

    fun getStudents(classID: Long) {
        this.classID = classID
        viewModelScope.launch(IO) {
            val classDetails = classRepository.getClassById(classID)
            val students = classDetails?.enrolledStudents?.let { userRepository.getAllById(it) } ?: listOf()
            _students.postValue(students)
        }
    }

    fun addNewStudentsById(studentList: LongArray?) {
        studentList?.let { studentIds ->
            viewModelScope.launch(IO) {
                classRepository.getClassById(classID)?.let {
                    val newStudents = it.enrolledStudents.toMutableList()
                    newStudents.addAll(studentIds.toList())
                    it.enrolledStudents = newStudents.distinct()
                    classRepository.updateClass(it)
                    getStudents(classID)
                }
            }
        }
    }

}
