package com.face.cool.databasa.users

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Query("SELECT * FROM user_table WHERE deleted = 0 ORDER BY name")
    suspend fun getAll(): List<UserEntity>
    @Query("SELECT * FROM user_table WHERE deleted = 0 and enrolmentStatus = :enrollment and (name like '%' || :name || '%' or lastName like '%' || :name || '%') LIMIT :limit")
    suspend fun getAllWhereNameAndEnrollmentStatus(name: String, enrollment: String, limit: Long): List<UserEntity>
    @Query("SELECT * FROM user_table WHERE deleted = 0 ORDER BY enrolmentStatus DESC, name")
    suspend fun getAllOrderByEnrollment(): List<UserEntity>
    @Query("SELECT * FROM user_table WHERE deleted = 0 and enrolmentStatus=:status ORDER BY name LIMIT :limit")
    suspend fun getAllByEnrollment(status: String, limit: Long): List<UserEntity>
    @Query("SELECT * FROM user_table WHERE deleted = 0 and enrolmentStatus!=:status ORDER BY name LIMIT :limit")
    suspend fun getAllByEnrollmentNot(status: String, limit: Long): List<UserEntity>
    @Query("UPDATE user_table set  deleted = 1 WHERE deleted = 0 and enrolmentStatus = 'REJECTED' and creationTime < :limit")
    suspend fun cleanRejectedEnrollment(limit: Long)
    @Query("UPDATE user_table set enableNotification = :enable")
    suspend fun enableParentNitificationForAllUsers(enable: Int)
    @Query("SELECT * FROM user_table WHERE deleted = 0 ORDER BY name")
    fun getAllLive(): LiveData<List<UserEntity>>

    @Query("SELECT * FROM user_table WHERE uid = :userId AND deleted = 0 ORDER BY name")
    suspend fun getById(userId: Long): List<UserEntity>

    @Query("SELECT * FROM user_table WHERE uid IN (:userId) AND deleted = 0 ORDER BY name")
    suspend fun getAllByIds(userId: List<Long>): List<UserEntity>

    @Query("SELECT * FROM user_table WHERE enrolledClass = :enrolledClass AND deleted = 0 ORDER BY name")
    suspend fun getByClass(enrolledClass: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg user: UserEntity): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Delete
    suspend fun delete(user: UserEntity)

}
