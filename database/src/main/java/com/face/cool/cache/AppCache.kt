package com.face.cool.cache

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.provider.ContactsContract.CommonDataKinds.Email

class AppCache(app: Application) : Cache {

    private val preferences: SharedPreferences =
        app.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    companion object {
        private const val PACKAGE = "com.facecool.package."
        private const val PREFERENCES = PACKAGE + "PREFERENCES"
        private const val TOKEN = PACKAGE + "TOKEN"
        private const val FACE_DETECTION_THRESHOLD = PACKAGE + "FACE_DETECTION_THRESHOLD"
        private const val TWIN_DETECTION_THRESHOLD = PACKAGE + "TWIN_DETECTION_THRESHOLD"
        private const val ENABLE_LIVENESS = PACKAGE + "ENABLE_LIVENESS"
        private const val LIVENESS_THRESHOLD = PACKAGE + "LIVENESS_THRESHOLD"
        private const val LIVENESS_TIMEOUT = PACKAGE + "LIVENESS_TIMEOUT"
        private const val PRE_LESSON_TIME_MIN = PACKAGE + "PRE_LESSON_TIME_MIN"
        private const val DEBUG_MODE = PACKAGE + "DEBUG_MODE"
        private const val ADMIN_PIN = PACKAGE + "ADMIN_PIN"
        private const val ADMIN_PIN_DEFAULT_VALUE = "123456"
        private const val IMAGE_QUALITY = PACKAGE + "IMAGE_QUALITY"
        private const val ENABLE_NOTIFICATION = PACKAGE + "ENABLE_NOTIFICATION"
        private const val ENABLE_NOTIFICATION_DEFAULT_VALUE = true
        private const val EMAIL_SERVICE = PACKAGE + "EMAIL_SERVICE"
        private const val EMAIL_SERVICE_DEFAULT_VALUE = 0
        private const val EMAIL_ADDRESS = PACKAGE + "EMAIL_ADDRESS"
        private const val EMAIL_ADDRESS_DEFAULT_VALUE = ""
        private const val EMAIL_PASSWORD = PACKAGE + "EMAIL_PASSWORD"
        private const val EMAIL_PASSWORD_DEFAULT_VALUE = ""
        private const val IMAGE_QUALITY_THRESHOLD_DEFAULT_VALUE = 80
        private const val FACE_DETECTION_THRESHOLD_DEFAULT_VALUE = 0.8f
        private const val TWIN_DETECTION_THRESHOLD_DEFAULT_VALUE = 0.9f
        private const val ENABLE_LIVENESS_DEFAULT_VALUE = false
        private const val LIVENESS_THRESHOLD_DEFAULT_VALUE = 0.5f
        private const val LIVENESS_TIMEOUT_DEFAULT_VALUE = 4
        private const val PRE_LESSON_TIME_MIN_DEFAULT_VALUE = 15L
        private const val DEBUG_MODE_DEFAULT_VALUE = false
    }

    override fun updateToken(token: String) {
        preferences.edit().putString(TOKEN, token).apply()
    }

    override fun getToken(): String {
        return preferences.getString(TOKEN, "") ?: ""
    }

    override fun updateFaceDetectionThreshold(threshold: Float) {
        preferences.edit().putFloat(FACE_DETECTION_THRESHOLD, threshold).apply()
    }

    override fun getFaceDetectionThreshold(): Float {
        return preferences.getFloat(
            FACE_DETECTION_THRESHOLD,
            FACE_DETECTION_THRESHOLD_DEFAULT_VALUE
        )
    }

    override fun updateTwinThreshold(threshold: Float) {
        preferences.edit().putFloat(TWIN_DETECTION_THRESHOLD, threshold).apply()
    }

    override fun getTwinThreshold(): Float {
        return preferences.getFloat(
            TWIN_DETECTION_THRESHOLD,
            TWIN_DETECTION_THRESHOLD_DEFAULT_VALUE
        )
    }

