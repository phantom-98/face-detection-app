package com.facecool.ui.camera.businesslogic.events

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.face.cool.databasa.Database
import com.face.cool.databasa.detection_events.EventEntity
import com.facecool.ui.camera.businesslogic.CameraDetectionModel


class EventRepositoryImpl constructor(
    private val database: Database,
    private val eventMapper: EventMapper
) : EventRepository {

    override suspend fun getAllEvents(): List<EventModel> {
        return database.eventDao().getAll().map { eventMapper.fromEntityToModel(it) }
    }

    override suspend fun getAllEntity(): List<EventEntity> {
        return database.eventDao().getAll()
    }

    override suspend fun saveEvent(event: EventModel): Long {
        return database.eventDao().insertAll(eventMapper.fromModelToEntity(event)).first()
    }

    override suspend fun saveEvent(event: ManualEvent): Long {
        return database.eventDao().insertAll(eventMapper.fromModelToEntity(event)).first()
    }

    override suspend fun saveAllEvents(events: List<EventEntity>): List<Long> {
        return database.eventDao().insertAll(*events.toTypedArray())
    }

    override suspend fun saveEventForUser(
        user: CameraDetectionModel,
        eventImage: Bitmap,
        mode: EventModeModel,
        fake: Boolean
    ): Long {
        val event = eventMapper.generateEventFromModel(user, eventImage, mode, fake)
        return saveEvent(event)
    }

    override suspend fun deleteEventById(id: Long) {
        database.eventDao().deleteEventById(id)
    }

    override suspend fun updateEventIdsToNewId(oldIds: List<Long>, newId: Long) {
        database.eventDao().updateOldIdsWIthNewId(newId, oldIds)
    }

    override suspend fun getAllIdAndTime(
        studentId: Long,
        startTime: Long,
        endTIme: Long
    ): List<EventModel> {
        return database.eventDao().getAllByUserIdAndTime(studentId, startTime, endTIme).map {
            eventMapper.fromEntityToModel(it)
        }
    }

    override suspend fun getAllByTime(startTime: Long, endTIme: Long): List<EventEntity> {
        return database.eventDao().getAllByTime(startTime, endTIme)
    }

    override fun getLatestEvents(limit: Int): LiveData<List<EventEntity>> {
        return database.eventDao().getLatest(limit)
    }

    override suspend fun getAllByLessonId(userId: Long, lessonId: Long): List<EventEntity> {
        return database.eventDao().getAllByEventModeAndLessonId(userId, lessonId)
    }

    override suspend fun getById(id: Long): EventEntity {
        return database.eventDao().getById(id).first()
    }
}