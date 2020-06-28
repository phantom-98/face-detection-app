package com.face.cool.databasa.classes

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClassDao {

    @Query("SELECT * FROM class_table")
    suspend fun getAll(): List<ClassEntity>

    @Query("SELECT * FROM class_table")
    fun getAllLive(): LiveData<List<ClassEntity>>

    @Query("SELECT * FROM class_table WHERE uid = :classID ")
    suspend fun getClassById(classID: Long): ClassEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg event: ClassEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ClassEntity): Long

    @Delete
    suspend fun delete(entity: ClassEntity)


}