    override fun updateTrackingSpareTime(minutes: Long) {
        preferences.edit().putLong(PRE_LESSON_TIME_MIN, minutes).apply()
    }

    override fun getTrackingSpareTime(): Long {
        return preferences.getLong(
            PRE_LESSON_TIME_MIN,
            PRE_LESSON_TIME_MIN_DEFAULT_VALUE
        )
    }

    override fun updateDebugMode(mode: Boolean) {
        preferences.edit().putBoolean(DEBUG_MODE, mode).apply()
    }

    override fun getDebugMode(): Boolean {
        return preferences.getBoolean(
            DEBUG_MODE,
            DEBUG_MODE_DEFAULT_VALUE
        )
    }

    override fun getAdminPin(): String {
        return preferences.getString(
            ADMIN_PIN,
            ADMIN_PIN_DEFAULT_VALUE
        )?: ADMIN_PIN_DEFAULT_VALUE
    }

    override fun updateAdminPin(pin: String) {
        preferences.edit().putString(ADMIN_PIN, pin).apply()
    }

    override fun getImageQualityThreshold(): Int {
        return preferences.getInt(
            IMAGE_QUALITY,
            IMAGE_QUALITY_THRESHOLD_DEFAULT_VALUE
        )
    }

    override fun updateImageQualityThreshold(threshold: Int) {
        preferences.edit().putInt(IMAGE_QUALITY, threshold).apply()
    }

    override fun booleanValue(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }

    override fun booleanValue(key: String, v: Boolean) {
        preferences.edit().putBoolean(key, v).apply()
    }

    override fun intValue(key: String): Int {
        return preferences.getInt(key, 0)
    }

    override fun intValue(key: String, v: Int) {
        preferences.edit().putInt(key, v).apply()
    }

    override fun getEnableLiveness(): Boolean {
        return preferences.getBoolean(ENABLE_LIVENESS, ENABLE_LIVENESS_DEFAULT_VALUE)
    }

    override fun updateEnableLiveness(enable: Boolean) {
        preferences.edit().putBoolean(ENABLE_LIVENESS, enable).apply()
    }

    override fun getLivenessThreshold(): Float {
        return preferences.getFloat(LIVENESS_THRESHOLD, LIVENESS_THRESHOLD_DEFAULT_VALUE)
    }

    override fun updateLivenessThreshold(threshold: Float) {
        preferences.edit().putFloat(LIVENESS_THRESHOLD, threshold).apply()
    }

    override fun getLivenessTimeout(): Int {
        return preferences.getInt(LIVENESS_TIMEOUT, LIVENESS_TIMEOUT_DEFAULT_VALUE)
    }

    override fun updateLivenessTimeout(timeout: Int) {
        preferences.edit().putInt(LIVENESS_TIMEOUT, timeout).apply()
    }

    override fun getEnableNotification(): Boolean {
        return preferences.getBoolean(ENABLE_NOTIFICATION, ENABLE_NOTIFICATION_DEFAULT_VALUE)
    }

    override fun updateEnableNotification(enable: Boolean) {
        preferences.edit().putBoolean(ENABLE_NOTIFICATION, enable).apply()
    }

    override fun getEmailService(): Int {
        return preferences.getInt(EMAIL_SERVICE, EMAIL_SERVICE_DEFAULT_VALUE)
    }

    override fun updateEmailService(service: Int) {
        preferences.edit().putInt(EMAIL_SERVICE, service).apply()
    }

    override fun getEmail(): String {
        return preferences.getString(EMAIL_ADDRESS, EMAIL_ADDRESS_DEFAULT_VALUE)!!
    }

    override fun updateEmail(email: String) {
        preferences.edit().putString(EMAIL_ADDRESS, email).apply()
    }

    override fun getPassword(): String {
        return preferences.getString(EMAIL_PASSWORD, EMAIL_PASSWORD_DEFAULT_VALUE)!!
    }

    override fun updatePassword(pwd: String) {
        preferences.edit().putString(EMAIL_PASSWORD, pwd).apply()
    }
}