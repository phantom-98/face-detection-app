package com.face.cool.databasa.detection_events

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_table")
data class EventEntity(
    val userId: Long,
    var lessonId: Long? = null,
    var prevStatus: String? = null,
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    val timeInMilesOfCreation: Long = System.currentTimeMillis(),
    val imageName: String,
    val enrollmentStatus: String,
    val userName: String,
    var eventMode: String
)

