package com.facecool.ui.common.lessons

import com.face.cool.databasa.lessons.LessonEntity
import com.facecool.ui.classes.add.time.LessonModel
import com.facecool.utils.getDate

class LessonMapperImpl : LessonMapper {

    override fun fromEntityToModel(entity: LessonEntity): LessonModel {
        return LessonModel(
            lessonName = entity.lessonName,
            lessonClassId = entity.lessonClassId,
            startLesson = entity.startLesson,
            endLesson = entity.endLesson,
            lessonDdId = entity.uid ?: -1,
            autoKiosk = entity.autoKiosk ?: false,
            liveness = entity.liveness ?: false,
            date = entity.startLesson.getDate()
        )
    }

    override fun fromModelToEntity(mode: LessonModel): LessonEntity {
        return LessonEntity(
            uid = mode.lessonDdId,
            lessonName = mode.lessonName,
            lessonClassId = mode.lessonClassId,
            startLesson = mode.startLesson!!,
            endLesson = mode.endLesson!!,
            autoKiosk = mode.autoKiosk,
            liveness = mode.liveness
        )
    }
}
