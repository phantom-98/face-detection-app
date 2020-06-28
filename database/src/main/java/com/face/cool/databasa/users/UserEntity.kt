package com.face.cool.databasa.users

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    val name: String?,
    val enrolledClass: String?,
    val enrolmentStatus: String?,
    val imageLocalAddress: String?,
    val similarity: Float?,
    val detectedFaceJson: String?,
    val realLiveProbability: Float?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val detectedFeatures: ByteArray?,
    var twin: Boolean?,
    var email: String?,
    var address: String?,
    var lastName: String?,
    var studentId: String?,
    val creationTime: Long?,
    var errors: List<ErrorStatus>?,
    var deleted: Boolean?,
    var phoneNumber: String?,
    var enableNotification: Boolean? = true,
    var parentNames: String?,
    var enableSMS: Boolean? = true,
    var enableEmail: Boolean? = true,
    var enableWhatsapp: Boolean? = true,
    var parentSMSNumbers: String?,
    var parentEmails: String?,
    var parentWhatsappNumbers: String?
)


enum class ErrorStatus {
    ID_ENROLLED,
    NAME_ERROR,
    LAST_NAME_ERROR,
    NO_FACE_DETECTED,
    FACE_ALREADY_ENROLLED,
    IMAGE_NOT_PRESENT,
    IMAGE_QUALITY_IS_LOW
}