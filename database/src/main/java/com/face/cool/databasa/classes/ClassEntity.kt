package com.face.cool.databasa.classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_table")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    val className: String?,
    val classId: String?,
    val enrolledStudents: List<Long>?
)
