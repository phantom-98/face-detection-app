package com.face.cool.databasa.lessons

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LessonDao {

    @Query("SELECT * FROM lesson_table ORDER BY startLesson, lessonClassId")
    suspend fun getAll(): List<LessonEntity>

    @Query("SELECT * FROM lesson_table WHERE lessonClassId = :classId AND startLesson BETWEEN :startTime AND :endTime ORDER BY startLesson")
    suspend fun getAllByUserIdAndTime(
        classId: Long,
        startTime: Long,
        endTime: Long
    ): List<LessonEntity>

    @Query("SELECT * FROM lesson_table WHERE lessonClassId = :classId AND :currentTime BETWEEN startLesson - :timeToSpear AND endLesson ORDER BY startLesson")
    suspend fun getAllByClassIdAndCurrentTime(classId: Long, currentTime: Long, timeToSpear: Long): List<LessonEntity>

    @Query("SELECT * FROM lesson_table WHERE startLesson BETWEEN :from AND :to ORDER BY startLesson")
    suspend fun getAllByTime(from: Long, to: Long): List<LessonEntity>

    @Query("SELECT * FROM lesson_table WHERE lessonClassId = :classId ORDER BY startLesson")
    suspend fun getAllById(classId: Long): List<LessonEntity>

    @Query("SELECT * FROM lesson_table WHERE uid = :lessonId")
    suspend fun getById(lessonId: Long): List<LessonEntity>

    @Query("DELETE FROM lesson_table WHERE uid = :lessonId")
    suspend fun deleteById(lessonId: Long)

    @Query("DELETE FROM lesson_table WHERE lessonClassId = :classID")
    suspend fun deleteAllByClassId(classID: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg event: LessonEntity)

}
