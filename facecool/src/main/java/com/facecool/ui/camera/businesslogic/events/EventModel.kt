package com.facecool.ui.camera.businesslogic.events

import android.graphics.Bitmap
import com.facecool.ui.students.common.EnrollmentStatus

data class EventModel(
    val userId: Long,
    val uid: Long,
    val timeInMilesOfCreation: Long,
    val detectedImage: Bitmap?,
    val imagePathName: String,
    val enrollmentStatus: EnrollmentStatus,
    val userName: String,
    val eventMode: EventModeModel,
    val prevStatus: String,
)

enum class EventModeModel{
    IN,OUT, MANUAL// used for manual attendance
}
