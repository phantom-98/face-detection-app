package com.face.cool.databasa.lessons

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_table")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    val lessonName: String,
    val lessonClassId: Long,
    val startLesson: Long,
    val endLesson: Long,
    val autoKiosk: Boolean? = false,
    val liveness: Boolean? = false
)
