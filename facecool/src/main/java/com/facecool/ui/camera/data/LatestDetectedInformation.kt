package com.facecool.ui.camera.data

import android.graphics.Bitmap
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.students.common.EnrollmentStatus

data class LatestDetectedInformation(
    val detectedFaceImage: Bitmap,
    val databaseMatchingImage: Bitmap,
    val name: String,
    val enrollmentStatus: EnrollmentStatus
){
    var data: CameraDetectionModel? = null
}
