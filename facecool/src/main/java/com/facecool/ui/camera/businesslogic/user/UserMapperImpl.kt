package com.facecool.ui.camera.businesslogic.user

import android.util.Log
import com.face.cool.common.image.ImageManager
import com.face.cool.databasa.detection_events.EventEntity
import com.face.cool.databasa.users.UserEntity
import com.facecool.attendance.facedetector.FaceModel
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.camera.businesslogic.events.EventMapper
import com.facecool.ui.camera.businesslogic.events.EventModel
import com.facecool.ui.students.common.EnrollmentStatus
import com.facecool.ui.students.select.StudentSelectionModel
import com.google.gson.Gson
import com.google.mlkit.vision.face.Face
import javax.inject.Inject

class UserMapperImpl @Inject constructor(
    private val gson: Gson,
    private val imageManager: ImageManager,
    private val eventMapper: EventMapper
) : UserMapper {

    override fun entityToModel(entity: UserEntity): CameraDetectionModel {
//        Log.d("modeltoEnrtuyty", "entityToModel: ${entity.errors?.joinToString(" ")}")

        val formJson = gson.fromJson(entity.detectedFaceJson, Face::class.java)
        val bitmap = imageManager.getImage(entity.imageLocalAddress ?: "")
        return CameraDetectionModel(
            entity.name ?: "",
            entity.enrolmentStatus?.let { EnrollmentStatus.valueOf(it) }
                ?: EnrollmentStatus.UNKNOWN,
            object : FaceModel {
                override fun getDetectedFace() = formJson
                override fun getRealLiveProbability() = entity.realLiveProbability ?: 0f
                override fun getFaceImage() = bitmap
                override fun getDetectedFeatures() = entity.detectedFeatures ?: byteArrayOf()
            },
            entity.similarity ?: 0f,
            entity.uid ?: -1,
            entity.imageLocalAddress ?: "",
            creationTime = entity.creationTime ?: 0L
        ).apply {
            twin = entity.twin ?: false
            email = entity.email ?: ""
            address = entity.address ?: ""
            lastName = entity.lastName ?: ""
            studentId = entity.studentId
            error = entity.errors?.toMutableList() ?: mutableListOf()
            phoneNumber = entity.phoneNumber
            enableNotification = entity.enableNotification ?: true
            parentNames = entity.parentNames
            enableSMS = entity.enableSMS ?: true
            parentSMSNumbers = entity.parentSMSNumbers
            enableEmail = entity.enableEmail ?: true
            parentEmails = entity.parentEmails
            enableWhatsapp = entity.enableWhatsapp ?: true
            parentWhatsappNumbers = entity.parentWhatsappNumbers
        }
    }

    override fun entityToSelectionModel(entity: UserEntity): StudentSelectionModel {
        return StudentSelectionModel(
            studentId = entity.uid ?: -1,
            name = entity.name + " " +entity.lastName,
            isSelected = false,
            studentImagePath = entity.imageLocalAddress ?: "",
        )
    }

    override fun modelToEntity(model: CameraDetectionModel): UserEntity {
        val json = gson.toJson(model.rez?.getDetectedFace())

        imageManager.deleteImage(model.imageName)
        model.imageName = model.imageName
        model.rez?.getFaceImage()?.let { imageManager.saveImage(it, model.imageName) }

        return UserEntity(
            model.id,
            model.name,
            "",
            model.enrolmentStatus.name,
            model.imageName,
            model.similarity,
            json,
            model.rez?.getRealLiveProbability() ?: 0f,
            model.rez?.getDetectedFeatures() ?: byteArrayOf(),
            model.twin,
            model.email,
            model.address,
            model.lastName,
            model.studentId,
            model.creationTime,
            model.error,
            model.deleted,
            model.phoneNumber,
            model.enableNotification,
            model.parentNames,
            model.enableSMS,
            model.enableEmail,
            model.enableWhatsapp,
            model.parentSMSNumbers,
            model.parentEmails,
            model.parentWhatsappNumbers
        )
    }

    override fun eventListToUsers(
        events: List<EventEntity>,
        usersData: List<UserEntity>
    ): List<Pair<CameraDetectionModel, EventModel>> {
        fun getUserFromEvent(usersData: List<UserEntity>, event: EventEntity): UserEntity? {
            return usersData.firstOrNull { it.uid == event.userId }
        }
        return events.mapNotNull {
            val model = getUserFromEvent(usersData, it)?.let { it1 -> entityToModel(it1) }
                ?: return@mapNotNull null
            val event = eventMapper.fromEntityToModel(it)
            Pair(
                model,
                event
            )
        }.sortedByDescending { it.second.timeInMilesOfCreation }
    }

}
