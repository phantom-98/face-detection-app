package com.facecool.ui.common.lessons

import com.face.cool.databasa.lessons.LessonEntity
import com.facecool.ui.classes.add.time.LessonModel

interface LessonRepository {

    suspend fun getAll(): List<LessonModel>

    suspend fun getAllEntity(): List<LessonEntity>

    suspend fun getAllByTime(
        from: Long,
        to: Long
    ): List<LessonModel>

    suspend fun getAllByClassAndCurrentTime(classId: Long, currentTime: Long, timeToSpear: Long) : List<LessonModel>

    suspend fun getAllByClassID(classId: Long): List<LessonModel>

    suspend fun getByLessonID(lessonId: Long): LessonModel

    suspend fun addAll(lessons: List<LessonModel>)

    suspend fun addAllEntity(lessons: List<LessonEntity>)

    suspend fun deleteLesson(lesson: LessonModel)

    suspend fun deleteLessons(classId: Long)
}