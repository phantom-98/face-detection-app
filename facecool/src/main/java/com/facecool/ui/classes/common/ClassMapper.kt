package com.facecool.ui.classes.common

import com.face.cool.databasa.classes.ClassEntity
import com.facecool.ui.classes.list.ClassModel

interface ClassMapper {

    fun entityToModel(entity: ClassEntity, nextLesson: String = ""): ClassModel

    fun modelToEntity(model: ClassModel): ClassEntity

    fun modelToEntityWithId(model: ClassModel): ClassEntity

}
