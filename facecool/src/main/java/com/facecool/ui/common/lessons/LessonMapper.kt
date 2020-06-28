package com.facecool.ui.common.lessons

import com.face.cool.databasa.lessons.LessonEntity
import com.facecool.ui.classes.add.time.LessonModel

interface LessonMapper {

    fun fromEntityToModel(entity: LessonEntity): LessonModel

    fun fromModelToEntity(mode: LessonModel): LessonEntity

}