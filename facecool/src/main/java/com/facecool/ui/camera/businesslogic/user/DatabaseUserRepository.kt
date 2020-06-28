package com.facecool.ui.camera.businesslogic.user

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.face.cool.cache.Cache
import com.face.cool.common.image.ImageManager
import com.face.cool.databasa.Database
import com.face.cool.databasa.detection_events.EventEntity
import com.face.cool.databasa.users.UserEntity
import com.facecool.R
import com.facecool.attendance.facedetector.FaceDetectionEngine
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.events.EventModel
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.ui.students.select.StudentSelectionModel
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class DatabaseUserRepository @Inject constructor(
    private val database: Database,
    private val mapper: UserMapper,
    private val imageManager: ImageManager,
    private val faceDetectionEngine: FaceDetectionEngine,
    private val cache: Cache,
) : UserRepository {

    /**
     * TODO
     * when saving user, check the database for unknown users to and remove them that match the enrolled data and udpate the events ID
     */
    override suspend fun saveUser(
        model: CameraDetectionModel,
        enrollmentStatus: EnrollmentStatus
    ): CameraDetectionModel? {
        if (enrollmentStatus == EnrollmentStatus.ENROLLED) {
            var threshold = cache.getFaceDetectionThreshold()
            if (model.twin) {
                threshold = cache.getTwinThreshold()
            }
            val feat1 = model.rez?.getDetectedFeatures() ?: ByteArray(0)
            val allEntity = database.userDao().getAll().mapNotNull {
                val feat2 = it.detectedFeatures ?: ByteArray(0)
                if (faceDetectionEngine.getFaceFeatureSimilarity(feat1, feat2) > threshold)
                    it
                else
                    null
            }
            allEntity.forEach {
                // TODO, add logic to fix the event detections deletions
                deleteUser(it)
            }
        }
        model.enrolmentStatus = enrollmentStatus
        val entity = mapper.modelToEntity(model)
        val returnId = database.userDao().insert(entity)
        entity.uid = returnId
        return mapper.entityToModel(entity)
    }

    override suspend fun saveAll(users: List<UserEntity>): List<Long> {
        return database.userDao().insertAll(*users.toTypedArray())
    }

    override suspend fun getUser(userId: Long): CameraDetectionModel? {
        val entity =
            database.userDao().getById(userId).firstOrNull()
                ?: return null
        val model = mapper.entityToModel(entity)
        model.error = entity.errors?.toMutableList() ?: mutableListOf()
        model.error2 = entity.errors?.toMutableList() ?: mutableListOf()
        return model
    }

    override suspend fun updateUser(user: CameraDetectionModel) {
        database.userDao().insert(mapper.modelToEntity(user))
    }

    override suspend fun getAll(): List<CameraDetectionModel> {
        return database.userDao().getAll().map {
            mapper.entityToModel(it)
        }
    }

    override suspend fun getAllWhereNameAndEnrollment(
        name: String,
        status: EnrollmentStatus,
        limit: Long?
    ): List<CameraDetectionModel> {
        return database.userDao().getAllWhereNameAndEnrollmentStatus(name, status.name, limit?: Long.MAX_VALUE).map {
            mapper.entityToModel(it)
        }
    }

    override suspend fun getAllByEnrollment(status: EnrollmentStatus, limit: Long?): List<CameraDetectionModel> {
        return database.userDao().getAllByEnrollment(status.name, (limit?:Long.MAX_VALUE)).map {
            mapper.entityToModel(it)
        }
    }

    override suspend fun getAllByEnrollmentNot(status: EnrollmentStatus, limit: Long?): List<CameraDetectionModel> {
        return database.userDao().getAllByEnrollmentNot(status.name, (limit?:Long.MAX_VALUE)).map {
            mapper.entityToModel(it)
        }
    }

    override suspend fun getAllOrderByEnrollment(): List<CameraDetectionModel> {
        return database.userDao().getAllOrderByEnrollment().map {
            mapper.entityToModel(it)
        }
    }

    override suspend fun cleanRejectedEnrollment(limit: Long?) {
        database.userDao().cleanRejectedEnrollment(limit?:Calendar.getInstance().timeInMillis)
    }

    override suspend fun getAllEntity(): List<UserEntity> {
        return database.userDao().getAll()
    }

    override suspend fun getAllById(idList: List<Long>): List<CameraDetectionModel> {
        return database.userDao().getAllByIds(idList).map { mapper.entityToModel(it) }
    }

    override suspend fun getAllUsersForSelection(): List<StudentSelectionModel> {
        return database.userDao().getAll()
            .filter { it.enrolmentStatus?.equals(EnrollmentStatus.ENROLLED.name) ?: false }
            .map {
                mapper.entityToSelectionModel(it)
            }
    }

    override suspend fun getByEventsListAll(events: List<EventEntity>): List<Pair<CameraDetectionModel, EventModel?>> {
        val idList = events.map { it.userId }
        val users = database.userDao().getAllByIds(idList)
        return mapper.eventListToUsers(events, users)
    }

    override suspend fun getAllByCLass(enrolledClass: String): List<CameraDetectionModel> {
        val entityList = database.userDao().getByClass(enrolledClass)
        return entityList.map { mapper.entityToModel(it) }
    }

    override suspend fun deleteUser(id: Long) {
        val foundItem = database.userDao().getById(id).firstOrNull() ?: return
        foundItem.deleted = true
        saveAll(listOf(foundItem))
    }

    override suspend fun deleteUser(entity: UserEntity) {
        database.userDao().delete(entity)
    }

    override suspend fun getAllFaceFeatures(): List<ByteArray> {
        return database.userDao().getAll().mapNotNull {
            it.detectedFeatures
        }
    }

    override fun getAllLive(): LiveData<List<UserEntity>> {
        return database.userDao().getAllLive()
    }

    override suspend fun checkIfEnrolled(currentFeature: ByteArray, twin: Boolean): String {
        var users = database.userDao().getAllByEnrollment(EnrollmentStatus.ENROLLED.name, Long.MAX_VALUE)
        val threshold = if(twin) cache.getTwinThreshold() else cache.getFaceDetectionThreshold()
        users.forEach {
            val feat1 = it.detectedFeatures ?: ByteArray(0)
            if (faceDetectionEngine.getFaceFeatureSimilarity(feat1, currentFeature) > threshold)
                return "No"
        }
        return "OK"
    }

    override suspend fun checkIfRejected(currentFeature: ByteArray, twin: Boolean): Long? {
        var users = database.userDao().getAllByEnrollment(EnrollmentStatus.REJECTED.name, Long.MAX_VALUE)
        val threshold = if (twin) cache.getTwinThreshold() else cache.getFaceDetectionThreshold()
        users.forEach {
            val feat1 = it.detectedFeatures ?: ByteArray(0)
            if (faceDetectionEngine.getFaceFeatureSimilarity(feat1, currentFeature) > threshold)
                return it.uid
        }
        return null
    }

    override suspend fun checkIfAdmin(currentFeature: ByteArray): String {
        val threshold = cache.getFaceDetectionThreshold()
        var admins = database.adminDao().getAll()
        admins.forEach {
            val feat = it.detectedFeatures ?: ByteArray(0)
            if (faceDetectionEngine.getFaceFeatureSimilarity(feat, currentFeature) > threshold)
                return "No"
        }
        return "OK"
    }

    override suspend fun enableNotification(enable: Boolean) {
        database.userDao().enableParentNitificationForAllUsers(if (enable) 1 else 0)
    }
}