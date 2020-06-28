package com.facecool.ui.camera.businesslogic.events

import android.graphics.Bitmap
import com.face.cool.databasa.detection_events.EventEntity
import com.facecool.ui.camera.businesslogic.CameraDetectionModel

interface EventMapper {

    fun fromModelToEntity(event: EventModel): EventEntity

    fun fromModelToEntity(event: ManualEvent): EventEntity

    fun fromEntityToModel(event: EventEntity): EventModel

    fun generateEventFromModel(model: CameraDetectionModel, eventImage: Bitmap, eventMode: EventModeModel, fake: Boolean = false) : EventModel

}

