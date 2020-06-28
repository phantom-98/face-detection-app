package com.facecool.ui.camera.businesslogic

import com.face.cool.databasa.users.ErrorStatus
import com.facecool.attendance.facedetector.FaceModel
import com.facecool.ui.students.common.EnrollmentStatus
import com.google.gson.annotations.Expose

data class CameraDetectionModel(
    var name: String,
    var enrolmentStatus: EnrollmentStatus = EnrollmentStatus.UNKNOWN,
    var rez: FaceModel? = null,
    var similarity: Float = 0f,
    var id: Long? = null,
    var imageName: String,
    var creationTime: Long
){
    var twin: Boolean = false
    var email: String? = null
    var address: String? = null
    var lastName: String? = null
//    var studentId: Long? = null
    var studentId: String? = null // to do fix!!
    var trackingID: Int? = null
    var deleted: Boolean = false
    var phoneNumber: String? = null
    var enableNotification: Boolean = true
    var parentNames: String? = null
    var enableSMS: Boolean = true
    var parentSMSNumbers: String? = null
    var enableEmail: Boolean = true
    var parentEmails: String? = null
    var enableWhatsapp: Boolean = true
    var parentWhatsappNumbers: String? = null

    // TODO create proper model and map them for the adapter
//    var isHeader: Boolean? = null
//    var header:String = ""

    @Expose var error: MutableList<ErrorStatus> = mutableListOf()
    @Expose var error2: MutableList<ErrorStatus>? = null
}

fun getNullCameraDetectionModel(): CameraDetectionModel {
    return CameraDetectionModel(
        name = "",
        imageName = "",
        creationTime = System.currentTimeMillis(),
    )
}
