package com.facecool.ui.camera.businesslogic.user

import androidx.lifecycle.LiveData
import com.face.cool.databasa.detection_events.EventEntity
import com.face.cool.databasa.users.UserEntity
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.events.EventModel
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.ui.students.select.StudentSelectionModel

interface UserRepository {

    suspend fun saveUser(model: CameraDetectionModel, enrollmentStatus: EnrollmentStatus) : CameraDetectionModel?

    suspend fun saveAll(users: List<UserEntity>): List<Long>

    suspend fun getUser(userId: Long): CameraDetectionModel?

    suspend fun updateUser(user: CameraDetectionModel)

    suspend fun getAll(): List<CameraDetectionModel>

    suspend fun getAllWhereNameAndEnrollment(name: String, status: EnrollmentStatus, limit: Long? = null): List<CameraDetectionModel>

    suspend fun getAllByEnrollment(status: EnrollmentStatus, limit: Long? = null): List<CameraDetectionModel>
    suspend fun getAllByEnrollmentNot(status: EnrollmentStatus, limit: Long? = null): List<CameraDetectionModel>
    suspend fun cleanRejectedEnrollment(limit: Long? = null)
    suspend fun getAllOrderByEnrollment(): List<CameraDetectionModel>

    suspend fun getAllEntity(): List<UserEntity>

    suspend fun getAllById(idList: List<Long>): List<CameraDetectionModel>

    suspend fun getAllUsersForSelection(): List<StudentSelectionModel>

    suspend fun getByEventsListAll(events: List<EventEntity>): List<Pair<CameraDetectionModel, EventModel?>>

    suspend fun getAllByCLass(enrolledClass: String): List<CameraDetectionModel>

    suspend fun deleteUser(id: Long)

    suspend fun deleteUser(entity: UserEntity)

    suspend fun getAllFaceFeatures(): List<ByteArray>

    fun getAllLive(): LiveData<List<UserEntity>>

    suspend fun checkIfEnrolled(currentFeature: ByteArray, twin: Boolean): String
    suspend fun checkIfRejected(currentFeature: ByteArray, twin: Boolean): Long?

    suspend fun checkIfAdmin(currentFeature: ByteArray): String
    suspend fun enableNotification(enable: Boolean)
}
