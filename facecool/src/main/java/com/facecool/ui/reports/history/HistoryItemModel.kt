package com.facecool.ui.reports.history

import com.facecool.ui.students.common.EnrollmentStatus


data class HistoryItemModel(
    val studentIdAndName: String,
    val studentDatabaseId: Long,
    val eventDatabaseId: Long,
    val eventCreation: String,
    val detectedImageAddress: String,
    val enrolledImageAddress: String = UNKNOWN_ENROLLMENT,
    val enrollmentStatus: EnrollmentStatus = EnrollmentStatus.UNKNOWN
)

const val UNKNOWN_ENROLLMENT = "UNKNOWN_ENROLLMENT"