package com.facecool.ui.reports.bystudent.selection

import java.io.Serializable

data class StudentSelectionModel(
    val studentName: String,
    val studentId: Long?
): Serializable