package com.facecool.ui.students.select

data class StudentSelectionModel(
    val studentId: Long,
    val name: String,
    var isSelected: Boolean = false,
    val studentImagePath: String,
)
