package com.facecool.ui.classes.common

import com.face.cool.databasa.classes.ClassEntity
import com.facecool.ui.classes.list.ClassModel
import com.facecool.utils.getReadableDate

class ClassMapperImpl : ClassMapper {

    override fun entityToModel(entity: ClassEntity, nextLesson: String): ClassModel {
        return ClassModel(
            name = entity.className ?: "",
            id = entity.classId ?: "",
            uuid = entity.uid ?: -1,
            description = "",
            nextLessonTime = nextLesson,
            status = "",
            enrolledStudents = entity.enrolledStudents ?: listOf()
        )
    }

    override fun modelToEntity(model: ClassModel): ClassEntity {
        return ClassEntity(
            className = model.name,
            classId = model.id,
            enrolledStudents = model.enrolledStudents,
        )
    }

    override fun modelToEntityWithId(model: ClassModel): ClassEntity {
        return ClassEntity(
            uid = model.uuid,
            className = model.name,
            classId = model.id,
            enrolledStudents = model.enrolledStudents,
        )
    }

}