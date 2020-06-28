package com.facecool.ui.classes.common

import androidx.lifecycle.LiveData
import com.face.cool.databasa.classes.ClassEntity
import com.facecool.ui.classes.list.ClassModel

interface ClassRepository {

    suspend fun getAllCLasses(): List<ClassModel>

    suspend fun getAllEntity(): List<ClassEntity>

    suspend fun saveAllEntity(list: List<ClassEntity>)

    suspend fun getClassById(id: Long): ClassModel?

    suspend fun addClass(mode: ClassModel): Long

    suspend fun updateClass(mode: ClassModel): Long

    suspend fun delete(classModel: ClassModel)

    fun getAllLive(): LiveData<List<ClassEntity>>

}

