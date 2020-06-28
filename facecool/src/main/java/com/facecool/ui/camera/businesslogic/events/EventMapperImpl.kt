package com.facecool.ui.camera.businesslogic.events

import android.graphics.Bitmap
import com.face.cool.common.image.ImageManager
import com.face.cool.databasa.detection_events.EventEntity
import com.facecool.ui.camera.businesslogic.CameraDetectionModel
import com.facecool.ui.students.common.EnrollmentStatus

class EventMapperImpl(private val imageManager: ImageManager) : EventMapper {

    override fun fromModelToEntity(event: EventModel): EventEntity {
        val imageName = "event_" + event.timeInMilesOfCreation.toString()
        event.detectedImage?.let { imageManager.saveImage(it, imageName) }
        return EventEntity(
            userId = event.userId,
            timeInMilesOfCreation = System.currentTimeMillis(),
            imageName = imageName,
            enrollmentStatus = event.enrollmentStatus.name,
            userName = event.userName,
            eventMode = event.eventMode.name
        )
    }

    override fun fromModelToEntity(event: ManualEvent): EventEntity {
        return EventEntity(
            userId = event.userId,
            timeInMilesOfCreation = event.timeInMilesOfCreation,
            imageName = event.imagePathName,
            enrollmentStatus = event.enrollmentStatus.name,
            userName = event.userName,
            eventMode = event.eventMode.name,
            prevStatus = event.prevStatus,
            lessonId = event.lessonId
        )
    }

    override fun fromEntityToModel(event: EventEntity): EventModel {
        return EventModel(
            userId = event.userId,
            uid = event.uid ?: -1,
            timeInMilesOfCreation = event.timeInMilesOfCreation,
            detectedImage = null,
            imagePathName = event.imageName,
            enrollmentStatus = EnrollmentStatus.valueOf(event.enrollmentStatus),
            userName = event.userName,
            eventMode = EventModeModel.valueOf(event.eventMode),
            prevStatus = event.prevStatus?:""
        )
    }

    override fun generateEventFromModel(
        model: CameraDetectionModel,
        eventImage: Bitmap,
        eventMode: EventModeModel,
        fake: Boolean
    ): EventModel {
        val imageName = "event_" + model.creationTime.toString()
        imageManager.saveImage(eventImage, imageName)
        return EventModel(
            model.id ?: -1,
            -1,
            System.currentTimeMillis(),
            eventImage,
            imageName,
            enrollmentStatus = if (fake) EnrollmentStatus.UNKNOWN else model.enrolmentStatus,
            if (fake) "spoof" else model.name,
            eventMode = eventMode,
            prevStatus = ""
        )
    }

}