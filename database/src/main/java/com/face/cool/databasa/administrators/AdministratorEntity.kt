package com.face.cool.databasa.administrators

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_table")
data class AdministratorEntity(
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    var name: String? = null,
    val base64Image: String? = null,
    val detectedFaceJson: String? = null,
    val realLiveProbability: Float? = null,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val detectedFeatures: ByteArray? = null,
    var email: String? = null,
    var lastName: String? = null,
    var creationTime: Long? = null,
    var phoneNumber: String? = null,
    var pin: String? = null
)