package com.facecool.ui.reports.yxl.report

sealed class ReportData {

    data class Date(
        val readableDate: String,
        val startLesson: Long,
        val endLesson: Long,
    ) : ReportData()

    data class Attendance(
        val value: String,
        val date: String
    ) : ReportData()

    data class ClassName(
        val name: String,
        val classID: String
    ) : ReportData()

    data class StudentName(
        val firstName: String,
        val lastName: String,
        val studentId: Long,
        val displayStudentId: String
    ) : ReportData()

    data class StudentPhoto(
        val localPath: String,
        val studentId: Long
    ) : ReportData()
}
