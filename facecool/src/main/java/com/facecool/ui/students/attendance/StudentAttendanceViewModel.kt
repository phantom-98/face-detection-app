package com.facecool.ui.students.attendance

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.facecool.utils.toLiveData

class StudentAttendanceViewModel : ViewModel(){

    private val _attendanceList = MutableLiveData<List<AttendanceModel>>()
    val attendanceList = _attendanceList.toLiveData()

    fun getAttendanceForStudent(id: Int) {
        _attendanceList.postValue(
            listOf(
                AttendanceModel("12/12/2020", true, "Math"),
                AttendanceModel("12/12/2020", true, "Math"),
                AttendanceModel("12/12/2020", true, "Math"),
                AttendanceModel("12/12/2020", true, "Math"),
                AttendanceModel("12/12/2020", true, "Math"),
            )
        )
    }

}