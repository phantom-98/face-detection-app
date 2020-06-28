package com.facecool.ui.classes.details.students

import com.facecool.ui.students.common.Attendance

data class ClasStudentModel(
    val name: String,
    val id: Long,
    val attendance: Attendance
)
