package com.facecool.ui.classes.details.ocurrences.details

import com.facecool.ui.students.common.Attendance

data class ClassOccurrenceStudentModel(
    val id: Long,
    val name: String,
    var attendance: Attendance,
    var time: Long,
    val imageName: String,
    var eventId: Long? = null,
    var prevStatus: String? = null,
)
