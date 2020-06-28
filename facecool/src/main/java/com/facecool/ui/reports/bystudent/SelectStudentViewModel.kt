package com.facecool.ui.reports.bystudent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.reports.bystudent.selection.StudentSelectionModel
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectStudentViewModel @Inject constructor(
    private val userRepository: UserRepository
):ViewModel(){

    private val _students = MutableLiveData<List<StudentSelectionModel>>()
    val students = _students.toLiveData()

    private val _selectedStudent = MutableLiveData<StudentSelectionModel>()
    val selectedStudent = _selectedStudent.toLiveData()

    fun getStudents() {
        viewModelScope.launch(Dispatchers.IO) {
            val students = userRepository.getAllEntity().map {
                StudentSelectionModel(it.name?:""+" "+it.lastName, it.uid)
            }
            _students.postValue(students)
        }
    }

    fun onStudentSelected(studentSelectionModel: StudentSelectionModel) {
        _selectedStudent.postValue(studentSelectionModel)
    }
}