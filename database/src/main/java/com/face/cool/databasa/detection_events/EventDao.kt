package com.face.cool.databasa.detection_events

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {

    @Query("SELECT * FROM event_table WHERE eventMode!='MANUAL'")
    suspend fun getAll(): List<EventEntity>

    @Query("SELECT * FROM event_table WHERE eventMode!='MANUAL' ORDER BY timeInMilesOfCreation DESC LIMIT :limit")
    fun getLatest(limit: Int): LiveData<List<EventEntity>>

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT * FROM event_table WHERE eventMode!='MANUAL' AND userId = :userId")
    suspend fun getAllByUserId(userId: Int): List<EventEntity>

    @Query("SELECT * FROM event_table WHERE eventMode!='MANUAL' AND userId = :userId AND timeInMilesOfCreation BETWEEN :startTime AND :endTime")
    suspend fun getAllByUserIdAndTime(
        userId: Long,
        startTime: Long,
        endTime: Long
    ): List<EventEntity>

    @Query("SELECT * FROM event_table WHERE eventMode!='MANUAL' AND timeInMilesOfCreation BETWEEN :startTime AND :endTime")
    suspend fun getAllByTime(
        startTime: Long,
        endTime: Long
    ): List<EventEntity>

    @Query("SELECT * FROM event_table WHERE eventMode='MANUAL' AND userId=:userId AND lessonId=:lessonId")
    suspend fun getAllByEventModeAndLessonId(userId: Long, lessonId: Long): List<EventEntity>

    @Query("SELECT * FROM EVENT_TABLE WHERE uid=:id")
    suspend fun getById(id: Long): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg event: EventEntity): List<Long>

    @Query("DELETE FROM event_table WHERE uid = :id")
    suspend fun deleteEventById(id: Long)

    @Query("UPDATE event_table SET userId = :newId WHERE userId IN (:idList)")
    suspend fun updateOldIdsWIthNewId(newId: Long, idList: List<Long>)


//    @Query("SELECT * FROM event_table ORDER BY timeInMilesOfCreation")
//    fun all(): PagingSource<Int, EventEntity>


}
