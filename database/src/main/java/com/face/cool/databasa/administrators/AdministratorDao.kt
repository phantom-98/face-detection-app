package com.face.cool.databasa.administrators

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.face.cool.databasa.administrators.AdministratorEntity

@Dao
interface AdministratorDao {

    @Query("SELECT * FROM admin_table")
    suspend fun getAll(): List<AdministratorEntity>
    
    @Query("SELECT * FROM admin_table")
    fun getAllLive(): LiveData<List<AdministratorEntity>>

    @Query("SELECT * FROM admin_table WHERE uid = :adminId")
    suspend fun getById(adminId: Long): List<AdministratorEntity>

    @Query("SELECT * FROM admin_table WHERE uid IN (:adminId)")
    suspend fun getAllByIds(adminId: List<Long>): List<AdministratorEntity>

    @Query("SELECT * FROM admin_table WHERE detectedFeatures = :detectedFeatures")
    suspend fun getAllFeatures(detectedFeatures: ByteArray): List<AdministratorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg admin: AdministratorEntity): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(admin: AdministratorEntity): Long

    @Delete
    suspend fun delete(admin: AdministratorEntity)

}
