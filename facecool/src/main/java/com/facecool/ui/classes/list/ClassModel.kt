package com.facecool.ui.classes.list

data class ClassModel(
    var name: String,
    var id: String,
    val uuid: Long,
    val description: String,
    val status: String,
    val nextLessonTime: String,
    var enrolledStudents: List<Long>
)
