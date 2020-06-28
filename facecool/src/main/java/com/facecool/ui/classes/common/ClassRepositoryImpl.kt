package com.facecool.ui.classes.common

import androidx.lifecycle.LiveData
import com.face.cool.databasa.Database
import com.face.cool.databasa.classes.ClassEntity
import com.facecool.ui.classes.list.ClassModel
import com.facecool.utils.getReadableDate
import javax.inject.Inject

class ClassRepositoryImpl @Inject constructor(
    private val database: Database,
    private val classMapper: ClassMapper
) : ClassRepository {

    override suspend fun getAllCLasses(): List<ClassModel> {
        return database.classDao().getAll().map {
            val nextLessons = database.lessonDao().getAllById(it.uid!!).filter { it.startLesson >= System.currentTimeMillis() }
            classMapper.entityToModel(it, nextLessons.firstOrNull()?.let { it.startLesson?.getReadableDate(pattern = "MMM dd") }?:"No Lesson")
        }
    }

    override suspend fun getAllEntity(): List<ClassEntity> {
        return database.classDao().getAll()
    }

    override fun getAllLive(): LiveData<List<ClassEntity>> {
        return database.classDao().getAllLive()
    }

    override suspend fun saveAllEntity(list: List<ClassEntity>) {
        return database.classDao().insertAll(*list.toTypedArray())
    }

    override suspend fun getClassById(id: Long): ClassModel? {
        val entity = database.classDao().getClassById(id)
        return entity?.let {
            val nextLessons = database.lessonDao().getAllById(it.uid!!).filter { it.startLesson >= System.currentTimeMillis() }
            classMapper.entityToModel(it, nextLessons.firstOrNull()?.let { it.startLesson?.getReadableDate(pattern = "MMM dd") }?:"No Lesson")
        }
    }

    override suspend fun addClass(mode: ClassModel): Long {
        return database.classDao().insert(
            classMapper.modelToEntity(mode)
        )
    }

    override suspend fun updateClass(mode: ClassModel): Long {
        return database.classDao().insert(
            classMapper.modelToEntityWithId(mode)
        )
    }

    override suspend fun delete(classModel: ClassModel) {
        val entity = classMapper.modelToEntityWithId(classModel)
        database.classDao().delete(entity)
        database.lessonDao().deleteAllByClassId(entity.uid?:return)
    }

}
