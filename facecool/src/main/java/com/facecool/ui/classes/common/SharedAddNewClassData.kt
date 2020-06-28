package com.facecool.ui.classes.common

import com.facecool.ui.students.select.StudentSelectionModel

object SharedAddNewClassData {

    var selectedStudents: List<StudentSelectionModel> = listOf()
    var className: String = ""
    var classID: String = ""
    var location: String = ""

    var startDate: Long = 0
    var startTime: Long = 0
    var endDate: Long = 0
    var endTime: Long = 0
    var autoKiosk: Boolean = false
    var liveness: Boolean = false

    fun clear() {
        selectedStudents = listOf()
        className = ""
        classID = ""
        location = ""
        startDate = 0
        startTime = 0
        endDate = 0
        endTime = 0
        autoKiosk = false
        liveness = false
    }

}
