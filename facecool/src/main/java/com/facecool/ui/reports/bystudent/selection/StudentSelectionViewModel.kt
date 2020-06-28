package com.facecool.ui.reports.bystudent.selection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentSelectionViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {

    private val _students = MutableLiveData<List<StudentSelectionModel>>()
    val students = _students.toLiveData()

    private val _selectedStudent = MutableLiveData<StudentSelectionModel>()
    val selectedStudent = _selectedStudent.toLiveData()

    var studentList = mutableListOf<StudentSelectionModel>()
    fun getStudents(){
        viewModelScope.launch {
            studentList = userRepository.getAllEntity().filter { it.enrolmentStatus == "ENROLLED" }.map {
                StudentSelectionModel(it.name + " " + it.lastName, it.uid)
            }.toMutableList()
            _students.postValue(studentList)
        }
    }

    fun searchForStudent(keyword: String){
        _students.postValue(studentList.filter {
            (it.studentName + it.studentId.toString()).uppercase().contains(keyword.uppercase())
        })
    }

    fun onStudentSelected(studentModel: StudentSelectionModel) {
        _selectedStudent.postValue(studentModel)
    }

}