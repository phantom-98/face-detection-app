package com.facecool.ui.camera.businesslogic.events

import com.facecool.ui.students.common.EnrollmentStatus

data class ManualEvent(
    val userId: Long,
    val timeInMilesOfCreation: Long,
    val imagePathName: String,
    val enrollmentStatus: EnrollmentStatus,
    val userName: String,
    val eventMode: EventModeModel,
    val prevStatus: String,
    val lessonId: Long,
)
