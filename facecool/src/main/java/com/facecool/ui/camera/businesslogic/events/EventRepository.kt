package com.facecool.ui.camera.businesslogic.events

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.face.cool.databasa.detection_events.EventEntity
import com.facecool.ui.camera.businesslogic.CameraDetectionModel

interface EventRepository {

    suspend fun getAllEvents(): List<EventModel>

    suspend fun getAllEntity(): List<EventEntity>

    suspend fun saveEvent(event: EventModel): Long

    suspend fun saveEvent(event: ManualEvent): Long

    suspend fun saveAllEvents(events: List<EventEntity>): List<Long>

    suspend fun saveEventForUser(
        user: CameraDetectionModel,
        eventImage: Bitmap,
        mode: EventModeModel,
        fake: Boolean = false
    ): Long

    suspend fun getAllByTime(startTime: Long, endTIme: Long): List<EventEntity>

    suspend fun deleteEventById(id: Long)

    suspend fun updateEventIdsToNewId(oldIds: List<Long>, newId: Long)

    suspend fun getAllIdAndTime(studentId: Long, startTime: Long, endTIme: Long): List<EventModel>

    fun getLatestEvents(limit: Int): LiveData<List<EventEntity>>

    suspend fun getById(id: Long): EventEntity

    suspend fun getAllByLessonId(userId: Long, lessonId: Long): List<EventEntity>


}

