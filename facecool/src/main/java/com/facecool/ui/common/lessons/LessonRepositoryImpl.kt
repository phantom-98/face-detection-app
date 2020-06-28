package com.facecool.ui.common.lessons

import android.util.Log
import com.face.cool.databasa.Database
import com.face.cool.databasa.lessons.LessonEntity
import com.facecool.ui.classes.add.time.LessonModel
import javax.inject.Inject

class LessonRepositoryImpl @Inject constructor(
    private val database: Database,
    private val lessonMapper: LessonMapper
) : LessonRepository {

    override suspend fun getAll(): List<LessonModel> {
        return database.lessonDao().getAll().map {
            lessonMapper.fromEntityToModel(it)
        }
    }

    override suspend fun getAllEntity(): List<LessonEntity> {
        return database.lessonDao().getAll()
    }

    override suspend fun getAllByTime(
        from: Long,
        to: Long
    ): List<LessonModel> {
        return database.lessonDao().getAllByTime(
            from, to
        ).map {
            lessonMapper.fromEntityToModel(it)
        }
    }

    override suspend fun getAllByClassAndCurrentTime(
        classId: Long,
        currentTime: Long,
        timeToSpear: Long
    ): List<LessonModel> {
        return database.lessonDao().getAllByClassIdAndCurrentTime(classId, currentTime, timeToSpear).map {
            lessonMapper.fromEntityToModel(it)
        }
    }

    override suspend fun getAllByClassID(classId: Long): List<LessonModel> {
        return database.lessonDao().getAllById(classId).map {
            lessonMapper.fromEntityToModel(it)
        }
    }

    override suspend fun addAll(lessons: List<LessonModel>) {
        database.lessonDao().insertAll(
            *lessons.map {
                lessonMapper.fromModelToEntity(it)
            }.toTypedArray()
        )
    }

    override suspend fun getByLessonID(lessonId: Long): LessonModel {
        return lessonMapper.fromEntityToModel(database.lessonDao().getById(lessonId).first())
    }

    override suspend fun addAllEntity(lessons: List<LessonEntity>) {
        database.lessonDao().insertAll(*lessons.toTypedArray())
    }

    override suspend fun deleteLesson(lesson: LessonModel) {
        database.lessonDao().deleteById(lesson.lessonDdId?:return)
    }

    override suspend fun deleteLessons(classId: Long) {
        database.lessonDao().deleteAllByClassId(classId)
    }
}