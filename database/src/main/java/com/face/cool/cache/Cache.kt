package com.face.cool.cache

interface Cache {

    fun updateToken(token: String)

    fun getToken(): String

    fun updateFaceDetectionThreshold(threshold: Float)

    fun getFaceDetectionThreshold(): Float

    fun updateTrackingSpareTime(minutes: Long)

    fun getTrackingSpareTime(): Long

    fun updateDebugMode(mode: Boolean)

    fun getDebugMode(): Boolean

    fun booleanValue(key: String): Boolean

    fun booleanValue(key: String, v: Boolean)

    fun intValue(key: String): Int

    fun intValue(key: String, v: Int)

    fun getImageQualityThreshold(): Int

    fun updateImageQualityThreshold(threshold: Int)

    fun getTwinThreshold(): Float

    fun updateTwinThreshold(threshold: Float)

    fun getAdminPin(): String

    fun updateAdminPin(pin: String)

    fun getEnableLiveness(): Boolean

    fun updateEnableLiveness(enable: Boolean)

    fun getLivenessThreshold(): Float

    fun updateLivenessThreshold(threshold: Float)

    fun getLivenessTimeout(): Int

    fun updateLivenessTimeout(timeout: Int)

    fun getEnableNotification(): Boolean

    fun updateEnableNotification(enable: Boolean)

    fun getEmailService(): Int

    fun updateEmailService(service: Int)

    fun getEmail(): String

    fun updateEmail(email: String)

    fun getPassword(): String

    fun updatePassword(pwd: String)
}