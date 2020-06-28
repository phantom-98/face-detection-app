package com.facecool.ui.students.select

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facecool.R
import com.facecool.ui.camera.businesslogic.user.UserRepository
import com.facecool.ui.classes.common.ClassRepository
import com.facecool.ui.classes.common.SharedAddNewClassData
import com.facecool.ui.classes.list.ClassModel
import com.facecool.utils.toLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStudentsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val classRepository: ClassRepository,
    private val app: Application
) : ViewModel() {

    private val _studentFilter = MutableLiveData<List<String>>()
    val studentFilter = _studentFilter.toLiveData()

    private val _generalProgress = MutableLiveData<Boolean>()
    val generalProgress = _generalProgress.toLiveData()

    private val _students = MutableLiveData<List<StudentSelectionModel>>()
    val students = _students.toLiveData()

    private val _selectedStudentsId = MutableLiveData<List<Long>>()
    val selectedStudentsId = _selectedStudentsId.toLiveData()

//    private val _selectedStudents = MutableLiveData<List<StudentSelectionModel>>()
//    val selectedStudents = _selectedStudents.toLiveData()

    private val _className = MutableLiveData<String>()
    val className = _className.toLiveData()

    private val cachedStudentList = mutableListOf<StudentSelectionModel>()
    private var selectedStudentList = mutableListOf<StudentSelectionModel>()
    var selectedClass: String = app.getString(R.string.setting_select_all)
    var searchWord: String = ""

    fun updateStudentList() {
        _generalProgress.postValue(true)
        viewModelScope.launch(IO) {
            val students = userRepository.getAllUsersForSelection()
            cachedStudentList.clear()
            cachedStudentList.addAll(students)
            _students.postValue(cachedStudentList)
            _generalProgress.postValue(false)
        }
    }

    private var cachedClasses = mutableListOf<ClassModel>()

    fun getStudentFilter() {
        viewModelScope.launch(IO) {
            val classes = classRepository.getAllCLasses()
            cachedClasses.clear()
            cachedClasses.addAll(classes)
            val filterList = classes.map { it.name }
            _studentFilter.postValue(listOf(app.getString(R.string.setting_select_all), *filterList.toTypedArray()))
        }
    }

    fun onFilterSelected(filter: Int) {
        if (filter == 0) {
            updateVisibleStudents(app.getString(R.string.setting_select_all))
        } else {
            val className = cachedClasses.getOrNull(filter - 1)?.name ?: app.getString(R.string.setting_select_all)
            updateVisibleStudents(className)
        }
    }

    private fun updateVisibleStudents(filterField: String = app.getString(R.string.setting_select_all)) {
        selectedClass = filterField
        if (filterField == app.getString(R.string.setting_select_all)) {
            _students.postValue(cachedStudentList)
        } else {
            val selectedClass = cachedClasses.firstOrNull { it.name.contains(filterField) }
            val studentIds = selectedClass?.enrolledStudents ?: listOf()
            val searchedData = cachedStudentList.filter { it.studentId in studentIds }
            _students.postValue(searchedData)
        }
    }

    fun onStudentSelected(student: StudentSelectionModel, selectionStatus: Boolean) {
        val foundStudentIndex = cachedStudentList.indexOf(student)
        cachedStudentList.getOrNull(foundStudentIndex)?.isSelected = selectionStatus
//        _students.postValue(cachedStudentList)
        updateSelectedStudents(cachedStudentList)
    }

    fun onStudentClick(student: StudentSelectionModel) {

    }

    fun setAllSelectedStateTo(selectionStatus: Boolean) {
        var force = true
        var ids = mutableListOf<Long>()
        if (selectedClass != app.getString(R.string.setting_select_all)){
            force = false
            val selectClass = cachedClasses.firstOrNull { it.name.contains(selectedClass) }
            ids.addAll(selectClass?.enrolledStudents ?: listOf())
        }
        val list = cachedStudentList.filter { force || it.studentId in ids}.map {
            it.isSelected = selectionStatus
            it
        }
        _students.postValue(list)
        updateSelectedStudents(list)
    }

    private fun updateSelectedStudents(students: List<StudentSelectionModel>) {
        val selectedStudents = students.filter { it.isSelected }
        // TODO fix this stupid singleton
        SharedAddNewClassData.selectedStudents = selectedStudents
//        _selectedStudents.postValue(selectedStudents)
        this.selectedStudentList = selectedStudents.toMutableList()
    }

    fun searchForStudentData(searchTerm: String) {
        var force = true
        var ids = mutableListOf<Long>()
        if (selectedClass != app.getString(R.string.setting_select_all)){
            force = false
            val selectClass = cachedClasses.firstOrNull { it.name.contains(selectedClass) }
            ids.addAll(selectClass?.enrolledStudents ?: listOf())
        }
        if (searchTerm.isEmpty()) {
            _students.postValue(cachedStudentList.filter { force || it.studentId in ids })
        } else {
            val searchedData = cachedStudentList.filter { (force || it.studentId in ids) && it.name.contains(searchTerm, true) }
            _students.postValue(searchedData)
        }
    }

    fun onFinishSelection() {
        _selectedStudentsId.postValue(selectedStudentList.map { it.studentId })
    }

    fun updateScreenByClassData(classID: Long) {
        viewModelScope.launch(IO) {

            val classData = classRepository.getClassById(classID)
            _className.postValue(classData?.name)
            classData?.enrolledStudents

        }
    }

    fun updateStudentListExcludingClass(classID: Long) {
        _generalProgress.postValue(true)
        viewModelScope.launch(IO) {
            val students = userRepository.getAllUsersForSelection()
            val classData = classRepository.getClassById(classID)
            val existingStudents = classData?.enrolledStudents ?: listOf()
            val filteredStudents = students.filter { it.studentId !in existingStudents }
            cachedStudentList.clear()
            cachedStudentList.addAll(filteredStudents)
            _students.postValue(cachedStudentList)
            _generalProgress.postValue(false)
        }
    }

}