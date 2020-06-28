package com.facecool.ui.reports.bystudent.reports

import java.util.*

data class StudentReportModel(
    val studentClass: String,
    val studentClassId: String,
    val date: String,
    val timeIn: String,
    val timeOut: String,
)
